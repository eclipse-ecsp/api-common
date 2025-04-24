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

package org.eclipse.ecsp;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.ecsp.domain.ExceptionResponse;
import org.eclipse.ecsp.exceptions.AssociationFailedException;
import org.eclipse.ecsp.rest.RestControllerAdvice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

/**
 * Test cases to handle exception and convert them to meaningful api response.
 *
 * @author abhishekkumar
 */
@RunWith(JUnit4.class)
public class RestControllerAdviceUnitTest {

    private RestControllerAdvice restControllerAdvice;
    
    /**
     * preparing {@link RestControllerAdvice}.
     */
    @Before
    public void setup() {
        restControllerAdvice = new RestControllerAdvice();
        CollectorRegistry.defaultRegistry.clear();
    }
    
    @Test
    public void testHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex =
            Mockito.mock(MethodArgumentTypeMismatchException.class);
        doThrow(RuntimeException.class).doReturn("").when(ex).getMessage();
        doReturn(new RuntimeException("cause")).when(ex).getCause();
        ResponseEntity<List<ExceptionResponse>> response =
            restControllerAdvice.handleMethodArgumentTypeMismatchException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    public void testHandleMethodArgumentTypeMismatchExceptionWithoutCause() {
        MethodArgumentTypeMismatchException ex =
            Mockito.mock(MethodArgumentTypeMismatchException.class);
        doThrow(RuntimeException.class).doReturn("").when(ex).getMessage();
        ResponseEntity<List<ExceptionResponse>> response =
            restControllerAdvice.handleMethodArgumentTypeMismatchException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    public void testHandleAssociationFailedExceptionWithoutCause() {
        AssociationFailedException associationFailedException =
            new AssociationFailedException("message");
        ResponseEntity<List<ExceptionResponse>> response =
            restControllerAdvice.handleAssociationFailedException(associationFailedException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
    
    @Test
    public void testHandleAssociationFailedException() {
        AssociationFailedException associationFailedException =
            new AssociationFailedException("message", "cause");
        ResponseEntity<List<ExceptionResponse>> response =
            restControllerAdvice.handleAssociationFailedException(associationFailedException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
