package com.appdynamics.extension.cloudfoundry;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Satish Muddam
 */
@Component
public class ApplicationContextEventListener {
    
    @EventListener
    public void onApplicationEvent(final ApplicationContextEvent event) {

        MonitorConfiguration monitorConfiguration = CfFirehoseMonitor.configuration;

        ApplicationContext applicationContext = event.getApplicationContext();

        ConfigurableListableBeanFactory beanFactory = ((AnnotationConfigEmbeddedWebApplicationContext) applicationContext).getBeanFactory();

        CfFirehoseMonitorTask cfFirehoseMonitorTask = beanFactory.getBean(CfFirehoseMonitorTask.class);
        cfFirehoseMonitorTask.setMonitorConfiguration(monitorConfiguration);
    }
}
