package com.appdynamics.extension.cloudfoundry.config;

import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;

public class FirehoseClientConfiguration {

    public static DefaultConnectionContext connectionContext(CfProperties cfProps) {
        return DefaultConnectionContext.builder()
                .apiHost(cfProps.getHost())
                .skipSslValidation(cfProps.isSkipSslValidation())
                .build();
    }

    public static PasswordGrantTokenProvider tokenProvider(CfProperties cfProps) {
        return PasswordGrantTokenProvider.builder()
                .username(cfProps.getUser())
                .password(cfProps.getPassword())
                .build();
    }

    public static ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }
}