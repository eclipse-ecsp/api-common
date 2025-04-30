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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * Constant representing a conversion factor for nanoseconds to milliseconds.
     */
    public static final double A_DOUBLE = 1000000.0D;

    /**
     * Constant representing the integer value 4, used for substring operations.
     */
    public static final int INT_4 = 4;

    /**
     * Constant representing the integer value 16, used for substring operations.
     */
    public static final int INT_16 = 16;

    /**
     * Constant representing the integer value 2, used for substring operations.
     */
    public static final int INT_2 = 2;

    /**
     * Constant representing the string "method=", used for identifying method-related metrics.
     */
    public static final String METHOD = "method=";

    /**
     * Constant representing the opening HTML &lt;td&gt; tag.
     */
    public static final String TD_OPEN_TAG = "<td>";

    /**
     * Constant representing the closing HTML &lt;&#x2F;td&gt; tag.
     */
    public static final String TD_CLOSE_TAG = "</td>";

    /**
     * Constant representing the closing HTML &lt;&#x2F;th&gt; tag.
     */
    public static final String TH_CLOSE_TAG = "</th>";

    /**
     * Constant representing the opening HTML &lt;th&gt; tag.
     */
    public static final String TH_OPEN_TAG = "<th>";

    private final MetricRegistry metricRegistry;

    private final ServletContext context;
    private final java.text.DecimalFormat numberFormatter = new DecimalFormat("###.##");

    /**
     * Constructor to initialize the metric registry and servlet context.
     *
     * @param metricRegistry MetricRegistry instance
     * @param context        ServletContext instance
     */
    public PerformanceMonitorController(MetricRegistry metricRegistry, ServletContext context) {
        this.metricRegistry = metricRegistry;
        this.context = context;
    }

    /**
     * export api to fetch metrics for particular api or all.
     *
     * @param api endpoint to fetch metrics for
     * @return metrics in html format
     */
    @GetMapping(path = "/v1/jamon-metrics")
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
            if (StringUtils.isEmpty(api) || entry.getKey().contains(api)) {
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
            if (entry.getKey().contains(METHOD)) {
                uniqueApis.add(entry.getKey().substring(INT_4, entry.getKey().indexOf(METHOD) - 1));
            }
        }
        List<String> apiList = new ArrayList<>(uniqueApis);
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
        return signature.replace("com.harman.haa.api", "c.h.h.a").replace("java.lang", "j.l")
                .replace("public", "")
                .replace("java.util", "j.u").replace("jakarta.servlet", "j.s");
    }

    private void addTimer(StringBuilder buffer, Entry<String, Timer> entry,
                          boolean includeApiPrefix) {
        if (entry.getKey().contains(METHOD)) {
            String signature = includeApiPrefix ? entry.getKey()
                    : entry.getKey()
                    .substring(entry.getKey().indexOf("execution(") + INT_16, entry.getKey().length() - INT_2);
            signature = condenseSignature(signature);
            buffer.append("<tr>")
                    .append(TD_OPEN_TAG)
                    .append(signature)
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(entry.getValue().getSnapshot().getMean() / A_DOUBLE))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(entry.getValue().getSnapshot().getMin() / A_DOUBLE))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(entry.getValue().getSnapshot().getMax() / A_DOUBLE))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(entry.getValue().getCount())
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(
                            entry.getValue().getSnapshot().get95thPercentile() / A_DOUBLE))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(
                            entry.getValue().getSnapshot().get99thPercentile() / A_DOUBLE))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(
                            entry.getValue().getSnapshot().get75thPercentile() / A_DOUBLE))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(entry.getValue().getMeanRate()))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(entry.getValue().getOneMinuteRate()))
                    .append(TD_CLOSE_TAG)
                    .append(TD_OPEN_TAG)
                    .append(numberFormatter.format(entry.getValue().getFiveMinuteRate()))
                    .append(TD_CLOSE_TAG)
                    .append("</tr>");
        }
    }

    private void addHeader(StringBuilder buffer) {
        buffer.append("<tr>")
                .append(TH_OPEN_TAG)
                .append("Execution")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("Mean")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("Min")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("Max")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("Count")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("95th")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("99th")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("75th")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("Mean rate")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("1 min rate")
                .append(TH_CLOSE_TAG)
                .append(TH_OPEN_TAG)
                .append("5 min rate")
                .append(TH_CLOSE_TAG)
                .append("</tr>");
    }
}