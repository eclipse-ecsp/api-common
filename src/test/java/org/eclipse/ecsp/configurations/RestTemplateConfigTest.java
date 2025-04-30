/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and\
 * limitations under the License.
 * 
 * <p>SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.ecsp.configurations;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;

/**
 * {@link RestTemplateConfigTest} contains test cases.<br/>
 * for testing API calls using {@link RestTemplate}.
 *
 * @author abhishekkumar
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestTemplateConfig.class)
@TestPropertySource("classpath:/rest-template-test.properties")
public class RestTemplateConfigTest {
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(RestTemplateConfigTest.class);

    /**
     * RESPONSE_CODE_200.
     */
    public static final int RESPONSE_CODE_200 = 200;

    /**
     * INT_4.
     */
    public static final int INT_4 = 4;

    /**
     * LOOP_COUNT.
     */
    public static final int LOOP_COUNT = 10;

    /**
     * SLEEP_MILLIS.
     */
    public static final int SLEEP_MILLIS = 100;
    
    @Autowired
    RestTemplate restTemplate;

    /**
     * server.
     */
    @Rule
    public final MockWebServer server = new MockWebServer();
    
    @Test
    public void testResponseOk() throws Exception {
        server.url("/ok");
        server.enqueue(
            new MockResponse().setResponseCode(RESPONSE_CODE_200).setBody("ok")
        );
        
        ResponseEntity<String> response =
            restTemplate.getForEntity(server.url("/ok").uri(), String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
    }
    
    @Test
    public void testResponseDelay() {
        server.url("/ok");
        server.enqueue(
            new MockResponse().setResponseCode(RESPONSE_CODE_200).setBody("ok").setHeadersDelay(INT_4, TimeUnit.SECONDS)
        );
        URI url = server.url("/ok").uri();
        try {
            restTemplate.getForEntity(url, String.class);
            Assertions.fail("Expected ResourceAccessException");
        } catch (ResourceAccessException e) {
            Assertions.assertTrue(() -> ExceptionUtils.getRootCauseMessage(e).contains("Read timed out"));
        }
    }
    
    @Test
    public void testResponseConnectionPool() throws Exception {
        server.url("/ok");
        int loopCount = LOOP_COUNT;
        CountDownLatch countDownLatch = new CountDownLatch(loopCount);
        Map<String, Integer> requestCountMap = new HashMap<>();
        for (int i = 0; i < loopCount; i++) {
            server.enqueue(new MockResponse().setResponseCode(RESPONSE_CODE_200).setBody("ok")
                .setHeadersDelay(1, TimeUnit.SECONDS));
            new Thread(() -> {
                try {
                    restTemplate.getForEntity(server.url("/ok").uri(), String.class);
                    countDownLatch.countDown();
                    requestCountMap.compute("SUCCESS", (k, v) -> v == null ? 0 : ++v);
                } catch (RestClientException e) {
                    String msg = ExceptionUtils.getRootCauseMessage(e);
                    LOGGER.error(msg);
                    requestCountMap.compute(msg.split(":")[0], (k, v) -> v == null ? 0 : ++v);
                    countDownLatch.countDown();
                }
            }, "request-" + i).start();
            if (!new CountDownLatch(1).await(SLEEP_MILLIS, TimeUnit.MILLISECONDS)) {
                LOGGER.warn("Timeout occurred while waiting for latch countdown");
            }
        }
        countDownLatch.await();
        LOGGER.info(requestCountMap.toString());
        Assertions.assertNotNull(requestCountMap);
    }
}