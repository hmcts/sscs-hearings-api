package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Venue;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetails;
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetailsService;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class CcdLocationUpdateService {

    private final VenueRpcDetailsService venueRpcDetailsService;

    public CcdLocationUpdateService(VenueRpcDetailsService venueRpcDetailsService) {
        this.venueRpcDetailsService = venueRpcDetailsService;
    }

    public void updateVenue(HmcMessage hmcMessage, SscsCaseData sscsCaseData) {

        Optional<HearingDetails> hearingDetails = getHearingDetailsFromHearingList(hmcMessage, sscsCaseData);
        if (hearingDetails.isEmpty()) {
            log.error("Failed to update venue in CCD - can not find hearing with Id: {}", hmcMessage.getHearingID());
            return;
        }

        String updatedVenueId = hmcMessage.getHearingUpdate().getHearingVenueID();
        Venue venue = findVenue(updatedVenueId);
        if (isNull(venue)) {
            log.error("Failed to update location for CCD - can not find venue with Id {}", updatedVenueId);
            return;
        }

        HearingDetails existingHearingDetails = hearingDetails.get();
        HearingDetails updatedHearingDetails = HearingDetails.builder()
            .venue(venue)
            .hearingDate(existingHearingDetails.getHearingDate())
            .time(existingHearingDetails.getTime())
            .adjourned(existingHearingDetails.getAdjourned())
            .eventDate(existingHearingDetails.getEventDate())
            .hearingId(existingHearingDetails.getHearingId())
            .venueId(updatedVenueId)
            .build();

        // remove hearing with outdated venueId
        List<Hearing> updatedHearingList = sscsCaseData.getHearings().stream()
            .filter(obj -> !obj.getValue().getHearingId().equals(hmcMessage.getHearingID()))
            .collect(toList());

        // add hearing with updated venueId
        updatedHearingList.add(Hearing.builder().value(updatedHearingDetails).build());

        sscsCaseData.setHearings(updatedHearingList);
    }

    public Venue findVenue(String venueId) {

        Optional<VenueRpcDetails> venue = venueRpcDetailsService.getVenue(venueId);

        if (venue.isEmpty()) {
            return null;
        }

        VenueDetails venueDetails = venue.get().getVenueDetails();
        return Venue.builder()
            .address(Address.builder()
                         .line1(venueDetails.getVenAddressLine1())
                         .line2(venueDetails.getVenAddressLine2())
                         .postcodeAddress(venueDetails.getVenAddressPostcode())
                         .county(venueDetails.getVenAddressCounty())
                         .town(venueDetails.getVenAddressTown())
                         .build())
            .name(venueDetails.getVenName())
            .build();
    }


    private Optional<HearingDetails> getHearingDetailsFromHearingList(HmcMessage hmcMessage, SscsCaseData caseData) {
        return caseData.getHearings().stream()
            .map(Hearing::getValue)
            .filter(h -> hmcMessage.getHearingID().equals(h.getHearingId()))
            .findFirst();
    }

}
