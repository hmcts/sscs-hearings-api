package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.service.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;


@RestController
public final class HearingsCaseMapping {

    public static final String CASE_TYPE = "caseType";
    public static final String CASE_SUB_TYPE = "caseSubType";
    public static final String CASE_DETAILS_URL = "%s/cases/case-details/%s";

    private HearingsCaseMapping() {

    }

    public static CaseDetails buildHearingCaseDetails(HearingWrapper wrapper, ReferenceDataServiceHolder referenceDataServiceHolder) {
        SscsCaseData caseData = wrapper.getCaseData();
        return CaseDetails.builder()
                .hmctsServiceCode(getServiceCode(wrapper))
                .caseId(getCaseID(caseData))
                .caseDeepLink(getCaseDeepLink(wrapper))
                .hmctsInternalCaseName(getInternalCaseName(caseData))
                .publicCaseName(getPublicCaseName(caseData))
                .caseAdditionalSecurityFlag(shouldBeAdditionalSecurityFlag(caseData))
                .caseInterpreterRequiredFlag(isInterpreterRequired(caseData))
                .caseCategories(buildCaseCategories(caseData, referenceDataServiceHolder))
                .caseManagementLocationCode(getCaseManagementLocationCode(caseData))
                .caseRestrictedFlag(shouldBeSensitiveFlag())
                .caseSlaStartDate(getCaseCreated(caseData))
                .build();
    }

    public static String getServiceCode(HearingWrapper wrapper) {
        return wrapper.getSscsServiceCode();
    }

    public static String getCaseID(SscsCaseData caseData) {
        return caseData.getCcdCaseId();
    }

    public static String getCaseDeepLink(HearingWrapper wrapper) {
        return String.format(CASE_DETAILS_URL, wrapper.getExUiUrl(), getCaseID(wrapper.getCaseData()));
    }

    public static String getInternalCaseName(SscsCaseData caseData) {
        return caseData.getCaseAccessManagementFields().getCaseNameHmctsInternal();
    }

    public static String getPublicCaseName(SscsCaseData caseData) {
        return caseData.getCaseAccessManagementFields().getCaseNamePublic();
    }

    public static boolean shouldBeAdditionalSecurityFlag(SscsCaseData caseData) {
        return isYes(caseData.getDwpUcb())
                || shouldBeAdditionalSecurityOtherParties(caseData.getOtherParties());
    }

    public static boolean shouldBeAdditionalSecurityOtherParties(List<CcdValue<OtherParty>> otherParties) {
        return nonNull(otherParties) && otherParties.stream()
                .map(CcdValue::getValue)
                .anyMatch(o -> isYes(o.getUnacceptableCustomerBehaviour()));
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

    public static List<CaseCategory> buildCaseCategories(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        // TODO Adjournment - Check this is the correct logic for Adjournment
        List<CaseCategory> categories = new ArrayList<>();

        SessionCategoryMap sessionCaseCode = HearingsMapping.getSessionCaseCode(caseData, referenceDataServiceHolder);

        categories.addAll(getCaseSubTypes(sessionCaseCode, referenceDataServiceHolder));
        categories.addAll(getCaseTypes(sessionCaseCode, referenceDataServiceHolder));

        return categories;
    }

    public static List<CaseCategory> getCaseSubTypes(SessionCategoryMap sessionCaseCode, ReferenceDataServiceHolder referenceDataServiceHolder) {
        List<CaseCategory> categories = new ArrayList<>();
        categories.add(CaseCategory.builder()
                .categoryType(CASE_TYPE)
                .categoryValue(referenceDataServiceHolder.getSessionCategoryMaps().getCategoryTypeValue(sessionCaseCode))
                .build());
        return categories;
    }

    public static List<CaseCategory> getCaseTypes(SessionCategoryMap sessionCaseCode, ReferenceDataServiceHolder referenceDataServiceHolder) {
        List<CaseCategory> categories = new ArrayList<>();
        categories.add(CaseCategory.builder()
                .categoryType(CASE_SUB_TYPE)
                .categoryValue(referenceDataServiceHolder.getSessionCategoryMaps().getCategorySubTypeValue(sessionCaseCode))
                .categoryParent(referenceDataServiceHolder.getSessionCategoryMaps().getCategoryTypeValue(sessionCaseCode))
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


