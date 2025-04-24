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

package org.eclipse.ecsp.domain;

/**
 * Pojo for creating meaningful error response, used in RestControllerAdvice.<br/>
 * <p>message: contains the error message<br/>
 * detailedErrorCode:  contains details error code
 * </p>
 *
 * @author abhishekkumar
 */
public class ExceptionResponse {
    private String message;
    private String detailedErrorCode;

    /**
     * This method is a getter for detailederrorcode.
     *
     * @return String
     */
    
    public String getDetailedErrorCode() {
        return detailedErrorCode;
    }

    /**
     * This method is a setter for detailederrorcode.
     *
     * @param detailedErrorCode : String
     */
    
    public void setDetailedErrorCode(String detailedErrorCode) {
        this.detailedErrorCode = detailedErrorCode;
    }

    /**
     * This method is a getter for message.
     *
     * @return String
     */
    
    public String getMessage() {
        return message;
    }

    /**
     * This method is a setter for message.
     *
     * @param message : String
     */
    
    public void setMessage(String message) {
        this.message = message;
    }
}