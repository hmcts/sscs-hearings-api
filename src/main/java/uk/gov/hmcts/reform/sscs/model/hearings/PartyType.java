package uk.gov.hmcts.reform.sscs.model.hearings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PartyType {

    IND("IND"),
    ORG("ORG");

    private final String partyLabel;

}
