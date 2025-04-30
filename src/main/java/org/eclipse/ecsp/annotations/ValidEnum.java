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
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * custom annotation to valid the value is a valid enum type.
 *
 * @author Abhishek Kumar
 */
@Documented
@Constraint(validatedBy = ValidEnumImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@NotNull(message = "Value cannot be null")
@ReportAsSingleViolation
public @interface ValidEnum {
    /**
     * enum class.
     *
     * @return enum class type.
     */
    @SuppressWarnings("java:S1452")
    Class<? extends Enum<?>> enumClazz();
    
    /**
     * message if the value if invalid.
     * default: Value is not valid
     *
     * @return actual message.
     */
    String message() default "Value is not valid";
    
    /**
     * enum class type.
     *
     * @return enum class
     */
    Class<?>[] groups() default {};
    
    /**
     * actual enum value.
     *
     * @return value.
     */
    Class<? extends Payload>[] payload() default {};
}
