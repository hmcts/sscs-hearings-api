package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LocationType {

    COURT("court"),
    CLUSTER("cluster"),
    REGION("region");

    private final String locationLabel;
}
