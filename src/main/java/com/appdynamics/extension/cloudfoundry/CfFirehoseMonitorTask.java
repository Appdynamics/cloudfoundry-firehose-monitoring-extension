package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extension.cloudfoundry.config.CfProperties;
import com.appdynamics.extension.cloudfoundry.config.FirehoseClientConfiguration;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
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

        String password = (String) cf.get("password");
        String encryptedPassword = (String) cf.get("encryptedPassword");
        String encryptionKey = (String) monitorConfiguration.getConfigYml().get("encryptionKey");
        
        properties.setPassword(getPassword(password, encryptedPassword, encryptionKey));
        properties.setSkipSslValidation((Boolean) cf.get("skipSslValidation"));
        String metricPathComponents = (String) monitorConfiguration.getConfigYml().get("metricPathComponents");

        DefaultConnectionContext connectionContext = FirehoseClientConfiguration.connectionContext(properties);
        PasswordGrantTokenProvider tokenProvider = FirehoseClientConfiguration.tokenProvider(properties);
        ReactorDopplerClient dopplerClient = FirehoseClientConfiguration.dopplerClient(connectionContext, tokenProvider);


        Flux<Envelope> cfEvents = dopplerClient.firehose(
                FirehoseRequest
                        .builder()
                        .subscriptionId(UUID.randomUUID().toString()).build());

        cfEvents.filter(e -> e.getEventType().equals(EventType.VALUE_METRIC)).subscribe(new FirehoseConsumer(monitorConfiguration.getMetricPrefix(), monitorConfiguration.getMetricWriter(), metricPathComponents));
    }

    private String getPassword(String password, String encryptedPassword, String encryptionKey) {

        if (!Strings.isNullOrEmpty(password)) {
            return password;
        } else {
            try {
                Map<String, String> args = Maps.newHashMap();
                args.put(TaskInputArgs.PASSWORD_ENCRYPTED, encryptedPassword);
                args.put(TaskInputArgs.ENCRYPTION_KEY, encryptionKey);
                return CryptoUtil.getPassword(args);
            } catch (IllegalArgumentException e) {
                String msg = "Encryption Key not specified. Please set the value in config.yml.";
                logger.error(msg);
                throw new IllegalArgumentException(msg, e);
            } catch (Exception e) {
                String msg = "Error decrypting password";
                logger.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
    }
}