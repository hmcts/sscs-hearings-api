package uk.gov.hmcts.reform.sscs.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ServiceHearingValues;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceHearingValuesMapperTest {

    private static final ServiceHearingValuesMapper mapper = new ServiceHearingValuesMapper();

    private static SscsCaseDetails sscsCaseDetails;

    @BeforeEach
    public void setUp() {
        this.sscsCaseDetails = SscsCaseDetails.builder()
                                .data(SscsCaseData.builder()
                                    .ccdCaseId("1234")
                                    .autoListFlag(YesNo.YES)
                                    .appeal(Appeal.builder()
                                        .hearingType("final")
                                        .appellant(Appellant.builder()
                                                 .name(Name.builder()
                                                           .firstName("Fred")
                                                           .lastName("Flintstone")
                                                           .title("Mr")
                                                           .build())
                                                 .build())
                                        .build())
                                    .build())
                                .build();
    }

    @Test
    void shouldMapServiceHearingValuesSuccessfully(){
        // given
        SscsCaseData sscsCaseData = sscsCaseDetails.getData();

        // when
        ServiceHearingValues serviceHearingValues = mapper.mapServiceHearingValues(sscsCaseDetails);

        System.out.println(serviceHearingValues.getHearingType());
        System.out.println(serviceHearingValues.isAutoListFlag());
        System.out.println(sscsCaseData);

        //then
        assertEquals(serviceHearingValues.getCaseName(), sscsCaseData.getAppeal().getAppellant().getName().getFullName());
        assertEquals(serviceHearingValues.isAutoListFlag(), YesNo.isYes(sscsCaseData.getAutoListFlag()));
        assertEquals(serviceHearingValues.getHearingType(), sscsCaseData.getAppeal().getHearingType());
    }

}
