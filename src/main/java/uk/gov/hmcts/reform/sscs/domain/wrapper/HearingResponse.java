package uk.gov.hmcts.reform.sscs.domain.wrapper;

import java.util.List;

public class HearingResponse {
    private String listAssistTransactionID;
    private String receivedDateTime;
    private Number responseVersion;
    private LaCaseStatus laCaseStatus;
    private ListingStatus listingStatus;
    private String hearingCancellationReason;
    private List<HearingDaySchedule> hearingDaySchedule;

    public enum LaCaseStatus {
        tbd1,
        tbd2
    }

    public enum ListingStatus {
        tbd1,
        tbd2
    }
}
