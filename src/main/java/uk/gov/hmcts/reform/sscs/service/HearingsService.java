package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.deserialisation.SscsCaseCallbackDeserializer;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.config.HearingsConfig;
import uk.gov.hmcts.reform.sscs.domain.JobWrapper;
import uk.gov.hmcts.reform.sscs.domain.SscsCaseDataWrapper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

import java.util.List;

import static java.util.Arrays.asList;

@Service
@Slf4j
public class HearingsService {
    private static final List<String> PROCESS_AUDIO_VIDEO_ACTIONS_THAT_REQUIRES_NOTICE = asList("issueDirectionsNotice", "excludeEvidence", "admitEvidence");
    private static final String READY_TO_LIST = "readyToList";

    private final AuthorisationService authorisationService;
    private final CcdService ccdService;
    private final SscsCaseCallbackDeserializer deserializer;
    private final IdamService idamService;
    private final HearingsConfig hearingsConfig;

    @SuppressWarnings("squid:S107")
    @Autowired

    public HearingsService(AuthorisationService authorisationService,
        CcdService ccdService,
        SscsCaseCallbackDeserializer deserializer,
        IdamService idamService,
        HearingsConfig hearingsConfig) {
        this.authorisationService = authorisationService;
        this.ccdService = ccdService;
        this.deserializer = deserializer;
        this.idamService = idamService;
        this.hearingsConfig = hearingsConfig;
    }

    public void processJobRequest(String caseId, String eventType, String jobId, String jobGroup) {
        JobWrapper jobWrapper = buildJobWrapper(caseId, eventType, jobId, jobGroup);
        processJob(jobWrapper);
    }

    public void processJob(JobWrapper jobWrapper) {
        try {

            log.info("Scheduled event: {} triggered for case id: {}", jobWrapper.getEventType().getCcdType(),
                jobWrapper.getCaseId());

            IdamTokens idamTokens = idamService.getIdamTokens();

            SscsCaseDetails caseDetails = ccdService.getByCaseId(jobWrapper.getCaseId(), idamTokens);

            if (caseDetails == null) {
                log.warn("Case id: {} could not be found for event: {}", jobWrapper.getCaseId(), jobWrapper.getEventType().getCcdType());
            } else {
                SscsCaseDataWrapper sscsCaseDataWrapper = buildSscsCaseDataWrapper(
                    null,
                    caseDetails.getData(),
                    jobWrapper.getEventType());

                log.info("Ccd Response received for case id: {}",
                    sscsCaseDataWrapper.getOldSscsCaseData().getCcdCaseId());

                //do something

            }
        } catch (Exception exc) {
            log.error("Failed to process job [" + jobWrapper.getJobId() + "] for case [" + jobWrapper.getCaseId()
                + "] and event [" + jobWrapper.getEventType().getCcdType() + "]", exc);
        }
    }

    public SscsCaseDataWrapper buildSscsCaseDataWrapper(SscsCaseData newCaseData,
                                                         SscsCaseData oldCaseData,
                                                         EventType event) {
        return SscsCaseDataWrapper.builder()
            .newSscsCaseData(newCaseData)
            .oldSscsCaseData(oldCaseData)
            .eventType(event)
            .build();
    }

    public JobWrapper buildJobWrapper(
        String caseId, String eventType, String jobId, String jobGroup) {

        return JobWrapper.builder()
            .caseId(Long.parseLong(caseId))
            .eventType(EventType.getEventTypeByCcdType(eventType))
            .jobId(jobId)
            .jobGroup(jobGroup)
            .build();
    }
}
