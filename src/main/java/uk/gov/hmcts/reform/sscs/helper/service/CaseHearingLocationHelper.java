package uk.gov.hmcts.reform.sscs.helper.service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Venue;
import uk.gov.hmcts.reform.sscs.exception.InvalidHearingDataException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.VenueService;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

import static java.util.Objects.isNull;

@Slf4j
public final class CaseHearingLocationHelper {

    private CaseHearingLocationHelper() {

    }

    public static Hearing getHearingFromCaseData(HmcMessage hmcMessage, @Valid SscsCaseData caseData)
            throws InvalidHearingDataException {
        return caseData.getHearings().stream()
                .filter(hearing -> hearing.getValue().getHearingId().equals(hmcMessage.getHearingId()))
                .findFirst()
                .orElseThrow(() -> new InvalidHearingDataException(
                                String.format("Unable to find hearing in case data with Id %s for case %s",
                                hmcMessage.getHearingId(),
                                caseData.getCcdCaseId())));
    }

    public static void updateHearing(SscsCaseData sscsCaseData, Hearing oldHearing, Hearing updatedHearing) {
        List<Hearing> hearings = new ArrayList<>(sscsCaseData.getHearings());

        hearings.remove(oldHearing);
        hearings.add(updatedHearing);

        sscsCaseData.setHearings(hearings);
    }

    public static Venue findVenue(String epimsId, VenueService venueService)
            throws InvalidMappingException {

        VenueDetails venueDetails = venueService.getVenueDetailsForActiveVenueByEpimsId(epimsId);

        if (isNull(venueDetails)) {
            throw new InvalidMappingException(String.format("Invalid epims Id %s, unable to find active venue with that id", epimsId));
        }

        return mapVenueDetailsToVenue(venueDetails);
    }

    public static Venue mapVenueDetailsToVenue(VenueDetails venueDetails) {
        return Venue.builder()
                .address(Address.builder()
                        .line1(venueDetails.getVenAddressLine1())
                        .line2(venueDetails.getVenAddressLine2())
                        .town(venueDetails.getVenAddressTown())
                        .county(venueDetails.getVenAddressCounty())
                        .postcode(venueDetails.getVenAddressPostcode())
                        .postcodeLookup(venueDetails.getVenAddressPostcode())
                        .postcodeAddress(venueDetails.getVenAddressPostcode())
                        .build())
                .googleMapLink(venueDetails.getUrl())
                .name(venueDetails.getVenName())
                .build();
    }
}
