package uk.gov.hmcts.reform.sscs.helper.mappingutils;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GetVenueMultipleEpims {

    private GetVenueMultipleEpims(){

    }

    public static final String MANCHESTER = "Manchester";
    public static final String CHESTER = "Chester";
    public static final String PLYMOUTH = "Plymouth";

    public static List<HearingLocations> getMultipleLocationDetails(CaseManagementLocation caseManagementLocation) {
        Map<String, List<String>> epimMap = new HashMap<>();
        List<String> chesterId = new ArrayList<>(List.of("226511", "443014"));
        List<String> manchesterId = new ArrayList<>(List.of("512401", "701411"));
        List<String> plymouthId = new ArrayList<>(List.of("764728", "235590"));

        epimMap.put(MANCHESTER, manchesterId);
        epimMap.put(CHESTER, chesterId);
        epimMap.put(PLYMOUTH, plymouthId);

        List<HearingLocations> locationId = new ArrayList<>();
        String processingCenter = caseManagementLocation.getRegion();
        HearingLocations hearingLocations = new HearingLocations();
        hearingLocations.setLocationId(caseManagementLocation.getBaseLocation());
        hearingLocations.setLocationType(processingCenter);
        switch (processingCenter) {
            case "Manchester": locationId.addAll(getEpims(epimMap, MANCHESTER, hearingLocations));
                break;
            case "Chester": locationId.addAll(getEpims(epimMap, CHESTER, hearingLocations));
                break;
            case "Plymouth": locationId.addAll(getEpims(epimMap, PLYMOUTH, hearingLocations));
                break;
            default: break;
        }
        return locationId;
    }

    @NotNull
    private static List<HearingLocations> getEpims(Map<String, List<String>> epimLists, String locationName, HearingLocations hearingLocations) {
        List<List<String>> epims;
        List<HearingLocations> epimss = new ArrayList<>();

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
