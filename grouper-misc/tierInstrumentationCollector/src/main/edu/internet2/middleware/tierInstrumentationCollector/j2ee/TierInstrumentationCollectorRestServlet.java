/*******************************************************************************
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/*
 * @author mchyzer $Id: GrouperRestServlet.java,v 1.13 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.tierInstrumentationCollector.j2ee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.tierInstrumentationCollector.config.TierInstrumentationCollectorConfig;
import edu.internet2.middleware.tierInstrumentationCollector.corebeans.TicResponseBeanBase;
import edu.internet2.middleware.tierInstrumentationCollector.corebeans.TicResultProblem;
import edu.internet2.middleware.tierInstrumentationCollector.exceptions.TicRestInvalidRequest;
import edu.internet2.middleware.tierInstrumentationCollector.logging.TicRequestLog;
import edu.internet2.middleware.tierInstrumentationCollector.rest.TicRestHttpMethod;
import edu.internet2.middleware.tierInstrumentationCollector.util.TierInstrumentationCollectorUtils;
import edu.internet2.middleware.tierInstrumentationCollector.version.TierInstrumentationCollectorVersion;

/**
 * servlet for rest web services
 */
public class TierInstrumentationCollectorRestServlet extends HttpServlet {

  /** logger */
  private static final Log LOG = LogFactory.getLog(TierInstrumentationCollectorRestServlet.class);

  /** when this servlet was started */
  private static long startupTime = System.currentTimeMillis();
  
  /**
   * keep warnings in thread local so they can be accessed from anywhere
   */
  private static ThreadLocal<StringBuilder> threadLocalWarnings = new ThreadLocal<StringBuilder>();

  /**
   * 
   * @return the warnings
   */
  public static StringBuilder threadLocalWarnings() {
    return threadLocalWarnings.get();
  }
  
  /**
   * id
   */
  private static final long serialVersionUID = 1L;

  
  /**
   * @return the startupTime
   */
  public static long getStartupTime() {
    return TierInstrumentationCollectorRestServlet.startupTime;
  }

  /**
   * get the current request id out of the request attribute
   * @return the request id
   */
  public static String requestId() {
    HttpServletRequest httpServletRequest = TierInstrumentationCollectorFilterJ2ee.retrieveHttpServletRequest();
    
    if (httpServletRequest == null) {
      return null;
    }
    
    String requestIdAttributeName = "tierRequestId";
    
    String requestId = (String)httpServletRequest.getAttribute(requestIdAttributeName);
    
    if (StringUtils.isBlank(requestId)) {
      requestId = GrouperClientUtils.uuid();
      
      httpServletRequest.setAttribute(requestIdAttributeName, requestId);
    }
    
    return requestId;
    
  }
  
  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    long servetStarted = System.nanoTime();

    TierInstrumentationCollectorFilterJ2ee.assignHttpServlet(this);
    List<String> urlStrings = null;
    StringBuilder warnings = new StringBuilder();
    threadLocalWarnings.set(warnings);

    TicResponseBeanBase asasResponseBean = null;
    
    boolean indent = false;
    
