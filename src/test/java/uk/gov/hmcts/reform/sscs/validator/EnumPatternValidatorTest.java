package uk.gov.hmcts.reform.sscs.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.sscs.model.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.CaseCategoryType;
import uk.gov.hmcts.reform.sscs.model.DayOfWeek;
import uk.gov.hmcts.reform.sscs.model.DayOfWeekUnavailabilityType;
import uk.gov.hmcts.reform.sscs.model.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.LocationType;
import uk.gov.hmcts.reform.sscs.model.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.PartyType;
import uk.gov.hmcts.reform.sscs.model.RequirementType;
import uk.gov.hmcts.reform.sscs.model.UnavailabilityDoW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.sscs.exceptions.ValidationError.CATEGORY_TYPE_EMPTY;
import static uk.gov.hmcts.reform.sscs.exceptions.ValidationError.PARTY_ROLE_EMPTY;
import static uk.gov.hmcts.reform.sscs.exceptions.ValidationError.PARTY_TYPE_EMPTY;

public class EnumPatternValidatorTest {

    static Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(EnumPatternValidatorTest.class);

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenInvalidLocationIdIsNull() {
        HearingLocations location = getHearingLocation();
        location.setLocationId(null);
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationId", validationErrors.get(0));
    }

    @Test
    void whenInvalidLocationIdIsEmpty() {
        HearingLocations location = new HearingLocations();
        location.setLocationId("");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationId", validationErrors.get(0));
    }

    @Test
    void whenInvalidLocationId() {
        HearingLocations location = new HearingLocations();
        location.setLocationId("Loc");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for locationId", validationErrors.get(0));
    }

    @Test
    void whenValidLocationId() {
        HearingLocations location = new HearingLocations();
        location.setLocationId(LocationType.COURT.toString());
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInvalidCaseCategoryIsNull() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
        category.setCategoryType(null);
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertEquals(2, violations.size());
        assertTrue(validationErrors.contains("Unsupported type for categoryType"));
        assertTrue(validationErrors.contains(CATEGORY_TYPE_EMPTY));
    }

    @Test
    void whenInvalidCaseCategoryIsEmpty() {
        CaseCategory category = getCaseCategory();
        category.setCategoryType("");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(validationErrors.contains("Unsupported type for categoryType"));
        assertTrue(validationErrors.contains(CATEGORY_TYPE_EMPTY));
    }

    @Test
    void whenInvalidCaseCategory() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
        category.setCategoryType("categoryType");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for categoryType", validationErrors.get(0));
    }

    @Test
    void whenValidCaseCategory() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("caseValue");
        category.setCategoryType(CaseCategoryType.CASESUBTYPE.toString());
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInvalidRequirementTypeIsNull() {
        PanelPreference panelPreference = getPanelPreference();
        panelPreference.setRequirementType(null);
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for requirementType", validationErrors.get(0));
    }


    @Test
    void whenInvalidRequirementTypeIsEmpty() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        panelPreference.setRequirementType("");
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for requirementType", validationErrors.get(0));
    }

    @Test
    void whenInvalidRequirementType() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        panelPreference.setRequirementType("preference");
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for requirementType", validationErrors.get(0));
    }

    @Test
    void whenValidRequirementType() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        panelPreference.setRequirementType(RequirementType.MUSTINC.toString());
        Set<ConstraintViolation<PanelPreference>> violations = validator.validate(panelPreference);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInvalidPartyDetailsIsEmpty() {
        PartyDetails partyDetails = getPartyDetails();
        partyDetails.setPartyType("");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
        assertTrue(validationErrors.contains(PARTY_ROLE_EMPTY));
        assertTrue(validationErrors.contains(PARTY_TYPE_EMPTY));
    }

    @Test
    void whenInvalidPartyDetailsIsNull() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType(null);
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
        assertTrue(validationErrors.contains(PARTY_TYPE_EMPTY));
        assertTrue(validationErrors.contains(PARTY_ROLE_EMPTY));
    }

    @Test
    void whenInvalidPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType("IND1");
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> {
            validationErrors.add(e.getMessage());
            logger.info(e.getMessage());
        });
        assertTrue(validationErrors.contains("Unsupported type for partyType"));
    }

    @Test
    void whenValidPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType(PartyType.IND.toString());
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInValidUnavailabilityDowIsNull() {
        UnavailabilityDoW unavailabilityDow = getUnavailabilityDow();
        unavailabilityDow.setDayOfWeek(null);
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @Test
    void whenInValidUnavailabilityDowIsEmpty() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek("");
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @Test
    void whenValidUnavailabilityDow() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek(DayOfWeek.FRIDAY.toString());
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInValidUnavailabilityDow() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek("January");
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dow", validationErrors.get(0));
    }

    @Test
    void whenInValidDowUnavailabilityType() {
        UnavailabilityDoW unavailabilityDow = getDaysOfWeek();
        unavailabilityDow.setDowUnavailabilityType("dow");
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dowUnavailabilityType", validationErrors.get(0));
    }

    @Test
    void whenInValidDowUnavailabilityTypeIsNull() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDowUnavailabilityType(null);
        unavailabilityDow.setDayOfWeek(DayOfWeek.MONDAY.toString());
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dowUnavailabilityType", validationErrors.get(0));
    }

    @Test
    void whenInValidDowUnavailabilityTypeIsEmpty() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDowUnavailabilityType("");
        unavailabilityDow.setDayOfWeek(DayOfWeek.MONDAY.toString());
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        List<String> validationErrors = new ArrayList<>();
        violations.forEach(e -> validationErrors.add(e.getMessage()));
        assertEquals("Unsupported type for dowUnavailabilityType", validationErrors.get(0));
    }


    @Test
    void whenValidUnavailabilityDowUnavailabilityType() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek(DayOfWeek.FRIDAY.toString());
        Set<ConstraintViolation<UnavailabilityDoW>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

    private HearingLocations getHearingLocation() {
        HearingLocations location = new HearingLocations();
        location.setLocationType("LocType");
        return location;
    }

    private UnavailabilityDoW getDaysOfWeek() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDayOfWeek(DayOfWeek.MONDAY.toString());
        return unavailabilityDow;
    }

    private UnavailabilityDoW getUnavailabilityDow() {
        UnavailabilityDoW unavailabilityDow = new UnavailabilityDoW();
        unavailabilityDow.setDowUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        return unavailabilityDow;
    }

    private PartyDetails getPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        return partyDetails;
    }

    private PanelPreference getPanelPreference() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        return panelPreference;
    }

    private CaseCategory getCaseCategory() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
        return category;
    }


}
