package uk.gov.hmcts.reform.sscs.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.sscs.exception.AuthorisationException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHeaderException;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    private static final String AUTHORIZATION = "Bearer eyJhbGciOiJIUzUxMiL7.eyJzdWIiOiJzc2NzIiwiZXhwIjoxNjQ2NDA5MjM5fQ"
            + ".zSEbvMJedOGo16yBOXecLgucWyavnoVu023cterreUF0sxPlmV-Qu8Y7OloJUKrLGlNweUr8mVpYWPzE0iNyYw";
    private static final String BAD_AUTHORIZATION = "eyJhbGciOiJIUzUxMiL7.eyJzdWIiOiJzc2NzIiwiZXhwIjoxNjQ2NDA5MjM5fQ"
            + ".zSEbvMJedOGo16yBOXecLgucWyavnoVu023cterreUF0sxPlmV-Qu8Y7OloJUKrLGlNweUr8mVpYWPzE0iNyYw";
    private static final String NOT_AUTHORIZATION = "Bearer notauthed";

    @Mock
    private ServiceAuthorisationApi serviceAuthorisationApi;

    private AuthorisationService authorisationService;

    @BeforeEach
    public void setUp() {
        authorisationService = new AuthorisationService(serviceAuthorisationApi);
    }

    @Test
    void shouldAuthorise() {
        given(serviceAuthorisationApi.getServiceName(eq(AUTHORIZATION))).willReturn("test");

        assertThatNoException().isThrownBy(
                () -> authorisationService.authorise(AUTHORIZATION));
    }

    @Test
    void shouldThrowExceptionWhenBadAuthorisation() {
        Request request = Request.create(Request.HttpMethod.GET, "url",
                new HashMap<>(), null, new RequestTemplate());

        given(serviceAuthorisationApi.getServiceName(anyString())).willThrow(
                new FeignException.Unauthorized("Invalid authorization header", request, null, null));

        assertThatExceptionOfType(InvalidHeaderException.class).isThrownBy(
                () -> authorisationService.authorise(BAD_AUTHORIZATION));
    }

    @Test
    void shouldThrowExceptionWhenNotAuthorised() {
        Request request = Request.create(Request.HttpMethod.GET, "url",
                new HashMap<>(), null, new RequestTemplate());

        given(serviceAuthorisationApi.getServiceName(anyString())).willThrow(
                new FeignException.Unauthorized("Error verifying token", request, null, null));

        assertThatExceptionOfType(AuthorisationException.class).isThrownBy(
                () -> authorisationService.authorise(NOT_AUTHORIZATION));
    }

}
