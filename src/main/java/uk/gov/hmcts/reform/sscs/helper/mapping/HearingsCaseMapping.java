package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails.CaseDetailsBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn"})
// TODO Unsuppress in future
public final class HearingsCaseMapping {

    public static final String CASE_TYPE = "caseType";
    public static final String CASE_SUB_TYPE = "caseSubType";

    @Value("${exui.url}")
    private static String exUiUrl;

    @Value("${sscs.serviceCode}")
    private static String sscsServiceCode;

    private HearingsCaseMapping() {

    }

    public static CaseDetails buildHearingCaseDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();
        CaseDetailsBuilder caseDetailsBuilder = CaseDetails.builder();

        caseDetailsBuilder.hmctsServiceCode(getServiceCode());
        caseDetailsBuilder.caseId(getCaseID(caseData));
        caseDetailsBuilder.caseDeepLink(getCaseDeepLink(caseData));
        caseDetailsBuilder.hmctsInternalCaseName(getInternalCaseName(caseData));
        caseDetailsBuilder.publicCaseName(getPublicCaseName(caseData));
        caseDetailsBuilder.caseAdditionalSecurityFlag(shouldBeAdditionalSecurityFlag(caseData));
        caseDetailsBuilder.caseInterpreterRequiredFlag(isInterpreterRequired(caseData));
        caseDetailsBuilder.caseCategories(buildCaseCategories(caseData));
        caseDetailsBuilder.caseManagementLocationCode(getCaseManagementLocationCode(caseData));
        caseDetailsBuilder.caseRestrictedFlag(shouldBeSensitiveFlag());
        caseDetailsBuilder.caseSlaStartDate(getCaseCreated(caseData));

        return caseDetailsBuilder.build();
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
        Appellant appellant = appeal.getAppellant();
        boolean isYes = isYes(caseData.getDwpUcb()) || isYes(appellant.getUnacceptableCustomerBehaviour())
                || isYes(appellant.getIsAppointee()) && nonNull(appellant.getAppointee()) && isYes(appellant.getAppointee().getUnacceptableCustomerBehaviour())
                || nonNull(appeal.getRep()) && isYes(appeal.getRep().getHasRepresentative()) && isYes(appeal.getRep().getUnacceptableCustomerBehaviour());
        if (nonNull(caseData.getOtherParties())) {
            isYes = isYes || caseData.getOtherParties().stream()
                    .map(CcdValue::getValue)
                    .anyMatch(o -> isYes(o.getUnacceptableCustomerBehaviour())
                            || o.hasAppointee() && nonNull(o.getAppointee()) && isYes(o.getAppointee().getUnacceptableCustomerBehaviour())
                            || o.hasRepresentative() && nonNull(o.getRep()) && isYes(o.getRep().getUnacceptableCustomerBehaviour()));
        }
        return isYes;
    }

    public static boolean isInterpreterRequired(SscsCaseData caseData) {
        // TODO Adjournment - Check this is the correct logic for Adjournment
        Appeal appeal = caseData.getAppeal();
        boolean isYes = isYes(caseData.getAdjournCaseInterpreterRequired())
                || isYes(appeal.getHearingOptions().getLanguageInterpreter())
                || appeal.getHearingOptions().wantsSignLanguageInterpreter();
        if (nonNull(caseData.getOtherParties())) {
            isYes = isYes || caseData.getOtherParties().stream()
                    .map(CcdValue::getValue)
                    .anyMatch(o -> isYes(o.getHearingOptions().getLanguageInterpreter())
                            || o.getHearingOptions().wantsSignLanguageInterpreter());
        }
        return isYes;
    }

    public static List<CaseCategory> buildCaseCategories(SscsCaseData caseData) {
        // TODO Adjournment - Check this is the correct logic for Adjournment
        // TODO Dependant on SSCS-10273
        List<CaseCategory> categories = new ArrayList<>();

        categories.add(CaseCategory.builder()
                .categoryType(CASE_TYPE)
                .categoryValue(caseData.getBenefitCode())
                .build());

        categories.add(CaseCategory.builder()
                .categoryType(CASE_SUB_TYPE)
                .categoryValue(caseData.getIssueCode())
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
