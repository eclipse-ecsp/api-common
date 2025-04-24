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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import jakarta.servlet.ServletContext;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.SpringProperties;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@link Application} class is an entry point for the spring boot application <br/>
 * which contains main method to initialize spring appliaction.
 *
 * @author abhishekkumar
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@PropertySources({
    @PropertySource(value = "classpath:/application-base.properties"),
    @PropertySource(ignoreResourceNotFound = true, value = "classpath:/application.properties")
})
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
public class Application extends SpringBootServletInitializer {
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(Application.class);

    /**
     * initialize application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        configureSpringBootBuilder(new SpringApplicationBuilder()).run(args);
    }
    
    private static SpringApplicationBuilder configureSpringBootBuilder(
        SpringApplicationBuilder springApplicationBuilder) {
        return springApplicationBuilder.sources(Application.class).bannerMode(Mode.OFF)
            .web(WebApplicationType.SERVLET).registerShutdownHook(true);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        SpringProperties.setFlag(JndiLocatorDelegate.IGNORE_JNDI_PROPERTY_NAME);
        return configureSpringBootBuilder(builder);
    }
    
    @Override
    protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
        if (servletContext.getContextPath().length() > 0) {
            String activeProfile = servletContext.getContextPath().substring(1);
            LOGGER.info("Setting current profile to {}", activeProfile);
            servletContext.setInitParameter("spring.profiles.active", activeProfile);
        }
        return super.createRootApplicationContext(servletContext);
    }

    /**
     * create instance of {@link ConversionService}.
     *
     * @return instance of {@link ConversionService}
     */
    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
    
    /**
     * This method create add Metrics Gauges for prometheus below metrics gauges.<br/>
     * <b>api-gc</b>
     * <b>api-memory</b>
     * <b>api-threads</b>
     *
     * @return com.codahale.metrics.MetricRegistry
     */
    @Bean
    public MetricRegistry metricRegistry() {
        MetricRegistry registry = new MetricRegistry();
        registry.register("api-gc", new GarbageCollectorMetricSet());
        registry.register("api-memory", new MemoryUsageGaugeSet());
        registry.register("api-threads", new ThreadStatesGaugeSet());
        return registry;
    }
}