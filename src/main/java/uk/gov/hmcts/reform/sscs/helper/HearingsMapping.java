package uk.gov.hmcts.reform.sscs.helper;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.domain.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload.HearingRequestPayloadBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.HearingsCaseMapping.buildHearingCaseDetails;
import static uk.gov.hmcts.reform.sscs.helper.HearingsDetailsMapping.buildHearingDetails;
import static uk.gov.hmcts.reform.sscs.helper.HearingsPartiesMapping.buildHearingPartiesDetails;
import static uk.gov.hmcts.reform.sscs.helper.HearingsRequestMapping.buildHearingRequestDetails;

@SuppressWarnings({"PMD.LinguisticNaming","PMD.UnnecessaryLocalBeforeReturn"})
// TODO Unsuppress in future
public final class HearingsMapping {

    public static final String OTHER_PARTY = "OtherParty";
    public static final String REPRESENTATIVE = "Representative";
    public static final String APPOINTEE = "Appointee";
    public static final String APPELLANT = "Appellant";

    private HearingsMapping() {

    }

    public static HearingRequestPayload buildHearingPayload(HearingWrapper wrapper) {
        HearingRequestPayloadBuilder requestPayloadBuilder = HearingRequestPayload.builder();

        requestPayloadBuilder.requestDetails(buildHearingRequestDetails(wrapper));
        requestPayloadBuilder.hearingDetails(buildHearingDetails(wrapper));
        requestPayloadBuilder.caseDetails(buildHearingCaseDetails(wrapper));
        requestPayloadBuilder.partiesDetails(buildHearingPartiesDetails(wrapper));

        return requestPayloadBuilder.build();
    }

    public static void updateIds(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();
        int maxId = getMaxId(caseData.getOtherParties(), appellant);
        maxId = updateEntityId(appellant, maxId);
        if (nonNull(appellant.getAppointee())) {
            maxId = updateEntityId(appellant.getAppointee(), maxId);
        }
        if (nonNull(appeal.getRep())) {
            maxId = updateEntityId(appellant.getRep(), maxId);
        }
        if (nonNull(caseData.getOtherParties())) {
            for (CcdValue<OtherParty> otherPartyCcdValue : caseData.getOtherParties()) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                maxId = updateEntityId(otherParty, maxId);
                if (nonNull(otherParty.getAppointee())) {
                    maxId = updateEntityId(otherParty.getAppointee(), maxId);
                }
                if (nonNull(otherParty.getRep())) {
                    maxId = updateEntityId(otherParty.getRep(), maxId);
                }
            }
        }
    }

    public static int updateEntityId(Entity entity, int maxId) {
        String id = entity.getId();
        if (isNotBlank(id)) {
            id = String.valueOf(++maxId);
        }
        entity.setId(id);
        return maxId;
    }

    public static int getMaxId(List<CcdValue<OtherParty>> otherParties, Party appellant) {
        List<Integer> currentIds = new ArrayList<>();
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                currentIds.addAll(getMaxId(ccdOtherParty.getValue()));
            }
        }

        currentIds.addAll(getMaxId(appellant));

        return currentIds.stream().max(Comparator.naturalOrder()).orElse(0);
    }

    public static List<Integer> getMaxId(Party party) {
        List<Integer> currentIds = new ArrayList<>();

        if (party.getId() != null) {
            currentIds.add(Integer.parseInt(party.getId()));
        }
        if (party.getAppointee() != null && party.getAppointee().getId() != null) {
            currentIds.add(Integer.parseInt(party.getAppointee().getId()));
        }
        if (party.getRep() != null && party.getRep().getId() != null) {
            currentIds.add(Integer.parseInt(party.getRep().getId()));
        }

        return currentIds;
    }

    public static void buildRelatedParties(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();
        List<String> allPartiesIds = new ArrayList<>();

        allPartiesIds.add(appellant.getId());

        if (nonNull(caseData.getOtherParties())) {
            allPartiesIds.addAll(caseData.getOtherParties().stream()
                    .map(o -> o.getValue().getId())
                    .collect(Collectors.toList()));
            for (CcdValue<OtherParty> otherPartyCcdValue: caseData.getOtherParties()) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                updateEntityRelatedParties(otherParty, OTHER_PARTY, allPartiesIds);
                if (otherParty.hasAppointee() && nonNull(otherParty.getAppointee())) {
                    updateEntityRelatedParties(otherParty.getAppointee(), APPOINTEE, List.of(otherParty.getId()));
                }
                if (otherParty.hasRepresentative() && nonNull(otherParty.getRep())) {
                    updateEntityRelatedParties(otherParty.getRep(), REPRESENTATIVE, List.of(otherParty.getId()));
                }
            }
        }

        updateEntityRelatedParties(appellant, APPELLANT, allPartiesIds);
        if (isYes(appellant.getIsAppointee()) && nonNull(appellant.getAppointee())) {
            updateEntityRelatedParties(appellant.getAppointee(), APPOINTEE, List.of(appellant.getId()));
        }
        Representative rep = appeal.getRep();
        if (nonNull(rep) && isYes(rep.getHasRepresentative())) {
            updateEntityRelatedParties(appellant.getRep(), REPRESENTATIVE, List.of(appellant.getId()));
        }
    }

    public static void updateEntityRelatedParties(Entity entity, String partyRole,  List<String> ids) {
        List<RelatedParty> relatedParties = new ArrayList<>();
        // TODO ref data that hasn't been published yet

        for (String id : ids) {
            relatedParties.add(RelatedParty.builder()
                    .relatedPartyId(id)
                    .relationshipType(partyRole)
                    .build());
        }

        entity.setRelatedParties(relatedParties);
    }
}
