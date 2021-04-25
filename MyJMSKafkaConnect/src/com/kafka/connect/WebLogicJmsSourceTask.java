package com.kafka.connect;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
// import org.apache.log4j.Logger;
// import org.slf4j.LoggerFactory;

import weblogic.jms.extensions.DestinationAvailabilityListener;
import weblogic.jms.extensions.DestinationDetail;
import weblogic.jms.extensions.JMSDestinationAvailabilityHelper;
import weblogic.jms.extensions.RegistrationHandle;

import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * WebLogic JMS Source Task implementation.
 */
public class WebLogicJmsSourceTask extends SourceTask implements WebLogicJmsTask {
   // private static final Logger LOGGER = LoggerFactory.getLogger(WebLogicJmsSourceTask.class);

    private String kafkaTopic;

    
    private Hashtable<String, String> wlsEnvParamHashTbl = null;
    private static InitialContext initialContext = null;
    private static QueueConnectionFactory queueConnectionFactory = null;
    private static QueueConnection queueConnection = null;
    private static QueueSession queueSession = null;
    private static Queue queue = null;
    private static QueueReceiver queueReceiver = null;
    private boolean quit = false;
    private String ackMode=null;
    private String connectFactoryName=null;
    private ArrayList<String> containerMap ;
  

    public String version() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        System.out.println("Task Started"); 
        wlsEnvParamHashTbl = new Hashtable<String, String>();
        wlsEnvParamHashTbl.put(Context.PROVIDER_URL, props.get(WEBLOGIC_T3_URL_DESTINATION_CONFIG)); // set Weblogic JMS URL
        wlsEnvParamHashTbl.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory"); // set Weblogic JNDI
        wlsEnvParamHashTbl.put(Context.SECURITY_PRINCIPAL, props.get(WEBLOGIC_USERNAME_CONFIG)); // set Weblogic UserName
        wlsEnvParamHashTbl.put(Context.SECURITY_CREDENTIALS, props.get(WEBLOGIC_PASSWORD_CONFIG)); // set Weblogic PassWord
        connectFactoryName=props.get(WEBLOGIC_JMS_CONNECTION_FACTORY_CONFIG);
        ackMode=props.get(WEBLOGIC_JMS_ACKNOWLEDGE_MODE_CONFIG);
        containerMap=new ArrayList<>(Arrays.asList(StringUtils.splitPreserveAllTokens(props.get("QUEUES"), ",")));
        kafkaTopic = props.get(KAFKA_TOPIC_PREFIX_CONFIG); //+ props.get(WEBLOGIC_JMS_DESTINATION_CONFIG);
        System.out.println("4");
      //  LOGGER.info
      System.out.println("Starting Kafka Connector - WebLogic JMS Source");
    }

    public void initializeConnParams(String memberjndi) throws NamingException, JMSException {
    	 
    	initialContext = new InitialContext(wlsEnvParamHashTbl);
		queueConnectionFactory = (QueueConnectionFactory) initialContext.lookup(connectFactoryName); // lookup using initial context
        queueConnection = queueConnectionFactory.createQueueConnection(); // create ConnectionFactory
        queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); // create QueueSession
        queue = (Queue) initialContext.lookup(memberjndi); // lookup Queue JNDI using initial context created above
        queueReceiver = queueSession.createReceiver(queue); // create Receiver using Queue JNDI as arguments
        
        queueConnection.start(); // start Queue connection
        
        System.out.println("Connected to "+memberjndi);
    }
    
    public List<SourceRecord> poll() throws InterruptedException {
    	ArrayList<SourceRecord> records=new ArrayList<SourceRecord>();
    	
    	for (String memberjndi:containerMap)
		{
    	try {
            final Map<String, Object> sourcePartition = Collections.singletonMap("destination", memberjndi);
            final Map<String, Object> sourceOffset = Collections.emptyMap();
            initializeConnParams( memberjndi);
            final Message message = queueReceiver.receiveNoWait();
            if(message==null)
            	continue;
            final ArrayList<String> propertyNames = Collections.list(message.getPropertyNames());
            final SchemaBuilder propertiesSchemaBuilder = SchemaBuilder.struct();
            propertyNames.forEach(propertyName -> propertiesSchemaBuilder.field(propertyName, Schema.OPTIONAL_STRING_SCHEMA));
            final Schema propertiesSchema = propertiesSchemaBuilder.build();
            final Struct propertiesStruct = new Struct(propertiesSchema);
            propertyNames.forEach(propertyName -> {
                try {
                    final String stringProperty = message.getStringProperty(propertyName);
                    propertiesStruct.put(propertyName, stringProperty);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });

            final SchemaBuilder headersBuilder = SchemaBuilder.struct();
            headersBuilder.field("JMSCorrelationID", Schema.OPTIONAL_STRING_SCHEMA);
            headersBuilder.field("JMSReplyTo", Schema.OPTIONAL_STRING_SCHEMA);
            headersBuilder.field("JMSType", Schema.OPTIONAL_STRING_SCHEMA);

            final Schema headersSchema = headersBuilder.build();
            final Struct headersStruct = new Struct(headersSchema);
            headersStruct.put("JMSCorrelationID", message.getJMSCorrelationID());
            headersStruct.put("JMSReplyTo", message.getJMSReplyTo());
            headersStruct.put("JMSType", message.getJMSType());

            final SchemaBuilder builder =
                SchemaBuilder.struct()
                    .name("jms")
                    .field("headers", headersSchema)
                    .field("properties", propertiesSchema);

            final Schema keySchema = builder.build();
            final Struct key = new Struct(keySchema);
            key.put("headers", headersStruct);
            key.put("properties", propertiesStruct);

            final TextMessage textMessage = (TextMessage) message;
            final String text = textMessage.getText();
            final SourceRecord sourceRecord =
                new SourceRecord(
                    sourcePartition,
                    sourceOffset,
                    kafkaTopic,
                    keySchema,
                    key,
                    Schema.STRING_SCHEMA,
                    text);
          //  LOGGER.info("Producing message: key={} value={}", key.toString(), context);
            //return Collections.singletonList(sourceRecord);
            records.add(sourceRecord);
            close();
        } catch (JMSException e) {
            // Underlying stream was killed, probably as a result of calling stop. Allow to return
            // null, and driving thread will handle any shutdown if necessary.
            System.out.println("In exception"); 
            e.printStackTrace(System.out);
            
         }
        //return null;
 catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		}
    	if(records.isEmpty())
    	{
    		records=null;
    	}
    	
    	return records;
    }

    public void stop() {
    	
    	try {
			close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

  
    public void close() throws JMSException {
    	 
        queueReceiver.close();
        queueSession.close();
        queueConnection.close();
    }
 
}
