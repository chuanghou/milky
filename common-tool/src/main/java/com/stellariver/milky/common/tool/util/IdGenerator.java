package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import com.stellariver.milky.common.tool.common.Clock;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Range;

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

    char[] host;
    final char[] suffix;
    final ZoneId zoneId = ZoneId.systemDefault();
    final int startYear;


    volatile long lastTime;
    long p0, p1, p2, p3, p4, p5, p6, p7;
    int seq;
    long p8, p9, p10, p11, p12, p13, p14, p15;
    @SneakyThrows
    public IdGenerator(String suffix) {
        this.suffix = suffix.toCharArray();
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
        if (selectedHost != null) {
            this.host = selectedHost.toCharArray();
        } else {
            throw new SysEx(ErrorEnumsBase.NOT_VALID_NET_ADDRESS);
        }
    }

    Map<Thread, char[]> map = new ConcurrentHashMap<>();
    public String next() {
        char[] chars = map.computeIfAbsent(Thread.currentThread(), k -> {
            char[] rawChars = new char[64];
            rawChars[8] = '-';
            rawChars[15] = '-';
            rawChars[22] = '-';
            return rawChars;
        });
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
        System.arraycopy(BUFFER_YEAR[localDateTime.getYear() - startYear], 0, chars, 0, 4);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getMonthValue()], 0, chars, 4, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getDayOfMonth()], 0, chars, 6, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getHour()], 0, chars, 9, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getMinute()], 0, chars, 11, 2);
        System.arraycopy(BUFFER_CHRONO[localDateTime.getSecond()], 0, chars, 13, 2);
        System.arraycopy(BUFFER_THOUSAND[(int) (lastTime%1000)], 0, chars, 16, 3);
        System.arraycopy(BUFFER_THOUSAND[s], 0, chars, 19, 3);
        System.arraycopy(host, 0, chars, 23, host.length);
        System.arraycopy("-".toCharArray(), 0, chars, 23 + host.length, 1);
        System.arraycopy(suffix, 0, chars, 24 + host.length, suffix.length);

        return new String(chars, 0, 24 + host.length + suffix.length);
    }
}
