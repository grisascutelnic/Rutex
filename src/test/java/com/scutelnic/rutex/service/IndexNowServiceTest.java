package com.scutelnic.rutex.service;

import com.scutelnic.rutex.event.IndexNowUrlsChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class IndexNowServiceTest {

    @Test
    void submitsUniqueAbsoluteUrls() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        IndexNowService service = new IndexNowService(
                builder,
                "https://rutex.md/",
                "test-key-123",
                true,
                "https://api.indexnow.org/indexnow"
        );

        server.expect(once(), requestTo("https://api.indexnow.org/indexnow"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "host": "rutex.md",
                          "key": "test-key-123",
                          "keyLocation": "https://rutex.md/test-key-123.txt",
                          "urlList": [
                            "https://rutex.md/ro/rides",
                            "https://rutex.md/ru/rides"
                          ]
                        }
                        """))
                .andRespond(withSuccess());

        service.submitChangedUrls(new IndexNowUrlsChangedEvent(List.of(
                "/ro/rides",
                "/ro/rides",
                "https://rutex.md/ru/rides"
        )));

        server.verify();
    }
}
