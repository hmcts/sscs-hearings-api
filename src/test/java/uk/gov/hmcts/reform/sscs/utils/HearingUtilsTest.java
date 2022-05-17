package uk.gov.hmcts.reform.sscs.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.sscs.ccd.domain.DateRange;
import uk.gov.hmcts.reform.sscs.ccd.domain.ExcludeDate;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.UnavailabilityRange;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingUtilsTest {



    @Test
    void shouldReturnFaceToFace() {
        // given HearingSubType
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        // when
        Mockito.when(hearingSubtype.isWantsHearingTypeFaceToFace()).thenReturn(true);
        //then
        assertEquals(HearingUtils.FACE_TO_FACE, HearingUtils.getPartyChannel(hearingSubtype));
    }

    @Test
    void shouldReturnTelephone() {
        // given HearingSubType
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        // when
        Mockito.when(hearingSubtype.isWantsHearingTypeTelephone()).thenReturn(true);
        //then
        assertEquals(HearingUtils.TELEPHONE, HearingUtils.getPartyChannel(hearingSubtype));
    }

    @Test
    void shouldReturnVideo() {
        // given HearingSubType
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        // when
        Mockito.when(hearingSubtype.isWantsHearingTypeVideo()).thenReturn(true);
        //then
        assertEquals(HearingUtils.VIDEO, HearingUtils.getPartyChannel(hearingSubtype));
    }

    @Test
    void shouldReturnPaper() {
        // given HearingSubType
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        // when
        Mockito.when(hearingSubtype.isWantsHearingTypeFaceToFace()).thenReturn(false);
        Mockito.when(hearingSubtype.isWantsHearingTypeTelephone()).thenReturn(false);
        Mockito.when(hearingSubtype.isWantsHearingTypeVideo()).thenReturn(false);
        //then
        assertEquals(HearingUtils.PAPER, HearingUtils.getPartyChannel(hearingSubtype));
    }

    @Test
    void shouldReturnUnavailabilityRanges() {
        // given HearingSubType
        HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);
        // when
        Mockito.when(hearingOptions.getExcludeDates()).thenReturn(getExcludeDates());
        //then
        List<UnavailabilityRange> unavailabilityRanges = HearingUtils.getPartyUnavailabilityRange(hearingOptions);
        assertEquals(1, unavailabilityRanges.size());
        assertEquals("12/01/2022", unavailabilityRanges.stream().findFirst().orElseThrow().getUnavailableFromDate());
        assertEquals("19/01/2022", unavailabilityRanges.stream().findFirst().orElseThrow().getUnavailableToDate());
    }



    private List<ExcludeDate> getExcludeDates() {
        return new ArrayList<>() {
            {
                add(ExcludeDate.builder()
                        .value(DateRange.builder()
                                .start("12/01/2022")
                                .end("19/01/2022")
                                .build())
                        .build());
            }
        };
    }
}
