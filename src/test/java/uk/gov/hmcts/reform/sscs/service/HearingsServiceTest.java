package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingState;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.HearingState;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

class HearingsServiceTest {

    public static final long HEARING_REQUEST_ID = 12345;
    public static final String HMC_STATUS = "TestStatus";
    public static final long VERSION = 1;
    public static final String CANCELLATION_REASON_CODE = "TestCancelCode";
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;

    @MockBean
    private HmcHearingApi hmcHearingApi;

    @MockBean
    private IdamService idamService;

    private final HearingsService hearingsService = new HearingsService(hmcHearingApi, idamService);

    @Disabled
    @DisplayName("When wrapper with a valid Hearing State is given addHearingResponse should run without error")
    @ParameterizedTest
    @CsvSource(value = {
        "READY_TO_LIST,CREATE_HEARING",
        "READY_TO_LIST,UPDATE_HEARING",
        "READY_TO_LIST,UPDATED_CASE",
        "READY_TO_LIST,CANCEL_HEARING",
        "READY_TO_LIST,PARTY_NOTIFIED",
    }, nullValues = {"null"})
    void processHearingRequest(EventType event, HearingState state) {
        // TODO Finish Test when method done

        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        given(idamService.getIdamTokens().getIdamOauth2Token()).willReturn("IdamOauth2Token");
        given(idamService.getIdamTokens().getServiceAuthorization()).willReturn("ServiceAuthorization");
        HearingResponse createHearingResponse = HearingResponse.builder().hearingRequestId(HEARING_REQUEST_ID).version(VERSION).build();
        given(hmcHearingApi.createHearingRequest(anyString(),anyString(),any(HearingRequestPayload.class))).willReturn(createHearingResponse);

        SscsCaseData caseData = SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder().name(Name.builder().firstName("Test").lastName("Person").title("Mx").build()).build())
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .event(event)
                .state(state)
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();
        assertThatNoException().isThrownBy(() -> hearingsService.processHearingRequest(wrapper));
    }

    @DisplayName("When wrapper with a invalid Hearing State is given "
            + "addHearingResponse should throw an UnhandleableHearingState error")
    @ParameterizedTest
    @CsvSource(value = {
        "READY_TO_LIST,null",
    }, nullValues = {"null"})
    void processHearingRequestInvalidState(EventType event, HearingState state) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .appeal(Appeal.builder().build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .event(event)
                .state(state)
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        assertThatExceptionOfType(UnhandleableHearingState.class).isThrownBy(
                () -> hearingsService.processHearingRequest(wrapper));
    }

    @DisplayName("When wrapper with a invalid Event Type is given addHearingResponse should run without error")
    @ParameterizedTest
    @CsvSource(value = {
        "SENT_TO_DWP,UPDATE_HEARING",
        "null,CREATE_HEARING",
        "CREATE_DRAFT,UPDATE_HEARING",
        "null,null",
    }, nullValues = {"null"})
    void processHearingRequestInvalidEvent(EventType event, HearingState state) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .appeal(Appeal.builder().build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .event(event)
                .state(state)
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        assertThatNoException().isThrownBy(() -> hearingsService.processHearingRequest(wrapper));
    }
}
