package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.IndexNowService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndexNowKeyControllerTest {

    @Test
    void returnsConfiguredKeyOnly() {
        IndexNowService service = mock(IndexNowService.class);
        when(service.isValidKey("test-key-123")).thenReturn(true);
        when(service.key()).thenReturn("test-key-123");
        IndexNowKeyController controller = new IndexNowKeyController(service);

        ResponseEntity<String> response = controller.verificationKey("test-key-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("test-key-123");
    }

    @Test
    void rejectsUnknownKey() {
        IndexNowService service = mock(IndexNowService.class);
        IndexNowKeyController controller = new IndexNowKeyController(service);

        ResponseEntity<String> response = controller.verificationKey("unknown-key");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
