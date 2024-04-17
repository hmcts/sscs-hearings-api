package uk.gov.hmcts.reform.sscs.service.hearings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.client.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.exception.GetHearingException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.OverridesMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.CaseHearing;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingsApiService;
import uk.gov.hmcts.reform.sscs.service.exceptions.UpdateCcdCaseDetailsException;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.buildHearingPayload;

@Slf4j
@Component
public class CreateHearingCaseUpdater extends HearingSaveActionBase {

    private final HmcHearingApiService hmcHearingApiService;

    private final HmcHearingsApiService hmcHearingsApiService;

    private final ReferenceDataServiceHolder refData;

    private final IdamService idamService;

    private static final Long HEARING_VERSION_NUMBER = 1L;


    @Autowired
    public CreateHearingCaseUpdater(CcdClient ccdClient, SscsCcdConvertService sscsCcdConvertService, HmcHearingApiService hmcHearingApiService,
                                    HmcHearingsApiService hmcHearingsApiService, ReferenceDataServiceHolder refData,
                                    IdamService idamService) {
        super(ccdClient, sscsCcdConvertService, refData);
        this.hmcHearingApiService = hmcHearingApiService;
        this.hmcHearingsApiService = hmcHearingsApiService;
        this.refData = refData;
        this.idamService = idamService;
    }

    public void createHearingAndUpdateCase(HearingRequest hearingRequest) throws UpdateCcdCaseDetailsException {
        HearingEvent event = HearingsServiceHelper.getHearingEvent(hearingRequest.getHearingState());
        updateCase(Long.valueOf(hearingRequest.getCcdCaseId()),
                                                     event.getEventType().getCcdType(),
                                                     idamService.getIdamTokens(),
                                                     hearingRequest
        );

        log.info("Case Updated using updateCaseV3 with Hearing Response for Case ID {}, Hearing State {} and CCD Event {}",
                 hearingRequest.getCcdCaseId(),
                 hearingRequest.getHearingState().getState(),
                 event.getEventType().getCcdType());
    }

    @Override
    protected UpdateCcdCaseService.UpdateResult applyUpdate(SscsCaseDetails caseDetails, HearingRequest hearingRequest) throws UpdateCcdCaseDetailsException {
        try {
            HearingWrapper hearingWrapper = createWrapper(hearingRequest, caseDetails);

            if (hearingRequest.getHearingState() == HearingState.ADJOURN_CREATE_HEARING) {
                hearingWrapper.setHearingState(HearingState.CREATE_HEARING);
            }

            createHearing(hearingWrapper);

            return new UpdateCcdCaseService.UpdateResult("Hearing created", "Hearing created");

        } catch (UpdateCaseException | ListingException | UnhandleableHearingStateException e) {
            log.error("Failed to update case with hearing response for case id: {}", caseDetails.getId(), e);
            throw new UpdateCcdCaseDetailsException("Failed to update case with hearing response", e);
        }
    }

    void createHearing(HearingWrapper wrapper) throws UpdateCaseException, ListingException {
        SscsCaseData caseData = wrapper.getCaseData();

        String caseId = caseData.getCcdCaseId();
        HearingsGetResponse hearingsGetResponse = hmcHearingsApiService.getHearingsRequest(caseId, null);
        CaseHearing hearing = HearingsServiceHelper.findExistingRequestedHearings(hearingsGetResponse);
        HmcUpdateResponse hmcUpdateResponse;

        OverridesMapping.setDefaultListingValues(wrapper, refData);

        if (isNull(hearing)) {
            HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, refData);
            log.debug("Sending Create Hearing Request for Case ID {}", caseId);
            hmcUpdateResponse = hmcHearingApiService.sendCreateHearingRequest(hearingPayload);

            log.debug("Received Create Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                caseId,
                wrapper.getHearingState().getState(),
                hmcUpdateResponse.toString());
        } else {
            hmcUpdateResponse = HmcUpdateResponse.builder()
                .hearingRequestId(hearing.getHearingId())
                .versionNumber(getHearingVersionNumber(hearing))
                .status(hearing.getHmcStatus())
                .build();

            log.debug("Existing hearing found, skipping Create Hearing Request for Case ID {}, Hearing State {}, Hearing version {} and "
                    + "Hearing Id {}",
                caseId,
                hearing.getHmcStatus(),
                hearing.getRequestVersion(),
                hearing.getHearingId());
        }

        hearingResponseUpdate(wrapper, hmcUpdateResponse);
    }

    private Long getHearingVersionNumber(CaseHearing hearing) {
        try {
            HearingGetResponse response = hmcHearingApiService.getHearingRequest(hearing.getHearingId().toString());
            return response.getRequestDetails().getVersionNumber();
        } catch (GetHearingException e) {
            log.debug("Hearing with id {} doesn't exist", hearing.getHearingId());
        }

        return HEARING_VERSION_NUMBER;
    }


}
