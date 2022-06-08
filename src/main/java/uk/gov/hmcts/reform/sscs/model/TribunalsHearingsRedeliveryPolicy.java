package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.qpid.jms.JmsDestination;
import org.apache.qpid.jms.policy.JmsRedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class TribunalsHearingsRedeliveryPolicy implements JmsRedeliveryPolicy {

    @Value("${azure.service-bus.tribunals-to-hearings-api.maxRedeliveries}")
    private int maxRedeliveries;

    private int outcome = 5;

    @Override
    public JmsRedeliveryPolicy copy() {
        return new TribunalsHearingsRedeliveryPolicy(maxRedeliveries, outcome);
    }

    @Override
    public int getMaxRedeliveries(JmsDestination destination) {
        return maxRedeliveries;
    }

    @Override
    public int getOutcome(JmsDestination destination) {
        return outcome;
    }
}
