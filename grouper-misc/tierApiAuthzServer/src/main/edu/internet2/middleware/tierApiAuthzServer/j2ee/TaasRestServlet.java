/*
 * @author mchyzer $Id: GrouperRestServlet.java,v 1.13 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzServer.j2ee;

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

import edu.internet2.middleware.tierApiAuthzServer.config.TaasWsClientConfig;
import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasDefaultResourceContainer;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasDefaultVersionResourceContainer;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasResponseBeanBase;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasResultProblem;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.logging.TaasRequestLog;
import edu.internet2.middleware.tierApiAuthzServer.rest.AsasRestHttpMethod;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerConfig;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServer.version.TaasWsVersion;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.StringUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.LogFactory;

/**
 * servlet for rest web services
 */
public class TaasRestServlet extends HttpServlet {

  /** logger */
  private static final Log LOG = LogFactory.getLog(TaasRestServlet.class);

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
    return TaasRestServlet.startupTime;
  }

  /**
   * get the current request id out of the request attribute
   * @return the request id
   */
  public static String requestId() {
    HttpServletRequest httpServletRequest = TaasFilterJ2ee.retrieveHttpServletRequest();
    
    if (httpServletRequest == null) {
      return null;
    }
    
    String requestIdAttributeName = "tierRequestId";
    
    String requestId = (String)httpServletRequest.getAttribute(requestIdAttributeName);
    
    if (StringUtils.isBlank(requestId)) {
      requestId = StandardApiServerUtils.uuid();
      
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

    TaasFilterJ2ee.assignHttpServlet(this);
    List<String> urlStrings = null;
    StringBuilder warnings = new StringBuilder();
    threadLocalWarnings.set(warnings);

    AsasResponseBeanBase asasResponseBean = null;
    
    //we need something here if errors, so default to xhtml
    AsasRestContentType wsRestContentType = AsasRestContentType.json;
    AsasRestContentType.assignContentType(wsRestContentType);

    boolean indent = false;
    
    try {
      
      TaasRequestLog.logToRequestLog("requestId", requestId());
      TaasRequestLog.logToRequestLog("clientUser", TaasFilterJ2ee.retrieveUserPrincipalNameFromRequest());
      TaasRequestLog.logToRequestLog("clientIp", TaasFilterJ2ee.clientIp());
      TaasRequestLog.logToRequestLog("format", wsRestContentType.name());
      
      
      if (StandardApiServerUtils.booleanValue(request.getParameter("indent"), false)) {
        indent = true;
        TaasRequestLog.logToRequestLog("indent", true);
      }
      
      //init params (if problem, exception will be thrown)
      request.getParameterMap();
      
      urlStrings = extractUrlStrings(request);
      int urlStringsLength = StandardApiServerUtils.length(urlStrings);

      TaasRequestLog.logToRequestLog("requestUri", request.getRequestURI());
      
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
        TaasRequestLog.logToRequestLog("urlStrings", urlStrings);
      }
      
      //get the body and convert to an object
      String body = StandardApiServerUtils.toString(request.getReader());

      int bodyLength = body == null ? 0 : body.length();
      TaasRequestLog.logToRequestLog("requestBodySize", bodyLength);
      if (bodyLength > 0) {
        TaasRequestLog.logToRequestLog("requestBody", StandardApiServerUtils.abbreviate(body, 100));
      }
      TaasWsVersion clientVersion = null;

      //get the method and validate (either from object, or HTTP method
      AsasRestHttpMethod asasRestHttpMethod = null;
      {
        String methodString = request.getMethod();
        TaasRequestLog.logToRequestLog("method", methodString);
        asasRestHttpMethod = AsasRestHttpMethod.valueOfIgnoreCase(methodString, true);
      }
      
      //if there are other content types, detect them here
      boolean foundContentType = false;
      
      //we are always json
      if (request.getRequestURI().endsWith(".json") || true) {
        wsRestContentType = AsasRestContentType.json;
        foundContentType = true;
      }
      AsasRestContentType.assignContentType(wsRestContentType);

      {
        String queryString = request.getQueryString();
        TaasRequestLog.logToRequestLog("queryString", StandardApiServerUtils.abbreviate(queryString, 100));
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
        
        if (asasRestHttpMethod != AsasRestHttpMethod.GET) {
          throw new AsasRestInvalidRequest("Cant have non-GET method for default resource: " + asasRestHttpMethod, "405", "ERROR_METHOD_NOT_ALLOWED");
        }
        
        if (foundContentType) {
          
          asasResponseBean = new AsasDefaultVersionResourceContainer();
          
        } else {
          asasResponseBean = new AsasDefaultResourceContainer();
        }
      } else {
        
//        if (!foundContentType) {
//          throw new AsasRestInvalidRequest("Request must end in .json or .xml: " + request.getRequestURI());
//        }
        
        //first see if version
        clientVersion = TaasWsVersion.valueOfIgnoreCase(StandardApiServerUtils.popUrlString(urlStrings), true);

        TaasRequestLog.logToRequestLog("urlVersion", clientVersion);

        TaasWsVersion.assignCurrentClientVersion(clientVersion, warnings);
        
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
    } catch (AsasRestInvalidRequest arir) {

      asasResponseBean = new AsasResultProblem();
      String error = arir.getMessage();

      //this is a user error, but an error nonetheless
      LOG.error(error + ", " + requestDebugInfo(request), arir);

      boolean showExceptionStack = TaasWsClientConfig.retrieveClientConfigForLoggedInUser().propertyValueBoolean("tierClient.showExceptionStack", false);
      
      String errorDescription = error;
      
      if (showExceptionStack) {
        error += ", " + StandardApiServerUtils.getFullStackTrace(arir);
      }

      TaasRequestLog.logToRequestLog("errorDescription", errorDescription);

      asasResponseBean.getMeta().setTierErrorMessage(errorDescription);
      asasResponseBean.getMeta().setTierSuccess(false);
      asasResponseBean.getMeta().setTierResultCode(arir.getTierResultCode());
      int httpStatusCode = StandardApiServerUtils.intValue(arir.getHttpResponseCode(), 400);
      asasResponseBean.getMeta().setTierHttpStatusCode(httpStatusCode);

    } catch (RuntimeException e) {

      TaasRequestLog.logToRequestLog("errorDescription", StandardApiServerUtils.getFullStackTrace(e));

      asasResponseBean = new AsasResultProblem();
      LOG.error("Problem with request: " + requestDebugInfo(request), e);
      String error = "Problem with request: "
          + requestDebugInfo(request);
      
      boolean showExceptionStack = TaasWsClientConfig.retrieveClientConfigForLoggedInUser().propertyValueBoolean("tierClient.showExceptionStack", false);
      if (showExceptionStack) {
        error += ",\n" + StandardApiServerUtils.getFullStackTrace(e);
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
          url = StandardApiServerUtils.prefixOrSuffix(url, "?", true);
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
          
          urlBuilder.append(StandardApiServerUtils.escapeUrlEncode(paramName))
            .append("=").append(StandardApiServerUtils.escapeUrlEncode(paramMap.get(paramName)));
          
        }
        String selfUri = urlBuilder.toString();
        if (selfUri.startsWith(StandardApiServerUtils.servletUrl() + "/")) {
          selfUri = selfUri.substring(StandardApiServerUtils.servletUrl().length());
        }
        selfUri = asasResponseBean.getMeta().getTierServiceRootUri() + selfUri;
        asasResponseBean.getMeta().setLocation(selfUri);
      }
      if (warnings.length() > 0) {
        asasResponseBean.getMeta().appendWarning(warnings.toString());
      }

      {
        Set<String> unusedParams = ((AsasHttpServletRequest)request).unusedParams();
        //add warnings about unused params
        if (StandardApiServerUtils.length(unusedParams) > 0) {
          for (String unusedParam : unusedParams) {
            asasResponseBean.getMeta().appendWarning("Unused HTTP param: " + unusedParam);
          }
        }
      }     
            
      if (asasResponseBean.getMeta().getTierSuccess() != null && asasResponseBean.getMeta().getTierSuccess()) {
        response.setHeader("X-TIER-success", "true");
        TaasRequestLog.logToRequestLog("success", true);
      } else {
        response.setHeader("X-TIER-success", "false");
        TaasRequestLog.logToRequestLog("success", false);
      }
      if (!StringUtils.isBlank(asasResponseBean.getMeta().getTierResultCode())) {
        response.setHeader("X-TIER-resultCode", asasResponseBean.getMeta().getTierResultCode());
        TaasRequestLog.logToRequestLog("resultCode", asasResponseBean.getMeta().getTierResultCode());
      }
      
      response.setHeader("X-TIER-requestId", requestId());
      asasResponseBean.getMeta().setTierRequestId(requestId());
      
      //headers should be there by now
      //set the status code
      response.setStatus(asasResponseBean.getMeta().getTierHttpStatusCode());
      TaasRequestLog.logToRequestLog("status", asasResponseBean.getMeta().getTierHttpStatusCode());

      String restCharset = StandardApiServerConfig.retrieveConfig().propertyValueString("tierApiAuthzServer.restHttpContentTypeCharset");
      String responseContentType = wsRestContentType.getContentType();
      
      if (!StandardApiServerUtils.isBlank(restCharset)) {
        responseContentType += "; charset=" + restCharset;
      }
      TaasRequestLog.logToRequestLog("responseContentType", responseContentType);
      
      response.setContentType(responseContentType);

      //temporarily set to uuid, so we can time the content generation
      long millisUuid = -314253647586987L;
      
      asasResponseBean.getMeta().setTierResponseDurationMillis(millisUuid);

      if (TaasWsClientConfig.retrieveClientConfigForLoggedInUser().propertyValueBoolean("tierClient.showDebug")) {
        
        asasResponseBean.getMeta().setTierDebugMessage(TaasRequestLog.requestLogString());
      }
      
      TaasRequestLog.logToRequestLog("warnings", asasResponseBean.getMeta().getTierWarning());

      boolean scimClient = TaasWsClientConfig.retrieveClientConfigForLoggedInUser().propertyValueBoolean("tierClient.scim", false);
      
      if (scimClient) {
        asasResponseBean.scimify();
      }
      String responseString = wsRestContentType.writeString(asasResponseBean);

      if (indent) {
        responseString = wsRestContentType.indent(responseString);
      }
      
      TaasRequestLog.logToRequestLog("responseSize", responseString == null ? 0 : responseString.length());

      long responseTimeMillis = (System.nanoTime()-servetStarted) / 1000000;
      responseString = StandardApiServerUtils.replace(responseString, Long.toString(millisUuid), Long.toString(responseTimeMillis));
      
      response.setHeader("X-TIER-responseDurationMillis", Long.toString(responseTimeMillis));
      TaasRequestLog.logToRequestLog("durationMillis", responseTimeMillis);
      
      try {
        response.getWriter().write(responseString);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }

      TaasRequestLog.logToRequestLog("sentBodySuccessfully", true);

    } catch (RuntimeException re) {
      TaasRequestLog.logToRequestLog("sentBodySuccessfully", false);
      TaasRequestLog.logToRequestLog("exception2", StandardApiServerUtils.getFullStackTrace(re));
      //problem!
      LOG.error("Problem with request: " + requestDebugInfo(request), re);
    } finally {

      StandardApiServerUtils.closeQuietly(response.getWriter());
      TaasWsVersion.removeCurrentClientVersion();
      AsasRestContentType.clearContentType();

      try {
        TaasRequestLog.logRequest();
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
    result.append(", HTTP method: ").append(((AsasHttpServletRequest)request).getOriginalMethod());
    if (!StandardApiServerUtils.isBlank(request.getParameter("method"))) {
      result.append(", HTTP param method: ").append(request.getParameter("method"));
    }
    result.append(", decoded url strings: ");
    List<String> urlStrings = extractUrlStrings(request);
    int urlStringsLength = StandardApiServerUtils.length(urlStrings);
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
    String[] requestResources = StandardApiServerUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();

    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(StandardApiServerUtils.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
