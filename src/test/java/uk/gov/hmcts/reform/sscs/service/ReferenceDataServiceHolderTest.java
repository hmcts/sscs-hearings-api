package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SignLanguagesService;
import uk.gov.hmcts.reform.sscs.reference.data.service.VerbalLanguagesService;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReferenceDataServiceHolderTest {

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private VerbalLanguagesService verbalLanguages;

    @Mock
    private SignLanguagesService signLanguages;

    @InjectMocks
    private ReferenceDataServiceHolder referenceData;

    @Test
    void testGetHearingDurations() throws NoSuchFieldException, IllegalAccessException {
        //given
        final Field field = referenceData.getClass().getDeclaredField("hearingDurations");
        field.setAccessible(true);

        field.set(referenceData, hearingDurations);

        //when
        final HearingDurationsService result = referenceData.getHearingDurations();

        //then
        assertThat(result).as("field wasn't retrieved properly").isEqualTo(hearingDurations);
    }

    @Test
    void testGetSessionCategoryMap() throws NoSuchFieldException, IllegalAccessException {
        //given
        final Field field = referenceData.getClass().getDeclaredField("sessionCategoryMaps");
        field.setAccessible(true);

        field.set(referenceData, sessionCategoryMaps);

        //when
        final SessionCategoryMapService result = referenceData.getSessionCategoryMaps();

        //then
        assertThat(result).as("field wasn't retrieved properly").isEqualTo(sessionCategoryMaps);
    }

    @Test
    void testGetVerbalLanguages() throws NoSuchFieldException, IllegalAccessException {
        //given
        final Field field = referenceData.getClass().getDeclaredField("verbalLanguages");
        field.setAccessible(true);

        field.set(referenceData, verbalLanguages);

        //when
        final VerbalLanguagesService result = referenceData.getVerbalLanguages();

        //then
        assertThat(result).as("field wasn't retrieved properly").isEqualTo(verbalLanguages);
    }

    @Test
    void testGetSignLanguages() throws NoSuchFieldException, IllegalAccessException {
        //given
        final Field field = referenceData.getClass().getDeclaredField("signLanguages");
        field.setAccessible(true);

        field.set(referenceData, signLanguages);

        //when
        final SignLanguagesService result = referenceData.getSignLanguages();

        //then
        assertThat(result).as("field wasn't retrieved properly").isEqualTo(signLanguages);
    }
}
