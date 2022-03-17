package uk.gov.hmcts.reform.sscs.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EnumPatternValidatorTest {

    static Validator validator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenLocationIdIsNullThenGiveUnsupportedErrorMessage() {
        HearingLocations location = getHearingLocation();
        location.setLocationId(null);
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals(
            "Unsupported type for locationId",
            violations.stream().collect(Collectors.toList()).get(0).getMessage()
        );
    }

    @Test
    void whenLocationIdIsEmptyThenGiveUnsupportedErrorMessage() {
        HearingLocations location = new HearingLocations();
        location.setLocationId("");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals(
            "Unsupported type for locationId",
            violations.stream().collect(Collectors.toList()).get(0).getMessage()
        );
    }

    @Test
    void whenLocationIdIsInvalidThenGiveUnsupportedErrorMessage() {
        HearingLocations location = new HearingLocations();
        location.setLocationId("Loc");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Unsupported type for locationId");
    }

    private HearingLocations getHearingLocation() {
        HearingLocations location = new HearingLocations();
        location.setLocationType("LocType");
        return location;
    }
}
