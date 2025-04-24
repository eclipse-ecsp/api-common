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

package org.eclipse.ecsp.rest;

import com.mongodb.MongoException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.constants.ResponseMsgConstants;
import org.eclipse.ecsp.domain.ExceptionResponse;
import org.eclipse.ecsp.exceptions.AssociationFailedException;
import org.eclipse.ecsp.exceptions.BadRequestException;
import org.eclipse.ecsp.exceptions.DisassociationFailedException;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.exceptions.ForbiddenException;
import org.eclipse.ecsp.exceptions.RequestPreconditionFailedException;
import org.eclipse.ecsp.exceptions.TooManyRequestException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * handles exception and convert them to meaningful api response.
 *
 * @author abhishekkumar
 */
@ControllerAdvice
public class RestControllerAdvice extends ResponseEntityExceptionHandler {
    private static final IgniteLogger IGNITE_LOGGER =
            IgniteLoggerFactory.getLogger(RestControllerAdvice.class);

    /**
     * This method convert generic exception to meaning response.
     *
     * @param e unexpected exception
     * @return response [{ "message": "actual error" }]
     */
    @ExceptionHandler({
        ConstraintViolationException.class,
        IllegalArgumentException.class,
        UnexpectedTypeException.class,
        ValidationException.class,
    })
    public ResponseEntity<List<ExceptionResponse>> handleUserException(final Exception e) {
        return genericHandle(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * convert {@link MethodArgumentTypeMismatchException} to meaningful api response.<br/>
     * status code: 400 Bad Request
     * response: [{ "message": "actual error" }]
     *
     * @param e actual error with message
     * @return response [{ "message": "actual error" }]
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<List<ExceptionResponse>> handleMethodArgumentTypeMismatchException(
            final MethodArgumentTypeMismatchException e) {
        try {
            String message = e.getMessage();
            if (!StringUtils.isEmpty(message)) {
                String[] str = message.split(":");
                Exception e1 = new Exception(
                        ResponseMsgConstants.INVALID_PARAM_VALUE + str[str.length - 1].replace("\"", ""));
                return genericHandle(e1, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            IGNITE_LOGGER.error("", ex);
        }
        return genericHandle(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * convert {@link MethodArgumentNotValidException} to meaningful api response.<br/>
     * This method is used when invalid structure passed for variable with in
     * domain object.
     * e.g limit: Invalid value provided.
     * status code: 400 Bad Request
     * response: [{ "message": "actual error" }]
     *
     * @param ex      actual error with message
     * @param headers available headers in the request
     * @param status  status code
     * @param request api request
     * @return response [{ "message": "actual error" }]
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        IGNITE_LOGGER.error("Exception encountered", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("\n"));
        return new ResponseEntity<>(getResponses(ex, message),
                HttpStatus.BAD_REQUEST);
    }


    /**
     * convert {@link HttpMessageNotReadableException} to meaningful api response.<br/>
     * This method is used when data provided is in invalid format. e.g when
     * string data provided for long value.
     * status code: 400 Bad Request
     * response: [{ "message": "actual error" }]
     *
     * @param ex      actual error with message
     * @param headers available headers in the request
     * @param status  status code
     * @param request api request
     * @return response [{ "message": "actual error" }]
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        IGNITE_LOGGER.error("Exception encountered: {}", ex.getMessage());
        ExceptionResponse response = new ExceptionResponse();
        if (ex.getMessage() != null) {
            if (!Objects.isNull(ex.getCause())) {
                response.setDetailedErrorCode(ex.getMessage());
                response.setMessage(ex.getCause().getMessage());
            } else {
                response.setMessage(ResponseMsgConstants.INVALID_PAYLOAD_MSG);
            }
        }
        List<ExceptionResponse> responses = new ArrayList<>();
        responses.add(response);
        return new ResponseEntity<>(responses,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * This method convert generic exception to meaning response.
     * <br/>
     * if {@link ExecutionException} then <br/>
     * status code: 503 Service Unavailable
     * response: [{ "message": "actual error" }]
     * <br/>
     * if {@link Exception} then<br/>
     * status code: 500 Internal Server Error
     * response: [{ "message": "actual error" }]
     *
     * @param ex unexpected exception
     * @return response [{ "message": "actual error" }]
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<ExceptionResponse>> handleException(Exception ex) {
        List<ExceptionResponse> response = buildExecutionResponse(ex);
        if (ex instanceof ExecutionException) {
            IGNITE_LOGGER.error("Exception encountered, Possible Timeout: {}", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
        }

        return genericHandle(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * convert {@link EmptyResponseException} to meaningful api response.<br/>
     * This method is used when data provided is not available.
     * status code: 404 Not Found
     * response: [{ "message": "actual error" }] - optional
     *
     * @param ex actual error with message
     * @return response if message is available [{ "message": "actual error" }]
     */
    @ExceptionHandler(EmptyResponseException.class)
    public ResponseEntity<List<ExceptionResponse>> handleEmptyResponseException(final Exception ex) {

        if (null != ex.getMessage() && !ex.getMessage().isEmpty()) {

            return new ResponseEntity<>(getResponses(ex, ex.getMessage()),
                    HttpStatus.NOT_FOUND);
        } else {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
    }

    /**
     * convert {@link DisassociationFailedException} to meaningful api response.<br/>
     * This method is used when disassociation fails.
     * status code: 500 Internal Server Error
     * response: [{ "message": "actual error" }]
     *
     * @param ex actual error with message
     * @return response if message is available [{ "message": "actual error" }]
     */
    @ExceptionHandler(DisassociationFailedException.class)
    public ResponseEntity<List<ExceptionResponse>> handleDisassociationFailedException(
            final Exception ex) {
        return new ResponseEntity<>(getResponses(ex, ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * convert {@link AssociationFailedException} to meaningful api response.<br/>
     * This method is used when association fails.
     * status code: 500 Internal Server Error
     * response: [{ "message": "actual error" }]
     *
     * @param ex actual error with message
     * @return response if message is available [{ "message": "actual error" }]
     */
    @ExceptionHandler(AssociationFailedException.class)
    public ResponseEntity<List<ExceptionResponse>> handleAssociationFailedException(
            final Exception ex) {
        return new ResponseEntity<>(getResponses(ex, ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * convert {@link BadRequestException} to meaningful api response.<br/>
     * This method is used when bad request is received.
     * status code: 400 Bad Request
     * response: [{ "message": "actual error" }]
     *
     * @param ex actual error with message
     * @return response if message is available [{ "message": "actual error" }]
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<List<ExceptionResponse>> handleBadRequestException(final Exception ex) {
        return new ResponseEntity<>(getResponses(ex, ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * convert {@link ForbiddenException} to meaningful api response.<br/>
     * This method is used when forbidden request is received.
     * status code: 403 Forbidden
     * response: [{ "message": "actual error" }]
     *
     * @param ex actual error with message
     * @return response if message is available [{ "message": "actual error" }]
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<List<ExceptionResponse>> handleForbiddenException(final Exception ex) {
        return new ResponseEntity<>(getResponses(ex, ex.getMessage()),
                HttpStatus.FORBIDDEN);
    }

    /**
     * convert {@link TooManyRequestException} to meaningful api response.<br/>
     * This method is used when too many request received.
     * status code: 429 Too Many Requests
     * response: [{ "message": "actual error" }]
     *
     * @param ex actual error with message
     * @return response if message is available [{ "message": "actual error" }]
     */
    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<List<ExceptionResponse>> handleTooManyRequestException(final Exception ex) {
        return new ResponseEntity<>(getResponses(ex, ex.getMessage()),
                HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * convert {@link RequestPreconditionFailedException} to meaningful api response.<br/>
     * This method is used when request precondition failed.
     * status code: 412 Precondition Failed
     * response: [{ "message": "actual error" }]
     *
     * @param ex actual error with message
     * @return response if message is available [{ "message": "actual error" }]
     */
    @ExceptionHandler(RequestPreconditionFailedException.class)
    public ResponseEntity<List<ExceptionResponse>> requestPreconditionFailedException(
            final Exception ex) {
        return new ResponseEntity<>(getResponses(ex, ex.getMessage()),
                HttpStatus.PRECONDITION_FAILED);
    }

    /**
     * convert {@link MongoException} to meaningful api response.<br/>
     * This method is used when interaction with mongoDB result in failure.
     * status code: 500 Internal Server Error
     * response: [{ "message": "Failed to receive Command - actual error" }]
     *
     * @param ex actual error with message
     * @return response if message is available<br/>
     *     [{ "message": "Failed to receive Command - actual error" }]
     */
    @ExceptionHandler(MongoException.class)
    public ResponseEntity<List<ExceptionResponse>> handleMongoException(Exception ex) {
        IGNITE_LOGGER.error("Error Saving Command to Mongo: {}", ex.getMessage());
        return new ResponseEntity<>(
                getResponses(ex, ResponseMsgConstants.COMMAND_FAIL),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<List<ExceptionResponse>> genericHandle(Exception e, HttpStatus status) {
        IGNITE_LOGGER.error("Exception encountered: {}", e);
        return new ResponseEntity<>(buildExecutionResponse(e), status);
    }

    private List<ExceptionResponse> buildExecutionResponse(Exception e) {
        ExceptionResponse response = new ExceptionResponse();
        if (!Objects.isNull(e.getCause())) {
            response.setDetailedErrorCode(e.getMessage());
            response.setMessage(e.getCause().getMessage());
            return Collections.singletonList(response);
        }
        response.setMessage(e.getMessage());
        return Collections.singletonList(response);
    }

    private List<ExceptionResponse> getResponses(Exception e, String message) {
        ExceptionResponse response = new ExceptionResponse();
        if (!Objects.isNull(e.getCause())) {
            response.setDetailedErrorCode(e.getMessage());
            response.setMessage(e.getCause().getMessage());
            return Collections.singletonList(response);
        }
        response.setMessage(message);
        return Collections.singletonList(response);
    }
}