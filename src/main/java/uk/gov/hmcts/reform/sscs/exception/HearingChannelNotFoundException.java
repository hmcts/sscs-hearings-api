package uk.gov.hmcts.reform.sscs.exception;

public class HearingChannelNotFoundException extends Exception {

    private static final long serialVersionUID = -1405605243260070042L;

    public HearingChannelNotFoundException() {
        super("Hearing Channel Not Found Exception");
    }
}
