package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Satish Muddam
 */
public class CfFirehoseMonitor extends AManagedMonitor {

    private static final Logger logger = Logger.getLogger(CfFirehoseMonitor.class);


    private static final String CONFIG_ARG = "config-file";
    private static final String METRIC_PREFIX = "Custom Metrics|CF";

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
        logger.info("CfFirehoseMonitor monitor run completed successfully.");
        return new TaskOutput("CfFirehoseMonitor monitor run completed successfully.");
    }

    private void initialize(Map<String, String> taskArgs) {

        final String configFilePath = taskArgs.get(CONFIG_ARG);
        MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(this);
        MonitorConfiguration conf = new MonitorConfiguration(METRIC_PREFIX, new TaskRunnable(), metricWriteHelper);
        conf.setConfigYml(configFilePath);
        conf.checkIfInitialized(MonitorConfiguration.ConfItem.CONFIG_YML, MonitorConfiguration.ConfItem.EXECUTOR_SERVICE,
                MonitorConfiguration.ConfItem.METRIC_PREFIX, MonitorConfiguration.ConfItem.METRIC_WRITE_HELPER);
        CfFirehoseMonitor.configuration = conf;

        SpringApplication.run(CfFirehoseMonitorTask.class, new String[]{});

        initialized = true;
    }


    private class TaskRunnable implements Runnable {
        public void run() {
            logger.info("Executing periodic run of CfFirehoseMonitor.");
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


        CfFirehoseMonitor monitor = new CfFirehoseMonitor();

        Map<String, String> taskArgs = new HashMap<>();
        taskArgs.put(CONFIG_ARG, "/Users/Muddam/AppDynamics/Code/extensions/cloudfoundry-firehose-monitoring-extension/src/main/resources/config.yaml");

        monitor.execute(taskArgs, null);

    }
}