    try {
      
      TicRequestLog.logToRequestLog("requestId", requestId());
      TicRequestLog.logToRequestLog("clientIp", TierInstrumentationCollectorFilterJ2ee.clientIp());
      
      
      if (GrouperClientUtils.booleanValue(request.getParameter("indent"), false)) {
        indent = true;
        TicRequestLog.logToRequestLog("indent", true);
      }
      
      //init params (if problem, exception will be thrown)
      request.getParameterMap();
      
      urlStrings = extractUrlStrings(request);
      int urlStringsLength = GrouperClientUtils.length(urlStrings);

      TicRequestLog.logToRequestLog("requestUri", request.getRequestURI());
      
      {
        StringBuilder result = new StringBuilder();
        if (urlStringsLength == 0) {
          result.append("[none]");
        } else {
          for (int i = 0; i < urlStringsLength; i++) {
            result.append(i).append(": '").append(urlStrings.get(i)).append("'");
            if (i != urlStringsLength - 1) {
              result.append(", ");
            }
          }
        }
        TicRequestLog.logToRequestLog("urlStrings", urlStrings);
      }
      
      //get the body and convert to an object
      String body = GrouperClientUtils.toString(request.getReader());

      int bodyLength = body == null ? 0 : body.length();
      TicRequestLog.logToRequestLog("requestBodySize", bodyLength);
      if (bodyLength > 0) {
        TicRequestLog.logToRequestLog("requestBody", GrouperClientUtils.abbreviate(body, 100));
      }
      TierInstrumentationCollectorVersion clientVersion = null;

      //get the method and validate (either from object, or HTTP method
      TicRestHttpMethod asasRestHttpMethod = null;
      {
        String methodString = request.getMethod();
        TicRequestLog.logToRequestLog("method", methodString);
        asasRestHttpMethod = TicRestHttpMethod.valueOfIgnoreCase(methodString, true);
      }
      
      //if there are other content types, detect them here
      boolean foundContentType = false;
      
      {
        String queryString = request.getQueryString();
        TicRequestLog.logToRequestLog("queryString", GrouperClientUtils.abbreviate(queryString, 100));
      }
      
//   we could strip off extension if we had different types
//      if (foundContentType && urlStringsLength > 0) {
//        
//        String lastUrlString = urlStrings.get(urlStringsLength-1);
//        if (lastUrlString.endsWith("." + wsRestContentType.name())) {
//          lastUrlString = lastUrlString.substring(0, lastUrlString.length()-(1+wsRestContentType.name().length()));
//        }
//        urlStrings.set(urlStringsLength-1, lastUrlString);
//      }
      
      if (urlStringsLength == 0) {
        throw new RuntimeException("Invalid request");
      } else {
        
//        if (!foundContentType) {
//          throw new AsasRestInvalidRequest("Request must end in .json or .xml: " + request.getRequestURI());
//        }
        
        //first see if version
        clientVersion = TierInstrumentationCollectorVersion.valueOfIgnoreCase(GrouperClientUtils.popUrlString(urlStrings), true);

        TicRequestLog.logToRequestLog("urlVersion", clientVersion);

        TierInstrumentationCollectorVersion.assignCurrentClientVersion(clientVersion, warnings);
        
  //      WsRequestBean requestObject = null;
  //
  //      if (!StringUtils.isBlank(body)) {
  //        requestObject = (WsRequestBean) wsRestRequestContentType.parseString(body,
  //            warnings);
  //      }
  //      
  //      //might be in params (which might not be in body
  //      if (requestObject == null) {
  //        //might be in http params...
  //        requestObject = (WsRequestBean) GrouperServiceUtils.marshalHttpParamsToObject(
  //            request.getParameterMap(), request, warnings);
  //
  //      }
                    
        asasResponseBean = asasRestHttpMethod.service(urlStrings, request.getParameterMap(), body);
      }
    } catch (TicRestInvalidRequest arir) {

      asasResponseBean = new TicResultProblem();
      String error = arir.getMessage();

      //this is a user error, but an error nonetheless
      LOG.error(error + ", " + requestDebugInfo(request), arir);

      boolean showExceptionStack = TierInstrumentationCollectorConfig.retrieveConfig().propertyValueBoolean("tierInstrumentationCollector.showExceptionStack", false);
      
      String errorDescription = error;
      
      if (showExceptionStack) {
        error += ", " + GrouperClientUtils.getFullStackTrace(arir);
      }

      TicRequestLog.logToRequestLog("errorDescription", errorDescription);

      asasResponseBean.getMeta().setTierErrorMessage(errorDescription);
      asasResponseBean.getMeta().setTierSuccess(false);
      asasResponseBean.getMeta().setTierResultCode(arir.getTierResultCode());
      int httpStatusCode = GrouperClientUtils.intValue(arir.getHttpResponseCode(), 400);
      asasResponseBean.getMeta().setTierHttpStatusCode(httpStatusCode);

    } catch (RuntimeException e) {

      TicRequestLog.logToRequestLog("errorDescription", GrouperClientUtils.getFullStackTrace(e));

      asasResponseBean = new TicResultProblem();
      LOG.error("Problem with request: " + requestDebugInfo(request), e);
      String error = "Problem with request: "
          + requestDebugInfo(request);
      
      boolean showExceptionStack = TierInstrumentationCollectorConfig.retrieveConfig().propertyValueBoolean("tierClient.showExceptionStack", false);
      if (showExceptionStack) {
        error += ",\n" + GrouperClientUtils.getFullStackTrace(e);
      }
      
      asasResponseBean.getMeta().setTierErrorMessage(error);
      asasResponseBean.getMeta().setTierSuccess(false);
      asasResponseBean.getMeta().setTierResultCode("EXCEPTION");
      asasResponseBean.getMeta().setTierHttpStatusCode(500);

    }
    
