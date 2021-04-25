package com.kafka.connect;

/**
 * Utility interface to manage JMS Connector properties and documentation.
 */
public interface WebLogicJmsConnector {
    String CONNECT_WEBLOGIC_JMS_PREFIX = "weblogic.jms.";
    String WEBLOGIC_T3_URL_DESTINATION_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "url";
    String WEBLOGIC_T3_URL_DESTINATION_DOC = "Provides WebLogic T3 url";
    String WEBLOGIC_USERNAME_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "username";
    String WEBLOGIC_USERNAME_DOC = "Provides weblogic username";
    String WEBLOGIC_PASSWORD_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "password";
    String WEBLOGIC_PASSWORD_DOC = "Provides weblogic password";
    String WEBLOGIC_JMS_CONNECTION_FACTORY_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "connection.factory";
    String WEBLOGIC_JMS_CONNECTION_FACTORY_DOC = "Provides the JMS Connection Factory name";
    String WEBLOGIC_JMS_DESTINATION_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "destination";
    String WEBLOGIC_JMS_DESTINATION_DOC = "Provides the JMS Queue name";
    String WEBLOGIC_JMS_ACKNOWLEDGE_MODE_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "acknowledge.mode";
    String WEBLOGIC_JMS_ACKNOWLEDGE_MODE_DOC = "Provides the JMS Acknowledge Mode";
    String WEBLOGIC_JMS_ACKNOWLEDGE_MODE_DEFAULT = "AUTO_ACKNOWLEDGE";
    String KAFKA_TOPIC_PREFIX_CONFIG = "topic.prefix";
    String KAFKA_TOPIC_PREFIX_DOC = "Provides the prefix for Kafka Topic name";
    String KAFKA_TOPIC_PREFIX_DEFAULT = "";
}
