package com.stellariver.milky.demo.adapter;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FtpConfiguration {

    @Bean
    public FtpServer ftpServer() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();

//        custom port
//        ListenerFactory listenerFactory = new ListenerFactory();
//        listenerFactory.setPort(customPort);
//        serverFactory.addListener("default", listenerFactory.createListener());

        BaseUser baseUser = new BaseUser();
        baseUser.setName("admin");
        baseUser.setPassword("admin");
        baseUser.setHomeDirectory("/home/admin");

        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        baseUser.setAuthorities(authorities);

        serverFactory.getUserManager().save(baseUser);

        FtpServer ftpServer = serverFactory.createServer();
        ftpServer.start();
        return ftpServer;
    }

}
