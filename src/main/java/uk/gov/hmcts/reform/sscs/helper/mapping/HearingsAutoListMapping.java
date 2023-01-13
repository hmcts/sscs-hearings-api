package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.PanelMember;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.shouldBeAdditionalSecurityFlag;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCode;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.checkBenefitIssueCode;

public final class HearingsAutoListMapping {

    private HearingsAutoListMapping() {

    }

    public static boolean shouldBeAutoListed(@Valid SscsCaseData caseData, ReferenceDataServiceHolder referenceData)
        throws ListingException {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (nonNull(overrideFields.getAutoList())) {
            return isYes(overrideFields.getAutoList());
        }

        return !(HearingsDetailsMapping.isCaseUrgent(caseData)
                || hasOrgRepresentative(caseData)
                || shouldBeAdditionalSecurityFlag(caseData)
                || isInterpreterRequired(caseData)
                || HearingsDetailsMapping.isCaseLinked(caseData)
                || isPaperCaseAndPoNotAttending(caseData)
                || hasMqpmOrFqpm(caseData, referenceData)
                || isThereOtherComments(caseData)
                || doesNotHaveDwpResponseDate(caseData)
            );
    }

    public static boolean hasOrgRepresentative(@Valid SscsCaseData caseData) {
        return isRepresentativeOrg(caseData.getAppeal().getRep())
                || hasOrgOtherParties(caseData.getOtherParties());
    }

    public static boolean hasOrgOtherParties(Collection<CcdValue<OtherParty>> otherParties) {
        return Optional.ofNullable(otherParties)
                .orElse(Collections.emptyList())
                .stream()
                .map(CcdValue::getValue)
                .map(OtherParty::getRep)
                .anyMatch(HearingsAutoListMapping::isRepresentativeOrg);
    }

    public static boolean isRepresentativeOrg(Representative rep) {
        return nonNull(rep)
                && isYes(rep.getHasRepresentative()) && isNotBlank(rep.getOrganisation());
    }

    public static boolean isPaperCaseAndPoNotAttending(@Valid SscsCaseData caseData) {
        return HearingsChannelMapping.isPaperCase(caseData)
                && !HearingsDetailsMapping.isPoOfficerAttending(caseData);
    }

    public static boolean isThereOtherComments(@Valid SscsCaseData caseData) {
        return isNotBlank(HearingsDetailsMapping.getListingComments(caseData));
    }

    public static boolean doesNotHaveDwpResponseDate(@Valid SscsCaseData caseData) {
        return isBlank(caseData.getDwpResponseDate());
    }

    public static boolean hasMqpmOrFqpm(@Valid SscsCaseData caseData, ReferenceDataServiceHolder referenceData) throws ListingException {
        SessionCategoryMap sessionCategoryMap = getSessionCaseCode(caseData, referenceData);

        checkBenefitIssueCode(sessionCategoryMap);

        return sessionCategoryMap.getCategory().getPanelMembers().stream()
                .anyMatch(HearingsAutoListMapping::isMqpmOrFqpm);
    }

    public static boolean isMqpmOrFqpm(PanelMember panelMember) {
        if (isNull(panelMember)) {
            return false;
        }
        switch (panelMember) {
            case MQPM1:
            case MQPM2:
            case FQPM:
                return true;
            default:
                return false;
        }
    }
}
