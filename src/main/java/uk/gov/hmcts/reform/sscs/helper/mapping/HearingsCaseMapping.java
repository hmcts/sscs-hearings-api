package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.checkBenefitIssueCode;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.CaseCategoryType.CASE_SUBTYPE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.CaseCategoryType.CASE_TYPE;


@RestController
@Slf4j
public final class HearingsCaseMapping {

    public static final String CASE_DETAILS_URL = "%s/cases/case-details/%s";

    private HearingsCaseMapping() {

    }

    public static CaseDetails buildHearingCaseDetails(HearingWrapper wrapper, ReferenceDataServiceHolder referenceDataServiceHolder) throws ListingException {
        SscsCaseData caseData = wrapper.getCaseData();
        return CaseDetails.builder()
                .hmctsServiceCode(getServiceCode(referenceDataServiceHolder))
                .caseId(getCaseID(caseData))
                .caseDeepLink(getCaseDeepLink(wrapper.getCaseData(), referenceDataServiceHolder))
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

    public static String getServiceCode(ReferenceDataServiceHolder referenceDataServiceHolder) {
        return referenceDataServiceHolder.getSscsServiceCode();
    }

    public static String getCaseID(SscsCaseData caseData) {
        return caseData.getCcdCaseId();
    }

    public static String getCaseDeepLink(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        return String.format(CASE_DETAILS_URL, referenceDataServiceHolder.getExUiUrl(), getCaseID(caseData));
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
        return isYes(caseData.getAdjournment().getInterpreterRequired())
                || isInterpreterRequiredHearingOptions(appeal.getHearingOptions())
                || isInterpreterRequiredOtherParties(caseData.getOtherParties());
    }

    public static boolean isInterpreterRequiredOtherParties(List<CcdValue<OtherParty>> otherParties) {
        return nonNull(otherParties) && otherParties.stream().map(CcdValue::getValue)
            .anyMatch(o -> isInterpreterRequiredHearingOptions(o.getHearingOptions()));
    }

    public static boolean isInterpreterRequiredHearingOptions(HearingOptions hearingOptions) {
        return  isYes(hearingOptions.getLanguageInterpreter()) || hearingOptions.wantsSignLanguageInterpreter();
    }

    public static List<CaseCategory> buildCaseCategories(SscsCaseData caseData,
                                                         ReferenceDataServiceHolder referenceDataServiceHolder) throws ListingException {
        // TODO Adjournment - Check this is the correct logic for Adjournment
        SessionCategoryMap sessionCategoryMap = HearingsMapping.getSessionCaseCodeMap(caseData, referenceDataServiceHolder);

        checkBenefitIssueCode(sessionCategoryMap);

        List<CaseCategory> categories = new ArrayList<>();
        categories.addAll(getCaseTypes(sessionCategoryMap, referenceDataServiceHolder));
        categories.addAll(getCaseSubTypes(sessionCategoryMap, referenceDataServiceHolder));

        return categories;
    }

    public static List<CaseCategory> getCaseTypes(SessionCategoryMap sessionCaseCode,
                                                  ReferenceDataServiceHolder referenceDataServiceHolder) {
        List<CaseCategory> categories = new ArrayList<>();
        categories.add(CaseCategory.builder()
                .categoryType(CASE_TYPE)
                .categoryValue(referenceDataServiceHolder.getSessionCategoryMaps().getCategoryTypeValue(sessionCaseCode))
                .build());
        return categories;
    }

    public static List<CaseCategory> getCaseSubTypes(SessionCategoryMap sessionCaseCode, ReferenceDataServiceHolder referenceDataServiceHolder) {
        List<CaseCategory> categories = new ArrayList<>();
        categories.add(CaseCategory.builder()
                .categoryType(CASE_SUBTYPE)
                .categoryParent(referenceDataServiceHolder.getSessionCategoryMaps().getCategoryTypeValue(sessionCaseCode))
                .categoryValue(referenceDataServiceHolder.getSessionCategoryMaps().getCategorySubTypeValue(sessionCaseCode))
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

    public static List<String> getReasonsForLink(SscsCaseData caseData) {
        return new ArrayList<>();
    }

}


