package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.EntityRoleCode;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.SessionCaseCodeMapping;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.buildHearingCaseDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.buildHearingDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping.buildHearingPartiesDetails;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping.buildHearingRequestDetails;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.APPELLANT;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.APPOINTEE;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.JOINT_PARTY;
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
        Appointee appointee = appellant.getAppointee();

        int maxId = getMaxId(caseData.getOtherParties(), appellant, appeal.getRep(), appointee);

        maxId = updateNonAppointeeEntityIds(appellant, appeal.getRep(), maxId);
        maxId = updateEntityId(appointee, maxId);
        updateOtherPartiesIds(caseData.getOtherParties(), maxId);
    }

    private static void updateOtherPartiesIds(List<CcdValue<OtherParty>> otherParties, int maxId) {
        int newMaxId = maxId;
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> otherPartyCcdValue : otherParties) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                newMaxId = updateNonAppointeeEntityIds(otherParty, otherParty.getRep(), newMaxId);
                if (nonNull(otherParty.getAppointee())) {
                    newMaxId = updateEntityId(otherParty.getAppointee(), newMaxId);
                }
            }
        }
    }


    private static int updateNonAppointeeEntityIds(Entity entity, Representative rep, int maxId) {
        int newMaxId = maxId;
        newMaxId = updateEntityId(entity, newMaxId);
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

    public static int getMaxId(List<CcdValue<OtherParty>> otherParties, Appellant appellant, Representative rep,
                               Appointee appointee) {
        return getAllIds(otherParties, appellant, rep, appointee).stream().max(Comparator.naturalOrder()).orElse(0);
    }

    public static List<Integer> getAllIds(List<CcdValue<OtherParty>> otherParties, Appellant appellant, Representative rep,
                                          Appointee appointee) {
        List<Integer> currentIds = new ArrayList<>();
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                currentIds.addAll(getAppellantAndRepresentativeIds(otherParty, otherParty.getRep()));
                if (nonNull(otherParty.getAppointee())) {
                    currentIds.add(Integer.parseInt(otherParty.getId()));
                }
            }
        }

        currentIds.addAll(getAppellantAndRepresentativeIds(appellant, rep));
        if (nonNull(appointee) && appointee.getId() != null) {
            currentIds.add(Integer.parseInt(appointee.getId()));
        }
        return currentIds;
    }

    public static List<Integer> getAppellantAndRepresentativeIds(Entity entity, Representative rep) {
        List<Integer> currentIds = new ArrayList<>();

        if (entity.getId() != null) {
            currentIds.add(Integer.parseInt(entity.getId()));
        }

        if (rep != null && rep.getId() != null) {
            currentIds.add(Integer.parseInt(rep.getId()));
        }

        return currentIds;
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
        if (entity instanceof JointParty) {
            return JOINT_PARTY;
        }
        return OTHER_PARTY;
    }

}
