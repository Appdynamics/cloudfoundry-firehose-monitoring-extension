package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.ValueMetric;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * @author Satish Muddam
 */
public class FirehoseConsumer implements Consumer<Envelope> {

    private MonitorConfiguration configuration;

    public FirehoseConsumer(MonitorConfiguration monitorConfiguration) {
        this.configuration = monitorConfiguration;
    }

    @Override
    public void accept(Envelope envelope) {

        String origin = envelope.getOrigin();
        String deployment = envelope.getDeployment();
        String job = envelope.getJob();

        //Long timestamp = envelope.getTimestamp();
        //ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp / 1000000).atZone(ZoneId.systemDefault());

        ValueMetric valueMetric = envelope.getValueMetric();

        if (valueMetric.value() == null) {
            return;
        }

        String metricPrefix = configuration.getMetricPrefix();
        String metricPath = metricPrefix + "|" + origin + "|" + deployment + "|" + job + "|" + valueMetric.getName();

        BigDecimal bigDecimalVal = BigDecimal.valueOf(valueMetric.value());

        configuration.getMetricWriter().printMetric(metricPath, bigDecimalVal, "OBS.CUR.COL");

        System.out.println("Printing metric " + metricPath + "  value" + bigDecimalVal);

    }
}
