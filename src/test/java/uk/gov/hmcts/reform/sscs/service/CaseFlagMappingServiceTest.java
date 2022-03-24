package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.DescendantCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CaseFlagMappingServiceTest {

    @Autowired
    private CaseFlagMappingService service;

    @Nested
    class MappingServiceTest {

        @Test
        void shouldAddTheMappingsGivenTheValuesAreNotNull() {
            var actual = service.getCaseFlagMapping(
                HearingWrapper.builder().originalCaseData(getSscsCaseData()).build()
            );
            var expected = getSscsExpectedMappingCaseData();
            Assertions.assertEquals(expected, actual);
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

        private DescendantCaseData getSscsExpectedMappingCaseData() {
            Map<String, String> caseFlags = Map.of(
                "DISABLED_ACCESS", "RA0019",
                "HEARING_LOOP", "RA0043",
                "SIGN_LANGUAGE_TYPE", "RA0042",
                "IS_CONFIDENTIAL_CASE", "PF0004",
                "DWP_UCB", "PF0007",
                "DWP_PHME", "CF0003",
                "URGENT_CASE", "CF0007",
                "AD_JOURN_CASE_INTERPRETER_LANGUAGE", "PF0015"
            );
            return
                DescendantCaseData
                    .DescendantCaseDataBuilder()
                    .caseFlags(caseFlags)
                    .build();
        }
    }
}
