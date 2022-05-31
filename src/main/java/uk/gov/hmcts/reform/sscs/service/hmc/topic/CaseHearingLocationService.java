package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.InvalidHearingDataException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.VenueService;

import javax.validation.Valid;

import static uk.gov.hmcts.reform.sscs.helper.service.CaseHearingLocationHelper.findVenue;
import static uk.gov.hmcts.reform.sscs.helper.service.CaseHearingLocationHelper.getHearingFromCaseData;
import static uk.gov.hmcts.reform.sscs.helper.service.CaseHearingLocationHelper.updateHearing;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseHearingLocationService {

    private final VenueService venueService;

    public void updateVenue(HmcMessage hmcMessage, @Valid SscsCaseData sscsCaseData)
            throws InvalidHearingDataException, InvalidMappingException {

        Hearing oldHearing = getHearingFromCaseData(hmcMessage, sscsCaseData);

        String updatedVenueId = hmcMessage.getHearingUpdate().getHearingVenueId();
        Venue venue = findVenue(updatedVenueId, venueService);

        HearingDetails hearingDetails = oldHearing.getValue();
        Hearing updatedHearing = Hearing.builder().value(
                HearingDetails.builder()
                        .venue(venue)
                        .hearingDate(hearingDetails.getHearingDate())
                        .time(hearingDetails.getTime())
                        .adjourned(hearingDetails.getAdjourned())
                        .eventDate(hearingDetails.getEventDate())
                        .hearingId(hearingDetails.getHearingId())
                        .venueId(updatedVenueId)
                        .build())
                .build();

        updateHearing(sscsCaseData, oldHearing, updatedHearing);

        log.info("Venue has been updated from epimsId {} to {} for Case Id: {} with hearingId {}",
            hearingDetails.getHearingId(),
            updatedVenueId,
            sscsCaseData.getCcdCaseId(),
            hmcMessage.getHearingId()
        );
    }
}
