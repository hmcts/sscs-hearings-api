package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.SessionCaseCodeMapping;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCode;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn"})
// TODO Unsuppress in future
@RestController
public final class HearingsCaseMapping {

    public static final String CASE_TYPE = "caseType";
    public static final String CASE_SUB_TYPE = "caseSubType";

    private static String exUiUrl;
    private static String sscsServiceCode;

    private HearingsCaseMapping() {

    }

    @Value("${exui.url}")
    public static void setExUiUrl(String value) {
        exUiUrl = value;
    }

    @Value("${sscs.serviceCode}")
    public static void setSscsServiceCode(String value) {
        sscsServiceCode = value;
    }

    public static CaseDetails buildHearingCaseDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getCaseData();
        return CaseDetails.builder()
                .hmctsServiceCode(getServiceCode())
                .caseId(getCaseID(caseData))
                .caseDeepLink(getCaseDeepLink(caseData))
                .hmctsInternalCaseName(getInternalCaseName(caseData))
                .publicCaseName(getPublicCaseName(caseData))
                .caseAdditionalSecurityFlag(shouldBeAdditionalSecurityFlag(caseData))
                .caseInterpreterRequiredFlag(isInterpreterRequired(caseData))
                .caseCategories(buildCaseCategories(caseData))
                .caseManagementLocationCode(getCaseManagementLocationCode(caseData))
                .caseRestrictedFlag(shouldBeSensitiveFlag())
                .caseSlaStartDate(getCaseCreated(caseData))
                .build();
    }

    public static String getServiceCode() {
        return sscsServiceCode;
    }

    public static String getCaseID(SscsCaseData caseData) {
        return caseData.getCcdCaseId();
    }

    public static String getCaseDeepLink(SscsCaseData caseData) {
        return String.format("%s/cases/case-details/%s", exUiUrl, getCaseID(caseData));
    }

    public static String getInternalCaseName(SscsCaseData caseData) {
        return caseData.getWorkAllocationFields().getCaseNameHmctsInternal();
    }

    public static String getPublicCaseName(SscsCaseData caseData) {
        return caseData.getWorkAllocationFields().getCaseNamePublic();
    }

    public static boolean shouldBeAdditionalSecurityFlag(SscsCaseData caseData) {
        Appeal appeal = caseData.getAppeal();
        return isYes(caseData.getDwpUcb())
                || shouldBeAdditionalSecurityParty(appeal.getAppellant(), appeal.getRep())
                || shouldBeAdditionalSecurityOtherParties(caseData.getOtherParties());
    }

    public static boolean shouldBeAdditionalSecurityOtherParties(List<CcdValue<OtherParty>> otherParties) {
        return nonNull(otherParties) && otherParties.stream()
                .map(CcdValue::getValue)
                .anyMatch(o -> shouldBeAdditionalSecurityParty(o, o.getRep()));
    }

    public static boolean shouldBeAdditionalSecurityParty(Party party, Representative rep) {
        return  isYes(party.getUnacceptableCustomerBehaviour())
                || isYes(party.getIsAppointee()) && nonNull(party.getAppointee()) && isYes(party.getAppointee().getUnacceptableCustomerBehaviour())
                || nonNull(rep) && isYes(rep.getHasRepresentative()) && isYes(rep.getUnacceptableCustomerBehaviour());
    }

    public static boolean isInterpreterRequired(SscsCaseData caseData) {
        // TODO Adjournment - Check this is the correct logic for Adjournment
        Appeal appeal = caseData.getAppeal();
        return isYes(caseData.getAdjournCaseInterpreterRequired())
                || isInterpreterRequiredHearingOptions(appeal.getHearingOptions())
                || isInterpreterRequiredOtherParties(caseData.getOtherParties());
    }

    public static boolean isInterpreterRequiredOtherParties(List<CcdValue<OtherParty>> otherParties) {
        return nonNull(otherParties) && otherParties.stream().map(CcdValue::getValue).anyMatch(o -> isInterpreterRequiredHearingOptions(o.getHearingOptions()));
    }

    public static boolean isInterpreterRequiredHearingOptions(HearingOptions hearingOptions) {
        return  isYes(hearingOptions.getLanguageInterpreter()) || hearingOptions.wantsSignLanguageInterpreter();
    }

    public static List<CaseCategory> buildCaseCategories(SscsCaseData caseData) {
        // TODO Adjournment - Check this is the correct logic for Adjournment
        // TODO SSCS-10116 - Replace SessionCaseCodeMapping with commons version
        List<CaseCategory> categories = new ArrayList<>();

        SessionCaseCodeMapping sessionCaseCode = getSessionCaseCode(caseData);

        categories.addAll(getCaseSubTypes(sessionCaseCode));
        categories.addAll(getCaseTypes(sessionCaseCode));

        return categories;
    }

    public static List<CaseCategory> getCaseSubTypes(SessionCaseCodeMapping sessionCaseCode) {
        List<CaseCategory> categories = new ArrayList<>();
        categories.add(CaseCategory.builder()
                .categoryType(CASE_TYPE)
                .categoryValue(sessionCaseCode.getCategoryTypeValue())
                .build());
        return categories;
    }

    public static List<CaseCategory> getCaseTypes(SessionCaseCodeMapping sessionCaseCode) {
        List<CaseCategory> categories = new ArrayList<>();
        categories.add(CaseCategory.builder()
                .categoryType(CASE_SUB_TYPE)
                .categoryValue(sessionCaseCode.getCategorySubTypeValue())
                .build());
        return categories;
    }

    public static String getCaseManagementLocationCode(SscsCaseData caseData) {
        return caseData.getCaseManagementLocation().getBaseLocation();
    }

    public static boolean shouldBeSensitiveFlag() {
        // TODO Future Work
        return false;
    }

    public static String getCaseCreated(SscsCaseData caseData) {
        return caseData.getCaseCreated();
    }


}


