package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.sscs.ccd.domain.Benefit.CHILD_SUPPORT;
import static uk.gov.hmcts.reform.sscs.ccd.domain.Benefit.IIDB;
import static uk.gov.hmcts.reform.sscs.ccd.domain.PanelMemberType.TRIBUNALS_MEMBER_FINANCIALLY_QUALIFIED;
import static uk.gov.hmcts.reform.sscs.ccd.domain.PanelMemberType.TRIBUNALS_MEMBER_MEDICAL;



@Getter
@RequiredArgsConstructor
public enum BenefitRoleRelationType {

    INDUSTRIAL_INJURIES_DISABLEMENT_BENEFIT(IIDB.getBenefitCode(), Arrays.asList(TRIBUNALS_MEMBER_MEDICAL.getReference())),
    CHILD_SUPPORT_BENEFIT(CHILD_SUPPORT.getBenefitCode(), Arrays.asList(TRIBUNALS_MEMBER_FINANCIALLY_QUALIFIED.getReference()));

    @JsonValue
    private final String benefitCode;
    private final List<String> roleTypes;

    public static List<String> findRoleTypesByBenefitCode(String benefitCode) {
        for (BenefitRoleRelationType tmp : values()) {
            if (tmp.getBenefitCode().equals(benefitCode)) {
                return tmp.roleTypes;
            }
        }
        return new ArrayList<>();
    }
}
