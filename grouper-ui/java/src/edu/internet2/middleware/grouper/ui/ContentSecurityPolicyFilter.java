/**
 * Copyright 2020 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.ui;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * web filter to provide a "Context-Security-Policy" header. While Tomcat provides some other security header filters, there
 * is not one for CSP, and no filter to add arbitrary headers. For sample policies and reference, see
 * https://content-security-policy.com/ .
 */
public class ContentSecurityPolicyFilter implements Filter {

    private String cspHeader;

    /**
     * The default policy for Grouper. It allows for inline Javascript, including evals, which is needed for UI functionality
     */
    public static final String DEFAULT_CSP_HEADER = "frame-ancestors 'none'; default-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval';";


    /**
     * Initializes the filter. header value can be set by property "value", otherwise will default to {@link #DEFAULT_CSP_HEADER}
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        String configCspHeader = config.getInitParameter("value");
        cspHeader = configCspHeader != null ? configCspHeader : DEFAULT_CSP_HEADER;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = ((HttpServletResponse) response);
        httpResponse.setHeader("Content-Security-Policy", cspHeader);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Not used
    }
}
