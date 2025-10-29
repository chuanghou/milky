package com.stellariver.milky.common.tool.ssh;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SshTunnel {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SShResult {

        Integer exitStatus;
        String standOutput;
        String errorOutput;
    }

    public static SShResult exec(String rawCommand, Duration timeout, SshConfig sshConfig) throws IOException {
        try (final SSHClient sshClient = new SSHClient()) {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(sshConfig.getIp(), sshConfig.getPort());
            sshClient.authPassword(sshConfig.getUsername(), sshConfig.getPassword());
            try (final Session session = sshClient.startSession()) {
                final Session.Command command = session.exec(rawCommand);
                command.join(timeout.getSeconds(), TimeUnit.SECONDS);
                String standOutput = IOUtils.readFully(command.getInputStream()).toString();
                String errorOutput = IOUtils.readFully(command.getErrorStream()).toString();
                return SShResult.builder()
                        .errorOutput(errorOutput).standOutput(standOutput).exitStatus(command.getExitStatus()).build();
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

//    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
//        SshConfig sshConfig = SshConfig.builder().ip("114.55.63.71").username("root").password("**********!").build();
//        SShResult exec = exec("echo \"begin sleep\"; sleep 5; echo \"not happen!\"", Duration.ofSeconds(3), sshConfig);
//        System.out.println(exec);
//    }
}
