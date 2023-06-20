package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingVenue;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingVenue.SOMEWHERE_ELSE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.LocationType.COURT;

@Slf4j
public final class HearingsLocationMapping {

    private HearingsLocationMapping() {
    }

    public static List<HearingLocation> getHearingLocations(SscsCaseData caseData, ReferenceDataServiceHolder refData)
        throws ListingException {

        String caseId = caseData.getCcdCaseId();
        boolean adjournmentInProgress = refData.isAdjournmentFlagEnabled() && isYes(caseData.getAdjournment().getAdjournmentInProgress());

        List<HearingLocation> locations;
        log.debug("Adjournment flag: {}, Adjournment in progress: {}", refData.isAdjournmentFlagEnabled(), isYes(caseData.getAdjournment().getAdjournmentInProgress()));
        // if adjournment in progress, we want to process these locations first, superseding the override fields
        if (adjournmentInProgress) {
            locations = getAdjournedLocations(caseData, refData);
            if (isNotEmpty(locations)) {
                log.debug("Hearing Locations for Case ID {} set as Adjournment values", caseId);
                return locations;
            }
        }


        log.info("getHearingLocations::getOverrideLocations");

        locations = getOverrideLocations(caseData);
        if (isNotEmpty(locations)) {
            log.debug("Hearing Locations for Case ID {} set as Override field values", caseId);
            return locations;
        }

        log.info("getHearingLocations::getPaperCaseLocations");

        locations = getPaperCaseLocations(caseData, refData);
        if (isNotEmpty(locations)) {
            log.debug("Hearing Locations for Case ID {} set as Paper Case values", caseId);
            return locations;
        }

        log.debug("Hearing Locations for Case ID {} set as multiple locations values", caseId);
        return getMultipleLocations(caseData, refData);
    }

    private static List<HearingLocation> getOverrideLocations(SscsCaseData caseData) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (isNotEmpty(overrideFields.getHearingVenueEpimsIds())) {
            return overrideFields.getHearingVenueEpimsIds().stream()
                .map(CcdValue::getValue)
                .map(CcdValue::getValue)
                .map(epimsId -> HearingLocation.builder()
                    .locationId(epimsId)
                    .locationType(COURT)
                    .build())
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private static List<HearingLocation> getPaperCaseLocations(SscsCaseData caseData, ReferenceDataServiceHolder refData) {
        if (HearingsChannelMapping.isPaperCase(caseData)) {
            List<VenueDetails> venueDetailsList = refData
                .getVenueService()
                .getActiveRegionalEpimsIdsForRpc(caseData.getRegionalProcessingCenter().getEpimsId());

            log.info("Found {} venues under RPC {} for paper case {}", venueDetailsList.size(),
                     caseData.getRegionalProcessingCenter().getName(), caseData.getCcdCaseId());

            return venueDetailsList.stream()
                .map(VenueDetails::getEpimsId)
                .map(id -> HearingLocation.builder()
                    .locationId(id)
                    .locationType(COURT)
                    .build())
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private static List<HearingLocation> getAdjournedLocations(SscsCaseData caseData, ReferenceDataServiceHolder refData)
        throws ListingException {

        log.info("getAdjournedLocations {}", caseData.getAdjournment());

        AdjournCaseNextHearingVenue nextHearingVenueName = caseData.getAdjournment().getNextHearingVenue();

        log.info("getAdjournedLocations {}", nextHearingVenueName);

        if (nonNull(nextHearingVenueName)) {
            return getNextHearingLocation(caseData, refData.getVenueService(), nextHearingVenueName);
        }

        return Collections.emptyList();
    }

    private static List<HearingLocation> getNextHearingLocation(SscsCaseData caseData,
                                                                VenueService venueService,
                                                                AdjournCaseNextHearingVenue nextHearingVenueName)
        throws ListingException {

        String epimsID = getEpimsID(caseData, venueService, nextHearingVenueName);
        VenueDetails venueDetails = venueService.getVenueDetailsForActiveVenueByEpimsId(epimsID);

        if (nonNull(venueDetails)) {
            log.info("Getting hearing location {} with the epims ID of {}", venueDetails.getGapsVenName(), epimsID);

            return List.of(HearingLocation.builder()
                               .locationId(epimsID)
                               .locationType(COURT)
                               .build());
        }

        log.info("getNextHearingLocation no venues found");

        throw new ListingException("Failed to determine next hearing location due to an invalid EpimsId \""
                                       + epimsID + "\" on the latest hearing");
    }

    private static List<HearingLocation> getMultipleLocations(SscsCaseData caseData, ReferenceDataServiceHolder refData) {
        String epimsId = refData
            .getVenueService()
            .getEpimsIdForVenue(caseData.getProcessingVenue());

        Map<String, List<String>> multipleHearingLocations = refData.getMultipleHearingLocations();

        return multipleHearingLocations.values().stream()
            .filter(listValues ->  listValues.contains(epimsId))
            .findFirst()
            .orElseGet(() -> Collections.singletonList(epimsId))
            .stream().map(epims -> HearingLocation.builder().locationId(epims).locationType(COURT).build())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String getEpimsID(SscsCaseData caseData,
                                     VenueService venueService,
                                     AdjournCaseNextHearingVenue nextHearingVenue)
        throws InvalidMappingException {

        if (SOMEWHERE_ELSE.equals(nextHearingVenue)) {
            String venueId = caseData.getAdjournment().getNextHearingVenueSelected().getValue().getCode();

            return venueService.getEpimsIdForVenueId(venueId);
        }

        Hearing latestHearing = caseData.getLatestHearing();

        if (nonNull(latestHearing)) {
            return latestHearing.getValue().getEpimsId();
        }

        log.info("getEpimsID no hearing location found, throwing exception");

        throw new InvalidMappingException("Failed to determine next hearing location due to no latest hearing on case "
                                              + caseData.getCcdCaseId());
    }
}
