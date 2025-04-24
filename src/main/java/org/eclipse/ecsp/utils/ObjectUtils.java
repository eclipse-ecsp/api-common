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

import java.util.Collection;
import java.util.Objects;

/**
 * Utility class for java objects.
 *
 * @author abhishekkumar
 */
public class ObjectUtils {

    /**
     * private constructor to avoid instantiation.
     */
    private ObjectUtils() {
    }

    /**
     * checks if the object is null then throws NullPointerException.<br/>
     * if the instance of the object is string <br/>
     * then checks if string is empty and throws RuntimeException.
     *
     * @param obj      object to check for null and empty
     * @param errorMsg error message to be sent with exception.
     * @param <T>      type of the object
     * @return object if validation passed
     */
    public static <T> T requireNonEmpty(T obj, String errorMsg) {
        obj = Objects.requireNonNull(obj, errorMsg);
        if (obj instanceof String str && str.isEmpty()) {
            throw new RuntimeException(errorMsg);
        }
        return obj;
    }

    /**
     * checks if the collection size if matches with the expected size.
     *
     * @param t            collection of objects.
     * @param expectedSize expected object size in the collection
     * @param errorMsg     error message in the RuntimeException <br/>
     *                     if collection doesn't match with expected size
     * @param <T>          generic type of the object in the collection.
     * @return true if expected size matches with collection size
     */
    public static <T> boolean requireSizeOf(Collection<T> t, int expectedSize, String errorMsg) {
        if (t.size() != expectedSize) {
            throw new RuntimeException(errorMsg);
        }
        return true;
    }

    /**
     * checks if the object is null then throws NullPointerException.
     *
     * @param obj      object to check for null
     * @param errorMsg error message to be sent with exception.
     * @param <T>      type of the object
     * @return object if validation passed
     */
    public static <T> T requireNonNull(T obj, String errorMsg) {
        return Objects.requireNonNull(obj, errorMsg);
    }

    /**
     * checks if the collection size if matches with the expected minimum size.
     *
     * @param t            collection of objects.
     * @param expectedSize expected object size in the collection
     * @param errorMsg     error message in the RuntimeException <br/>
     *                     if collection doesn't match with expected minimum size
     * @param <T>          generic type of the object in the collection.
     * @return true if expected minimum size matches with collection size
     */
    public static <T> boolean requireMinSize(Collection<T> t, int expectedSize, String errorMsg) {
        if (t.size() < expectedSize) {
            throw new RuntimeException(errorMsg);
        }
        return true;
    }

    /**
     * Checks if the provided collection is null or empty.
     *
     * @param t        collection to check if its null or empty
     * @param errorMsg message in the RuntimeException if the
     * @param <T>      generic type of the object in the collection
     * @return true if not null and empty , otherwise throws NullPointerException or RuntimeException
     */
    public static <T> boolean requiresNotNullAndNotEmpy(Collection<T> t, String errorMsg) {
        t = Objects.requireNonNull(t, errorMsg);
        if (t.isEmpty()) {
            throw new RuntimeException(errorMsg);
        }
        return true;
    }
}