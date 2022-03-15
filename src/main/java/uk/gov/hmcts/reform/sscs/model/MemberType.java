package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;

@Getter
public enum MemberType {

    JUDGE("JUDGE"),
    PANEL_MEMBER("PANEL_MEMBER");

    private final String value;

    MemberType(String value) {
        this.value = value;
    }
}
