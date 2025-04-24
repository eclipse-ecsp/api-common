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

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link ObjectUtilsTest} contains test cases for {@link ObjectUtils}.
 *
 * @author abhishekkumar
 */
public class ObjectUtilsTest {
    

    /**
     * INT_2.
     */
    public static final int INT_2 = 2;
    
    @Test(expected = RuntimeException.class)
    public void requireNonEmptyTest() {
        String str1 = "A";
        String str2 = "";
        Assert.assertNotNull(ObjectUtils.requireNonEmpty(str1, "errorMsg"));
        ObjectUtils.requireNonEmpty(str2, "errorMsg");
        Assert.assertNotNull(ObjectUtils.requireNonNull(str1, "errorMsg"));
    }
    
    @Test(expected = RuntimeException.class)
    public void requireSizeOfTest() {
        List list = Arrays.asList("A");
        Assert.assertTrue(ObjectUtils.requireSizeOf(list, 1, "errorMsg"));
        ObjectUtils.requireSizeOf(list, INT_2, "errorMsg");
    }
    
    @Test(expected = RuntimeException.class)
    public void requireMinSizeTest() {
        List list = Arrays.asList("A");
        Assert.assertTrue(ObjectUtils.requireMinSize(list, 1, "errorMsg"));
        ObjectUtils.requireMinSize(list, INT_2, "errorMsg");
    }
    
    @Test(expected = RuntimeException.class)
    public void requiresNotNullAndNotEmpyTest() {
        List list = Arrays.asList("A");
        Assert.assertTrue(ObjectUtils.requiresNotNullAndNotEmpy(list, "errorMsg"));
        list = new ArrayList<>();
        ObjectUtils.requiresNotNullAndNotEmpy(list, "errorMsg");
    }
    
    
}