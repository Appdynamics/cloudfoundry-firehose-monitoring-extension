package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.cloudfoundry.doppler.FirehoseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * @author Satish Muddam
 */

@SpringBootApplication
@Component
public class CfFirehoseMonitorTask implements CommandLineRunner {

    @Autowired
    private DopplerClient dopplerClient;


    private MonitorConfiguration monitorConfiguration;

    public void setMonitorConfiguration(MonitorConfiguration monitorConfiguration) {
        this.monitorConfiguration = monitorConfiguration;
    }

    @Override
    public void run(String... args) throws Exception {

        Flux<Envelope> cfEvents = this.dopplerClient.firehose(
                FirehoseRequest
                        .builder()
                        .subscriptionId(UUID.randomUUID().toString()).build());

        cfEvents.filter(e -> e.getEventType().equals(EventType.VALUE_METRIC)).subscribe(new FirehoseConsumer(monitorConfiguration));

    }
}