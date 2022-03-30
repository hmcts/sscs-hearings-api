package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyType {

    IND("IND"),
    ORG("ORG");

    private final String partyLabel;
}
