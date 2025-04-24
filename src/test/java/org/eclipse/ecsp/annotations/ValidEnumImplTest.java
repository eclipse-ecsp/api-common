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

import jakarta.validation.ConstraintValidatorContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * test custom annotation {@link ValidEnum} and its implementation.
 *
 * @author abhishekkumar
 */
@RunWith(JUnit4.class)
public class ValidEnumImplTest {
    private ValidEnumImpl validEnumImpl;
    
    private ValidEnum validEnum;
    private ConstraintValidatorContext constraintValidatorContext;
    
    /**
     * setup test and create intance of {@link ValidEnumImpl}.
     */
    @Before
    public void setup() {
        validEnumImpl = new ValidEnumImpl();
        validEnum = Mockito.mock(ValidEnum.class);
        constraintValidatorContext = Mockito.mock(ConstraintValidatorContext.class);
    }
    
    @Test
    public void testValidEnum() {
        doReturn(ExampleClazz.State.class).when(validEnum).enumClazz();
        validEnumImpl.initialize(validEnum);
        
        boolean result = validEnumImpl.isValid("ACTIVE", constraintValidatorContext);
        assertTrue(result);
    }
    
    @Test
    public void testInValidEnum() {
        doReturn(ExampleClazz.State.class).when(validEnum).enumClazz();
        validEnumImpl.initialize(validEnum);
        
        boolean result = validEnumImpl.isValid("ACTIVE123", constraintValidatorContext);
        assertFalse(result);
    }
    
    static class ExampleClazz {
        @ValidEnum(enumClazz = State.class)
        public String state;
        
        public enum State {
            ACTIVE, PENDING
        }
    }
}
