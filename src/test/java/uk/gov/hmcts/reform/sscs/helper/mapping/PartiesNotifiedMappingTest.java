package uk.gov.hmcts.reform.sscs.helper.mapping;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequestPayload;

import static org.assertj.core.api.Assertions.assertThat;

class PartiesNotifiedMappingTest extends HearingsMappingBase {
    private static final Long HEARING_ID = 2344L;
    private static final Long VERSION_NUMBER = 2L;

    @DisplayName("When a valid hearing wrapper is given buildUpdatePartiesNotifiedPayload returns the correct Hearing Request")
    @Test
    void testBuildUpdatePartiesNotifiedPayload() {

        SscsCaseData caseData = SscsCaseData.builder()
            .hearings(Lists.newArrayList(Hearing.builder()
                .value(HearingDetails.builder()
                    .hearingId(String.valueOf(HEARING_ID))
                    .versionNumber(VERSION_NUMBER)
                    .build())
                .build()))
            .build();

        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .updatedCaseData(caseData)
                .build();

        PartiesNotifiedRequestPayload partiesNotifiedRequestPayload = PartiesNotifiedMapping.buildUpdatePartiesNotifiedPayload(wrapper);

        assertThat(partiesNotifiedRequestPayload.getRequestVersion()).isEqualTo(VERSION_NUMBER);
    }
}
