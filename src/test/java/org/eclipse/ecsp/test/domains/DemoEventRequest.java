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

package org.eclipse.ecsp.test.domains;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

/**
 * test pojo.
 *
 * @author abhishekkumar
 */
public class DemoEventRequest {
    @NotEmpty
    private String msisdn;
    
    @Min(0)
    @Max(100)
    private int id = 1;
    
    DemoEventRequest() {
    
    }
    
    public DemoEventRequest(String msisdn, int id) {
        this.msisdn = msisdn;
        this.id = id;
    }
    
    DemoEventRequest(String msisdn) {
        this.msisdn = msisdn;
    }

    /**
     * This method is a getter for msisdn.
     *
     * @return String
     */
    
    public String getMsisdn() {
        return msisdn;
    }

    /**
     * This method is a setter for msisdn.
     *
     * @param msisdn : String
     */
    
    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }
}