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

package org.eclipse.ecsp.constants;

/**
 * Common constants used by APIs.
 *
 * @author abhishekkumar
 */
public class Constants {
    
    private Constants() {
    }
    

    /**
     * DEFAULT.
     */
    public static final String DEFAULT = "default";

    /**
     * HYPHEN.
     */
    public static final String HYPHEN = "-";

    /**
     * URL_SEPARATOR.
     */
    public static final String URL_SEPARATOR = "/";

    /**
     * CONTENT_TYPE.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * APPLICATION_JSON.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * SUCCESS.
     */
    public static final String SUCCESS = "OK";

    /**
     * ERROR.
     */
    public static final String ERROR = "error";

    /**
     * COLON.
     */
    public static final String COLON = ":";

    /**
     * EVENT_FILTER.
     */
    public static final String EVENT_FILTER = "eventFilter";
    

    /**
     * HTTP_HEADER_PLATFORM_RESPONSE_ID.
     */
    public static final String HTTP_HEADER_PLATFORM_RESPONSE_ID = "PlatformResponseId";

    /**
     * HTTP_HEADER_SESSION_ID.
     */
    public static final String HTTP_HEADER_SESSION_ID = "SessionId";

    /**
     * HTTP_HEADER_CLIENT_REQUEST_ID.
     */
    public static final String HTTP_HEADER_CLIENT_REQUEST_ID = "ClientRequestId";

    /**
     * HTTP_HEADER_REQUEST_ID.
     */
    public static final String HTTP_HEADER_REQUEST_ID = "RequestId";

    /**
     * HTTP_HEADER_ORIGIN_ID.
     */
    public static final String HTTP_HEADER_ORIGIN_ID = "OriginId";
    

    /**
     * KAFKA_KEY_ACKS.
     */
    public static final String KAFKA_KEY_ACKS = "acks";

    /**
     * KAFKA_KEY_LINGER_MS.
     */
    public static final String KAFKA_KEY_LINGER_MS = "linger.ms";

    /**
     * KAFKA_KEY_RETRIES.
     */
    public static final String KAFKA_KEY_RETRIES = "retries";

    /**
     * KAFKA_KEY_KEY_SERIALIZER.
     */
    public static final String KAFKA_KEY_KEY_SERIALIZER = "key.serializer";

    /**
     * KAFKA_KEY_VALUE_SERIALIZER.
     */
    public static final String KAFKA_KEY_VALUE_SERIALIZER = "value.serializer";

    /**
     * KAFKA_KEY_REQUEST_TIME_OUT_MS.
     */
    public static final String KAFKA_KEY_REQUEST_TIME_OUT_MS = "request.timeout.ms";

    /**
     * KAFKA_KEY_COMPRESSION_TYPE.
     */
    public static final String KAFKA_KEY_COMPRESSION_TYPE = "compression.type";

    /**
     * KAFKA_KEY_BATCH_SIZE.
     */
    public static final String KAFKA_KEY_BATCH_SIZE = "batch.size";

    /**
     * KAFKA_KEY_MAX_BLOCK_MS.
     */
    public static final String KAFKA_KEY_MAX_BLOCK_MS = "max.block.ms";

    /**
     * KAFKA_KEY_MAX_INFLIGHT_REQUEST_PER_CONN.
     */
    public static final String KAFKA_KEY_MAX_INFLIGHT_REQUEST_PER_CONN =
        "max.in.flight.requests.per.connection";

    /**
     * KAFKA_CLIENT_KEYSTORE_PASS_KEY.
     */
    public static final String KAFKA_CLIENT_KEYSTORE_PASS_KEY = "kafka_client_keystore_password";

    /**
     * KAFKA_CLIENT_KEY_PASS_KEY.
     */
    public static final String KAFKA_CLIENT_KEY_PASS_KEY = "kafka_client_key_password";

    /**
     * KAFKA_CLIENT_TRUSTSTORE_PASS_KEY.
     */
    public static final String KAFKA_CLIENT_TRUSTSTORE_PASS_KEY = "kafka_client_truststore_password";

    /**
     * DOT.
     */
    public static final String DOT = ".";

    /**
     * UNDER_SCORE.
     */
    public static final String UNDER_SCORE = "_";

    /**
     * ZERO.
     */
    public static final int ZERO = 0;

    /**
     * DEFAULT_PAGE_NUMBER.
     */
    public static final int DEFAULT_PAGE_NUMBER = 1;

    /**
     * DESC_ORDER.
     */
    public static final String DESC_ORDER = "desc";

    /**
     * ERROR_NULL_OBJECT_ID.
     */
    public static final String ERROR_NULL_OBJECT_ID = "Object ID must not be null or empty.";
}