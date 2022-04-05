package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingState;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.HearingState;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_HEARING_TYPE;
import static uk.gov.hmcts.reform.sscs.model.HearingState.CREATE_HEARING;

class HearingsServiceTest {

    public static final long HEARING_REQUEST_ID = 12345;
    public static final String HMC_STATUS = "TestStatus";
    public static final long VERSION = 1;
    public static final String CANCELLATION_REASON_CODE = "TestCancelCode";
    public static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    public static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    public static final String EPIMS_ID = "239785";
    public static final String REGION = "Region A";

    public static SscsCaseData caseData;

    @Mock
    private HmcHearingApi hmcHearingApi;

    @Mock
    private IdamService idamService;

    private HearingsService hearingsService;
    private HearingWrapper wrapper;

    @BeforeEach
    void setup() {
        openMocks(this);

        caseData = SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder()
                                .name(Name.builder().build())
                                .build())
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .caseManagementLocation(CaseManagementLocation.builder()
                        .baseLocation(EPIMS_ID)
                        .region(REGION)
                        .build())
                .build();

        wrapper = HearingWrapper.builder()
                .event(UPDATE_HEARING_TYPE)
                .state(CREATE_HEARING)
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().idamOauth2Token(IDAM_OAUTH2_TOKEN).serviceAuthorization(SERVICE_AUTHORIZATION).build());

        hearingsService = new HearingsService(hmcHearingApi, idamService);
    }

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
        wrapper.setEvent(event);
        wrapper.setState(state);
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
        wrapper.setEvent(event);
        wrapper.setState(state);

        assertThatExceptionOfType(UnhandleableHearingState.class).isThrownBy(
                () -> hearingsService.processHearingRequest(wrapper));
    }
}
