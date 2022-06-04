package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Venue;
import uk.gov.hmcts.reform.sscs.exception.InvalidHearingDataException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.service.VenueService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.service.CaseHearingLocationHelper.mapVenueDetailsToVenue;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingUpdateService {

    public static final int EXPECTED_SESSIONS = 1;
    private final VenueService venueService;

    public void updateHearing(HearingGetResponse hearingGetResponse, @Valid SscsCaseData sscsCaseData)
            throws MessageProcessingException, InvalidMappingException {

        HearingResponse hearingResponse = hearingGetResponse.getHearingResponse();
        String hearingId = String.valueOf(hearingResponse.getHearingRequestId());

        List<HearingDaySchedule> hearingSessions = hearingResponse.getHearingSessions();

        if (hearingSessions.size() != EXPECTED_SESSIONS) {
            throw new InvalidHearingDataException(
                    String.format("Invalid HearingDaySchedule, should have 1 session but instead has %d sessions, for Case Id %s and Hearing Id %s",
                            hearingSessions.size(),
                            sscsCaseData.getCcdCaseId(),
                            hearingId));
        }

        HearingDaySchedule hearingDaySchedule = hearingSessions.get(0);

        String hearingEpimsId = hearingDaySchedule.getHearingVenueEpimsId();

        VenueDetails venueDetails = venueService.getVenueDetailsForActiveVenueByEpimsId(hearingEpimsId);

        if (isNull(venueDetails)) {
            throw new InvalidMappingException(String.format("Invalid epims Id %s, unable to find active venue with that id, regarding Case Id %s",
                    hearingEpimsId,
                    sscsCaseData.getCcdCaseId()));
        }

        Venue venue = mapVenueDetailsToVenue(venueDetails);

        Hearing hearing = findOrCreateHearingInCaseData(hearingId, sscsCaseData);

        HearingDetails hearingDetails = hearing.getValue();
        hearingDetails.setHearingId(hearingId);
        hearingDetails.setVenueId(hearingEpimsId);
        hearingDetails.setVenue(venue);

        // TODO SSCS-10620 - Set StartDateTime and EndDateTime

        log.info("Venue has been updated from epimsId {} to {} for Case Id: {} with hearingId {}",
            hearingDetails.getHearingId(),
            hearingEpimsId,
            sscsCaseData.getCcdCaseId(),
            hearingId);
    }

    public Hearing findOrCreateHearingInCaseData(String hearingId, @Valid SscsCaseData caseData) {
        List<Hearing> hearings = new ArrayList<>(Optional
                .ofNullable(caseData.getHearings())
                .orElse(Collections.emptyList()));

        Hearing targetHearing = hearings.stream()
                .filter(hearing -> hearing.getValue().getHearingId().equals(hearingId))
                .findFirst()
                .orElse(null);

        if (isNull(targetHearing)) {
            targetHearing = Hearing.builder()
                    .value(HearingDetails.builder().build())
                    .build();
            hearings.add(targetHearing);
        }

        caseData.setHearings(hearings);

        return targetHearing;
    }
}
