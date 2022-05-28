package uk.gov.hmcts.reform.sscs.model.servicebus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServiceBusKey {

    private final String host;

    private final String sharedAccessKeyName;

    private final String sharedAccessKey;
}
