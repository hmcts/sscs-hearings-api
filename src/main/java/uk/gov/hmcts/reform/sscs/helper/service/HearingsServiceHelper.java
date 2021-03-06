package uk.gov.hmcts.reform.sscs.helper.service;

import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.CaseHearing;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class HearingsServiceHelper {

    private HearingsServiceHelper() {
    }

    public static void updateHearingId(Hearing hearing, HmcUpdateResponse response) {
        if (nonNull(response.getHearingRequestId())) {
            hearing.getValue().setHearingId(String.valueOf(response.getHearingRequestId()));
        }
    }

    public static void updateVersionNumber(Hearing hearing, HmcUpdateResponse response) {
        hearing.getValue().setVersionNumber(response.getVersionNumber());
    }

    public static HearingEvent getHearingEvent(HearingState state) {
        return HearingEvent.valueOf(state.name());
    }

    public static String getHearingId(HearingWrapper wrapper) {
        return Optional.ofNullable(wrapper.getCaseData().getLatestHearing())
            .map(Hearing::getValue)
            .map(HearingDetails::getHearingId)
            .orElse(null);
    }

    public static Long getVersion(HearingWrapper wrapper) {
        return Optional.ofNullable(wrapper.getCaseData().getLatestHearing())
            .map(Hearing::getValue)
            .map(HearingDetails::getVersionNumber)
            .filter(version -> version > 0)
            .orElse(null);
    }

    public static Hearing createHearing(Long hearingId) {
        return Hearing.builder()
            .value(HearingDetails.builder()
                .hearingId(String.valueOf(hearingId))
                .build())
            .build();
    }

    public static void addHearing(Hearing hearing, @Valid SscsCaseData caseData) {
        caseData.getHearings().add(hearing);
    }

    @Nullable
    public static Hearing getHearingById(Long hearingId, @Valid SscsCaseData caseData) {
        if (isNull(caseData.getHearings())) {
            caseData.setHearings(new ArrayList<>());
        }

        return caseData.getHearings().stream()
            .filter(hearing -> doHearingIdsMatch(hearing, hearingId))
            .findFirst()
            .orElse(null);
    }

    private static boolean doHearingIdsMatch(Hearing hearing, Long hearingId) {
        return hearing.getValue().getHearingId()
            .equals(String.valueOf(hearingId));
    }

    @Nullable
    public static CaseHearing findExistingRequestedHearings(HearingsGetResponse hearingsGetResponse) {
        return Optional.ofNullable(hearingsGetResponse)
            .map(HearingsGetResponse::getCaseHearings)
            .orElse(Collections.emptyList()).stream()
            .filter(caseHearing -> isCaseHearingRequestedOrAwaitingListing(caseHearing.getHmcStatus()))
            .min(Comparator.comparing(CaseHearing::getHearingRequestDateTime))
            .orElse(null);
    }

    public static boolean isCaseHearingRequestedOrAwaitingListing(HmcStatus hmcStatus) {
        return HmcStatus.HEARING_REQUESTED == hmcStatus
            || HmcStatus.AWAITING_LISTING == hmcStatus;
    }
}
