package uk.gov.hmcts.reform.sscs.helper.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

class HearingsServiceHelperTest {

    @Test
    void shouldReturnHearingId_givenValidWrapper() {
        HearingWrapper wrapper = activeHearingIdFixture(12345L);

        final String actualHearingId = getHearingId(wrapper);

        assertThat(actualHearingId, is("12345"));
    }

    @Test
    void shouldReturnNull_givenInvalidWrapper() {
        final String actualHearingId = getHearingId(new HearingWrapper());

        assertNull(actualHearingId);
    }

    @Test
    void shouldReturnNullHearingId_givenNullValue() {
        HearingWrapper wrapper = activeHearingIdFixture(null);

        final String actualHearingId = getHearingId(wrapper);

        assertNull(actualHearingId);
    }


    private HearingWrapper activeHearingIdFixture(final Long hearingId) {
        return HearingWrapper.builder()
            .updatedCaseData(SscsCaseData.builder()
                                 .schedulingAndListingFields(SchedulingAndListingFields.builder()
                                                                 .activeHearingId(hearingId)
                                                                 .build())
                                 .build())
            .build();
    }
}
