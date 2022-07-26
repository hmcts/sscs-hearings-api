package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.sscs.ccd.domain.AmendReason;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicList;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicListItem;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingInterpreter;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingWindow;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.ReservedToMember;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.reference.data.model.Language;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsChannelMapping.getIndividualPreferredHearingChannel;

@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports"})
public final class OverridesMapping {

    private OverridesMapping() {

    }

    public static OverrideFields getOverrideFields(@Valid SscsCaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(SscsCaseData::getSchedulingAndListingFields)
            .map(SchedulingAndListingFields::getOverrideFields)
            .orElse(OverrideFields.builder().build());
    }

    public static List<AmendReason> getAmendReasonCodes(@Valid SscsCaseData caseData) {
        return Optional.ofNullable(caseData.getSchedulingAndListingFields().getAmendReasons())
            .orElse(Collections.emptyList());
    }

    public static void setDefaultOverrideFields(HearingWrapper wrapper, ReferenceDataServiceHolder referenceDataServiceHolder) throws InvalidMappingException, IOException {
        SscsCaseData caseData = wrapper.getCaseData();

        Appeal appeal = caseData.getAppeal();

        OverrideFields defaultOverrideFields = OverrideFields.builder()
            .duration(HearingsDetailsMapping.getHearingDuration(caseData, referenceDataServiceHolder))
            .reservedToJudge(getReservedToJudge(caseData))
            .appellantInterpreter(getAppellantInterpreter(appeal, referenceDataServiceHolder))
            .appellantHearingChannel(getIndividualPreferredHearingChannel(appeal.getHearingSubtype(), appeal.getHearingOptions(), null))
            .hearingWindow(getHearingDetailsHearingWindow(caseData))
            .autoList(getHearingDetailsAutoList(caseData, referenceDataServiceHolder))
            .hearingVenueEpimsIds(getHearingDetailsLocations(caseData, referenceDataServiceHolder))
            .poToAttend(getPoToAttend(caseData))
            .build();

        caseData.getSchedulingAndListingFields().setDefaultOverrideFields(defaultOverrideFields);

        log.debug("Default Override Fields set to {} for Case ID {}",
            defaultOverrideFields,
            wrapper.getCaseData().getCcdCaseId());
    }

    public static ReservedToMember getReservedToJudge(SscsCaseData caseData) {
        return ReservedToMember.builder()
            .isReservedToMember(isYes(caseData.getReservedToJudge()) ? YES : NO)
            .build();
    }

    public static HearingInterpreter getAppellantInterpreter(Appeal appeal, ReferenceDataServiceHolder referenceDataServiceHolder) throws InvalidMappingException {
        HearingOptions hearingOptions = appeal.getHearingOptions();

        Language language = getInterpreterLanguage(hearingOptions, referenceDataServiceHolder);

        if (isNull(language)) {
            return HearingInterpreter.builder()
                .isInterpreterWanted(NO)
                .build();
        }

        String languageName = nonNull(language.getDialectEn()) ? language.getDialectEn() : language.getNameEn();
        String languageReference = HearingsPartiesMapping.getLanguageReference(language);

        DynamicListItem listItem = new DynamicListItem(languageReference, languageName);
        DynamicList interpreterLanguage = new DynamicList(listItem, List.of(listItem));

        YesNo interpreterWanted = getInterpreterWanted(hearingOptions);

        return HearingInterpreter.builder()
            .isInterpreterWanted(interpreterWanted)
            .interpreterLanguage(interpreterLanguage)
            .build();
    }

    @NotNull
    public static YesNo getInterpreterWanted(HearingOptions hearingOptions) {
        return isYes(hearingOptions.getLanguageInterpreter()) || isTrue(hearingOptions.wantsSignLanguageInterpreter()) ? YES : NO;
    }

    public static Language getInterpreterLanguage(HearingOptions hearingOptions, ReferenceDataServiceHolder referenceData) throws InvalidMappingException {
        if (isNull(hearingOptions)) {
            return null;
        }

        if (isTrue(hearingOptions.wantsSignLanguageInterpreter())) {
            String signLanguage = hearingOptions.getSignLanguageType();
            Language language = referenceData.getSignLanguages().getSignLanguage(signLanguage);

            if (isNull(language)) {
                throw new InvalidMappingException(String.format("The language %s cannot be mapped", signLanguage));
            }

            return language;
        }
        if (isYes(hearingOptions.getLanguageInterpreter())) {
            String verbalLanguage = hearingOptions.getLanguages();
            Language language = referenceData.getVerbalLanguages().getVerbalLanguage(verbalLanguage);

            if (isNull(language)) {
                throw new InvalidMappingException(String.format("The language %s cannot be mapped", verbalLanguage));
            }

            return language;
        }

        return null;

    }

    public static HearingWindow getHearingDetailsHearingWindow(SscsCaseData caseData) {
        LocalDate hearingWindowStart = HearingsDetailsMapping.getHearingWindowStart(caseData);

        return HearingWindow.builder()
            .firstDateTimeMustBe(null)
            .dateRangeStart(hearingWindowStart)
            .dateRangeEnd(null)
            .build();
    }

    public static YesNo getHearingDetailsAutoList(@Valid SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        return HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceDataServiceHolder) ? YES : NO;
    }

    public static List<CcdValue<CcdValue<String>>> getHearingDetailsLocations(@Valid SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) throws IOException {
        return HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder).stream()
            .map(HearingLocation::getLocationId)
            .filter(Objects::nonNull)
            .map(CcdValue::new)
            .map(CcdValue::new)
            .collect(Collectors.toList());
    }

    public static YesNo getPoToAttend(SscsCaseData caseData) {
        return isYes(caseData.getDwpIsOfficerAttending()) ? YES : NO;
    }
}
