package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocationType {

    COURT("court"),
    CLUSTER("cluster"),
    REGION("region");

    private final String locationLabel;
}
