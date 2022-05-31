package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.SessionCategoryMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.mockito.BDDMockito.given;

class HearingsAutoListMappingTest extends HearingsMappingBase {

    private SscsCaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = SscsCaseData.builder()
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder()
                                .name(Name.builder()
                                        .firstName("Appel")
                                        .lastName("Lant")
                                        .build())
                                .build())
                        .hearingOptions(HearingOptions.builder()
                                .build())
                        .build())
                .build();
    }


    @DisplayName("When there are no conditions that affect autolisting, shouldBeAutoListed returns true")
    @Test
    void testShouldBeAutoListed() {
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
                .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        false,false,SessionCategory.CATEGORY_01,null));

        given(referenceData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        boolean result = HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceData);

        assertThat(result).isTrue();
    }

    @DisplayName("When there is a condition that affects autolisting, shouldBeAutoListed returns false")
    @Test
    void testShouldBeAutoListedFalse() {
        caseData.setLinkedCase(List.of(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference("123456")
                        .build())
                .build()));

        boolean result = HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceData);

        assertThat(result).isFalse();
    }

    @DisplayName("When appellant has a org as a representative, hasOrgRepresentative should return True")
    @Test
    void testHasOrgRepresentative() {
        caseData.getAppeal()
                .setRep(Representative.builder()
                        .hasRepresentative("Yes")
                        .organisation("test")
                        .build());

        boolean result = HearingsAutoListMapping.hasOrgRepresentative(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When appellant and other parties dont have a org as a representative hasOrgRepresentative should return false")
    @Test
    void testHasOrgRepresentativeNoOrg() {
        boolean result = HearingsAutoListMapping.hasOrgRepresentative(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When any other party has a rep with a name, hasOrgOtherParties should return true")
    @Test
    void testHasOrgOtherParties() {
        List<CcdValue<OtherParty>> otherParties = List.of(
                new CcdValue<OtherParty>(OtherParty.builder()
                        .rep(Representative.builder()
                                .hasRepresentative("Yes")
                                .organisation("Test")
                                .build())
                        .build()),
                new CcdValue<OtherParty>(OtherParty.builder()
                        .rep(Representative.builder()
                                .hasRepresentative("Yes")
                                .build())
                        .build()));

        boolean result = HearingsAutoListMapping.hasOrgOtherParties(otherParties);

        assertThat(result).isTrue();
    }

    @DisplayName("When no other party has a rep with a non blank name, hasOrgOtherParties should return false")
    @Test
    void testHasOrgOtherPartiesNone() {
        List<CcdValue<OtherParty>> otherParties = List.of(
                new CcdValue<OtherParty>(OtherParty.builder()
                        .rep(Representative.builder()
                                .hasRepresentative("Yes")
                                .organisation("")
                                .build())
                        .build()),
                new CcdValue<OtherParty>(OtherParty.builder()
                        .rep(Representative.builder()
                                .hasRepresentative("Yes")
                                .build())
                        .build()));

        boolean result = HearingsAutoListMapping.hasOrgOtherParties(otherParties);

        assertThat(result).isFalse();
    }

    @DisplayName("When no other party has a rep with a non blank name, hasOrgOtherParties should return false")
    @Test
    void testHasOrgOtherPartiesNull() {
        boolean result = HearingsAutoListMapping.hasOrgOtherParties(null);

        assertThat(result).isFalse();
    }

    @DisplayName("When hasRepresentative is Yes and organisation not blank isRepresentativeOrg should return True")
    @Test
    void testIsRepresentativeOrg() {
        Representative rep = Representative.builder()
                .hasRepresentative("Yes")
                .organisation("Test")
                .build();

        boolean result = HearingsAutoListMapping.isRepresentativeOrg(rep);

        assertThat(result).isTrue();
    }

    @DisplayName("When hasRepresentative is No, blank or null, isRepresentativeOrg should return False")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void testIsRepresentativeOrgNull(String value) {
        Representative rep = Representative.builder()
                .hasRepresentative(value)
                .organisation("Test")
                .build();

        boolean result = HearingsAutoListMapping.isRepresentativeOrg(rep);

        assertThat(result).isFalse();
    }

    @DisplayName("When Organisation is null, isRepresentativeOrg should return False")
    @ParameterizedTest
    @NullAndEmptySource
    void testIsRepresentativeOrgBlankOrg(String value) {
        Representative rep = Representative.builder()
                .hasRepresentative("Yes")
                .organisation(value)
                .build();

        boolean result = HearingsAutoListMapping.isRepresentativeOrg(rep);

        assertThat(result).isFalse();
    }

    @DisplayName("When Representative is null, isRepresentativeOrg should return False")
    @Test
    void testIsRepresentativeOrgRepNull() {
        boolean result = HearingsAutoListMapping.isRepresentativeOrg(null);

        assertThat(result).isFalse();
    }

    @DisplayName("When hearingType is Paper, isPaperCaseAndNoPoNotAttending return True")
    @Test
    void testIsPaperCaseAndNoPoNotAttending() {
        caseData.setDwpIsOfficerAttending("No");
        caseData.getAppeal().setHearingType("paper");

        boolean result = HearingsAutoListMapping.isPaperCaseAndNoPoNotAttending(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When hearingType is not Paper, isPaperCaseAndNoPoNotAttending return False")
    @Test
    void testIsPaperCaseAndNoPoNotAttendingNotPaper() {
        boolean result = HearingsAutoListMapping.isPaperCaseAndNoPoNotAttending(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When other in HearingOptions is not blank, isThereOtherComments return True")
    @Test
    void testIsThereOtherComments() {
        caseData.getAppeal().getHearingOptions().setOther("Test");

        boolean result = HearingsAutoListMapping.isThereOtherComments(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When other in HearingOptions is blank, isThereOtherComments return False")
    @Test
    void testIsThereOtherCommentsNone() {
        boolean result = HearingsAutoListMapping.isThereOtherComments(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When other in HearingOptions is not blank, isThereOtherComments return True")
    @Test
    void testHasDqpmOrFqpm() {

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
                .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        false,false,SessionCategory.CATEGORY_06,null));

        given(referenceData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        caseData.getAppeal().getHearingOptions().setOther("Test");

        boolean result = HearingsAutoListMapping.hasMqpmOrFqpm(caseData, referenceData);

        assertThat(result).isTrue();
    }

    @DisplayName("When other in HearingOptions is blank, isThereOtherComments return False")
    @Test
    void testHasDqpmOrFqpmNone() {

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
                .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        false,false,SessionCategory.CATEGORY_01,null));

        given(referenceData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        boolean result = HearingsAutoListMapping.hasMqpmOrFqpm(caseData, referenceData);

        assertThat(result).isFalse();
    }

    @DisplayName("When dwpIsOfficerAttending is yes, isPoAttending return True")
    @ParameterizedTest
    @EnumSource(
            value = PanelMember.class,
            names = {"FQPM", "MQPM1", "MQPM2"},
            mode = INCLUDE)
    void testIsMqpmOrFqpm(PanelMember value) {
        boolean result = HearingsAutoListMapping.isMqpmOrFqpm(value);

        assertThat(result).isTrue();
    }

    @DisplayName("When dwpIsOfficerAttending is No or blank, isPoAttending return False")
    @ParameterizedTest
    @EnumSource(
            value = PanelMember.class,
            names = {"FQPM", "MQPM1", "MQPM2"},
            mode = EXCLUDE)
    @NullSource
    void testIsMqpmOrFqpmNot(PanelMember value) {
        boolean result = HearingsAutoListMapping.isMqpmOrFqpm(value);

        assertThat(result).isFalse();
    }
}
