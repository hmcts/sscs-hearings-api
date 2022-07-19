package uk.gov.hmcts.reform.sscs.helper.mappingutils;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.LocationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class GetVenueMultipleEpims {

    public static final String MANCHESTER = "Manchester";
    public static final String CHESTER = "Chester";
    public static final String PLYMOUTH = "Plymouth";

    private GetVenueMultipleEpims() {
    }

    public static List<HearingLocation> getMultipleLocationDetails(VenueDetails venueDetailId, SscsCaseData caseData) {
        ConcurrentHashMap<String,List<String>> epimMap = new ConcurrentHashMap<>();
        List<String> chesterId = List.of("226511", "443014");
        List<String> manchesterId = List.of("512401", "701411");
        List<String> plymouthId = List.of("764728", "235590");

        epimMap.put(MANCHESTER, manchesterId);
        epimMap.put(CHESTER, chesterId);
        epimMap.put(PLYMOUTH, plymouthId);

        List<HearingLocation> locationId = new ArrayList<>();
        String processingCenter = caseData.getProcessingVenue();
        HearingLocation hearingLocations = new HearingLocation();
        hearingLocations.setLocationId(venueDetailId.getEpimsId());
        hearingLocations.setLocationType(LocationType.REGION);
        switch (processingCenter) {
            case MANCHESTER: locationId.addAll(getEpims(epimMap, MANCHESTER, hearingLocations));
                break;
            case CHESTER: locationId.addAll(getEpims(epimMap, CHESTER, hearingLocations));
                break;
            case PLYMOUTH: locationId.addAll(getEpims(epimMap, PLYMOUTH, hearingLocations));
                break;
            default: break;
        }
        return locationId;
    }

    @NotNull
    private static List<HearingLocation> getEpims(Map<String, List<String>> epimLists, String locationName, HearingLocation hearingLocations) {
        List<List<String>> epims;
        List<HearingLocation> epimss = new ArrayList<>();

        epims = epimLists
            .entrySet()
            .stream()
            .filter(e -> Objects.equals(e.getKey(), locationName))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

        epims.forEach(epimId -> {
            if (epimId.contains(hearingLocations.getLocationId())) {
                hearingLocations.setMultipleLocationId(epimId);
                epimss.add(hearingLocations);
            }
        });
        return epimss;
    }

}
