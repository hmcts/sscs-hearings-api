package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class OrganisationDetails {

    private String name;

    private String organisationType;

    private String cftOrganisationID;
}
