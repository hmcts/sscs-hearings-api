package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.client.JudicialUserBase;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.BenefitRoleRelationType;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.RequirementType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.Benefit.CHILD_SUPPORT;
import static uk.gov.hmcts.reform.sscs.ccd.domain.PanelMemberMedicallyQualified.getPanelMemberMedicallyQualified;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isNoOrNull;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCodeMap;

public final class HearingsPanelMapping {

    public static final Pattern MEMBER_ID_ROLE_REFERENCE_REGEX = Pattern.compile("(\\w*)\\|(\\w*)");

    private HearingsPanelMapping() {

    }

    public static PanelRequirements getPanelRequirements(SscsCaseData caseData,
                                                         ReferenceDataServiceHolder referenceDataServiceHolder) {
        return PanelRequirements.builder()
            .roleTypes(getRoleTypes(caseData.getBenefitCode()))
            .authorisationTypes(getAuthorisationTypes())
            .authorisationSubTypes(getAuthorisationSubTypes())
            .panelPreferences(getPanelPreferences(caseData, referenceDataServiceHolder))
            .panelSpecialisms(getPanelSpecialisms(caseData, getSessionCaseCodeMap(caseData, referenceDataServiceHolder)))
            .build();
    }

    public static List<String> getRoleTypes(String benefitCode) {
        return BenefitRoleRelationType.findRoleTypesByBenefitCode(benefitCode);
    }

    public static List<String> getAuthorisationTypes() {
        //TODO Need to retrieve AuthorisationTypes from caseData and/or ReferenceData
        return Collections.emptyList();
    }

    public static List<String> getAuthorisationSubTypes() {
        //TODO Need to retrieve AuthorisationSubTypes from caseData and/or ReferenceData
        return Collections.emptyList();
    }

    public static List<PanelPreference> getPanelPreferences(SscsCaseData caseData,
                                                            ReferenceDataServiceHolder referenceDataServiceHolder) {
        Adjournment adjournment = caseData.getAdjournment();
        ArrayList<PanelPreference> panelPreferences = new ArrayList<>();

        if (referenceDataServiceHolder.isAdjournmentFlagEnabled()
            && !isNoOrNull(adjournment.getAdjournmentInProgress())) {
            panelPreferences.addAll(getAdjournmentPanelPreferences(adjournment.getPanelMembers()));

            AdjournCasePanelMembersExcluded panelMembersExcluded = adjournment.getPanelMembersExcluded();
            if (panelMembersExcluded == AdjournCasePanelMembersExcluded.YES) {
                panelPreferences.forEach(panelPreference -> panelPreference.setRequirementType(RequirementType.EXCLUDE));
            } else if (panelMembersExcluded == AdjournCasePanelMembersExcluded.RESERVED) {
                panelPreferences.forEach(panelPreference -> panelPreference.setRequirementType(RequirementType.MUST_INCLUDE));
            }
        }

        return  panelPreferences;
    }

    private static List<PanelPreference> getAdjournmentPanelPreferences(List<JudicialUserBase> panelMembers) {
        return panelMembers.stream()
            .filter(panelMember -> nonNull(panelMember.getPersonalCode()))
            .map(paneMember -> getPanelPreference(paneMember.getPersonalCode()))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private static PanelPreference getPanelPreference(String memberID) {
        return PanelPreference.builder()
            .memberID(memberID)
            .memberType("PANEL_MEMBER")
            .requirementType(RequirementType.OPTIONAL_INCLUDE)
            .build();
    }

    public static List<String> getPanelSpecialisms(@Valid SscsCaseData caseData, SessionCategoryMap sessionCategoryMap) {
        List<String> panelSpecialisms = new ArrayList<>();

        if (isNull(sessionCategoryMap)) {
            return panelSpecialisms;
        }
        // if benefit is child support specialism should be empty
        if (isNotBlank(caseData.getBenefitCode()) && caseData.getBenefitCode().equals(CHILD_SUPPORT.getBenefitCode())) {
            return panelSpecialisms;
        }

        String doctorSpecialism = caseData.getSscsIndustrialInjuriesData().getPanelDoctorSpecialism();
        String doctorSpecialismSecond = caseData.getSscsIndustrialInjuriesData().getSecondPanelDoctorSpecialism();
        panelSpecialisms = sessionCategoryMap.getCategory().getPanelMembers().stream()
            .map(panelMember -> getPanelMemberSpecialism(panelMember, doctorSpecialism, doctorSpecialismSecond))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return panelSpecialisms;
    }

    public static String getPanelMemberSpecialism(PanelMember panelMember,
                                                  String doctorSpecialism, String doctorSpecialismSecond) {
        switch (panelMember) {
            case MQPM1:
                return getReference(doctorSpecialism);
            case MQPM2:
                return getReference(doctorSpecialismSecond);
            default:
                return panelMember.getReference();
        }
    }

    public static String getReference(String panelMemberSubtypeCcdRef) {
        PanelMemberMedicallyQualified subType = getPanelMemberMedicallyQualified(panelMemberSubtypeCcdRef);
        return nonNull(subType)
            ? subType.getHmcReference()
            : null;
    }
}
