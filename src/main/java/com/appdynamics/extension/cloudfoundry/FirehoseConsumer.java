package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extensions.util.MetricWriteHelper;
import org.apache.log4j.Logger;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.ValueMetric;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * @author Satish Muddam
 */
public class FirehoseConsumer implements Consumer<Envelope> {

    private static final Logger logger = Logger.getLogger(FirehoseConsumer.class);


    private String metricPrefix;
    private MetricWriteHelper metricWriteHelper;

    public FirehoseConsumer(String metricPrefix, MetricWriteHelper metricWriteHelper) {
        this.metricPrefix = metricPrefix;
        this.metricWriteHelper = metricWriteHelper;
    }

    @Override
    public void accept(Envelope envelope) {

        String deployment = envelope.getDeployment();
        String job = envelope.getJob();

        //Long timestamp = envelope.getTimestamp();
        //ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp / 1000000).atZone(ZoneId.systemDefault());

        ValueMetric valueMetric = envelope.getValueMetric();

        String metricPath = metricPrefix + "|" + deployment + "|" + job + "|" + valueMetric.getName();

        if (valueMetric.value() == null) {
            logger.debug(String.format("Ignoring metric [%s] with null value", metricPath));
            return;
        }

        BigDecimal bigDecimalVal = BigDecimal.valueOf(valueMetric.value());

        logger.debug("Printing metric : " + metricPath + "  value : " + bigDecimalVal);
        metricWriteHelper.printMetric(metricPath, bigDecimalVal, "OBS.CUR.COL");


    }
}
