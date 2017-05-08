package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extensions.util.MetricWriteHelper;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.ValueMetric;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * @author Satish Muddam
 */
public class FirehoseConsumer implements Consumer<Envelope> {

    private enum PathComponents {
        ORIGIN, DEPLOYMENT, JOB
    }

    private static final Logger logger = Logger.getLogger(FirehoseConsumer.class);


    private String metricPrefix;
    private MetricWriteHelper metricWriteHelper;
    private String metricPathComponents;

    public FirehoseConsumer(String metricPrefix, MetricWriteHelper metricWriteHelper, String metricPathComponents) {
        this.metricPrefix = metricPrefix;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPathComponents = metricPathComponents;
    }

    @Override
    public void accept(Envelope envelope) {

        if (Strings.isNullOrEmpty(metricPathComponents)) {
            metricPathComponents = "ORIGIN|DEPLOYMENT|JOB";
        }

        metricPathComponents = metricPathComponents.trim();

        String[] split = metricPathComponents.split("\\|");

        String metricPath = metricPrefix + "|";
        for (String pathComponent : split) {
            if (PathComponents.ORIGIN.name().equalsIgnoreCase(pathComponent)) {
                metricPath += envelope.getOrigin() + "|";
            } else if (PathComponents.DEPLOYMENT.name().equalsIgnoreCase(pathComponent)) {
                metricPath += envelope.getDeployment() + "|";
            } else if (PathComponents.JOB.name().equalsIgnoreCase(pathComponent)) {
                metricPath += envelope.getJob() + "|";
            } else {
                logger.info("Ignoring invalid metric path component specified. Accepts only ORIGIN, DEPLOYMENT and JOB");
            }
        }

        ValueMetric valueMetric = envelope.getValueMetric();

        metricPath += valueMetric.getName();

        if (valueMetric.value() == null) {
            logger.debug(String.format("Ignoring metric [%s] with null value", metricPath));
            return;
        }

        BigDecimal bigDecimalVal = BigDecimal.valueOf(valueMetric.value());

        logger.debug("Printing metric : " + metricPath + "  value : " + bigDecimalVal);
        metricWriteHelper.printMetric(metricPath, bigDecimalVal, "OBS.CUR.COL");
    }
}