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

package org.eclipse.ecsp.testutils;

import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.utils.ApiPaginationUtils;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ApiPaginationUtilsTest contains the test cases for {@link ApiPaginationUtils}.
 *
 * @author abhishekkumar
 */
public class ApiPaginationUtilsTest {

    /**
     * SINCE.
     */
    public static final long SINCE = 100000L;

    /**
     * UNTIL.
     */
    public static final long UNTIL = 20000L;

    /**
     * DEFAULT_PAGE_SIZE_20.
     */
    public static final int DEFAULT_PAGE_SIZE_20 = 20;

    /**
     * RESPONSES_LIMIT_30.
     */
    public static final int RESPONSES_LIMIT_30 = 30;

    /**
     * RESPONSE_SIZE_20.
     */
    public static final int RESPONSE_SIZE_20 = 20;
    private ApiPaginationUtils paginationUtils =
        new ApiPaginationUtils("eventIdKey", "timestampKey", "vehicleIdKey");
    
    @Test
    public void testBuildBasicCriteriaGroup() {
        IgniteCriteriaGroup group =
            paginationUtils.buildBasicCriteriaGroup("1111",
                SINCE,
                UNTIL,
                "eventId");
        assertEquals(
            "((vehicleIdKey=1111)and"
                +
                "(timestampKey>=100000)and(timestampKey<=20000)and(eventIdKey=eventId))",
            group.toString());
        group = paginationUtils.buildBasicCriteriaGroup("1111",
            SINCE,
            UNTIL,
            "");
        assertEquals(
            "((vehicleIdKey=1111)and"
                +
                "(timestampKey>=100000)and(timestampKey<=20000)and(eventIdKey!=Acknowledgement))",
            group.toString());
    }
    
    @Test(expected = RuntimeException.class)
    public void testBuildIgniteQuery_Exception() {
        IgniteCriteriaGroup group =
            paginationUtils.buildBasicCriteriaGroup("1111",
                SINCE,
                UNTIL,
                "eventId");
        IgniteCriteriaGroup groupCopy =
            paginationUtils.buildBasicCriteriaGroup("1111",
                SINCE,
                UNTIL,
                "eventId");
        paginationUtils.buildIgniteQuery(group, groupCopy, "desc");
    }
    
    @Test
    public void testBuildIgniteQuery_desc() {
        IgniteCriteriaGroup group =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        IgniteCriteriaGroup groupCopy =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        paginationUtils.setObjectId("1212");
        IgniteQuery igniteQuery = paginationUtils.buildIgniteQuery(group, groupCopy, "desc");
        assertEquals(
            "(((vehicleIdKey=1111)and(timestampKey>=100000)"
                +
                "and(timestampKey<=20000)and(eventIdKey=eventId)and(timestampKey<0))"
                +
                "or((vehicleIdKey=1111)and(timestampKey>=100000)and(timestampKey<=20000)"
                +
                "and(eventIdKey=eventId)and(timestampKey=0)and(_id<1212)))",
            igniteQuery.toString());
    }
    
    @Test
    public void testBuildIgniteQuery_asc() {
        IgniteCriteriaGroup group =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        IgniteCriteriaGroup groupCopy =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        paginationUtils.setObjectId("1212");
        IgniteQuery igniteQuery = paginationUtils.buildIgniteQuery(group, groupCopy, "asc");
        assertEquals(
            "(((vehicleIdKey=1111)and(timestampKey>=100000)"
                +
                "and(timestampKey<=20000)and(eventIdKey=eventId)and(timestampKey>0))"
                +
                "or((vehicleIdKey=1111)and(timestampKey>=100000)and(timestampKey<=20000)"
                +
                "and(eventIdKey=eventId)and(timestampKey=0)and(_id<1212)))",
            igniteQuery.toString());
    }
    
    @Test
    public void testBuildSortByAndLimit_desc() {
        IgniteCriteriaGroup group =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        IgniteCriteriaGroup groupCopy =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        paginationUtils.setObjectId("1212");
        IgniteQuery igniteQuery = paginationUtils.buildIgniteQuery(group, groupCopy, "desc");
        paginationUtils.buildSortByAndLimit(igniteQuery, "desc", null, DEFAULT_PAGE_SIZE_20);
        assertEquals(
            "(((vehicleIdKey=1111)"
                + "and(timestampKey>=100000)"
                + "and(timestampKey<=20000)"
                + "and(eventIdKey=eventId)"
                + "and(timestampKey<0))"
                + "or((vehicleIdKey=1111)"
                + "and(timestampKey>=100000)"
                + "and(timestampKey<=20000)"
                + "and(eventIdKey=eventId)"
                + "and(timestampKey=0)"
                + "and(_id<1212)))",
            igniteQuery.toString());
        assertEquals(1, igniteQuery.getPageNumber());
        assertEquals(RESPONSE_SIZE_20, igniteQuery.getPageSize());
        assertFalse(igniteQuery.getOrderBys().isEmpty());
    }
    
    @Test
    public void testBuildSortByAndLimit_asc() {
        IgniteCriteriaGroup group =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        IgniteCriteriaGroup groupCopy =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        paginationUtils.setObjectId("1212");
        IgniteQuery igniteQuery = paginationUtils.buildIgniteQuery(group, groupCopy, "asc");
        paginationUtils.buildSortByAndLimit(igniteQuery, "asc", RESPONSES_LIMIT_30, DEFAULT_PAGE_SIZE_20);
        assertEquals(
            "(((vehicleIdKey=1111)"
                + "and(timestampKey>=100000)"
                + "and(timestampKey<=20000)"
                + "and(eventIdKey=eventId)"
                + "and(timestampKey>0))"
                + "or((vehicleIdKey=1111)"
                + "and(timestampKey>=100000)"
                + "and(timestampKey<=20000)"
                + "and(eventIdKey=eventId)"
                + "and(timestampKey=0)"
                + "and(_id<1212)))",
            igniteQuery.toString());
        assertEquals(1, igniteQuery.getPageNumber());
        assertEquals(RESPONSES_LIMIT_30, igniteQuery.getPageSize());
        assertFalse(igniteQuery.getOrderBys().isEmpty());
    }
    
    @Test
    public void testBuildBasicCriteriaGroupWithTimestamp() {
        IgniteCriteriaGroup group =
            paginationUtils.buildBasicCriteriaGroup("1111", SINCE, UNTIL, "eventId");
        assertEquals(
            "((vehicleIdKey=1111)"
                + "and(timestampKey>=100000)"
                + "and(timestampKey<=20000)"
                + "and(eventIdKey=eventId))",
            group.toString());
        paginationUtils.setTimestamp(SINCE);
        group = paginationUtils.buildBasicCriteriaGroup("1111", 0L, 0L, "");
        assertTrue(group.toString().contains("((vehicleIdKey=1111)"));
        assertTrue(group.toString().contains("and(eventIdKey!=Acknowledgement))"));
    }
}