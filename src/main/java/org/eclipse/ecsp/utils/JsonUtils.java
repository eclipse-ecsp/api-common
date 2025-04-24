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

package org.eclipse.ecsp.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.EventDataDeSerializer;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link JsonUtils} contains utility methods for json serialization and de-serializations.
 *
 * @author Abhishek Kumar
 */
public class JsonUtils {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(JsonUtils.class);
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            ISODateTimeFormat.dateTime().withZoneUTC();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
        SimpleModule simpleModule = new SimpleModule();
        EventDataDeSerializer eventDataDeSerializer = new EventDataDeSerializer();
        simpleModule.addDeserializer(EventData.class, eventDataDeSerializer);
        OBJECT_MAPPER.registerModule(simpleModule);
        OBJECT_MAPPER.setFilterProvider(
                new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAll()));
    }

    private JsonUtils() {
    }

    /**
     * Fetch specific field value from json string by using field name.
     *
     * @param key  field name/attribute name in the json string
     * @param data json in string format
     * @return {@link String} field value from the json
     */
    public static String getValueAsString(String key, String data) {

        JsonNode json = null;
        try {
            json = OBJECT_MAPPER.readValue(data, JsonNode.class);
        } catch (IOException e) {
            LOGGER.info("Unable to parse the event data: {} ", data);
            return null;
        }
        return safeGetStringFromJsonNode(key, json);

    }

    /**
     * Deserialize json string to specific class object.
     *
     * @param inputJson json string
     * @param clazz     deserialization class type
     * @param <T>       class type
     * @return object of type T class instance
     * @throws IOException when unable to deserialized json to specified class instance
     */
    public static <T> T parseInputJson(String inputJson, Class<T> clazz)
            throws IOException {
        return OBJECT_MAPPER.readValue(inputJson.getBytes(StandardCharsets.UTF_8),
                OBJECT_MAPPER.getTypeFactory().constructType(clazz));
    }

    /**
     * deserialization json string to specific class object.
     *
     * @param json  json string
     * @param clazz deserialization class type
     * @param <T>   class type
     * @return list of type T class instance
     * @throws IOException when unable to deserialized json to specified class instance
     */
    public static <T> List<T> parseJsonAsList(String json, Class<T> clazz)
            throws IOException {
        List<T> response = OBJECT_MAPPER.readValue(json.getBytes(StandardCharsets.UTF_8),
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        LOGGER.trace("parsed json: {}", response);
        return response;
    }

    /**
     * deserialize json string to {@link List}.<br/>
     * return null if param is null or cannot be converted to {@link List}
     *
     * @param obj java object
     * @return json data in {@link List} format
     */
    @SuppressWarnings("rawtypes")
    public static List getObjectValueAsList(Object obj) {
        return OBJECT_MAPPER.convertValue(obj, List.class);
    }

    /**
     * fetch specific field value from {@link JsonNode}.<br/>
     * if the specific field is not available return null
     *
     * @param key  field name in JsonNode
     * @param json json payload
     * @return {@link String} field value from JsonNode
     */
    public static String safeGetStringFromJsonNode(String key, JsonNode json) {

        if (json == null) {
            return null;
        }

        JsonNode node = json.get(key);

        if (node != null) {
            return node.asText();
        } else {
            Iterator<?> it = json.fieldNames();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (str.equalsIgnoreCase(key)) {
                    return json.get(str).asText();
                }
            }
        }

        return null;
    }

    /**
     * fetch specific field value as boolean from {@link JsonNode}.<br/>
     * if the specific field is not available return false
     *
     * @param key  field name in JsonNode
     * @param json json payload
     * @return boolean field value from JsonNode
     */
    public static boolean safeGetBooleanFromJsonNode(String key, JsonNode json) {
        if (json == null) {
            return false;
        }

        JsonNode node = json.get(key);
        if (node != null) {
            return node.asBoolean();
        }
        return false;
    }

    /**
     * serialize Object to Json String.<br/>
     * if the obj is null return null
     *
     * @param obj object instance to be converted to Json
     * @return Json as {@link String}
     */
    public static String getObjectValueAsString(Object obj) {
        try {
            if (obj instanceof IgniteEvent) {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } else {
                return OBJECT_MAPPER.writeValueAsString(obj);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Json Parsing failed", e);
            LOGGER.info("Unable to create the class for the object {}", obj.toString());
            return null;
        }
    }

    /**
     * serialize Object to json byte array.<br/>
     * if the obj is null return null
     *
     * @param obj object instance to be converted to json bytes
     * @return Json as byte array
     */
    public static byte[] getObjectValueAsBytes(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to create the class for the object {} error {}", obj.toString(), e);
            return null;
        }
    }

    /**
     * serialize specific portion of the json by field to {@link JsonNode}.<br/>
     * if the obj is null or json doesn't contains specifed field name then return null
     *
     * @param key  field name
     * @param data json string to be converted to JsonNode
     * @return JsonNode of specified field
     */
    public static JsonNode getJsonNode(String key, String data) {
        JsonNode json = null;
        try {
            json = OBJECT_MAPPER.readValue(data, JsonNode.class);
        } catch (IOException e) {
            LOGGER.error("Unable to parse the event data: {}, error {}", data, e);
            return null;
        }

        if (json == null) {
            return null;
        }

        return json.get(key);
    }

    /**
     * Deserialize json string to {@link Map}.<br/>
     * return null if param is null or cannot be converted to {@link Map}
     *
     * @param eventData json in string format
     * @return json data in {@link Map} format
     */
    public static Map<String, Object> getJsonAsMap(String eventData) {
        try {
            return OBJECT_MAPPER.readValue(eventData, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            LOGGER.error("Unable to convert the eventData to object and error is {}", e);
            return null;
        }

    }

    /**
     * Deserialize json string to {@link Map}.<br/>
     * return null if param is null or cannot be converted to {@link Map}
     *
     * @param obj java object
     * @return json data in {@link Map} format
     */
    @SuppressWarnings("rawtypes")
    public static Map getObjectValueAsMap(Object obj) {
        return OBJECT_MAPPER.convertValue(obj, Map.class);
    }

    /**
     * From the given JsonNode.<br/>
     * fetches the value for a key and put the value in
     * a list
     *
     * @param key  field name
     * @param node Json payload in {@link JsonNode}
     * @return list of {@link String} of specified field name
     */
    public static List<String> getValuesAsList(JsonNode node, String key) {
        List<String> list = new ArrayList<>();
        JsonNode val = node.get(key);
        if (null != val) {
            if (val.isArray()) {
                Iterator<JsonNode> iter = val.iterator();
                while (iter.hasNext()) {
                    String value = iter.next().asText();
                    list.add(value);
                }

            } else if (val.isTextual()) {
                // should be single value
                list.add(val.asText());
            } else {
                LOGGER.error("Only single string value or arrays of string values are supported");
            }
        }
        return list;
    }

    /**
     * Method data takes the json data and binds to the POJO.
     *
     * @param eventData json string
     * @param clazz     deserialize to class
     * @param <T>       class type
     * @return deserialized json to specified class representation
     * @throws IOException when unable to deserialized json to specified class instance
     */
    public static <T> T bindData(String eventData, Class<T> clazz)
            throws IOException {
        return OBJECT_MAPPER.readValue(eventData, clazz);
    }

    /**
     * Method data takes the json data and binds to the POJO.
     *
     * @param data json string
     * @param cl   deserialize to class
     * @param <T>  class type
     * @return deserialized json to specified class representation
     * @throws IOException if unable to deserialized json to specified class instance
     */
    public static <T> List<T> getListObjects(String data, Class<T> cl)
            throws IOException {
        return OBJECT_MAPPER.readValue(data,
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, cl));
    }

    /**
     * For converting the joda time to ISO date which is used by MongoDB.
     */
    public static class IsoDateSerializer extends JsonSerializer<DateTime> {
        @Override
        public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            String isoDate = DATE_TIME_FORMATTER.print(value);
            jgen.writeString(isoDate);
        }
    }
}