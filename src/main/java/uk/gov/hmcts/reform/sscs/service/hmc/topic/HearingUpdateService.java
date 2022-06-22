package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingStatus;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Venue;
import uk.gov.hmcts.reform.sscs.ccd.domain.WorkBasketFields;
import uk.gov.hmcts.reform.sscs.exception.InvalidHearingDataException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.VenueService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.service.CaseHearingLocationHelper.mapVenueDetailsToVenue;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingUpdateService {

    public static final int EXPECTED_SESSIONS = 1;
    private final VenueService venueService;

    public void updateHearing(HearingGetResponse hearingGetResponse, @Valid SscsCaseData sscsCaseData)
            throws MessageProcessingException, InvalidMappingException {

        Long hearingId = Long.valueOf(hearingGetResponse.getRequestDetails().getHearingRequestId());

        List<HearingDaySchedule> hearingSessions = hearingGetResponse.getHearingResponse().getHearingSessions();

        if (hearingSessions.size() != EXPECTED_SESSIONS) {
            throw new InvalidHearingDataException(
                    String.format("Invalid HearingDaySchedule, should have 1 session but instead has %d sessions, for Case Id %s and Hearing Id %s",
                            hearingSessions.size(),
                            sscsCaseData.getCcdCaseId(),
                            hearingId));
        }

        HearingDaySchedule hearingDaySchedule = hearingSessions.get(0);

        String hearingEpimsId = hearingDaySchedule.getHearingVenueEpimsId();

        VenueDetails venueDetails = venueService.getVenueDetailsForActiveVenueByEpimsId(hearingEpimsId);

        if (isNull(venueDetails)) {
            throw new InvalidMappingException(String.format("Invalid epims Id %s, unable to find active venue with that id, regarding Case Id %s",
                    hearingEpimsId,
                    sscsCaseData.getCcdCaseId()));
        }

        Venue venue = mapVenueDetailsToVenue(venueDetails);

        Hearing hearing = HearingsServiceHelper.getHearingById(hearingId, sscsCaseData);

        if (isNull(hearing)) {
            hearing = HearingsServiceHelper.createHearing(hearingId);
            HearingsServiceHelper.addHearing(hearing, sscsCaseData);
        }

        HearingDetails hearingDetails = hearing.getValue();
        hearingDetails.setEpimsId(hearingEpimsId);
        hearingDetails.setVenue(venue);
        LocalDateTime hearingStartDateTime = hearingDaySchedule.getHearingStartDateTime();
        hearingDetails.setStart(hearingStartDateTime);
        hearingDetails.setEnd(hearingDaySchedule.getHearingEndDateTime());
        String hearingDate = hearingStartDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        hearingDetails.setHearingDate(hearingDate);
        String hearingTime = hearingStartDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        hearingDetails.setTime(hearingTime);

        log.info("Venue has been updated from epimsId '{}' to '{}' for Case Id: {} with hearingId {}",
            hearingDetails.getEpimsId(),
            hearingEpimsId,
            sscsCaseData.getCcdCaseId(),
            hearingId);
    }

    public void setHearingStatus(String hearingId, @Valid SscsCaseData sscsCaseData, HmcStatus hmcStatus) {
        HearingStatus hearingStatus = hmcStatus.getHearingStatus();
        if (isNull(hearingStatus)) {
            return;
        }

        Hearing hearing = HearingsServiceHelper.getHearingById(Long.valueOf(hearingId), sscsCaseData);
        if (isNull(hearing)) {
            return;
        }

        hearing.getValue().setHearingStatus(hearingStatus);
    }

    public void setWorkBasketFields(String hearingId, @Valid SscsCaseData sscsCaseData, HmcStatus listAssistCaseStatus) {

        WorkBasketFields workBasketFields = sscsCaseData.getWorkBasketFields();

        if (isCaseListed(listAssistCaseStatus)) {
            LocalDate hearingDate = getHearingDate(hearingId, sscsCaseData);
            workBasketFields.setHearingDate(hearingDate);
        } else {
            workBasketFields.setHearingDate(null);
        }

    }

    public LocalDate getHearingDate(String hearingId, @Valid SscsCaseData sscsCaseData) {
        return Optional.ofNullable(HearingsServiceHelper.getHearingById(Long.valueOf(hearingId), sscsCaseData))
            .map(Hearing::getValue)
            .map(HearingDetails::getStart)
            .map(LocalDateTime::toLocalDate)
            .orElse(null);
    }

    public boolean isCaseListed(HmcStatus hmcStatus) {
        return LISTED == hmcStatus;
    }
}
