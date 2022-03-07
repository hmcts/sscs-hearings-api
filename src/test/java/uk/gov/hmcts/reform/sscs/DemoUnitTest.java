package uk.gov.hmcts.reform.sscs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemoUnitTest {

    @Test
    void exampleOfTest() {
        assertThat(System.currentTimeMillis()).as("Here for setup, delete me.").isPositive();
    }
}
