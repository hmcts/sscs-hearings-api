package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMapping;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.PartyFlags;

import java.util.ArrayList;
import java.util.List;

class PartyFlagsMappingTest {

    PartyFlagsMapping mapping = new PartyFlagsMapping();

    @Test
    void shouldAddTheMappingsGivenTheValuesAreNotNull() {
        var expected = getPartyFlags();
        var actual = mapping.getPartyFlags(getSscsCaseData());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldNotThrowNullPointerWhenChainedValuesInCaseDataIsNUll() {
        NullPointerException npe = null;
        try {
            mapping.getPartyFlags(getNullSscsCaseData());
        } catch (NullPointerException ex) {
            npe = ex;
        }
        Assertions.assertNull(npe);
    }

    private SscsCaseData getNullSscsCaseData() {
        return SscsCaseData.builder()
            .dwpPhme(null)
            .dwpUcb(null)
            .urgentCase(null)
            .adjournCaseInterpreterLanguage(null)
            .isConfidentialCase(null)
            .appeal(Appeal.builder().hearingOptions(
                HearingOptions.builder()
                    .signLanguageType(null)
                    .arrangements(
                        List.of("", ""))
                    .build()).build())
            .build();
    }

    private SscsCaseData getSscsCaseData() {
        return SscsCaseData.builder()
            .dwpPhme("dwpPHME")
            .dwpUcb("dwpUCB")
            .urgentCase("urgentCase")
            .adjournCaseInterpreterLanguage("adjournCaseInterpreterLanguage")
            .isConfidentialCase(YesNo.YES)
            .appeal(Appeal.builder().hearingOptions(
                HearingOptions.builder()
                    .signLanguageType("signLanguageType")
                    .arrangements(
                        List.of("disabledAccess", "hearingLoop"))
                    .build()).build())
            .build();
    }

    private List<PartyFlags> getPartyFlags() {
        List<PartyFlags> partyFlagsList = new ArrayList<>();
        var disabledAccess = PartyFlags.builder()
                                                    .flagId("21")
                                                    .flagParentId("6")
                                                    .flagDescription("Step free / wheelchair access").build();
        var signLanguageType = PartyFlags.builder()
                                                    .flagId("44")
                                                    .flagParentId("10")
                                                    .flagDescription("Sign Language Interpreter").build();
        var hearingLoop = PartyFlags.builder()
                                                .flagId("45")
                                                .flagParentId("11")
                                                .flagDescription("Hearing loop (hearing enhancement system)").build();
        var isConfidentialCase =  PartyFlags.builder()
                                                        .flagId("53")
                                                        .flagParentId("2")
                                                        .flagDescription("Confidential address").build();
        var dwpUcb = PartyFlags.builder()
                                            .flagId("56")
                                            .flagParentId("2")
                                            .flagDescription("Unacceptable customer behaviour").build();

        var dwpPhme =  PartyFlags.builder()
                                                .flagId("63")
                                                .flagParentId("1")
                                                .flagDescription("Potentially harmful medical evidence").build();
        var urgentCase = PartyFlags.builder()
                                                .flagId("67")
                                                .flagParentId("1")
                                                .flagDescription("Urgent flag").build();
        var adjournCaseIL = PartyFlags.builder()
                                                    .flagId("70")
                                                    .flagParentId("2")
                                                    .flagDescription("Language Interpreter").build();
        partyFlagsList.add(signLanguageType);
        partyFlagsList.add(disabledAccess);
        partyFlagsList.add(hearingLoop);
        partyFlagsList.add(isConfidentialCase);
        partyFlagsList.add(dwpUcb);
        partyFlagsList.add(dwpPhme);
        partyFlagsList.add(urgentCase);
        partyFlagsList.add(adjournCaseIL);

        return partyFlagsList;
    }
}
