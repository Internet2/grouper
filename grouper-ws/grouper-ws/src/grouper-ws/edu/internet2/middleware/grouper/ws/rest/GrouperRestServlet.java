/*
 * @author mchyzer $Id: GrouperRestServlet.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestRequestContentType;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestResponseContentType;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import edu.internet2.middleware.grouper.ws.soap.WsResultMeta;

/**
 * servlet for rest web services
 */
public class GrouperRestServlet extends HttpServlet {

  /**
   * response header for if this is a success or not T or F
   */
  public static final String X_GROUPER_SUCCESS = "X-Grouper-success";

  /**
   * response header for the grouper response code
   */
  public static final String X_GROUPER_RESPONSE_CODE = "X-Grouper-responseCode";

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperRestServlet.class);

  /**
   * id
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    List<String> urlStrings = null;
    StringBuilder warnings = new StringBuilder();
    WsResponseBean wsResponseBean = null;
    //default to xhtml
    WsRestResponseContentType wsRestResponseContentType = WsRestResponseContentType.xhtml;

    try {
      //get the method and validate
      GrouperRestHttpMethod grouperRestHttpMethod = GrouperRestHttpMethod
          .valueOfIgnoreCase(request.getMethod(), true);

      urlStrings = extractUrlStrings(request);
      int urlStringsLength = GrouperUtil.length(urlStrings);

      //this is in content-type
      String contentType = request.getContentType();
      
      //get the body and convert to an object
      String body = IOUtils.toString(request.getReader());

      //get the enum
      WsRestRequestContentType wsRestRequestContentType = WsRestRequestContentType
        .findByContentType(contentType, body);
      
      GrouperWsVersion clientVersion = null;
      wsRestResponseContentType = null;

      if (urlStringsLength > 0) {
        boolean firstIsVersion = false;
        try {
          //first see if version
          GrouperWsVersion.valueOfIgnoreCase(urlStrings.get(0), true);
          firstIsVersion = true;
        } catch (Exception e) {
          //ignore
        }

        if (!firstIsVersion && urlStringsLength > 1) {
          
          //see if second is version (it better be at this point)
          GrouperWsVersion.valueOfIgnoreCase(urlStrings.get(1), true);
          
          //if so, then the first must be the content type
          String wsRestResponseContentTypeString = urlStrings.get(0);
          wsRestResponseContentType = WsRestResponseContentType.valueOfIgnoreCase(
              wsRestResponseContentTypeString, false);
          if (wsRestResponseContentType != null ) {
            //pop this off
            urlStrings.remove(0);
          }
        } 
      }
      //if no response type, calculate:
      if (wsRestResponseContentType == null) {
        wsRestResponseContentType = wsRestRequestContentType
          .calculateResponseContentType();
      }
      
      //will get enum and validate
      String clientVersionString = null;
      if (urlStringsLength > 0) {
        clientVersionString = urlStrings.get(0);
        //pop this off
        urlStrings.remove(0);
      }
      //will get enum and validate
      clientVersion = GrouperWsVersion.valueOfIgnoreCase(clientVersionString, true);

      WsRequestBean requestObject = null;

      if (!StringUtils.isBlank(body)) {
        requestObject = (WsRequestBean) wsRestRequestContentType.parseString(body,
            warnings);
      }
      wsResponseBean = grouperRestHttpMethod.service(clientVersion, urlStrings, requestObject);

    } catch (GrouperRestInvalidRequest glir) {

      wsResponseBean = new WsRestResultProblem();
      WsResultMeta wsResultMeta = wsResponseBean.getResultMetadata();
      String error = glir.getMessage() + ", " + requestDebugInfo(request);

      //this is a user error, but an error nonetheless
      LOG.error(error, glir);

      wsResultMeta.appendResultMessage(error);
      wsResultMeta.assignHttpStatusCode(400);
      wsResultMeta.assignResultCode("INVALID_QUERY");

    } catch (RuntimeException e) {

      //this is not a user error, is a big problem

      wsResponseBean = new WsRestResultProblem();
      LOG.error("Problem with request: " + requestDebugInfo(request), e);
      WsResultMeta wsResultMeta = wsResponseBean.getResultMetadata();
      wsResultMeta.appendResultMessage("Problem with request: "
          + requestDebugInfo(request) + ",\n" + ExceptionUtils.getFullStackTrace(e));
      wsResultMeta.assignSuccess("F");
      wsResultMeta.assignResultCode("EXCEPTION");
      wsResultMeta.assignHttpStatusCode(500);
    }

    try {
      if (warnings.length() > 0) {
        wsResponseBean.getResponseMetadata().appendResultWarning(warnings.toString());
      }
      //response code
      //better be there
      //TODO       if (wsResponseBean.getResultMetadata().re)

      //headers, response code
      //      int httpCode();
      //      String statusCode();

      //TODO
      response.setContentType("application/xhtml+xml");
      //      GrouperServiceUtils.addResponseHeaders(response, GrouperServiceUtils.booleanToStringOneChar(success), resultCode);

      wsRestResponseContentType.writeString(wsResponseBean, response.getWriter());
    } catch (RuntimeException re) {
      //problem!
      LOG.error("Problem with request: " + requestDebugInfo(request), re);
    } finally {

      IOUtils.closeQuietly(response.getWriter());
    }
  }

  /**
   * for error messages, get a detailed report of the request
   * @param request
   * @return the string of descriptive result
   */
  public static String requestDebugInfo(HttpServletRequest request) {
    StringBuilder result = new StringBuilder();
    result.append(" uri: ").append(request.getRequestURI());
    result.append(", method: ").append(request.getMethod());
    result.append(", decoded url strings: ");
    List<String> urlStrings = extractUrlStrings(request);
    int urlStringsLength = GrouperUtil.length(urlStrings);
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
   * if the input is: grouper-ws/servicesRest/xhtml/v1_3_000/group/members
   * then the result is a list of size 2: {"group", "members"}
   * 
   * </pre>
   * @param requestResourceFull
   * @return the url strings
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = StringUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();
    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(GrouperUtil.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
