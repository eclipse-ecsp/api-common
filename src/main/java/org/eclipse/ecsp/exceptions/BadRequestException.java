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

package org.eclipse.ecsp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@link BadRequestException} is used to represent an invalid request status.<br/>
 * propagate same response in the api with status code 400 - Bad Request
 *
 * @author Abhishek Kumar
 */

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends Exception {
    static final long serialVersionUID = -3387516993334229948L;

    /**
     * create instance with message.
     *
     * @param message error message
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * create instance with error message and cause.
     *
     * @param message error message
     * @param cause error cause
     */
    public BadRequestException(String message, String cause) {
        super(message, new Throwable(cause));
    }

    /**
     * create instance with error message and cause.
     *
     * @param message error message
     * @param cause error cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
