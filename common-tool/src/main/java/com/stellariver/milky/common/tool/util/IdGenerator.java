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
import java.time.temporal.ChronoField;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@CustomLog
@RequiredArgsConstructor
public class IdGenerator {
    static String[] BUFFER_CHRONO = build(Range.between(0, 60), 2);
    static String[] BUFFER_THOUSAND = build(Range.between(0, 1000), 3);

    static String[] build(Range<Integer> range, int len) {
        SysEx.trueThrow(range.getMinimum() < 0, ErrorEnumsBase.CONFIG_ERROR.message(range));
        boolean b = ((range.getMaximum() - 1) + "").length() > len;
        SysEx.trueThrow(b, ErrorEnumsBase.CONFIG_ERROR.message(range));
        String[] buffer = new String[(int) (range.getMaximum() - range.getMinimum())];
        String f = "%0" + len + "d";
        for (long i = range.getMinimum(); i < range.getMaximum(); i++) {
            buffer[(int) (i - range.getMinimum())] = String.format(f, i);
        }
        return buffer;
    }

    final String prefix;
    final String host;
    final ZoneId zoneId = ZoneId.systemDefault();
    final int startYear;
    final String[] BUFFER_YEAR;


    volatile long lastTime;
    @Contended
    AtomicInteger seq;

    @SneakyThrows
    public IdGenerator(String prefix) {
        this.prefix = prefix;
        this.lastTime = Clock.currentTimeMillis();
        this.seq = new AtomicInteger(0);

        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.lastTime), zoneId);
        this.startYear = localDateTime.getYear();
        int endYear = startYear + 10;
        BUFFER_YEAR = build(Range.between(startYear, endYear), ((endYear - 1) + "").length());
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

    public String next() {
        StringBuilder builder = new StringBuilder(64).append(prefix);
        long time;
        int s = 0;
        time = Clock.currentTimeMillis();
        if (time <= lastTime) {
            s = seq.getAndIncrement();
        } else {
            lastTime = time;
            seq.getAndSet(0);
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.lastTime), zoneId);
        builder.append(BUFFER_YEAR[localDateTime.getYear() - startYear]);
        builder.append(BUFFER_CHRONO[localDateTime.getMonthValue()]);
        builder.append(BUFFER_CHRONO[localDateTime.getDayOfMonth()]);
        builder.append("-");
        builder.append(BUFFER_CHRONO[localDateTime.getHour()]);
        builder.append(BUFFER_CHRONO[localDateTime.getMinute()]);
        builder.append(BUFFER_CHRONO[localDateTime.getSecond()]);
        builder.append("-");
        builder.append(BUFFER_THOUSAND[localDateTime.get(ChronoField.MILLI_OF_SECOND)]);
        builder.append(BUFFER_THOUSAND[s]);
        builder.append("-");
        builder.append(host);
        return builder.toString();
    }


    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator("");
        long l = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            idGenerator.next();
        }
        System.out.println(System.nanoTime() - l);
    }
}
