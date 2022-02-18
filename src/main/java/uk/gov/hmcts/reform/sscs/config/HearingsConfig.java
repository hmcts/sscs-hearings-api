package uk.gov.hmcts.reform.sscs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class HearingsConfig {

    private Environment env;

    HearingsConfig(@Autowired Environment env) {
        this.env = env;
    }

}
