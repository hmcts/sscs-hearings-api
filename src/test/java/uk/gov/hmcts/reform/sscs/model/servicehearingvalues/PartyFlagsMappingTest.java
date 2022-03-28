package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartyFlagsMappingTest {

    private final PartyFlagsMapping partyFlagsMapping = PartyFlagsMapping.builder().build();

    @Test
    void check_RA0019_providesCorrectMappingWhenInvoked() {
        partyFlagsMapping.doMap("RA0019");
        String expectedFlagId = "21";
        String expectedFlagDescription = "Step free / wheelchair access";
        String expectedFlagParentId = "6";
        assertEquals(expectedFlagId, partyFlagsMapping.getFlagId());
        assertEquals(expectedFlagDescription, partyFlagsMapping.getFlagDescription());
        assertEquals(expectedFlagParentId, partyFlagsMapping.getFlagParentId());
    }

    @Test
    void check_PF0004_providesCorrectMappingWhenInvoked() {
        partyFlagsMapping.doMap("PF0004");
        String expectedFlagId = "53";
        String expectedFlagDescription = "Confidential address";
        String expectedFlagParentId = "2";
        assertEquals(expectedFlagId, partyFlagsMapping.getFlagId());
        assertEquals(expectedFlagDescription, partyFlagsMapping.getFlagDescription());
        assertEquals(expectedFlagParentId, partyFlagsMapping.getFlagParentId());
    }

    @Test
    void check_PF0015_providesCorrectMappingWhenInvoked() {
        partyFlagsMapping.doMap("PF0015");
        String expectedFlagId = "70";
        String expectedFlagDescription = "Language Interpreter";
        String expectedFlagParentId = "2";
        assertEquals(expectedFlagId, partyFlagsMapping.getFlagId());
        assertEquals(expectedFlagDescription, partyFlagsMapping.getFlagDescription());
        assertEquals(expectedFlagParentId, partyFlagsMapping.getFlagParentId());
    }
}
