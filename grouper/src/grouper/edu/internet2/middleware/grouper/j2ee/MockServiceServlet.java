package edu.internet2.middleware.grouper.j2ee;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.app.azure.AzureMockServiceHandler;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class MockServiceServlet extends HttpServlet {

  /** logger */
  private static final Log LOG = LogFactory.getLog(MockServiceServlet.class);

  /**
   * 
   */
  private static final long serialVersionUID = 4231465642132533661L;

  public MockServiceServlet() {
  }

  /**
   * add handlers here to handle requests
   */
  private static final Map<String, String> urlToHandler = GrouperUtil.toMap(
      "azure", AzureMockServiceHandler.class.getName());
  
  /**
   * @param tableName
   */
  public static void dropMockTable(final String tableName) {
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
    } catch (Exception e) {
      return;
    }
    try {
      HibernateSession.bySqlStatic().executeSql("drop table " + tableName);
    } catch (Exception e) {
      return;
    }
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      throw new RuntimeException("Cant drop table: '" + tableName + "'");
    } catch (Exception e) {
      return;
    }
  }

  /**
   * 
   */
  @Override
  protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    
    MockServiceRequest mockServiceRequest = new MockServiceRequest();
    MockServiceResponse mockServiceResponse = new MockServiceResponse();
    long startTimeNanos = System.nanoTime();
    
    try {

//      {
//        // we need to use GrouperRequestWrapper so we can read parameters more than once for logging
//        Class grouperRequestWrapperClass = null;
//        
//        try {
//          grouperRequestWrapperClass = GrouperUtil.forName("edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper");
//          Constructor constructor = grouperRequestWrapperClass.getConstructor(HttpServletRequest.class);
//          httpServletRequest = (HttpServletRequest)constructor.newInstance(httpServletRequest);
//        } catch (Exception e) {
//          throw new RuntimeException("You must run the mockService in the Grouper UI!", e);
//        }
//        
//      }
      
      String pathInfo = httpServletRequest.getPathInfo();

      mockServiceRequest.getDebugMap().put("rawPathInfo", pathInfo);
      if (pathInfo.startsWith("/")) {
        pathInfo = pathInfo.substring(1, pathInfo.length());
      }
      if (pathInfo.endsWith("/")) {
        pathInfo = pathInfo.substring(0, pathInfo.length()-1);
      }
      if (StringUtils.isBlank(pathInfo)) {
        throw new RuntimeException("Bad request, pass in the mock name at least");
      }
      String[] pathParts = GrouperUtil.splitTrim(pathInfo, "/");
      mockServiceRequest.setMockName(pathParts[0]);
      mockServiceRequest.getDebugMap().put("mockName", mockServiceRequest.getMockName());
      
      StringBuilder postMockNamePath = new StringBuilder();
      String[] postMockNamePaths = new String[pathParts.length-1];
      for (int i=1;i<pathParts.length;i++) {
        if (postMockNamePath.length() > 0) {
          postMockNamePath.append("/");
        }
        postMockNamePath.append(pathParts[i]);
        postMockNamePaths[i-1] = pathParts[i];
      }
      // path without the mock name part
      mockServiceRequest.setPostMockNamePath(postMockNamePath.toString());
      
      // path parts without the mock name part
      mockServiceRequest.setPostMockNamePaths(postMockNamePaths);

      mockServiceRequest.getDebugMap().put("postMockNamePath", postMockNamePath.toString());

      mockServiceRequest.setHttpServletRequest(httpServletRequest);
      mockServiceResponse.setHttpServletResponse(httpServletResponse);

      {
        // if params get those
        httpServletRequest.getParameterMap();
        
        String requestBody = IOUtils.toString(httpServletRequest.getReader());
        mockServiceRequest.setRequestBody(requestBody);
        
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.mock.services.logRequestsResponses", false)) {
          StringBuilder requestLog = new StringBuilder("\n");
          requestLog.append(httpServletRequest.getMethod()).append(" ").append(httpServletRequest.getRequestURI());
          if (!StringUtils.isBlank(httpServletRequest.getQueryString())) {
            requestLog.append("?").append(httpServletRequest.getQueryString());
          }
          requestLog.append("\n");
          for (Enumeration<String> enumeration = httpServletRequest.getHeaderNames(); enumeration.hasMoreElements();) {
            String headerName = enumeration.nextElement();
            String headerValue = httpServletRequest.getHeader(headerName);
            requestLog.append(headerName).append(": ").append(headerValue).append("\n");
          }
          for (Enumeration<String> enumeration = httpServletRequest.getParameterNames(); enumeration.hasMoreElements();) {
            String parameterName = enumeration.nextElement();
            String parameterValue = httpServletRequest.getParameter(parameterName);
            requestLog.append("Parameter: ").append(parameterName).append(" = ").append(parameterValue).append("\n");
          }
          if (!StringUtils.isBlank(requestBody)) {
            requestLog.append("\n").append(requestBody).append("\n");
          }
          mockServiceRequest.getDebugMap().put("requestLog", requestLog.toString());
        }
      }
      
      String className = urlToHandler.get(mockServiceRequest.getMockName());
      mockServiceRequest.getDebugMap().put("mockHandlerClassName", className);
      if (className == null) {
        throw new RuntimeException("Cant find mock name for '" + className + "'");
      }
      Class<MockServiceHandler> mockServiceHandlerClass = GrouperUtil.forName(className);
      MockServiceHandler mockServiceHandler = GrouperUtil.newInstance(mockServiceHandlerClass);
      
      mockServiceHandler.handleRequest(mockServiceRequest, mockServiceResponse);
      
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.mock.services.logRequestsResponses", false)) {
        StringBuilder responseLog = new StringBuilder("\n");
        responseLog.append("Response code: ").append(mockServiceResponse.getResponseCode()).append("\n");
        for (String headerName : mockServiceResponse.getResponseHeaders().keySet()) {
          String headerValue = mockServiceResponse.getResponseHeaders().get(headerName);
          responseLog.append(headerName).append(": ").append(headerValue).append("\n");
        }
        
        if (!StringUtils.isBlank(mockServiceResponse.getContentType())) {
          responseLog.append("Content-Type: ").append(mockServiceResponse.getContentType());
        }
        if (!StringUtils.isBlank(mockServiceResponse.getResponseBody())) {
          responseLog.append("\n").append(mockServiceResponse.getResponseBody()).append("\n");
        }
        mockServiceRequest.getDebugMap().put("responseLog", responseLog.toString());
      }

      if (mockServiceResponse.getResponseCode() == -1) {
        throw new RuntimeException("You must set the response code!");
      }
      httpServletResponse.setStatus(mockServiceResponse.getResponseCode());

      for (String headerName : mockServiceResponse.getResponseHeaders().keySet()) {
        String headerValue = mockServiceResponse.getResponseHeaders().get(headerName);
        httpServletResponse.addHeader(headerName, headerValue);
      }
      if (!StringUtils.isBlank(mockServiceResponse.getContentType())) {
        httpServletResponse.setContentType(mockServiceResponse.getContentType());
      }
      if (!StringUtils.isBlank(mockServiceResponse.getResponseBody())) {
        //just write whatever we got
        PrintWriter out = null;
      
        try {
          out = httpServletResponse.getWriter();
          out.println(mockServiceResponse.getResponseBody());
        } catch (Exception e) {
          throw new RuntimeException("Cant get response.getWriter: ", e);
        } finally {
          GrouperClientUtils.closeQuietly(out);
        }         
        
      }
      

    } catch (RuntimeException e) {
      mockServiceRequest.getDebugMap().put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {

      if (LOG.isDebugEnabled()) {
        mockServiceRequest.getDebugMap().put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
        LOG.debug(GrouperClientUtils.mapToString(mockServiceRequest.getDebugMap()));
      }

    }
    
    
    
  }

  
  
}
