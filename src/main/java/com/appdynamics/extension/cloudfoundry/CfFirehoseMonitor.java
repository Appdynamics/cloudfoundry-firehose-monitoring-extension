package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Satish Muddam
 */
public class CfFirehoseMonitor extends AManagedMonitor {

    private static final Logger logger = Logger.getLogger(CfFirehoseMonitor.class);


    private static final String CONFIG_ARG = "config-file";
    private static final String METRIC_PREFIX = "Custom Metrics|CloudFoundry";

    static MonitorConfiguration configuration;
    private boolean initialized;

    public CfFirehoseMonitor() {
        System.out.println(logVersion());
    }

    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext out) throws TaskExecutionException {
        logVersion();
        if (!initialized) {
            initialize(taskArgs);
        }
        logger.debug(String.format("The raw arguments are %s", taskArgs));
        configuration.executeTask();


        //As this is continuous extension, make this thread wait indefinitely.
        CountDownLatch infiniteWait = new CountDownLatch(1);
        try {
            infiniteWait.await();   //Will make this thread to wait till the CountDownLatch reaches to 0.
        } catch (InterruptedException e) {
            logger.error("Failed to wait indefinitely ", e);
        }

        logger.info("CfFirehoseMonitor monitor run completed successfully.");
        return new TaskOutput("CfFirehoseMonitor monitor run completed successfully.");
    }

    private void initialize(Map<String, String> argsMap) {

        if (!initialized) {
            final String configFilePath = argsMap.get(CONFIG_ARG);

            MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(this);
            MonitorConfiguration conf = new MonitorConfiguration(METRIC_PREFIX, new TaskRunnable(), metricWriteHelper);
            conf.setConfigYml(configFilePath);

            conf.checkIfInitialized(MonitorConfiguration.ConfItem.CONFIG_YML, MonitorConfiguration.ConfItem.METRIC_PREFIX,
                    MonitorConfiguration.ConfItem.METRIC_WRITE_HELPER, MonitorConfiguration.ConfItem.EXECUTOR_SERVICE);
            this.configuration = conf;
            initialized = true;
        }
    }


    private class TaskRunnable implements Runnable {

        public void run() {
            if (!initialized) {
                logger.info("CfFirehoseMonitor Monitor is still initializing");
                return;
            }

            Map<String, ?> config = configuration.getConfigYml();

            List<Map> cfs = (List<Map>) config.get("cf");

            if (cfs == null || cfs.isEmpty()) {
                logger.error("No CF servers configured in config.yml");
                return;
            }

            for (Map cf : cfs) {
                CfFirehoseMonitorTask task = new CfFirehoseMonitorTask(configuration, cf);
                configuration.getExecutorService().execute(task);
            }
        }
    }

    private static String getImplementationVersion() {
        return CfFirehoseMonitor.class.getPackage().getImplementationTitle();
    }

    private String logVersion() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        logger.info(msg);
        return msg;
    }

    public static void main(String[] args) throws TaskExecutionException {

        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));

        logger.getRootLogger().addAppender(ca);
        
        CfFirehoseMonitor monitor = new CfFirehoseMonitor();

        Map<String, String> taskArgs = new HashMap<>();
        taskArgs.put(CONFIG_ARG, "/Users/Muddam/AppDynamics/Code/extensions/cloudfoundry-firehose-monitoring-extension/src/main/resources/config.yaml");

        monitor.execute(taskArgs, null);
    }
}