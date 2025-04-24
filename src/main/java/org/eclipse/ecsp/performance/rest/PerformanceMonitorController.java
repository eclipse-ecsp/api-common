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

package org.eclipse.ecsp.performance.rest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import jakarta.servlet.ServletContext;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

/**
 * export api to fetch performance metrics.
 *
 * @author abhishekkumar
 */
@RestController
public class PerformanceMonitorController {
    
    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(PerformanceMonitorController.class);

    /**
     * A_DOUBLE.
     */
    public static final double A_DOUBLE = 1000000.0D;

    /**
     * INT_4.
     */
    public static final int INT_4 = 4;

    /**
     * INT_16.
     */
    public static final int INT_16 = 16;

    /**
     * INT_2.
     */
    public static final int INT_2 = 2;
    
    @Autowired
    private MetricRegistry metricRegistry;
    @Autowired
    private ServletContext context;
    private java.text.DecimalFormat numberFormatter = new DecimalFormat("###.##");
    
    /**
     * export api to fetch metrics for particular api or all.
     *
     * @param api endpoint to fetch metrics for
     * @return metrics in html format
     */
    @GetMapping(path = "/v1/jamon-metrics")
    @ResponseBody
    public String get(@RequestParam(name = "api", required = false) String api) {
        LOGGER.info("API: " + api);
        if ((api != null) && api.equals("all")) {
            api = null;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append("<html>");
        createHead(buffer);
        buffer.append("<h1>Metrics</h1>");
        
        createSelection(buffer, api, context.getContextPath());
        buffer.append("*All timings in ms. Rates are in requests/second.");
        buffer.append("<table>");
        addHeader(buffer);
        SortedMap<String, Timer> timers = metricRegistry.getTimers();
        for (Entry<String, Timer> entry : timers.entrySet()) {
            if ((api == null) || (api.length() == 0) || entry.getKey().contains(api)) {
                addTimer(buffer, entry, (api == null || api.isEmpty()));
            }
        }
        buffer.append("</table>");
        buffer.append("</html>");
        return buffer.toString();
    }
    
    /**
     * api to reset metrics.<br/>
     * reset all the Histogram, Counter, Gauge.
     *
     * @return success if the reset is successful
     */
    @GetMapping(path = "/v1/metrics/reset")
    @ResponseBody
    public String get() {
        for (String s : metricRegistry.getTimers().keySet()) {
            metricRegistry.remove(s);
        }
        for (String s : metricRegistry.getCounters().keySet()) {
            metricRegistry.remove(s);
        }
        for (String s : metricRegistry.getGauges().keySet()) {
            metricRegistry.remove(s);
        }
        return "success";
    }
    
    private void createHead(StringBuilder buffer) {
        buffer.append("<style>")
            .append("table, th, td {")
            .append("border: 1px solid black;")
            .append("}")
            .append("</style>");
        
    }
    
    private void createSelection(StringBuilder buffer, String selection, String contextPath) {
        Set<String> uniqueApis = new HashSet<>();
        for (Entry<String, Timer> entry : metricRegistry.getTimers().entrySet()) {
            if (entry.getKey().contains("method=")) {
                uniqueApis.add(entry.getKey().substring(INT_4, entry.getKey().indexOf("method=") - 1));
            }
        }
        List<String> apiList = new ArrayList<String>(uniqueApis);
        Collections.sort(apiList);
        buffer.append(
                "<form action=\"" + ((contextPath == null || contextPath.isEmpty()) ? "" : contextPath)
                    + "/v1/metrics\" method=\"get\">")
            .append("Choose an API &nbsp; <select name=api>");
        boolean selected = false;
        apiList.add(0, "all");
        for (String a : apiList) {
            selected = (selection != null) && a.equals(selection);
            buffer.append("<option value=\"")
                .append(a)
                .append("\"")
                .append(selected ? " selected" : "")
                .append(">")
                .append(condenseSignature(a))
                .append("</option>");
        }
        buffer.append("</select>")
            .append("&nbsp;<input type=\"submit\">")
            .append("</form>");
        
    }
    
    private String condenseSignature(String signature) {
        return signature.replace("org.eclipse.ecsp", "o.e.e").replace("java.lang", "j.l")
            .replace("public", "")
            .replace("java.util", "j.u").replace("javax.servlet", "j.s");
    }
    
    private void addTimer(StringBuilder buffer, Entry<String, Timer> entry,
                          boolean includeApiPrefix) {
        if (entry.getKey().contains("method=")) {
            String signature = includeApiPrefix ? entry.getKey()
                : entry.getKey()
                .substring(entry.getKey().indexOf("execution(") + INT_16, entry.getKey().length() - INT_2);
            signature = condenseSignature(signature);
            buffer.append("<tr>")
                .append("<td>")
                .append(signature)
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(entry.getValue().getSnapshot().getMean() / A_DOUBLE))
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(entry.getValue().getSnapshot().getMin() / A_DOUBLE))
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(entry.getValue().getSnapshot().getMax() / A_DOUBLE))
                .append("</td>")
                .append("<td>")
                .append(entry.getValue().getCount())
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(
                    entry.getValue().getSnapshot().get95thPercentile() / A_DOUBLE))
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(
                    entry.getValue().getSnapshot().get99thPercentile() / A_DOUBLE))
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(
                    entry.getValue().getSnapshot().get75thPercentile() / A_DOUBLE))
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(entry.getValue().getMeanRate()))
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(entry.getValue().getOneMinuteRate()))
                .append("</td>")
                .append("<td>")
                .append(numberFormatter.format(entry.getValue().getFiveMinuteRate()))
                .append("</td>")
                .append("</tr>");
        }
    }
    
    private void addHeader(StringBuilder buffer) {
        buffer.append("<tr>")
            .append("<th>")
            .append("Execution")
            .append("</th>")
            .append("<th>")
            .append("Mean")
            .append("</th>")
            .append("<th>")
            .append("Min")
            .append("</th>")
            .append("<th>")
            .append("Max")
            .append("</th>")
            .append("<th>")
            .append("Count")
            .append("</th>")
            .append("<th>")
            .append("95th")
            .append("</th>")
            .append("<th>")
            .append("99th")
            .append("</th>")
            .append("<th>")
            .append("75th")
            .append("</th>")
            .append("<th>")
            .append("Mean rate")
            .append("</th>")
            .append("<th>")
            .append("1 min rate")
            .append("</th>")
            .append("<th>")
            .append("5 min rate")
            .append("</th>")
            .append("</tr>");
    }
}