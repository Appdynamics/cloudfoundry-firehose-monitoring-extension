package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extension.cloudfoundry.config.CfProperties;
import com.appdynamics.extension.cloudfoundry.config.FirehoseClientConfiguration;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.apache.log4j.Logger;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.cloudfoundry.doppler.FirehoseRequest;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;

/**
 * @author Satish Muddam
 */

public class CfFirehoseMonitorTask implements Runnable {

    private static final Logger logger = Logger.getLogger(CfFirehoseMonitorTask.class);

    private MonitorConfiguration monitorConfiguration;
    private Map cf;

    public CfFirehoseMonitorTask(MonitorConfiguration configuration, Map cf) {
        this.cf = cf;
        this.monitorConfiguration = configuration;
    }

    @Override
    public void run() {

        logger.info("Starting CfFirehoseMonitor Task");

        CfProperties properties = new CfProperties();
        properties.setHost((String) cf.get("host"));
        properties.setUser((String) cf.get("user"));
        properties.setPassword((String) cf.get("password"));
        properties.setSkipSslValidation((Boolean) cf.get("skipSslValidation"));


        DefaultConnectionContext connectionContext = FirehoseClientConfiguration.connectionContext(properties);
        PasswordGrantTokenProvider tokenProvider = FirehoseClientConfiguration.tokenProvider(properties);
        ReactorDopplerClient dopplerClient = FirehoseClientConfiguration.dopplerClient(connectionContext, tokenProvider);


        Flux<Envelope> cfEvents = dopplerClient.firehose(
                FirehoseRequest
                        .builder()
                        .subscriptionId(UUID.randomUUID().toString()).build());

        cfEvents.filter(e -> e.getEventType().equals(EventType.VALUE_METRIC)).subscribe(new FirehoseConsumer(monitorConfiguration.getMetricPrefix(), monitorConfiguration.getMetricWriter()));
    }
}