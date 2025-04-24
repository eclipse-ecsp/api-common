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
 * {@link TooManyRequestException} is used to represent too many request api status.<br/>
 * propagate same response in the api with status code 429 - Too Many Requests
 *
 * @author abhishekkumar
 */
@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestException extends Exception {
    
    private static final long serialVersionUID = -2688334724032417291L;

    /**
     * create instance with error message.
     *
     * @param message error message.
     */
    public TooManyRequestException(String message) {
        super(message);
    }

    /**
     * create instance with error message.
     *
     * @param message error message.
     * @param cause error message.
     */
    public TooManyRequestException(String message, String cause) {
        super(message, new Throwable(cause));
    }
    
}
