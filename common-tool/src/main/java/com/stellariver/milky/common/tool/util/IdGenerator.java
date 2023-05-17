package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Clock;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Range;
import sun.misc.Contended;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
@RequiredArgsConstructor
public class IdGenerator {
    static char[][] BUFFER_CHRONO = build(Range.between(0, 60), 2);
    static char[][] BUFFER_THOUSAND = build(Range.between(0, 1000), 3);

    static char[][] BUFFER_YEAR;
    static {
        long now = Clock.currentTimeMillis();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault());
        int startYear = localDateTime.getYear();
        int endYear = startYear + 10;
        BUFFER_YEAR = build(Range.between(startYear, endYear), ((endYear - 1) + "").length());
    }

    static char[][] build(Range<Integer> range, int len) {
        SysEx.trueThrow(range.getMinimum() < 0, ErrorEnumsBase.CONFIG_ERROR.message(range));
        boolean b = ((range.getMaximum() - 1) + "").length() > len;
        SysEx.trueThrow(b, ErrorEnumsBase.CONFIG_ERROR.message(range));
        int length = range.getMaximum() - range.getMinimum();
        char[][] buffer = new char[length][];
        String f = "%0" + len + "d";
        for (long i = range.getMinimum(); i < range.getMaximum(); i++) {
            buffer[(int) i - range.getMinimum()] = String.format(f, i).toCharArray();
        }
        return buffer;
    }

    final char[] prefix;
    final String host;
    final ZoneId zoneId = ZoneId.systemDefault();
    final int startYear;


    volatile long lastTime;
    @Contended
    int seq;

    @SneakyThrows
    public IdGenerator(char[] prefix) {
        this.prefix = prefix;
        this.lastTime = Clock.currentTimeMillis();
        this.seq = 0;
        this.startYear = Integer.parseInt(new String(BUFFER_YEAR[0]));
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        String selectedHost = null;
        while (interfaces.hasMoreElements()) {
            NetworkInterface n = interfaces.nextElement();
            if (!n.isUp() || n.isLoopback() || n.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> inets = n.getInetAddresses();
            while (inets.hasMoreElements()) {
                InetAddress inetAddress = inets.nextElement();
                boolean notLoop = !inetAddress.isLoopbackAddress();
                boolean notLinkLocal = !inetAddress.isLinkLocalAddress();
                boolean notAnyLocal = !inetAddress.isAnyLocalAddress();
                boolean ipv4 = inetAddress instanceof Inet4Address;
                boolean b = notLoop && notLinkLocal && notAnyLocal && ipv4;
                if (b) {
                    selectedHost = inetAddress.getHostAddress();
                }
            }
        }
        this.host = selectedHost;
        SysEx.nullThrow(this.host, ErrorEnumsBase.NOT_VALID_NET_ADDRESS);
    }

//    Map<Thread, StringBuilder> map = new ConcurrentHashMap<>();
//    public String next() {
//        StringBuilder builder = map.computeIfAbsent(Thread.currentThread(), k -> new StringBuilder(64));
//        builder.setLength(0);
//        builder.append(prefix);
//        long time;
//        int s = 0;
//        time = Clock.currentTimeMillis();
//        synchronized (this) {
//            if (time <= lastTime) {
//                s = seq++;
//            } else {
//                lastTime = time;
//                seq = 0;
//            }
//        }
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.lastTime), zoneId);
//        builder.append(BUFFER_YEAR[localDateTime.getYear() - startYear]);
//        builder.append(BUFFER_CHRONO[localDateTime.getMonthValue()]);
//        builder.append(BUFFER_CHRONO[localDateTime.getDayOfMonth()]);
//        builder.append("-");
//        builder.append(BUFFER_CHRONO[localDateTime.getHour()]);
//        builder.append(BUFFER_CHRONO[localDateTime.getMinute()]);
//        builder.append(BUFFER_CHRONO[localDateTime.getSecond()]);
//        builder.append("-");
//        builder.append(BUFFER_THOUSAND[(int) (lastTime%1000)]);
//        builder.append(BUFFER_THOUSAND[s]);
//        builder.append("-");
//        builder.append(host);
//        return builder.toString();
//    }

    Map<Thread, char[]> map = new ConcurrentHashMap<>();
    public String next() {
        char[] chars = map.computeIfAbsent(Thread.currentThread(), k -> {
            char[] rawChars = new char[64];
            rawChars[4] = '-';
            rawChars[13] = '-';
            rawChars[20] = '-';
            return rawChars;
        });
        System.arraycopy(prefix, 0, chars, 0, 4);

        long time;
        int s = 0;
        time = Clock.currentTimeMillis();
        synchronized (this) {
            if (time <= lastTime) {
                s = seq++;
            } else {
                lastTime = time;
                seq = 0;
            }
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.lastTime), zoneId);
        System.arraycopy(BUFFER_YEAR[localDateTime.getYear() - startYear], 0, chars, 5, 4);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getMonthValue()], 0, chars, 9, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getDayOfMonth()], 0, chars, 11, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getHour()], 0, chars, 14, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getMinute()], 0, chars, 16, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getSecond()], 0, chars, 18, 2);
        System.arraycopy(BUFFER_THOUSAND[(int) (lastTime%1000)], 0, chars, 21, 3);
        System.arraycopy(BUFFER_THOUSAND[s], 0, chars, 24, 3);
        return new String(chars);
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator("test".toCharArray());
        long l = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            idGenerator.next();
        }
        System.out.println(System.nanoTime() - l);
    }
}
