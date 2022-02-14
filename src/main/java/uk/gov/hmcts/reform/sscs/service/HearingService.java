package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.domain.SscsCaseDataWrapper;

@Service
@Slf4j
public class HearingService {

    @SuppressWarnings("squid:S107")
    @Autowired
    public HearingService() {
    }

    public void doSomething(SscsCaseDataWrapper sscsCaseDataWrapper) {

    }
}
