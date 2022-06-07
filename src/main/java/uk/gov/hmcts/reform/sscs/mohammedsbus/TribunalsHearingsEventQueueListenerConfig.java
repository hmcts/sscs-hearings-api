package uk.gov.hmcts.reform.sscs.mohammedsbus;

import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

@Configuration
@EnableJms
public class TribunalsHearingsEventQueueListenerConfig {

    private static final String connectionString = "";

    @Autowired
    ConnectionFactory connectionFactory;

    @Bean
    public ConnectionFactory tribunalsHearingsConnectionFactory() {
        ServiceBusJmsConnectionFactorySettings connFactorySettings = new ServiceBusJmsConnectionFactorySettings();
        connFactorySettings.setConnectionIdleTimeoutMS(20000);
        ConnectionFactory factory = new ServiceBusJmsConnectionFactory(connectionString, connFactorySettings);
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(){
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("1-1");
        return factory;
    }



}
