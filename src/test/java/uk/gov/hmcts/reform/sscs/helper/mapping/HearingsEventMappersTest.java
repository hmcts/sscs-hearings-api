package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.PostponementRequest;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.DORMANT;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.READY_TO_LIST;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_NOT_LISTABLE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.ProcessRequestAction.GRANT;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;

class HearingsEventMappersTest {


    private HearingGetResponse response;

    private SscsCaseData caseData;

    @BeforeEach
    void setup() {
        response = HearingGetResponse.builder()
            .requestDetails(RequestDetails.builder().build())
            .hearingResponse(HearingResponse.builder().build())
            .hearingDetails(HearingDetails.builder().build())
            .caseDetails(CaseDetails.builder().build())
            .partyDetails(Collections.emptyList())
            .hearingResponse(HearingResponse.builder().build())
            .build();

        caseData = SscsCaseData.builder()
            .postponementRequest(PostponementRequest.builder()
                .unprocessedPostponementRequest(NO)
                .actionPostponementRequestSelected(GRANT.getValue())
                .listingOption(State.READY_TO_LIST.getId())
                .build())
            .build();
    }


    @DisplayName("When the cancellation reason is valid, the EventType should be null")
    @ParameterizedTest
    @EnumSource(
        value = CancellationReason.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"WITHDRAWN", "STRUCK_OUT", "LAPSED"})
    @NullSource
    void testCancelledHandler(CancellationReason value) {
        response.getHearingResponse().setHearingCancellationReason(value);

        caseData.setPostponementRequest(PostponementRequest.builder().build());

        EventType result = HearingsEventMappers.cancelledHandler(response, caseData);

        assertThat(result).isNull();
    }


    @DisplayName("When the cancellation reason is valid for Dormant, the EventType should be Dormant")
    @ParameterizedTest
    @EnumSource(
        value = CancellationReason.class,
        mode = EnumSource.Mode.INCLUDE,
        names = {"WITHDRAWN", "STRUCK_OUT", "LAPSED"})
    void testCancelledHandlerDormant(CancellationReason value) {
        response.getHearingResponse().setHearingCancellationReason(value);

        EventType result = HearingsEventMappers.cancelledHandler(response, caseData);

        assertThat(result).isEqualTo(DORMANT);
    }

    @DisplayName("When the case is postponed and listing option Ready to List, cancelledHandler returns the EventType"
        + " should be Ready to List")
    @Test
    void testCancelledHandlerRelisted() {
        caseData.getPostponementRequest().setListingOption(State.READY_TO_LIST.getId());

        EventType result = HearingsEventMappers.cancelledHandler(response, caseData);

        assertThat(result).isEqualTo(READY_TO_LIST);
    }

    @DisplayName("When the case is postponed and listing option Not Listable, cancelledHandler returns the EventType "
        + "should be Not Listable")
    @Test
    void testCancelledHandlerNotListable() {
        caseData.getPostponementRequest().setListingOption(State.NOT_LISTABLE.getId());

        EventType result = HearingsEventMappers.cancelledHandler(response, caseData);

        assertThat(result).isEqualTo(UPDATE_NOT_LISTABLE);
    }
}
