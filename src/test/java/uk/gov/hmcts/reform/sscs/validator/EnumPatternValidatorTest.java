package uk.gov.hmcts.reform.sscs.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.single.hearing.LocationType;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumPatternValidatorTest {

    static Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void given_LocationIdIsValid_ThenSetEnumValue() {
        HearingLocation location = new HearingLocation();
        location.setLocationId(LocationType.COURT.toString());
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocation>> violations = validator.validate(location);
        assertTrue(violations.isEmpty());
    }

    private HearingLocation getHearingLocation() {
        HearingLocation location = new HearingLocation();
        location.setLocationType("LocType");
        return location;
    }
}
