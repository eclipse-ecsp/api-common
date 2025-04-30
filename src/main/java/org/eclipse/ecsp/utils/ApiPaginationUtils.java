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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.constants.Constants;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteOrderBy;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;

/**
 * ApiPaginationUtils contains the common logic <br/>
 * to create query to fetch the list of record in pagination manner.
 *
 * @author Abhishek Kumar
 */
public class ApiPaginationUtils {
    private static final String MONGO_COLLECTION_ID = "_id";
    private String eventIdKey;
    private String timestampKey;
    private String vehicleIdKey;
    private long timestamp;
    private Object objectId;
    
    /**
     * Constructor to create ApiPaginationUtils object.
     *
     * @param eventIdKey   event id key to be match with record in db.
     * @param timestampKey timestamp key to be match with record in db.
     * @param vehicleIdKey vehicle id key to be match with record in db.
     */
    public ApiPaginationUtils(String eventIdKey, String timestampKey, String vehicleIdKey) {
        this.eventIdKey = eventIdKey;
        this.timestampKey = timestampKey;
        this.vehicleIdKey = vehicleIdKey;
    }
    
    /**
     * Method to create criteria query.
     *
     * @param vehicleId actual vehicle id to be mapped with vehicle id key
     * @param since     timestamp to be mapped with timestamp key with GTE condition<br/>
     *                  if null or zero
     * @param until     timestamp to be mapped with timestamp key with LTE condition<br/>
     *                  if null or zero
     * @param eventId   event id to be mapped with eventId key,
     *                  if null/empty then it assigned event id as  ACKNOWLEDGEMENT with NEQ condition
     * @return {@link IgniteCriteriaGroup}
     */
    public IgniteCriteriaGroup buildBasicCriteriaGroup(String vehicleId, Long since, Long until,
                                                       String eventId) {
        IgniteCriteria vehicleIdCriteria =
            new IgniteCriteria(this.vehicleIdKey, Operator.EQ, vehicleId);
        IgniteCriteriaGroup basicCriteriaGroup = new IgniteCriteriaGroup(vehicleIdCriteria);
        
        if (since != null && since.longValue() > org.eclipse.ecsp.constants.Constants.ZERO) {
            basicCriteriaGroup.and(new IgniteCriteria(this.timestampKey, Operator.GTE, since));
        }
        
        basicCriteriaGroup.and(new IgniteCriteria(this.timestampKey, Operator.LTE,
            (until != null && until.longValue() > Constants.ZERO) ? until.longValue() :
                System.currentTimeMillis()));
        
        if (StringUtils.isNotEmpty(eventId)) {
            basicCriteriaGroup.and(new IgniteCriteria(this.eventIdKey, Operator.EQ, eventId));
        } else {
            basicCriteriaGroup.and(
                new IgniteCriteria(this.eventIdKey, Operator.NEQ, EventID.ACKNOWLEDGEMENT));
        }
        
        return basicCriteriaGroup;
    }
    
    /**
     * Add sort by field with asc or desc and limit on the query {@link IgniteQuery}.
     *
     * @param igQuery         {@link IgniteQuery} in which the sort and limit to be added
     * @param sortOrder       if value matches desc then desc is set otherwise asc
     * @param responsesLimit  used to limit the record in the query result
     * @param defaultPageSize used to limit the record in the query result, if response limit is null
     */
    public void buildSortByAndLimit(IgniteQuery igQuery, String sortOrder, Integer responsesLimit,
                                    int defaultPageSize) {
        // build sort order
        if (Constants.DESC_ORDER.equalsIgnoreCase(sortOrder)) {
            igQuery.orderBy(new IgniteOrderBy().byfield(this.timestampKey).desc());
        } else {
            igQuery.orderBy(new IgniteOrderBy().byfield(this.timestampKey).asc());
        }
        igQuery.orderBy(new IgniteOrderBy().byfield(MONGO_COLLECTION_ID).desc());
        // build limit
        if (responsesLimit == null) {
            igQuery.setPageSize(defaultPageSize);
        } else {
            igQuery.setPageSize(responsesLimit.intValue());
        }
        igQuery.setPageNumber(Constants.DEFAULT_PAGE_NUMBER);
    }
    
    /**
     * Build {@link IgniteQuery} using criteria groups.
     *
     * @param basicCriteriaGroup     criteria groups to be added with sort order
     * @param basicCriteriaGroupCopy criteria group to be added with <br/>
     *                               OR condition in {@link IgniteQuery}
     * @param sortOrder              sort order in the {@link IgniteQuery}
     * @return {@link IgniteQuery}
     */
    public IgniteQuery buildIgniteQuery(IgniteCriteriaGroup basicCriteriaGroup,
                                        IgniteCriteriaGroup basicCriteriaGroupCopy,
                                        String sortOrder) {
        if (this.getObjectId() == null) {
            throw new IllegalStateException(Constants.ERROR_NULL_OBJECT_ID);
        }
        IgniteCriteria timestampCriteria;
        if (Constants.DESC_ORDER.equalsIgnoreCase(sortOrder)) {
            timestampCriteria = new IgniteCriteria(this.timestampKey, Operator.LT, this.getTimestamp());
        } else {
            timestampCriteria = new IgniteCriteria(this.timestampKey, Operator.GT, this.getTimestamp());
        }
        
        basicCriteriaGroup.and(timestampCriteria);
        
        IgniteCriteria equalTimestampCriteria =
            new IgniteCriteria(this.timestampKey, Operator.EQ, this.getTimestamp());
        IgniteCriteria idCriteria =
            new IgniteCriteria(MONGO_COLLECTION_ID, Operator.LT, this.getObjectId());
        
        basicCriteriaGroupCopy.and(equalTimestampCriteria).and(idCriteria);
        
        return new IgniteQuery(basicCriteriaGroup).or(basicCriteriaGroupCopy);
    }

    /**
     * This method is a getter for timestamp.
     *
     * @return long
     */
    
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * This method is a setter for timestamp.
     *
     * @param timestamp : long
     */
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * This method is a getter for objectid.
     *
     * @return Object
     */
    
    public Object getObjectId() {
        return objectId;
    }

    /**
     * This method is a setter for objectid.
     *
     * @param objectId : Object
     */
    
    public void setObjectId(Object objectId) {
        this.objectId = objectId;
    }
    
    
}