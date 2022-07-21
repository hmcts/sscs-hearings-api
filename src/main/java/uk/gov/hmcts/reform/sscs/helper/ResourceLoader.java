package uk.gov.hmcts.reform.sscs.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ResourceLoader {

    private ResourceLoader() {
    }

    public static String loadJson(final String filePath) throws URISyntaxException, IOException {
        return new String(loadResource(filePath), StandardCharsets.UTF_8);
    }

    private static byte[] loadResource(final String filePath) throws URISyntaxException, IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);

        if (url == null) {
            throw new IllegalArgumentException(String.format("Could not find resource in path %s", filePath));
        }

        return Files.readAllBytes(Paths.get(url.toURI()));
    }
}
