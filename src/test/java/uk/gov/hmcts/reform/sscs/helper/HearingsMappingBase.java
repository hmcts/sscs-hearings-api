package uk.gov.hmcts.reform.sscs.helper;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class HearingsMappingBase {

    public static final String CASE_CREATED = "2022-04-01";

    protected HearingsMappingBase() {

    }

    public static final long HEARING_REQUEST_ID = 12345;
    public static final String HMC_STATUS = "TestStatus";
    public static final long VERSION = 1;
    public static final String CANCELLATION_REASON_CODE = "TestCancelCode";
    public static final long CASE_ID = 1625080769409918L;
    public static final String ARRAY_SPLIT_REGEX = "\\s*\\|\\s*";
    public static final String ARRAY_SPLIT_REGEX_2 = "\\s*\\#\\s*";
    public static final long MISSING_CASE_ID = 99250807409918L;
    public static final String CASE_REFERENCE = "SC123/11/11111";
    public static final String CASE_NAME_PUBLIC = "Case Name Public";
    public static final String CASE_NAME_INTERNAL = "Case Name Internal";
    public static final String BENEFIT_CODE = "002";
    public static final String ISSUE_CODE = "DD";
    public static final String REGION = "Test Region";
    public static final String EPIMS_ID = "239585";

    @Value("${exui.url}")
    public static String SSCS_SERVICE_CODE;

    @Value("${sscs.serviceCode}")
    public static String EX_UI_URL;

    @NotNull
    public static List<String> splitCsvParamArray(String expected) {
        return List.of(expected.split(ARRAY_SPLIT_REGEX));
    }
}
