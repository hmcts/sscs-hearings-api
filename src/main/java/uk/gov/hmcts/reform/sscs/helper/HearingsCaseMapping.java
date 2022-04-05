package uk.gov.hmcts.reform.sscs.helper;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails.CaseDetailsBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

@SuppressWarnings({"PMD.LinguisticNaming","PMD.UnnecessaryLocalBeforeReturn"})
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
        caseDetailsBuilder.caseRef(getCaseID(caseData));
        caseDetailsBuilder.caseDeepLink(getCaseDeepLink(caseData));
        caseDetailsBuilder.hmctsInternalCaseName(getInternalCaseName(caseData));
        caseDetailsBuilder.publicCaseName(getPublicCaseName(caseData));
        caseDetailsBuilder.caseAdditionalSecurityFlag(shouldBeAdditionalSecurityFlag(caseData));
        caseDetailsBuilder.caseInterpreterRequiredFlag(isInterpreterRequired(caseData.getAdjournCaseInterpreterRequired()));
        caseDetailsBuilder.caseCategories(buildCaseCategories(caseData));
        caseDetailsBuilder.caseManagementLocationCode(getCaseManagementLocationCode(caseData.getCaseManagementLocation()));
        caseDetailsBuilder.caseRestrictedFlag(shouldBeSensitiveFlag());
        caseDetailsBuilder.caseSlaStartDate(caseData.getCaseCreated());

        return caseDetailsBuilder.build();
    }

    private static String getServiceCode() {
        return sscsServiceCode;
    }

    private static String getCaseID(SscsCaseData caseData) {
        return caseData.getCcdCaseId();
    }

    public static String getCaseDeepLink(SscsCaseData caseData) {
        // TODO Lucas - Confirm this is correct and create automated tests
        return String.format("%s/cases/case-details/%s", exUiUrl, getCaseID(caseData));
    }

    private static String getInternalCaseName(SscsCaseData caseData) {
        // TODO Lucas - Check these - should they be tied to WA?
        return caseData.getWorkAllocationFields().getCaseNameHmctsInternal();
    }

    private static String getPublicCaseName(SscsCaseData caseData) {
        // TODO Lucas - Check these - should they be tied to WA?
        return caseData.getWorkAllocationFields().getCaseNamePublic();
    }

    public static boolean shouldBeAdditionalSecurityFlag(SscsCaseData caseData) {
        boolean isYes = false;
        // TODO To be done
        //      Check unacceptableCustomerBehaviour for Appellant, their Appointee and their Representatives
        //      Check unacceptableCustomerBehaviour for each OtherParty, their Appointee and their Representatives

        return isYes;
    }

    public static boolean isInterpreterRequired(String adjournCaseInterpreterRequired) {
        boolean isYes = isYes(adjournCaseInterpreterRequired);
        // TODO Adjournment - Check this is the correct logic for Adjournment
        // TODO To be done
        return isYes;
    }

    public static List<CaseCategory> buildCaseCategories(SscsCaseData caseData) {
        List<CaseCategory> categories = new ArrayList<>();

        String benefitCodeValue = sscsServiceCode+"-" +
                                  caseData.getBenefitCode();

        categories.add(CaseCategory.builder()
                .categoryType(CASE_TYPE)
                .categoryValue(benefitCodeValue)
                .build());


        String issueCodeValue = benefitCodeValue +
                                caseData.getIssueCode();

        categories.add(CaseCategory.builder()
                .categoryType(CASE_SUB_TYPE)
                .categoryValue(issueCodeValue)
                .build());

        return categories;
    }

    public static String getCaseManagementLocationCode(CaseManagementLocation caseManagementLocation) {
        // TODO SSCS-10245 - map from caseManagementLocation to epims
        return null;
    }

    public static boolean shouldBeSensitiveFlag() {
        // TODO Future Work
        return false;
    }
}
