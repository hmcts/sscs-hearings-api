package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder
public class PartyFlagsMapping {

    private String flagParentId;
    private String flagId;
    private String flagDescription;

    public void doMap(String flagCode) {
        PartyFlagsMap[] mapArr = PartyFlagsMap.values();
        for (PartyFlagsMap str : mapArr) {
            if (Objects.equals(flagCode, str.getFlagCode())) {
                this.flagId = str.getFlagId();
                this.flagParentId = str.getParentId();
                this.flagDescription = str.getFlagDescription();
            }
        }
    }
}
