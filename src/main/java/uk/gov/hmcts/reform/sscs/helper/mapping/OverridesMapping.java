package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.helper.adjournment.AdjournmentCalculateDateHelper;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.reference.data.model.Language;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

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
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsChannelMapping.getIndividualPreferredHearingChannel;

@Slf4j
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

    public static void setDefaultOverrideValues(HearingWrapper wrapper, ReferenceDataServiceHolder refData) throws ListingException {
        // get case data from hearing wrapper and required appeal fields
        SscsCaseData caseData = wrapper.getCaseData();
        Appeal appeal = caseData.getAppeal();
        HearingSubtype subtype = appeal.getHearingSubtype();
        HearingOptions options = appeal.getHearingOptions();
        // collect default listing values using case, ref and appeal data
        int duration = HearingsDurationMapping.getHearingDuration(caseData, refData);
        HearingInterpreter interpreter = getAppellantInterpreter(appeal, refData);
        HearingChannel channel = getIndividualPreferredHearingChannel(subtype, options, null);
        HearingWindow hearingWindow = getHearingDetailsHearingWindow(caseData);
        YesNo autoList = getHearingDetailsAutoList(caseData, refData);
        List<CcdValue<CcdValue<String>>> venueEpimsIds = getHearingDetailsLocations(caseData, refData);
        // build default override listing values and set on case data
        OverrideFields defaultOverrideValues = OverrideFields.builder()
            .duration(duration)
            .appellantInterpreter(interpreter)
            .appellantHearingChannel(channel)
            .hearingWindow(hearingWindow)
            .autoList(autoList)
            .hearingVenueEpimsIds(venueEpimsIds)
            .build();
        caseData.getSchedulingAndListingFields().setDefaultListingValues(defaultOverrideValues);

        log.debug("Default Override Listing Values set to {} for Case ID {}",
                  defaultOverrideValues,
                  wrapper.getCaseData().getCcdCaseId());
    }


    public static ReservedToMember getReservedToJudge(SscsCaseData caseData) {
        return ReservedToMember.builder()
            .isReservedToMember(isYes(caseData.getReservedToJudge()) ? YesNo.YES : YesNo.NO)
            .build();
    }

    public static HearingInterpreter getAppellantInterpreter(Appeal appeal, ReferenceDataServiceHolder refData)
        throws InvalidMappingException {
        HearingOptions hearingOptions = appeal.getHearingOptions();

        Language language = getInterpreterLanguage(hearingOptions, refData);

        if (isNull(language)) {
            return HearingInterpreter.builder()
                .isInterpreterWanted(YesNo.NO)
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
        return isYes(hearingOptions.getLanguageInterpreter())
            || isTrue(hearingOptions.wantsSignLanguageInterpreter()) ? YesNo.YES : YesNo.NO;
    }

    public static Language getInterpreterLanguage(HearingOptions hearingOptions, ReferenceDataServiceHolder refData)
        throws InvalidMappingException {
        if (isNull(hearingOptions)) {
            return null;
        }

        if (isTrue(hearingOptions.wantsSignLanguageInterpreter())) {
            String signLanguage = hearingOptions.getSignLanguageType();
            Language language = refData.getSignLanguages().getSignLanguage(signLanguage);

            if (isNull(language)) {
                throw new InvalidMappingException(String.format("The language %s cannot be mapped", signLanguage));
            }

            return language;
        }
        if (isYes(hearingOptions.getLanguageInterpreter())) {
            String verbalLanguage = hearingOptions.getLanguages();
            Language language = refData.getVerbalLanguages().getVerbalLanguage(verbalLanguage);

            if (isNull(language)) {
                throw new InvalidMappingException(String.format("The language %s cannot be mapped", verbalLanguage));
            }

            return language;
        }

        return null;

    }

    public static HearingWindow getHearingDetailsHearingWindow(SscsCaseData caseData, ReferenceDataServiceHolder referenceData) {

        LocalDate hearingWindowStart =
            referenceData.isAdjournmentFlagEnabled() && isYes(caseData.getAdjournment().getAdjournmentInProgress())
            ? AdjournmentCalculateDateHelper.getHearingWindowStart(caseData)
            : HearingsWindowMapping.getHearingWindowStart(caseData);

        return HearingWindow.builder()
            .firstDateTimeMustBe(null)
            .dateRangeStart(hearingWindowStart)
            .dateRangeEnd(null)
            .build();
    }

    public static YesNo getHearingDetailsAutoList(@Valid SscsCaseData caseData, ReferenceDataServiceHolder refData)
        throws ListingException {
        return HearingsAutoListMapping.shouldBeAutoListed(caseData, refData) ? YesNo.YES : YesNo.NO;
    }

    public static List<CcdValue<CcdValue<String>>> getHearingDetailsLocations(
        @Valid SscsCaseData caseData,
        ReferenceDataServiceHolder refData) throws InvalidMappingException {
        return HearingsLocationMapping.getHearingLocations(caseData, refData).stream()
            .map(HearingLocation::getLocationId)
            .filter(Objects::nonNull)
            .map(CcdValue::new)
            .map(CcdValue::new)
            .collect(Collectors.toList());
    }

    public static YesNo getPoToAttend(SscsCaseData caseData) {
        return isYes(caseData.getDwpIsOfficerAttending()) ? YesNo.YES : YesNo.NO;
    }
}
