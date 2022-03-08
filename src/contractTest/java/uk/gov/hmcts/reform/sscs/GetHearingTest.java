package uk.gov.hmcts.reform.sscs;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.jupiter.api.AfterAll;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.model.Attendees;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
public class GetHearingTest {


    private static WireMockServer WireMockServer;

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @BeforeClass
    public static void init(){
        WireMockServer = new WireMockServer(10000);
        WireMockServer.start();
    }

    @AfterClass
    public static void end(){
        WireMockServer.stop();
    }

    @Test
    public void test(){
        WireMockServer.stubFor(get("/test2?id=1")
                    .willReturn(okJson("{\"partyID\":\"12\", \"hearingSubChannel\":\"123123\"}")));

        Attendees result = hmcHearingApi.test("1");

        System.out.println(result.toString());


    }

}