    //set http status code, content type, and write the response
    try {
      { 
        StringBuilder urlBuilder = new StringBuilder();
        {
          String url = request.getRequestURL().toString();
          url = GrouperClientUtils.prefixOrSuffix(url, "?", true);
          urlBuilder.append(url);
        }
        //lets put the params back on (the ones we expect)
        Map<String, String> paramMap = request.getParameterMap();
        boolean firstParam = true;
        for (String paramName : paramMap.keySet()) {
          if (firstParam) {
            urlBuilder.append("?");
          } else {
            urlBuilder.append("&");
          }
          firstParam = false;
          
          urlBuilder.append(GrouperClientUtils.escapeUrlEncode(paramName))
            .append("=").append(GrouperClientUtils.escapeUrlEncode(paramMap.get(paramName)));
          
        }
        String selfUri = urlBuilder.toString();
        if (selfUri.startsWith(TierInstrumentationCollectorUtils.servletUrl() + "/")) {
          selfUri = selfUri.substring(TierInstrumentationCollectorUtils.servletUrl().length());
        }
        selfUri = asasResponseBean.getMeta().getTierServiceRootUri() + selfUri;
        asasResponseBean.getMeta().setLocation(selfUri);
      }
      if (warnings.length() > 0) {
        asasResponseBean.getMeta().appendWarning(warnings.toString());
      }

      {
        Set<String> unusedParams = ((TierInstrumentationCollectorHttpServletRequest)request).unusedParams();
        //add warnings about unused params
        if (GrouperClientUtils.length(unusedParams) > 0) {
          for (String unusedParam : unusedParams) {
            asasResponseBean.getMeta().appendWarning("Unused HTTP param: " + unusedParam);
          }
        }
      }     
            
      if (asasResponseBean.getMeta().getTierSuccess() != null && asasResponseBean.getMeta().getTierSuccess()) {
        response.setHeader("X-TIER-success", "true");
        TicRequestLog.logToRequestLog("success", true);
      } else {
        response.setHeader("X-TIER-success", "false");
        TicRequestLog.logToRequestLog("success", false);
      }
      if (!StringUtils.isBlank(asasResponseBean.getMeta().getTierResultCode())) {
        response.setHeader("X-TIER-resultCode", asasResponseBean.getMeta().getTierResultCode());
        TicRequestLog.logToRequestLog("resultCode", asasResponseBean.getMeta().getTierResultCode());
      }
      
      response.setHeader("X-TIER-requestId", requestId());
      asasResponseBean.getMeta().setTierRequestId(requestId());
      
      //headers should be there by now
      //set the status code
      response.setStatus(asasResponseBean.getMeta().getTierHttpStatusCode());
      TicRequestLog.logToRequestLog("status", asasResponseBean.getMeta().getTierHttpStatusCode());

      String restCharset = TierInstrumentationCollectorConfig.retrieveConfig().propertyValueString("tierApiAuthzServer.restHttpContentTypeCharset");
      String responseContentType = "text/x-json";
      
      if (!GrouperClientUtils.isBlank(restCharset)) {
        responseContentType += "; charset=" + restCharset;
      }
      TicRequestLog.logToRequestLog("responseContentType", responseContentType);
      
      response.setContentType(responseContentType);

      //temporarily set to uuid, so we can time the content generation
      long millisUuid = -314253647586987L;
      
      asasResponseBean.getMeta().setTierResponseDurationMillis(millisUuid);

      if (TierInstrumentationCollectorConfig.retrieveConfig().propertyValueBoolean("tierClient.showDebug", false)) {
        
        asasResponseBean.getMeta().setTierDebugMessage(TicRequestLog.requestLogString());
      }
      
      TicRequestLog.logToRequestLog("warnings", asasResponseBean.getMeta().getTierWarning());

      boolean scimClient = TierInstrumentationCollectorConfig.retrieveConfig().propertyValueBoolean("tierClient.scim", false);
      
      if (scimClient) {
        asasResponseBean.scimify();
      }
      String responseString = "";  //TODO is there ever a response string?

      if (indent) {
        //responseString = wsRestContentType.indent(responseString);
      }
      
      TicRequestLog.logToRequestLog("responseSize", responseString == null ? 0 : responseString.length());

      long responseTimeMillis = (System.nanoTime()-servetStarted) / 1000000;
      responseString = GrouperClientUtils.replace(responseString, Long.toString(millisUuid), Long.toString(responseTimeMillis));
      
      response.setHeader("X-TIER-responseDurationMillis", Long.toString(responseTimeMillis));
      TicRequestLog.logToRequestLog("durationMillis", responseTimeMillis);
      
      try {
        response.getWriter().write(responseString);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }

      TicRequestLog.logToRequestLog("sentBodySuccessfully", true);

    } catch (RuntimeException re) {
      TicRequestLog.logToRequestLog("sentBodySuccessfully", false);
      TicRequestLog.logToRequestLog("exception2", GrouperClientUtils.getFullStackTrace(re));
      //problem!
      LOG.error("Problem with request: " + requestDebugInfo(request), re);
    } finally {

      GrouperClientUtils.closeQuietly(response.getWriter());
      TierInstrumentationCollectorVersion.removeCurrentClientVersion();

      try {
        TicRequestLog.logRequest();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
      httpSession.invalidate();
    }
    threadLocalWarnings.remove();
  }

