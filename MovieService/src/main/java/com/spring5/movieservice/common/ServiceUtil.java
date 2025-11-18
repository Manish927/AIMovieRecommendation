package com.spring5.movieservice.common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ServiceUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);

    private String port;
    private String serviceAddress = null;

    @Autowired
    ServiceUtil(@Value("${server.port:8081}") String port) {
        this.port = port;
    }

    public String getServiceAddress() {
        if (serviceAddress == null) {
            serviceAddress = findHostName() + "/" + findIpAddr() + ":" + port;
        }

        return serviceAddress;
    }

    private String findHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-hostname";
        }
    }

    private String findIpAddr() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown-host-addr";
        }
    }
}
