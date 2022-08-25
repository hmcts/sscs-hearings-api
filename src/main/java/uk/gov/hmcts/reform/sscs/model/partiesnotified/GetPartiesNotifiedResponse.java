package uk.gov.hmcts.reform.sscs.model.partiesnotified;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetPartiesNotifiedResponse {
    private Long hearingId;

    private List<PartiesNotifiedResponse> responses;
}
