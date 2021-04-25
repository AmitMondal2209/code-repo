package com.kafka.connect;

import weblogic.jms.client.JMSConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Utility interface to create a JMS for a JMS Task.
 */
public interface WebLogicJmsTask extends WebLogicJmsConnector {

    String WEBLOGIC_JNDI_INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";

 
}
