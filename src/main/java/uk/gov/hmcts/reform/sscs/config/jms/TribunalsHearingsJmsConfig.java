package uk.gov.hmcts.reform.sscs.config.jms;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@Slf4j
@Configuration
public class TribunalsHearingsJmsConfig {

    @Value("${azure.service-bus.tribunals-to-hearings-api.connectionString}")
    private String connectionString;

    @Value("${azure.service-bus.tribunals-to-hearings-api.username}")
    private String username;

    @Value("${azure.service-bus.tribunals-to-hearings-api.password}")
    private String password;

    @Value("${azure.service-bus.tribunals-to-hearings-api.receiveTimeout}")
    private Long receiveTimeout;

    @Value("${azure.service-bus.tribunals-to-hearings-api.idleTimeout}")
    private Long idleTimeout;
    public static final String AMQP_CONNECTION_STRING_TEMPLATE = "amqps://%1s?amqp.idleTimeout=%2d";
    @Bean
    @ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
    public ConnectionFactory tribunalsHearingsJmsConnectionFactory(@Value("${spring.application.name}") final String clientId) {
        String connection = String.format(AMQP_CONNECTION_STRING_TEMPLATE, connectionString, idleTimeout);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(connection);
        jmsConnectionFactory.setUsername(username);
        jmsConnectionFactory.setPassword(password);
        jmsConnectionFactory.setClientID(clientId);
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    @ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> tribunalsHearingsEventQueueContainerFactory(
        ConnectionFactory tribunalsHearingsJmsConnectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer defaultJmsListenerContainerFactoryConfigurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(tribunalsHearingsJmsConnectionFactory);
        factory.setReceiveTimeout(receiveTimeout);
        factory.setSessionTransacted(Boolean.TRUE);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setMessageConverter(new JsonMessageConvertor());
        defaultJmsListenerContainerFactoryConfigurer.configure(factory, tribunalsHearingsJmsConnectionFactory);
        return factory;
    }
}