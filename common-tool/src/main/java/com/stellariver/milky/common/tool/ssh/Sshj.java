package com.stellariver.milky.common.tool.ssh;

import lombok.SneakyThrows;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

public class Sshj {




    @SneakyThrows
    public static void main(String[] args) {
        final SSHClient client = new SSHClient();
        client.loadKnownHosts();
        client.connect("8.217.162.128");
        try {
            client.authPassword("root", "Octopus123");
            final Session session = client.startSession();
            try {
                final Session.Command cmd = session.exec("java -version");
                String result = IOUtils.toString(cmd.getInputStream(), StandardCharsets.UTF_8);
                System.out.println(result);
            } finally {
                session.close();
            }
        } finally {
            client.disconnect();
        }
    }








}
