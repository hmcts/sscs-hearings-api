package uk.gov.hmcts.reform.sscs.jms.listener;

import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class TribunalsJmsListenerContainerFactory extends DefaultJmsListenerContainerFactory {
    private final String selector;

    public TribunalsJmsListenerContainerFactory(String jmsSelector) {
        super();
        this.selector = jmsSelector;
    }

    @Override
    public DefaultMessageListenerContainer createListenerContainer(JmsListenerEndpoint endpoint) {
        final DefaultMessageListenerContainer instance = super.createListenerContainer(endpoint);
        instance.setMessageSelector(selector);
        return instance;
    }

}
