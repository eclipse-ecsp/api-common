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

import com.mongodb.MongoException;
import io.prometheus.client.CollectorRegistry;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.eclipse.ecsp.exceptions.AssociationFailedException;
import org.eclipse.ecsp.exceptions.BadRequestException;
import org.eclipse.ecsp.exceptions.DisassociationFailedException;
import org.eclipse.ecsp.exceptions.EmptyResponseException;
import org.eclipse.ecsp.exceptions.ForbiddenException;
import org.eclipse.ecsp.exceptions.RequestPreconditionFailedException;
import org.eclipse.ecsp.exceptions.TooManyRequestException;
import org.eclipse.ecsp.rest.RestControllerAdvice;
import org.eclipse.ecsp.test.controllers.DemoEventController;
import org.eclipse.ecsp.test.domains.DemoEventRequest;
import org.eclipse.ecsp.test.services.DemoService;
import org.eclipse.ecsp.utils.JsonUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * controller advice to test failure scenarios.
 *
 * @author abhishekkumar
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DemoEventController.class})
@WebAppConfiguration
public class RestControllerAdviceTest {

    /**
     * INT_2.
     */
    public static final int INT_2 = 2;
    private MockMvc mockMvc;
    
    @InjectMocks
    private DemoEventController controller = new DemoEventController();
    
    @Mock
    private DemoService service;
    
    /**
     * setting up controller to test api.
     */
    @Before
    public void setup() {
        CollectorRegistry.defaultRegistry.clear();
        MockitoAnnotations.initMocks(this);
        final ExceptionHandlerExceptionResolver exceptionResolver =
            new ExceptionHandlerExceptionResolver();
        final StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerBeanDefinition("exceptionController",
            new RootBeanDefinition(RestControllerAdvice.class));
        exceptionResolver.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        exceptionResolver.setApplicationContext(applicationContext);
        exceptionResolver.afterPropertiesSet();
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setHandlerExceptionResolvers(exceptionResolver).build();
    }
    
    @Test
    public void testNoExceptionsWithProperJson() throws Exception {
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        
        mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isAccepted());
    }
    
    @Test
    public void testHttpMessageNotReadableExceptionOccursWithInvalidJson() throws Exception {
        String json = "InvalidJsonFormat";
        MvcResult result = mockMvc.perform(post("/v1.0/demo")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(HttpMessageNotReadableException.class));
    }
    
    @Test
    public void testMethodArgumentNotValidExceptionOccursWithMandatoryParamMissingInJson()
        throws Exception {
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"foo\":\"ABCDEFG\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("[{\"message\":\"msisdn: must not be empty\"}]")).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(MethodArgumentNotValidException.class));
    }
    
    @Test
    public void testMethodArgumentTypeMismatchExceptionOccursWithWrongTypeInJson() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/v1.0/demo/ABC"))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("[{\"message\":\"Invalid parameter value :  ABC\"}]"))
            .andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(MethodArgumentTypeMismatchException.class));
    }
    
    @Test
    public void testConstraintViolationExceptionoccursWithConstraintViolationsInJson()
        throws Exception {
        DemoEventRequest eventRequest = new DemoEventRequest(null, -INT_2);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<DemoEventRequest>> violations = validator.validate(eventRequest);
        when(service.createDemoEventRequest()).thenThrow(new ConstraintViolationException(violations));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .contentType(MediaType.APPLICATION_JSON)
                    // content here does not matter for getting error,
                    // just give some valid content so that the
                    // service method is invoked and above mocked
                    // violation exception kicks in
                    .content("{\"msisdn\":\"ABC\", \"id\":\"-2\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.[0].message", Matchers.containsString("msisdn: must not be empty")))
            .andExpect(jsonPath("$.[0].message",
                Matchers.containsString("id: must be greater than or equal to 0"))).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(ConstraintViolationException.class));
    }
    
    @Test
    public void testInternalServerErrorOccursWithUnexpectedException() throws Exception {
        class FooBarException extends RuntimeException {
            private static final long serialVersionUID = 1L;
            
            FooBarException(String message) {
                super(message);
            }
        }
        
        when(service.createDemoEventRequest()).thenThrow(new FooBarException("Baz qux"));
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(jsonPath("$.[0].message", Matchers.containsString("Baz qux")))
            .andExpect(status().is5xxServerError()).andReturn();
        assertThat(result.getResolvedException().getClass(), typeCompatibleWith(FooBarException.class));
    }
    
    @Test
    public void testEmptyResponseException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new EmptyResponseException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().is4xxClientError()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(EmptyResponseException.class));
    }
    
    @Test
    public void testDisassociationFailedException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new DisassociationFailedException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isInternalServerError()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(DisassociationFailedException.class));
    }
    
    @Test
    public void testAssociationFailedException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new AssociationFailedException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isInternalServerError()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(AssociationFailedException.class));
    }
    
    @Test
    public void testBadRequestException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new BadRequestException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(BadRequestException.class));
    }
    
    @Test
    public void testForbiddenException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new ForbiddenException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isForbidden()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(ForbiddenException.class));
    }
    
    @Test
    public void testTooManyRequestException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new TooManyRequestException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isTooManyRequests()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(TooManyRequestException.class));
    }
    
    @Test
    public void testRequestPreconditionFailedException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new RequestPreconditionFailedException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isPreconditionFailed()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(RequestPreconditionFailedException.class));
    }
    
    @Test
    public void testMongoException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new MongoException("Something went wrong");
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isInternalServerError()).andReturn();
        assertThat(result.getResolvedException().getClass(), typeCompatibleWith(MongoException.class));
    }
    
    @Test
    public void testExecutionException() throws Exception {
        Mockito.doAnswer(
                invocation -> {
                    throw new ExecutionException(new Throwable());
                })
            .when(service)
            .createDemoEventRequest();
        String json = JsonUtils.getObjectValueAsString(new DemoEventRequest("ABCDEFG", INT_2));
        MvcResult result = mockMvc.perform(
                post("/v1.0/demo")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isServiceUnavailable()).andReturn();
        assertThat(result.getResolvedException().getClass(),
            typeCompatibleWith(ExecutionException.class));
    }
}