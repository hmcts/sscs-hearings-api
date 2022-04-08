package uk.gov.hmcts.reform.sscs.hearing.mapping;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvPartyFlags;

import java.util.List;

class PartyFlagsMappingTest {


    @Test
    void shouldAddTheMappingsGivenTheValuesAreNotNull() {
        var expected = getPartyFlags();
        var actual = PartyFlagsMapping.getPartyFlags(getSscsCaseData());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldNotThrowNullPointerWhenChainedValuesInCaseDataIsNUll() {
        NullPointerException npe = null;
        try {
            PartyFlagsMapping.getPartyFlags(getNullSscsCaseData());
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

    private List<ShvPartyFlags> getPartyFlags() {
        return List.of(
            ShvPartyFlags.builder().flagId("44").flagParentId("10")
                .flagDescription("Sign Language Interpreter").build(),
            ShvPartyFlags.builder().flagId("21").flagParentId("6")
                .flagDescription("Step free / wheelchair access").build(),
            ShvPartyFlags.builder().flagId("45").flagParentId("11")
                .flagDescription("Hearing loop (hearing enhancement system)").build(),
            ShvPartyFlags.builder().flagId("53").flagParentId("2")
                .flagDescription("Confidential address").build(),
            ShvPartyFlags.builder().flagId("56").flagParentId("2")
                .flagDescription("Unacceptable customer behaviour").build(),
            ShvPartyFlags.builder().flagId("63").flagParentId("1")
                .flagDescription("Potentially harmful medical evidence").build(),
            ShvPartyFlags.builder().flagId("67").flagParentId("1")
                .flagDescription("Urgent flag").build(),
            ShvPartyFlags.builder().flagId("70").flagParentId("2")
                .flagDescription("Language Interpreter").build());
    }
}
