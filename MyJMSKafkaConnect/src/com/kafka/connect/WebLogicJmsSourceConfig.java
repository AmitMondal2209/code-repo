package com.kafka.connect;

import org.apache.kafka.common.config.ConfigDef;

/**
 *
 */
final class WebLogicJmsSourceConfig {
    static final ConfigDef config =
        new ConfigDef()
            .define(
                WebLogicJmsConnector.WEBLOGIC_JMS_CONNECTION_FACTORY_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.Importance.HIGH,
                WebLogicJmsConnector.WEBLOGIC_JMS_CONNECTION_FACTORY_DOC)
            .define(
                WebLogicJmsConnector.WEBLOGIC_JMS_DESTINATION_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.Importance.HIGH,
                WebLogicJmsConnector.WEBLOGIC_JMS_DESTINATION_DOC)
            .define(
                WebLogicJmsConnector.WEBLOGIC_JMS_ACKNOWLEDGE_MODE_CONFIG,
                ConfigDef.Type.STRING,
                WebLogicJmsConnector.WEBLOGIC_JMS_ACKNOWLEDGE_MODE_DEFAULT,
                ConfigDef.Importance.MEDIUM,
                WebLogicJmsConnector.WEBLOGIC_JMS_ACKNOWLEDGE_MODE_DOC)
            .define(
                WebLogicJmsConnector.WEBLOGIC_T3_URL_DESTINATION_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.Importance.HIGH,
                WebLogicJmsConnector.WEBLOGIC_T3_URL_DESTINATION_DOC)
            .define(
                WebLogicJmsConnector.WEBLOGIC_USERNAME_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.Importance.HIGH,
                WebLogicJmsConnector.WEBLOGIC_USERNAME_DOC)
            .define(
                WebLogicJmsConnector.WEBLOGIC_PASSWORD_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.Importance.HIGH,
                WebLogicJmsConnector.WEBLOGIC_PASSWORD_DOC)
            .define(
                WebLogicJmsConnector.KAFKA_TOPIC_PREFIX_CONFIG,
                ConfigDef.Type.STRING,
                WebLogicJmsConnector.KAFKA_TOPIC_PREFIX_DEFAULT,
                ConfigDef.Importance.MEDIUM,
                WebLogicJmsConnector.KAFKA_TOPIC_PREFIX_DOC);
}
