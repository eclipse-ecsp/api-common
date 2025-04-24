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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link JsonUtilsTest} contains test cases for {@link JsonUtils}.
 *
 * @author abhishekkumar
 */
public class JsonUtilsTest {
    
    @Autowired
    private JsonUtils jsonUtils;
    
    private static ObjectMapper jsonMapper = new ObjectMapper();
    
    /**
     * inner class used to test cases.
     */
    public static class Inner {
        String val;
        
        public Inner() {
        }
        
        public Inner(String val) {
            this.val = val;
        }
        
        public String getVal() {
            return this.val;
        }
        
        public void setVal(String value) {
            this.val = value;
        }
    }
    
    @Test
    public void testGetValueAsString() {
        String jsonString = "{\"EventID\":\"Request\"}";
        Assert.assertEquals("Request", jsonUtils.getValueAsString("EventID", jsonString));
        assertNull(jsonUtils.getValueAsString("Key", jsonString));
    }
    
    @Test
    public void parseJsonAsListTest() throws IOException {
        String jsonListString = "[\"A\",\"B\"]";
        Assert.assertNotNull(jsonUtils.parseJsonAsList(jsonListString, String.class));
    }
    
    @Test
    public void getObjectValueAsStringTest() {
        Inner inner = new Inner("test");
        inner.setVal("test");
        Assert.assertEquals("{\"val\":\"test\"}", jsonUtils.getObjectValueAsString(inner));
        inner = null;
        Assert.assertEquals("null", jsonUtils.getObjectValueAsString(inner));
        IgniteEvent event = new IgniteEventImpl();
        event.setSchemaVersion(Version.V1_0);
        Assert.assertNotNull(jsonUtils.getObjectValueAsString(event));
    }
    
    @Test
    public void getObjectValueAsListTest() {
        List list = new ArrayList<>();
        Inner inner = new Inner("test");
        list.add(inner);
        List result = jsonUtils.getObjectValueAsList(list);
        Assert.assertTrue(result.size() > 0);
    }
    
    @Test
    public void parseInputJsonTest() throws JsonParseException, JsonMappingException, IOException {
        Inner inner = jsonUtils.parseInputJson("{\"val\":\"test\"}", Inner.class);
        Assert.assertEquals("test", inner.getVal());
    }
    
    @Test
    public void safeGetBooleanFromJsonNodeTest()
        throws JsonMappingException, JsonProcessingException {
        String jsonString = "{\"switch\":true}";
        JsonNode json = jsonMapper.readValue(jsonString, JsonNode.class);
        Assert.assertTrue(jsonUtils.safeGetBooleanFromJsonNode("switch", json));
    }
    
    @Test
    public void getObjectValueAsBytesTest() {
        Inner inner = new Inner("A");
        byte[] byteArray = jsonUtils.getObjectValueAsBytes(inner);
        Assert.assertNotNull(byteArray);
    }
    
    @Test
    public void getJsonNodeTest() {
        String jsonString = "{\"Data\":{\"switch\":\"True\"}}";
        JsonNode json = jsonUtils.getJsonNode("Data", jsonString);
        Assert.assertEquals(json.size(), 1);
        json = jsonUtils.getJsonNode("Data2", jsonString);
        assertNull(json);
        String jsonString2 = "A";
        json = jsonUtils.getJsonNode("Data2", jsonString2);
        assertNull(json);
    }
    
    @Test
    public void getJsonAsMapTest() {
        String jsonString = "{\"switch\":true}";
        String jsonString2 = "A";
        Map map = jsonUtils.getJsonAsMap(jsonString);
        Assert.assertNotNull(map.get("switch"));
        map = jsonUtils.getJsonAsMap(jsonString2);
        assertNull(map);
    }
    
    @Test
    public void getObjectValueAsMapTest() {
        Inner inner = new Inner("A");
        Map map = jsonUtils.getObjectValueAsMap(inner);
        Assert.assertNotNull(map.get("val"));
    }
    
    @Test
    public void getValuesAsListTest() {
        String jsonString = "{\"Data\":{\"switch\":[\"A\",\"B\"]}}";
        JsonNode json = jsonUtils.getJsonNode("Data", jsonString);
        List list = jsonUtils.getValuesAsList(json, "switch");
        Assert.assertTrue(list.size() > 0);
    }
    
    @Test
    public void bindDataTest() throws JsonParseException, JsonMappingException, IOException {
        String jsonString = "{\"val\":\"test\"}";
        Inner inner = jsonUtils.bindData(jsonString, Inner.class);
        Assert.assertEquals(inner.getVal(), "test");
    }
    
    @Test
    public void testGetValueAsStringIoException() {
        assertNull(JsonUtils.getValueAsString("key", "[\"]"));
    }
    
    @Test
    public void testGetValueAsStringInvalidKey() {
        assertEquals("value", JsonUtils.getValueAsString("key", "{\"key\":\"value\"}"));
    }
    
    @Test
    public void testGetValueAsStringNullData() {
        assertNull(JsonUtils.safeGetStringFromJsonNode("key", null));
    }
    
}
