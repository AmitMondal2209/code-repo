package com.kafka.connect;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.naming.Context;

import org.apache.kafka.connect.connector.ConnectorContext;

import weblogic.jms.extensions.DestinationAvailabilityListener;
import weblogic.jms.extensions.DestinationDetail;
import weblogic.jms.extensions.JMSDestinationAvailabilityHelper;
import weblogic.jms.extensions.RegistrationHandle;

public class QueueMonitor extends Thread implements DestinationAvailabilityListener,WebLogicJmsTask {
		private Hashtable<String, String> wlsEnvParamHashTbl = null;
	  	private final Object containerLock = new Object();
	    private final  CountDownLatch startLatch ;
	    private RegistrationHandle registrationHandle;
	    private ArrayList<String> containerMap = new ArrayList<String>();
	    boolean shutdown =false,changeflg=false;
	    private final ConnectorContext context;
	    
	public QueueMonitor(Map<String, String> props,ConnectorContext context) {
			super();
			wlsEnvParamHashTbl = new Hashtable<String, String>();
	        wlsEnvParamHashTbl.put(Context.PROVIDER_URL, props.get(WEBLOGIC_T3_URL_DESTINATION_CONFIG)); // set Weblogic JMS URL
	        wlsEnvParamHashTbl.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory"); // set Weblogic JNDI
	        wlsEnvParamHashTbl.put(Context.SECURITY_PRINCIPAL, props.get(WEBLOGIC_USERNAME_CONFIG)); // set Weblogic UserName
	        wlsEnvParamHashTbl.put(Context.SECURITY_CREDENTIALS, props.get(WEBLOGIC_PASSWORD_CONFIG)); // set Weblogic PassWord
	        for (Map.Entry<String,String> entry : wlsEnvParamHashTbl.entrySet())  
	            System.out.println("Key = " + entry.getKey() + 
	                             ", Value = " + entry.getValue()); 
	    System.out.println(props.get(WEBLOGIC_JMS_DESTINATION_CONFIG));
	        this.context=context;
	        this.startLatch = new CountDownLatch(1);
			JMSDestinationAvailabilityHelper dah = JMSDestinationAvailabilityHelper.getInstance();
			this.registrationHandle= dah.register(wlsEnvParamHashTbl, props.get(WEBLOGIC_JMS_DESTINATION_CONFIG), this);
			
				
			
		}

	public void run()
	{
		try {
			startLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!shutdown)
		{
			if(changeflg)
			{
				context.requestTaskReconfiguration();
				changeflg=false;
			}
			
		}
		if(shutdown)
		{
			System.out.println("Called shutdown, returning");
			return;
		}
		
	}
	
	public  String getContainers()
	{
		String list=null;
		synchronized (containerLock) {
			while(containerMap.isEmpty())
			{
				System.out.println("Waiting...");
				
				try {
					containerLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			list=String.join(",", containerMap);
			System.out.println("list is :"+list);
		}
		return list;
	}

	  @Override
		 public void onDestinationsAvailable(String destJNDIName, List<DestinationDetail> physicalAvailableMembers) {
		    synchronized (containerLock) {
		    	System.out.println("destJNDIName is :"+destJNDIName);
		      // For all Physical destinations, start a container
		      for (DestinationDetail detail : physicalAvailableMembers) {
		    	  System.out.println("member is :"+detail.getJNDIName());
		    	  containerMap.add(detail.getJNDIName());
		        //containerMap.put(detail.getJNDIName(), detail.getJNDIName());
		      }
		      containerLock.notifyAll();
		      if(startLatch.getCount()==0)
		      {
		    	  changeflg=true;
		      }
		      startLatch.countDown();  
		    }
		        
		  }

	    @Override
		public void onDestinationsUnavailable(String destJNDIName, List<DestinationDetail> physicalUnavailableMembers) {
			// TODO Auto-generated method stub
			synchronized (containerLock) {
			      // Shutdown all containers whose physical members are no longer available
			      for (DestinationDetail detail : physicalUnavailableMembers) {
			        containerMap.remove(detail.getJNDIName());
			        // maybe i will need to do somethinh here
			      }
			      changeflg=true;
			    }
			
		}

	    @Override
		  public void onFailure(String destJndiName, Exception exception) {
		    // Looks like a cluster wide failure
	    	 System.out.println("inside on failure");
		    shutdown();
		    System.out.println(exception);
		    
		  }
	    public void shutdown() {
		    // Unregister for events about destination availability
			  registrationHandle.unregister();

		    // Shut down containers
		    synchronized (containerLock) {
		    	containerMap.removeAll(containerMap);
		    }
		    
		    shutdown=true;
		  }
}