  /**
   * for error messages, get a detailed report of the request
   * @param request
   * @return the string of descriptive result
   */
  public static String requestDebugInfo(HttpServletRequest request) {
    StringBuilder result = new StringBuilder();
    result.append(" uri: ").append(request.getRequestURI());
    result.append(", HTTP method: ").append(((TierInstrumentationCollectorHttpServletRequest)request).getOriginalMethod());
    if (!GrouperClientUtils.isBlank(request.getParameter("method"))) {
      result.append(", HTTP param method: ").append(request.getParameter("method"));
    }
    result.append(", decoded url strings: ");
    List<String> urlStrings = extractUrlStrings(request);
    int urlStringsLength = GrouperClientUtils.length(urlStrings);
    if (urlStringsLength == 0) {
      result.append("[none]");
    } else {
      for (int i = 0; i < urlStringsLength; i++) {
        result.append(i).append(": '").append(urlStrings.get(i)).append("'");
        if (i != urlStringsLength - 1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }

  /**
   * take a request and get the list of url strings for the rest web service
   * @see #extractUrlStrings(String)
   * @param request is the request to get the url strings out of
   * @return the list of url strings
   */
  private static List<String> extractUrlStrings(HttpServletRequest request) {
    String requestResourceFull = request.getRequestURI();
    return extractUrlStrings(requestResourceFull);
  }

  /**
   * <pre>
   * take a request uri and break up the url strings not including the app name or servlet
   * this does not include the url params (if applicable)
   * if the input is: grouper-ws/servicesRest/xhtml/v1_3_000/groups/members
   * then the result is a list of size 2: {"group", "members"}
   * 
   * </pre>
   * @param requestResourceFull
   * @return the url strings
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = GrouperClientUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();

    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(GrouperClientUtils.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
