package uk.gov.hmcts.reform.sscs.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SignLanguagesService;
import uk.gov.hmcts.reform.sscs.reference.data.service.VerbalLanguagesService;

@Getter
@RequiredArgsConstructor
@Component
public class ReferenceDataServiceHolder {

    private final HearingDurationsService hearingDurations;

    private final SessionCategoryMapService sessionCategoryMaps;

    private final VerbalLanguagesService verbalLanguages;

    private final SignLanguagesService signLanguages;

    @Value("${exui.url}")
    private String exUiUrl;

    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;

}
