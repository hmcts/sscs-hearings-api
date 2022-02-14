package uk.gov.hmcts.reform.sscs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;

@SpringBootApplication
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.sscs.service.coh",
        "uk.gov.hmcts.reform.idam"
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class HearingApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(HearingApiApplication.class, args);
    }

    @Bean
    public CcdRequestDetails getRequestDetails(@Value("${core_case_data.jurisdictionId}") String coreCaseDataJurisdictionId,
                                               @Value("${core_case_data.caseTypeId}") String coreCaseDataCaseTypeId) {
        return CcdRequestDetails.builder()
            .caseTypeId(coreCaseDataCaseTypeId)
            .jurisdictionId(coreCaseDataJurisdictionId)
            .build();
    }
}