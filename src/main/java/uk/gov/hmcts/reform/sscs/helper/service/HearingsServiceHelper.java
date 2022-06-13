package uk.gov.hmcts.reform.sscs.helper.service;

import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;

import java.util.ArrayList;
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
        Hearing hearing = wrapper.getCaseData().getLatestHearing();
        if (nonNull(hearing)) {
            return hearing.getValue().getHearingId();
        }
        return null;
    }

    public static Long getVersion(HearingWrapper wrapper) {
        Hearing hearing = wrapper.getCaseData().getLatestHearing();
        if (nonNull(hearing)) {
            Long version = hearing.getValue().getVersionNumber();
            if (nonNull(version) && version > 0) {
                return version;
            }
        }
        return null;
    }

    public static Hearing createHearing(Long hearingId, @Valid SscsCaseData caseData) {
        Hearing hearing = Hearing.builder()
            .value(HearingDetails.builder()
                .hearingId(String.valueOf(hearingId))
                .build())
            .build();
        caseData.getHearings().add(hearing);
        return hearing;
    }

    @Nullable
    public static Hearing getHearingById(Long hearingId, @Valid SscsCaseData caseData) {
        if (isNull(caseData.getHearings())) {
            caseData.setHearings(new ArrayList<>());
        }

        return caseData.getHearings().stream()
            .filter(hearing -> hearing.getValue().getHearingId().equals(String.valueOf(hearingId)))
            .findFirst()
            .orElse(null);
    }
}
