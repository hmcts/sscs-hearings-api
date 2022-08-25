package uk.gov.hmcts.reform.sscs.model.partiesnotified;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PartiesNotifiedRequestPayload {
    private Long requestVersion;
    private ServiceData serviceData;
}
