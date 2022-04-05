package uk.gov.hmcts.reform.sscs.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class HearingsServiceTest {

    @Mock
    private HmcHearingApi hmcHearingApi;

    @Mock
    private IdamService idamService;

    private HearingsService hearingsService;
    private HearingWrapper wrapper;
    private IdamTokens idamTokens;
    private static String CANCEL_REASON_TEMP = "AWAITING_LISTING";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        idamTokens = IdamTokens.builder()
            .idamOauth2Token("token_12334132")
            .serviceAuthorization("authorized")
            .userId("12")
            .build();

        given(idamService.getIdamTokens()).willReturn(idamTokens);
        hearingsService = new HearingsService(hmcHearingApi, idamService);
    }



    @DisplayName("sendDeleteHearingRequest should send request successfully")
    @Test
    void sendDeleteHearingRequest() {
        wrapper = HearingWrapper.builder()
                //.updatedCaseData(SscsCaseData.builder().canclelationCode(CANCEL_REASON_TEMP).build())
                //TODO: Uncomment when implemented
                .build();
        HearingResponse expectedHearingResponse = HearingResponse.builder()
                    .status(HttpServletResponse.SC_OK + "")
                    .build();
        HearingDeleteRequestPayload payload = HearingDeleteRequestPayload.builder()
            .cancellationReasonCode(CANCEL_REASON_TEMP)
            // .cancellationReasonCode(wrapper.getOriginalCaseData())
            // TODO: Uncomment when implemented.
            .build();

        given(hmcHearingApi.deleteHearingRequest(idamTokens.getIdamOauth2Token(),
                                                 idamTokens.getServiceAuthorization(),
                                                 idamTokens.getUserId(), payload))
            .willReturn(expectedHearingResponse);
        HearingResponse actualHearingResponse = hearingsService.sendDeleteHearingRequest(wrapper);

        assertThat(actualHearingResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK + "");
        assertThat(actualHearingResponse).isEqualTo(expectedHearingResponse);
    }
}
