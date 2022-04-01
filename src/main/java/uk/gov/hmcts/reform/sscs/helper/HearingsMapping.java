package uk.gov.hmcts.reform.sscs.helper;

<<<<<<< HEAD
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.domain.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload.HearingRequestPayloadBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.HearingsCaseMapping.buildHearingCaseDetails;
import static uk.gov.hmcts.reform.sscs.helper.HearingsDetailsMapping.buildHearingDetails;
import static uk.gov.hmcts.reform.sscs.helper.HearingsPartiesMapping.buildHearingPartiesDetails;
import static uk.gov.hmcts.reform.sscs.helper.HearingsRequestMapping.buildHearingRequestDetails;

public final class HearingsMapping {

    public static final String OTHER_PARTY = "OtherParty";
    public static final String REPRESENTATIVE = "Representative";
    public static final String APPOINTEE = "Appointee";
    public static final String APPELLANT = "Appellant";
=======
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.service.SessionLookupService;

import static java.util.Objects.nonNull;

@SuppressWarnings({"PMD.LinguisticNaming","PMD.UnnecessaryLocalBeforeReturn"})
// TODO Unsuppress in future
public final class HearingsMapping {

    private static SessionLookupService sessionLookupService;
>>>>>>> e8e04f0 (SSCS-10116)

    private HearingsMapping() {

    }

<<<<<<< HEAD
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
        int maxId = getMaxId(caseData.getOtherParties(), appellant, appeal.getRep());
        maxId = updateEntityId(appellant, maxId);
        if (nonNull(appellant.getAppointee())) {
            maxId = updateEntityId(appellant.getAppointee(), maxId);
        }
        if (nonNull(appeal.getRep())) {
            maxId = updateEntityId(appeal.getRep(), maxId);
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

    public static int getMaxId(List<CcdValue<OtherParty>> otherParties, Appellant appellant, Representative rep) {
        return getAllIds(otherParties, appellant, rep).stream().max(Comparator.naturalOrder()).orElse(0);
    }

    public static List<Integer> getAllIds(List<CcdValue<OtherParty>> otherParties, Appellant appellant, Representative rep) {
        List<Integer> currentIds = new ArrayList<>();
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                currentIds.addAll(getAllPartyIds(ccdOtherParty.getValue(), ccdOtherParty.getValue().getRep()));
            }
        }

        currentIds.addAll(getAllPartyIds(appellant, rep));

        return currentIds;
    }

    public static List<Integer> getAllPartyIds(Party party, Representative rep) {
        List<Integer> currentIds = new ArrayList<>();

        if (party.getId() != null) {
            currentIds.add(Integer.parseInt(party.getId()));
        }
        if (party.getAppointee() != null && party.getAppointee().getId() != null) {
            currentIds.add(Integer.parseInt(party.getAppointee().getId()));
        }
        if (rep != null && rep.getId() != null) {
            currentIds.add(Integer.parseInt(rep.getId()));
        }

        return currentIds;
    }

    public static int updateEntityId(Entity entity, int maxId) {
        String id = entity.getId();
        int newMaxId = maxId;
        if (isBlank(id)) {
            id = String.valueOf(++newMaxId);
        }
        entity.setId(id);
        return newMaxId;
    }

    public static void buildRelatedParties(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        List<String> allPartiesIds = getAllPartiesIds(caseData.getOtherParties(), appellant);

        if (nonNull(caseData.getOtherParties())) {
            for (CcdValue<OtherParty> otherPartyCcdValue: caseData.getOtherParties()) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                buildRelatedPartiesParty(otherParty, allPartiesIds, otherParty.hasAppointee(), otherParty.hasRepresentative(), otherParty.getRep());
            }
        }

        buildRelatedPartiesParty(appellant, allPartiesIds, isYes(appellant.getIsAppointee()), nonNull(appeal.getRep()) && isYes(appeal.getRep().getHasRepresentative()), appeal.getRep());
    }

    public static List<String> getAllPartiesIds(List<CcdValue<OtherParty>> otherParties, Appellant appellant) {
        List<String> currentIds = new ArrayList<>();
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                currentIds.add(ccdOtherParty.getValue().getId());
            }
        }

        currentIds.add(appellant.getId());

        return currentIds;
    }

    public static void buildRelatedPartiesParty(Party party, List<String> allPartiesIds, boolean hasAppointee, boolean hasRepresentative, Representative rep) {
        updateEntityRelatedParties(party, allPartiesIds);
        if (hasAppointee && nonNull(party.getAppointee())) {
            updateEntityRelatedParties(party.getAppointee(), List.of(party.getId()));
        }
        if (hasRepresentative && nonNull(rep)) {
            updateEntityRelatedParties(rep, List.of(party.getId()));
        }
    }

    public static void updateEntityRelatedParties(Entity entity, List<String> ids) {
        List<RelatedParty> relatedParties = new ArrayList<>();
        // TODO ref data that hasn't been published yet

        String partyRole = "Unknown";
        if (entity instanceof Appellant) {
            partyRole = APPELLANT;
        } else if (entity instanceof Appointee) {
            partyRole = APPOINTEE;
        } else if (entity instanceof Representative) {
            partyRole = REPRESENTATIVE;
        } else if (entity instanceof OtherParty) {
            partyRole = OTHER_PARTY;
        }

        for (String id : ids) {
            relatedParties.add(RelatedParty.builder()
                    .relatedPartyId(id)
                    .relationshipType(partyRole)
                    .build());
        }

        entity.setRelatedParties(relatedParties);
    }
}
