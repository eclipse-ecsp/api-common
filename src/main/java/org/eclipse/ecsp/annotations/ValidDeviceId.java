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

package org.eclipse.ecsp.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import org.eclipse.ecsp.constants.ResponseMsgConstants;
import org.intellij.lang.annotations.Pattern;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * custom annotation to validate deviceId.
 * specifies where this validation can be used (Field, Method, Parameter etc)
 *
 * @author Abhishek Kumar
 */
@Target({METHOD, FIELD, PARAMETER, CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Pattern("[a-zA-Z0-9]+")
public @interface ValidDeviceId {
    /**
     * validation failed response.
     * default: Received invalid device id in URI
     *
     * @return response if invalid deviceId
     */
    String message() default ResponseMsgConstants.INVALID_DEVICE_ID_MSG;
    
    /**
     * instance type of field.
     *
     * @return class of the
     */
    Class<?>[] groups() default {};
    
    /**
     * payload of the field.
     *
     * @return payload.
     */
    Class<? extends Payload>[] payload() default {};
}
