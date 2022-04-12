package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.domain.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.EntityRoleCode;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.SessionCaseCodeMapping;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.buildHearingCaseDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.buildHearingDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping.buildHearingPartiesDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping.buildHearingRequestDetails;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.APPELLANT;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.APPOINTEE;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.OTHER_PARTY;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.REPRESENTATIVE;

public final class HearingsMapping {

    public static final String DWP_ID = "DWP";
    public static final String DWP_ORGANISATION_TYPE = "OGD";

    private HearingsMapping() {
    }

    public static HearingRequestPayload buildHearingPayload(HearingWrapper wrapper) {
        return HearingRequestPayload.builder()
            .requestDetails(buildHearingRequestDetails(wrapper))
            .hearingDetails(buildHearingDetails(wrapper))
            .caseDetails(buildHearingCaseDetails(wrapper))
            .partiesDetails(buildHearingPartiesDetails(wrapper))
            .build();
    }

    public static void updateIds(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        int maxId = getMaxId(caseData.getOtherParties(), appellant, appeal.getRep());

        maxId = updatePartyIds(appellant, appeal.getRep(), maxId);
        updateOtherPartiesIds(caseData.getOtherParties(), maxId);
    }

    private static void updateOtherPartiesIds(List<CcdValue<OtherParty>> otherParties, int maxId) {
        int newMaxId = maxId;
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> otherPartyCcdValue : otherParties) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                newMaxId = updatePartyIds(otherParty, otherParty.getRep(), newMaxId);
            }
        }
    }

    private static int updatePartyIds(Party party, Representative rep, int maxId) {
        int newMaxId = maxId;
        newMaxId = updateEntityId(party, newMaxId);
        if (nonNull(party.getAppointee())) {
            newMaxId = updateEntityId(party.getAppointee(), newMaxId);
        }
        if (nonNull(rep)) {
            newMaxId = updateEntityId(rep, newMaxId);
        }
        return newMaxId;
    }

    public static int updateEntityId(Entity entity, int maxId) {
        String id = entity.getId();
        int newMaxId = maxId;
        if (isBlank(id)) {
            entity.setId(String.valueOf(++newMaxId));
        }
        return newMaxId;
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

    public static void buildRelatedParties(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        List<String> allPartiesIds = getAllPartiesIds(caseData.getOtherParties(), appellant);
        allPartiesIds.add("DWP");
        // TODO SSCS-10378 - Add joint party ID

        if (nonNull(caseData.getOtherParties())) {
            for (CcdValue<OtherParty> otherPartyCcdValue : caseData.getOtherParties()) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                buildRelatedPartiesParty(otherParty, allPartiesIds, otherParty.getRep());
            }
        }

        buildRelatedPartiesParty(appellant, allPartiesIds, appeal.getRep());
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

    public static void buildRelatedPartiesParty(Party party, List<String> allPartiesIds, Representative rep) {
        updateEntityRelatedParties(party, allPartiesIds);
        if (isYes(party.getIsAppointee()) && nonNull(party.getAppointee())) {
            updateEntityRelatedParties(party.getAppointee(), List.of(party.getId()));
        }
        if (nonNull(rep) && isYes(rep.getHasRepresentative())) {
            updateEntityRelatedParties(rep, List.of(party.getId()));
        }
    }

    public static void updateEntityRelatedParties(Entity entity, List<String> ids) {
        List<RelatedParty> relatedParties = new ArrayList<>();
        // TODO Depends on SSCS-10273 - EntityRoleCode -> parentRole - Mapping to be confirmed by Andrew

        String partyRole = getEntityRoleCode(entity).getParentRole();

        for (String id : ids) {
            relatedParties.add(RelatedParty.builder()
                    .relatedPartyId(id)
                    .relationshipType(partyRole)
                    .build());
        }

        entity.setRelatedParties(relatedParties);
    }

    public static SessionCaseCodeMapping getSessionCaseCode(SscsCaseData caseData) {
        //  TODO SSCS-10116 - replace return with:
        //             return SessionLookupService.getSessionMappingsByCcdKey(caseData.getBenefitCode(), caseData.getIssueCode());
        return SessionCaseCodeMapping.builder()
                .benefitCode(1)
                .issueCode("DD")
                .ccdKey("001DD")
                .benefitDescription("UNIVERSAL CREDIT")
                .issueDescription("APPEAL DIRECTLY LODGED")
                .sessionCat(1)
                .otherSessionCat(null)
                .durationFaceToFace(20)
                .durationPaper(10)
                .panelMembers(List.of("Judge"))
                .comment(null)
                .build();
    }

    public static EntityRoleCode getEntityRoleCode(Entity entity) {
        // TODO SSCS-10273 - replace with common object
        // TODO Future work - handle interpreter
        if (entity instanceof Appellant) {
            return APPELLANT;
        }
        if (entity instanceof Appointee) {
            return APPOINTEE;
        }
        if (entity instanceof Representative) {
            return REPRESENTATIVE;
        }
        return OTHER_PARTY;
    }

}
