package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SessionCaseCodeMapping {
    // TODO SSCS-10116 - Replace with commons object
    private Integer benefitCode;
    private String issueCode;
    private String ccdKey;
    private String benefitDescription;
    private String issueDescription;
    private Integer sessionCat;
    private Integer otherSessionCat;
    private Integer durationFaceToFace;
    private Integer durationPaper;
    private List<String> panelMembers;
    private String comment;

    public static final String SERVICE_CODE = "BBA3";

    public String getCategoryTypeValue() {
        return String.format("%s-%03d", SERVICE_CODE, benefitCode);
    }

    public String getCategorySubTypeValue() {
        return String.format("%s%s", getCategoryTypeValue(), issueCode);
    }
}
