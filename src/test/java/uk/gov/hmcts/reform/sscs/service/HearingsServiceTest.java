package uk.gov.hmcts.reform.sscs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.HearingState;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

@ExtendWith(MockitoExtension.class)
public class HearingsServiceTest {

    @MockBean
    private HmcHearingApi hmcHearingApi;

    @MockBean
    private IdamService idamService;

    @MockBean
    private SscsCaseData sscsCaseData;

    private HearingWrapper wrapper;

    @BeforeEach
    void setUp() {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        given(wrapper.getOriginalCaseData()).willReturn(HearingWrapper.builder().build().setOriginalCaseData(sscsCaseData));
        given(wrapper.getUpdatedCaseData()).willReturn(HearingWrapper.builder().build().setUpdatedCaseData(sscsCaseData));
        given(wrapper.getEvent()).willReturn(HearingWrapper.builder().build().setEvent(EventType.HEARING));
        given(wrapper.getState()).willReturn(HearingWrapper.builder().build().setState(HearingState.CREATE_HEARING));
    }

    private final HearingsService hearingsService = new HearingsService(hmcHearingApi, idamService);

    @DisplayName("sendDeleteHearingRequest should send request successfully")
    @Test
    void sendDeleteHearingRequest(){
        System.out.println(wrapper);
        //hearingsService.sendDeleteHearingRequest(wrapper);
    }
}
