package com.kafka.connect;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class WebLogicJmsSourceConnector extends SourceConnector {
    private final ConfigDef configDef = WebLogicJmsSourceConfig.config;
     QueueMonitor queuemonitor;
    private Map<String, String> configProps;

    public String version() {
        System.out.println("Called Version");
        return this.getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        System.out.println("Worker Started");
        configProps = props;
        queuemonitor = new QueueMonitor(props,context);
        queuemonitor.start();
    }

    public Class<? extends Task> taskClass() {
        return WebLogicJmsSourceTask.class;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
    	final ArrayList<Map<String, String>> configs = new ArrayList<>();
        System.out.println("TaskConfig Called");
        
        for(int j=0;j<maxTasks;j++)
        {
        	
        	System.out.println("created task :"+j);	
        final Map<String, String> taskConfigs = new HashMap<>();
        String queues=queuemonitor.getContainers();
        System.out.println("queues :"+queues);
        taskConfigs.put("QUEUES", queues);
        taskConfigs.putAll(configProps);
        configs.add(taskConfigs);
        }
        System.out.println("Returning Config");
        return configs;
    }

    public void stop() throws ConnectException{
    	System.out.println("Shutting down the monitor");
    	queuemonitor.shutdown();
    	
    		try {
				queuemonitor.join(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    }

    public ConfigDef config() {
        return configDef;
    }
}
