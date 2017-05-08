package com.appdynamics.extension.cloudfoundry;

import static org.mockito.Matchers.eq;

import com.appdynamics.extensions.util.MetricWriteHelper;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.cloudfoundry.doppler.ValueMetric;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

/**
 * @author Satish Muddam
 */
@RunWith(MockitoJUnitRunner.class)
public class FirehoseConsumerTest {

    @Mock
    private MetricWriteHelper mockMetricWriterHelper;

    @Test
    public void testAccept() throws Exception {

        FirehoseConsumer firehoseConsumer = new FirehoseConsumer("Custom Metrics|Firehose", mockMetricWriterHelper, "ORIGIN|DEPLOYMENT|JOB");

        ValueMetric valueMetric = ValueMetric.builder().name("Test Metric").value(100d).unit("abc").build();

        Envelope envelope = Envelope.builder().deployment("Test Deployment").job("Test Job").valueMetric(valueMetric).eventType(EventType.VALUE_METRIC).origin("Test Origin").build();

        firehoseConsumer.accept(envelope);

        Mockito.verify(mockMetricWriterHelper).printMetric(Mockito.eq("Custom Metrics|Firehose|Test Origin|Test Deployment|Test Job|Test Metric"), eq(BigDecimal.valueOf(valueMetric.value())), eq("OBS.CUR.COL"));
    }

    @Test
    public void testAcceptWithoutOrigin() throws Exception {

        FirehoseConsumer firehoseConsumer = new FirehoseConsumer("Custom Metrics|Firehose", mockMetricWriterHelper, "DEPLOYMENT|JOB");

        ValueMetric valueMetric = ValueMetric.builder().name("Test Metric").value(100d).unit("abc").build();

        Envelope envelope = Envelope.builder().deployment("Test Deployment").job("Test Job").valueMetric(valueMetric).eventType(EventType.VALUE_METRIC).origin("Test Origin").build();

        firehoseConsumer.accept(envelope);

        Mockito.verify(mockMetricWriterHelper).printMetric(Mockito.eq("Custom Metrics|Firehose|Test Deployment|Test Job|Test Metric"), eq(BigDecimal.valueOf(valueMetric.value())), eq("OBS.CUR.COL"));
    }
}
