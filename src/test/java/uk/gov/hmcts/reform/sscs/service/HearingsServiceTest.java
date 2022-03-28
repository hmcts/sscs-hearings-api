package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingState;
import uk.gov.hmcts.reform.sscs.model.HearingState;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HearingsServiceTest {

    final String hearingRequestId = "12345";
    final String hmcStatus = "TestStatus";
    final Number version = 123;
    final String cancellationReasonCode = "TestCancelCode";

    final HearingsService hearingsService = new HearingsService();

    @DisplayName("When wrapper with a valid Hearing State is given addHearingResponse should run without error")
    @ParameterizedTest
    @CsvSource(value = {
        "READY_TO_LIST,CREATE_HEARING",
        "READY_TO_LIST,UPDATE_HEARING",
        "READY_TO_LIST,UPDATED_CASE",
        "READY_TO_LIST,CANCEL_HEARING",
        "READY_TO_LIST,PARTY_NOTIFIED",
        "READY_TO_LIST,PARTY_HEARING",
    }, nullValues = {"null"})
    void processHearingRequest(EventType event, HearingState state) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .ccdCaseId("12345")
                .appeal(Appeal.builder().build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .event(event)
                .state(state)
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();
        assertThatNoException().isThrownBy(() -> hearingsService.processHearingRequest(wrapper));

        if (HearingState.CREATE_HEARING.equals(wrapper.getState())) {
            assertNotNull(wrapper.getUpdatedCaseData().getLatestHmcHearing());
        }
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
                .ccdCaseId("12345")
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
                .ccdCaseId("12345")
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

    @Test
    @DisplayName("When wrapper with no HearingResponse is given "
            + "addHearingResponse should return a new valid HearingResponse")
    void addHearingResponse() {
        List<HmcHearing> hearings = new ArrayList<>();
        hearings.add(HmcHearing.builder().value(HmcHearingDetails.builder().build()).build());
        HearingWrapper wrapper = HearingWrapper.builder()
                .updatedCaseData(SscsCaseData.builder().hmcHearings(hearings).build()).build();
        hearingsService.addHearingResponse(wrapper, hearingRequestId, hmcStatus, version);
        assertNotNull(wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse());
        assertEquals(hearingRequestId,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getHearingRequestId());
        assertEquals(hmcStatus,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getHmcStatus());
        assertEquals(version,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getVersion());
    }

    @Test
    @DisplayName("When wrapper with a valid HearingResponse is given "
            + "updateHearingResponse should return updated valid HearingResponse")
    void updateHearingResponse() {
        List<HmcHearing> hearings = new ArrayList<>();
        hearings.add(HmcHearing.builder().value(HmcHearingDetails.builder()
                .hearingResponse(HearingResponse.builder()
                        .hearingRequestId(hearingRequestId)
                        .hmcStatus("Test Status Before")
                        .version(0)
                        .build())
                .build()).build());
        HearingWrapper wrapper = HearingWrapper.builder()
                .updatedCaseData(SscsCaseData.builder().hmcHearings(hearings).build()).build();
        hearingsService.updateHearingResponse(wrapper, hmcStatus, version);
        assertNotNull(wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse());
        assertEquals(hearingRequestId,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getHearingRequestId());
        assertEquals(hmcStatus,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getHmcStatus());
        assertEquals(version,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getVersion());
    }

    @Test
    @DisplayName("When wrapper with a valid HearingResponse and cancellationReasonCode is given "
            + "updateHearingResponse should return updated valid HearingResponse")
    void testUpdateHearingResponse() {
        List<HmcHearing> hearings = new ArrayList<>();
        hearings.add(HmcHearing.builder().value(HmcHearingDetails.builder()
                .hearingResponse(HearingResponse.builder()
                        .hearingRequestId(hearingRequestId)
                        .hmcStatus("Test Status Before")
                        .version(0)
                        .build())
                .build()).build());
        HearingWrapper wrapper = HearingWrapper.builder()
                .updatedCaseData(SscsCaseData.builder().hmcHearings(hearings).build()).build();
        hearingsService.updateHearingResponse(wrapper, hmcStatus, version, cancellationReasonCode);
        assertNotNull(wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse());
        assertEquals(hearingRequestId,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getHearingRequestId());
        assertEquals(hmcStatus,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getHmcStatus());
        assertEquals(version,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getVersion());
        assertEquals(cancellationReasonCode,
                wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse().getHearingCancellationReason());
    }
}
