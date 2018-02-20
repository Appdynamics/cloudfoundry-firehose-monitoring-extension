Cloudfoundry Firehose monitoring extension
==============================================

This extension works only with the AppDynamics machine agent.

## Use Case

Cloud Foundry is an open source, multi cloud application platform as a service governed by the Cloud Foundry Foundation. This extension tries to connect to the Cloud Foundry Firehose and gathers metrics and reports them to the AppDynamics controller.

## Metrics Provided ##

This extension provides all value type metrics from the Firehose.

Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).
For eg.
```
    java -Dappdynamics.agent.maxMetrics=2500 -jar machineagent.jar
```

## Installation ##

1. Run "mvn clean install" and find the PCFMonitor-<version>.zip file in the "target" folder. You can also download the PCFMonitor-<version>.zip from [AppDynamics Exchange][].
2. Unzip as "PCFMonitor" and copy the "PCFMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`
  
# Configuration ##

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the Cloud Foundry instances by editing the config.yaml file in `<MACHINE_AGENT_HOME>/monitors/PCFMonitor/`.
2. Below is the default config.yaml
   
```
#This will create this metric in all the tiers, under this path
#metricPrefix: Custom Metrics|CloudFoundry

#This will create it in specific Tier/Component. Make sure to replace <COMPONENT_ID> with the appropriate one from your environment.
#To find the <COMPONENT_ID> in your environment, please follow the screenshot https://docs.appdynamics.com/display/PRO42/Build+a+Monitoring+Extension+Using+Java
metricPrefix: Server|Component:<COMPONENT_ID>|Custom Metrics|CloudFoundry

numberOfThreads : 2

cf:
  - host: api.local.pcfdev.io
    user: admin
    password: admin
    encryptedPassword:
    skipSslValidation: true

encryptionKey: Hello

#Supported Values are ORIGIN, DEPLOYMENT and JOB separated by |. Default is ORIGIN|DEPLOYMENT|JOB
#You can change the order or remove one if not needed. DEPLOYMENT|JOB is a valid input.
metricPathComponents: ORIGIN|DEPLOYMENT|JOB

```
3. Configure the path to the config.yaml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/FirehoseMonitor/` directory. Below is the sample
   For Windows, make sure you enter the right path.
     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/FirehoseMonitor/config.yaml" />
          ....
     </task-arguments>
    ```
    
## Password Encryption Support
To avoid setting the clear text password in the config.yaml, please follow the process below to encrypt the password

1. Download the util jar to encrypt the password from [https://github.com/Appdynamics/maven-repo/blob/master/releases/com/appdynamics/appd-exts-commons/1.1.2/appd-exts-commons-1.1.2.jar](https://github.com/Appdynamics/maven-repo/blob/master/releases/com/appdynamics/appd-exts-commons/1.1.2/appd-exts-commons-1.1.2.jar) and navigate to the downloaded directory
2. Encrypt password from the commandline
`java -cp appd-exts-commons-1.1.2.jar com.appdynamics.extensions.crypto.Encryptor encryptionKey myPassword`
3. Specify the encryptedPassword and encryptionKey in config.yaml    

## Workbench ##

Workbench is a feature by which you can preview the metrics before registering it with the controller. This is useful if you want to fine tune the configurations. Workbench is embedded into the extension jar.

To use the workbench

* Follow all the installation steps
* Start the workbench with the command
~~~
  java -jar /path/to/MachineAgent/monitors/PCFMonitor/firehose-monitoring-extension.jar
  This starts an http server at http://host:9090/. This can be accessed from the browser.
~~~
* If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.
~~~
    #Get the stats
    curl http://localhost:9090/api/stats
    #Get the registered metrics
    curl http://localhost:9090/api/metric-paths
~~~
* You can make the changes to config.yml and validate it from the browser or the API
* Once the configuration is complete, you can kill the workbench and start the Machine Agent

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Exchange][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.1
**Controller Compatibility:** 3.7+

[Github]: https://github.com/Appdynamics/cloudfoundry-firehose-monitoring-extension
[AppDynamics Exchange]: https://www.appdynamics.com/community/exchange/
[AppDynamics Center of Excellence]: mailto:help@appdynamics.com

