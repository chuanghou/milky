package com.stellariver.milky.common.tool.ssh;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SshTunnel {


    public static Pair<String, Integer> exec(String command, Duration timeout, SshConfig sshConfig) throws IOException {
        try(SSHClient ssh = new SSHClient()){
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(sshConfig.getIp(), sshConfig.getPort());
            ssh.authPassword(sshConfig.getUsername(), sshConfig.getPassword());
            try (Session session = ssh.startSession()) {
                final Session.Command cmd = session.exec(command);
                String printable = IOUtils.readFully(cmd.getInputStream()).toString();
                cmd.join(timeout.getSeconds(), TimeUnit.SECONDS);
                return Pair.of(printable, cmd.getExitStatus());
            }
        }

    }


    public static void upload(File file, String folder, SshConfig sshConfig) throws IOException {
        folder = folder.endsWith("/") ? folder : folder + "/";
        try(SSHClient ssh = new SSHClient()){
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(sshConfig.getIp(), sshConfig.getPort());
            ssh.authPassword(sshConfig.getUsername(), sshConfig.getPassword());
            ssh.useCompression();
            try (SFTPClient sftpClient = ssh.newSFTPClient()) {
                sftpClient.put(new FileSystemFile(file), folder + file.getName());
            }
        }

    }


    public static void main(String[] args) throws IOException {
        SshConfig sshConfig = SshConfig.builder().ip("8.217.162.128").username("root").password("Octopus123456").build();
        Pair<String, Integer> exec = exec("pwd", Duration.ofSeconds(5), sshConfig);
        System.out.println(exec);
        upload(new File("arthas-packaging-3.7.2-bin"), "/root/", sshConfig);
    }
}
