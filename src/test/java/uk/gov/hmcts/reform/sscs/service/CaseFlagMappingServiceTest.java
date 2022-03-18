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

import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CaseFlagMappingServiceTest {

    private static final String SIGN_LANGUAGE_TYPE = "Sign Language type";

    @Autowired
    private CaseFlagMappingService service;

    @Nested
    class MappingServiceTest {

        @Test
        void shouldAddTheMappingsGivenTheValuesAreNotNull() {
            var actual = service.updateHmcCaseData(
                SscsCaseDataService.builder().sscsCaseData(getSscsCaseData()).build()
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
                        .signLanguageType(SIGN_LANGUAGE_TYPE)
                        .arrangements(
                            List.of("disabledAccess", "hearingLoop"))
                        .build()).build())
                .build();
        }

        private DescendantCaseData getSscsExpectedMappingCaseData() {
            Map<String, Object> caseFlags = Map.of(
                "RA0019", "disabledAccess",
                "RA0043", "hearingLoop",
                "RA0042", "signLanguageType",
                "PF0004", "isConfidentialCase",
                "PF0007", "dwpUCB",
                "CF0003", "dwpPHME",
                "CF0007", "urgentCase",
                "PF0015", "adjournCaseInterpreterLanguage"
            );
            return
                DescendantCaseData
                    .DescendantCaseDataBuilder()
                    .caseFlags(caseFlags)
                    .build();
        }
    }
}
