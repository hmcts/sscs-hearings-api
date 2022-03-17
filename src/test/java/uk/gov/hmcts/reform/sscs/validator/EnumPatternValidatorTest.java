package uk.gov.hmcts.reform.sscs.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategoryType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.DayOfWeek;
import uk.gov.hmcts.reform.sscs.model.single.hearing.DayOfWeekUnavailabilityType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.LocationType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequirementType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityDayOfWeek;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumPatternValidatorTest {

    static Validator validator;

    public static final String CATEGORY_TYPE_EMPTY = "Category type must be present";
    public static final String CATEGORY_VALUE_EMPTY = "Category value must be present";
    public static final String PARTY_TYPE_EMPTY = "Party type must be present";
    public static final String PARTY_ROLE_EMPTY = "Party role must be present";

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
        assertEquals(
            "Unsupported type for locationId",
            violations.stream().collect(Collectors.toList()).get(0).getMessage()
        );
    }

    @Test
    void whenInvalidLocationIdIsEmpty() {
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
    void whenInvalidLocationId() {
        HearingLocations location = new HearingLocations();
        location.setLocationId("Loc");
        location.setLocationType("LocType");
        Set<ConstraintViolation<HearingLocations>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Unsupported type for locationId");
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
    void whenInvalidCaseCategory() {
        CaseCategory category = new CaseCategory();
        category.setCategoryValue("categoryValue");
        category.setCategoryType("categoryType");
        Set<ConstraintViolation<CaseCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .contains("Unsupported type for categoryType");
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
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains(
            "Unsupported type for requirementType");
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
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains(
            "Unsupported type for requirementType");
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
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains(
            "Unsupported type for requirementType");
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
    void whenInvalidPartyDetails() {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("id");
        partyDetails.setPartyType("IND1");
        partyDetails.setPartyRole("role1");
        Set<ConstraintViolation<PartyDetails>> violations = validator.validate(partyDetails);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .contains("Unsupported type for partyType");
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
        UnavailabilityDayOfWeek unavailabilityDow = getUnavailabilityDow();
        unavailabilityDow.setDayOfWeek(null);
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .contains("Unsupported type for dayOfWeek");
    }

    @Test
    void whenInValidUnavailabilityDowIsEmpty() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek("");
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .contains("Unsupported type for dayOfWeek");
    }

    @Test
    void whenValidUnavailabilityDow() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek(DayOfWeek.FRIDAY.toString());
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenInValidUnavailabilityDow() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek("January");
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Unsupported type for dayOfWeek", violations.stream()
            .collect(Collectors.toList()).get(0).getMessage());
    }

    @Test
    void whenInValidDowUnavailabilityType() {
        UnavailabilityDayOfWeek unavailabilityDow = getDaysOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType("dow");
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals(
            "Unsupported type for dayOfWeekUnavailabilityType",
            violations.stream().collect(Collectors.toList()).get(0).getMessage()
        );

    }

    @Test
    void whenInValidDowUnavailabilityTypeIsNull() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType(null);
        unavailabilityDow.setDayOfWeek(DayOfWeek.MONDAY.toString());
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains(
            "Unsupported type for dayOfWeekUnavailabilityType");
    }

    @Test
    void whenInValidDowUnavailabilityTypeIsEmpty() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType("");
        unavailabilityDow.setDayOfWeek(DayOfWeek.MONDAY.toString());
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains(
            "Unsupported type for dayOfWeekUnavailabilityType");
    }

    @Test
    void whenValidUnavailabilityDowUnavailabilityType() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        unavailabilityDow.setDayOfWeek(DayOfWeek.FRIDAY.toString());
        Set<ConstraintViolation<UnavailabilityDayOfWeek>> violations = validator.validate(unavailabilityDow);
        assertTrue(violations.isEmpty());
    }

    private HearingLocations getHearingLocation() {
        HearingLocations location = new HearingLocations();
        location.setLocationType("LocType");
        return location;
    }

    private UnavailabilityDayOfWeek getDaysOfWeek() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeek(DayOfWeek.MONDAY.toString());
        return unavailabilityDow;
    }

    private UnavailabilityDayOfWeek getUnavailabilityDow() {
        UnavailabilityDayOfWeek unavailabilityDow = new UnavailabilityDayOfWeek();
        unavailabilityDow.setDayOfWeekUnavailabilityType(DayOfWeekUnavailabilityType.ALLDAY.toString());
        return unavailabilityDow;
    }

    private PanelPreference getPanelPreference() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID("id");
        panelPreference.setMemberType("memType");
        return panelPreference;
    }
}
