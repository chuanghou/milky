package com.stellariver.milky.common.tool.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SshClient {

    SshInfo sshInfo;
    JSch jSch;
    ChannelExec channel;

    public SshClient(SshInfo sshInfo) throws JSchException, IOException {
        this.sshInfo = sshInfo;
        this.jSch = new JSch();
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        Session session = this.jSch.getSession(sshInfo.getUser(), sshInfo.getIp(), sshInfo.getPort());
        session.setConfig(config);
        session.setPassword(sshInfo.getPassword());
        session.connect(30000);
        this.channel = (ChannelExec) session.openChannel("exec");
        InputStream in = channel.getInputStream();
        channel.setCommand("dir");
        channel.setErrStream(System.err);
        channel.connect();
        String result = IOUtils.toString(in, "GBK");
        System.out.println(result);
    }

    public static void main(String[] args) throws JSchException, IOException {
        SshInfo sshInfo = SshInfo.builder().ip("118.184.179.116").password("co188.com").user("administrator").build();
        new SshClient(sshInfo);
    }
}
