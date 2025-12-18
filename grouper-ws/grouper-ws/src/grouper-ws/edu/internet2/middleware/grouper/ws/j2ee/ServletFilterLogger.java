/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

/**
 * log requests and responses
 */
public class ServletFilterLogger implements Filter {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(ServletFilterLogger.class);

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    //empty
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
    FilterChain filterChain)
    throws IOException, ServletException {

    //see if logging, if not just do filter chain so we dont waste cycles
    if (!shouldLogRequestsAndResponses(servletRequest)) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    HttpServletRequestCopier requestCopier = new HttpServletRequestCopier(
        (HttpServletRequest) servletRequest);
    HttpServletResponseCopier responseCopier = new HttpServletResponseCopier(
        (HttpServletResponse) servletResponse);

    try {
      filterChain.doFilter(requestCopier, responseCopier);
    } finally {
      logRequestAndResponse(requestCopier, responseCopier);
    }
  }

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig arg0) throws ServletException {
    //nothing
  }

  /**
   * see if should log requests and responses
   * if property ws.ServletFilterLogger.logRequests is false, then no
   * if property ws.ServletFilterLogger.logForSourceIpCidrs is blank, then yes
   * else see if source ip is in cidrs then yes
   * if error, then no
   * if none of the above, then no
   * @param servletRequest
   * @return if log requests and responses
   */
  public static boolean shouldLogRequestsAndResponses(ServletRequest servletRequest) {
    GrouperWsConfig grouperWsConfig = GrouperWsConfig.retrieveConfig();
    if (!grouperWsConfig.propertyValueBoolean("ws.ServletFilterLogger.logRequests", false)) {
      return false;
    }
    String logForSourceIpCidrs = grouperWsConfig.propertyValueString("ws.ServletFilterLogger.logForSourceIpCidrs");
    
    if (StringUtils.isBlank(logForSourceIpCidrs)) {
      return true;
    }
    
    try {
      if (!(servletRequest instanceof HttpServletRequest)) {
        return false;
      }
      HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
      
      String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
      
      // can be comma separated list and first is original client
      String sourceIpAddress = StringUtils.isNotBlank(xForwardedFor)
          ? StringUtils.trim(StringUtils.substringBefore(xForwardedFor, ","))
              : httpServletRequest.getRemoteAddr();
      
      return GrouperUtil.ipOnNetworks(sourceIpAddress, logForSourceIpCidrs);
      
    } catch (Exception e) {
      LOG.error("Error checking if should log requests and responses", e);
    }
    return false;
  }

  /**
   * @param servletRequest
   * @param servletResponse
   */
  @SuppressWarnings("unchecked")
  public static void logRequestAndResponse(HttpServletRequestCopier servletRequest, HttpServletResponseCopier servletResponse) {
    try {
      
      if (shouldLogRequestsAndResponses(servletRequest)) {
        
        StringBuilder requestParams = new StringBuilder();
        Enumeration<String> enumeration = servletRequest.getParameterNames();
        while (enumeration.hasMoreElements()) {
          String name = enumeration.nextElement();
          String parameterValue = StringUtils.defaultString(
              servletRequest.getParameter(name)
          );
//          // normalize the new lines
//          parameterValue = StringUtils.replaceEach(
//              parameterValue,
//              new String[] {"\r", "\n"},
//              new String[] {"\\r", "\\n"}
//          );
          if (Strings.CI.equalsAny(name, "password", "pass", "token", "secret")) {
            parameterValue = "*****";
          }
          requestParams.append(name + " = " + parameterValue + ", ");
        }
        
        servletRequest.finishReading();
        servletResponse.flushBuffer();
        byte[] requestCopy = servletRequest.getCopy();
        byte[] responseCopy = servletResponse.getCopy();
        String requestBody = safeBody(requestCopy, servletRequest.getCharacterEncoding());
        String responseBody = safeBody(responseCopy, servletResponse.getCharacterEncoding());
        
//        String queryString = StringUtils.replaceEach(
//            String.valueOf(servletRequest.getQueryString()),
//            new String[] {"\r", "\n"},
//            new String[] {"\\r", "\\n"}
//        );
        String queryString = StringUtils.defaultString(servletRequest.getQueryString());
        
        String logMessage = "sourceIp: " + servletRequest.getRemoteAddr() 
            + ", httpVersion: " + servletRequest.getProtocol()
            + ", url: " + servletRequest.getRequestURI()
            + ", queryString: " + queryString
            + ", method: " + servletRequest.getMethod()
            + "\n[GROUPER_REQUEST_HEADERS]: " + servletRequest.getHeaders()
            + "[GROUPER_REQUEST_PARAMS]: " + requestParams.toString()
            + "\n[GROUPER_REQUEST_BODY]: " + requestBody
            + "\n[GROUPER_RESPONSE_STATUS]: " + servletResponse.getStatusMessage()
            + "\n[GROUPER_RESPONSE_HEADERS]: " + servletResponse.getHeaders() 
            + "[GROUPER_RESPONSE_BODY]: " + responseBody;
        
        if (logMessage.length() > MAX_LOG_BODY_CHARS * 2) {
          logMessage = logMessage.substring(0, MAX_LOG_BODY_CHARS * 2) + "... [truncated, "
              + logMessage.length() + " chars total]";
        }
        
        // make sure this gets logged somewhere
        if (LOG.isDebugEnabled()) {
          LOG.debug(logMessage);
        } else if (LOG.isInfoEnabled()) {
          LOG.info(logMessage);
        } else if (LOG.isWarnEnabled()) {
          LOG.warn(logMessage);
        } else {
          LOG.error(logMessage);
        }
      }
    } catch (Exception e) {
      LOG.error("Error logging request/response", e);
    }
  }

  private static final int MAX_LOG_BODY_CHARS = 100_000;

  private static String safeBody(byte[] bodyBytes, String characterEncoding) {

    if (bodyBytes == null || bodyBytes.length == 0) {
      return "";
    }

    // Pick charset safely
    Charset charset;
    try {
      charset = StringUtils.isNotBlank(characterEncoding)
          ? Charset.forName(characterEncoding)
          : StandardCharsets.UTF_8;
    } catch (Exception e) {
      charset = StandardCharsets.UTF_8;
    }

    // Decode bytes
    String body;
    try {
      body = new String(bodyBytes, charset);
    } catch (Exception e) {
      return "[unprintable body]";
    }

//    // Normalize CR/LF to prevent log forging
//    body = StringUtils.replaceEach(
//        body,
//        new String[] { "\r", "\n" },
//        new String[] { "\\r", "\\n" }
//    );

    // Truncate to a safe size
    if (body.length() > MAX_LOG_BODY_CHARS) {
      body = body.substring(0, MAX_LOG_BODY_CHARS)
          + "... [truncated, " + body.length() + " chars total]";
    }

    return body;
  }

}
