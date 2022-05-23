package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetailsService;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class CcdLocationUpdateService {

    public static final String TEMPLATE_UPDATE_VENUE_ERROR = "Failed to update venue for Case Id %s:%n";

    private final VenueRpcDetailsService venueRpcDetailsService;

    public void updateVenue(HmcMessage hmcMessage, @Valid SscsCaseData sscsCaseData) throws UpdateCaseException {

        Hearing oldHearing = getHearingFromCaseData(hmcMessage, sscsCaseData);
        if (isNull(oldHearing)) {
            UpdateCaseException exc = new UpdateCaseException(
                String.format(TEMPLATE_UPDATE_VENUE_ERROR + "Could not find hearing with Hearing Id: %s",
                        sscsCaseData.getCcdCaseId(), hmcMessage.getHearingID()));
            log.error(exc.getMessage(), exc);
            throw exc;
        }

        String updatedVenueId = hmcMessage.getHearingUpdate().getHearingVenueID();
        Venue venue = findVenue(updatedVenueId);
        if (isNull(venue)) {

            UpdateCaseException exc = new UpdateCaseException(
                String.format(TEMPLATE_UPDATE_VENUE_ERROR + "Could not find venue with Epims Id %s ",
                        sscsCaseData.getCcdCaseId(), updatedVenueId));
            log.error(exc.getMessage(), exc);

            throw exc;
        }

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

        log.info("(Case Id: {}) Venue has been updated from epimsId {} to {} for hearingId {}",
                 sscsCaseData.getCcdCaseId(), hearingDetails.getHearingId(),
                 updatedVenueId, hmcMessage.getHearingID()
        );
    }

    private void updateHearing(SscsCaseData sscsCaseData, Hearing oldHearing, Hearing updatedHearing) {
        List<Hearing> hearings = new ArrayList<>(sscsCaseData.getHearings());

        hearings.remove(oldHearing);
        hearings.add(updatedHearing);

        sscsCaseData.setHearings(hearings);
    }

    public Venue findVenue(String epimsId) {

        VenueDetails venue = venueRpcDetailsService.getVenue(epimsId);

        if (isNull(venue)) {
            log.error("Could not find venue with Epims Id {}", epimsId);
            return null;
        }

        return Venue.builder()
            .address(Address.builder()
                         .line1(venue.getVenAddressLine1())
                         .line2(venue.getVenAddressLine2())
                         .postcodeAddress(venue.getVenAddressPostcode())
                         .county(venue.getVenAddressCounty())
                         .town(venue.getVenAddressTown())
                         .build())
            .name(venue.getVenName())
            .build();
    }


    Hearing getHearingFromCaseData(HmcMessage hmcMessage, @Valid SscsCaseData caseData) {
        return caseData.getHearings().stream()
                .filter(hearing -> hmcMessage.getHearingID().equals(hearing.getValue().getHearingId()))
                .findFirst()
                .orElse(null);
    }

}
