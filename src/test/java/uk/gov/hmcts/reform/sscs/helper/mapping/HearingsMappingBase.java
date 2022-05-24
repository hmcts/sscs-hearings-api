package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.ReferenceData;
import uk.gov.hmcts.reform.sscs.service.SessionCategoryMapService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class HearingsMappingBase {

    public static final String CASE_CREATED = "2022-04-01";

    @Mock
    public HearingDurationsService hearingDurations;

    @Mock
    public SessionCategoryMapService sessionCategoryMaps;

    @Mock
    public ReferenceData referenceData;

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
    public static final String SSCS_SERVICE_CODE = "BBA3";
    public static final String EX_UI_URL = "http://localhost:3455";

    @NotNull
    public static List<String> splitCsvParamArray(String expected) {
        List<String> paramArray = new ArrayList<>(List.of(expected.split(ARRAY_SPLIT_REGEX)));
        paramArray.removeAll(Arrays.asList("", null));
        return paramArray;
    }
}
