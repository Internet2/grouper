/*
 * @author mchyzer $Id: GrouperLiteServlet.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

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
import edu.internet2.middleware.grouper.ws.lite.contentType.WsLiteRequestContentType;
import edu.internet2.middleware.grouper.ws.lite.contentType.WsLiteResponseContentType;
import edu.internet2.middleware.grouper.ws.lite.method.GrouperLiteHttpMethod;
import edu.internet2.middleware.grouper.ws.soap.WsResultMeta;

/**
 * servlet for lite web services
 */
public class GrouperLiteServlet extends HttpServlet {

  /**
   * response header for if this is a success or not T or F
   */
  public static final String X_GROUPER_SUCCESS = "X-Grouper-success";

  /**
   * response header for the grouper response code
   */
  public static final String X_GROUPER_RESPONSE_CODE = "X-Grouper-responseCode";

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperLiteServlet.class);

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
    WsLiteResponseContentType wsLiteResponseContentType = WsLiteResponseContentType.xhtml;

    try {
      //get the method and validate
      GrouperLiteHttpMethod grouperLiteHttpMethod = GrouperLiteHttpMethod
          .valueOfIgnoreCase(request.getMethod(), true);

      urlStrings = extractUrlStrings(request);
      int urlStringsLength = GrouperUtil.length(urlStrings);

      //this is in content-type
      String contentType = request.getContentType();
      
      //get the body and convert to an object
      String body = IOUtils.toString(request.getReader());

      //get the enum
      WsLiteRequestContentType wsLiteRequestContentType = WsLiteRequestContentType
        .findByContentType(contentType, body);
      
      GrouperWsVersion clientVersion = null;
      wsLiteResponseContentType = null;

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
          String wsLiteResponseContentTypeString = urlStrings.get(0);
          wsLiteResponseContentType = WsLiteResponseContentType.valueOfIgnoreCase(
              wsLiteResponseContentTypeString, false);
          if (wsLiteResponseContentType != null ) {
            //pop this off
            urlStrings.remove(0);
          }
        } 
      }
      //if no response type, calculate:
      if (wsLiteResponseContentType == null) {
        wsLiteResponseContentType = wsLiteRequestContentType
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
        requestObject = (WsRequestBean) wsLiteRequestContentType.parseString(body,
            warnings);
      }
      wsResponseBean = grouperLiteHttpMethod.service(clientVersion, urlStrings, requestObject);

    } catch (GrouperLiteInvalidRequest glir) {

      wsResponseBean = new WsLiteResultProblem();
      WsResultMeta wsResultMeta = wsResponseBean.getResultMetadata();
      String error = glir.getMessage() + ", " + requestDebugInfo(request);

      //this is a user error, but an error nonetheless
      LOG.error(error, glir);

      wsResultMeta.appendResultMessage(error);
      wsResultMeta.assignHttpStatusCode(400);
      wsResultMeta.assignResultCode("INVALID_QUERY");

    } catch (RuntimeException e) {

      //this is not a user error, is a big problem

      wsResponseBean = new WsLiteResultProblem();
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

      wsLiteResponseContentType.writeString(wsResponseBean, response.getWriter());
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
   * if the input is: grouper-ws/servicesLite/xhtml/v1_3_000/group/members
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

  //  /**
  //   * create a standard grouper lite xhtml response
  //   * @param response to write to
  //   * @param statusCode to send back (e.g. 200)
  //   * @param success true or false
  //   * @param resultCode e.g. SUCCESS
  //   * @param resultMessage e.g. successfully added member 123 to group a:b:c
  //   * @param title is the title of the HTML sent back
  //   * @param wsLiteResponseHandler will write the body of the response
  //   */
  //  public static void callbackWriteResponse(HttpServletResponse response, 
  //      int statusCode, boolean success, 
  //      String resultCode, String resultMessage, String title,
  //      WsLiteResponseHandler wsLiteResponseHandler) {
  //    //if something happened to the response already, bail, inconsistent state.
  //    //all output starts in this method...
  //    if (response.isCommitted()) {
  //      throw new RuntimeException("Response is already committed, inconsistent state! " + statusCode 
  //          + ", " + resultCode + ", success? " + success + ", " + resultMessage);
  //    }
  //    
  //    response.setStatus(statusCode);
  //    GrouperServiceUtils.addResponseHeaders(response, GrouperServiceUtils.booleanToStringOneChar(success), resultCode);
  //    
  //    //XHTML header
  //    response.setContentType("application/xhtml+xml");
  //    
  //    XMLOutputFactory factory = XMLOutputFactory.newInstance();
  //    XMLStreamWriter writer = null;
  //    try {
  //      writer = factory.createXMLStreamWriter(response.getOutputStream());
  //      //<?xml version="1.0" encoding="iso-8859-1"?>
  //      writer.writeStartDocument("ISO-8859-1", "1.0");
  //      //<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
  //      writer.writeDTD("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
  //      //<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  //      writer.writeStartElement("html");
  //      writer.writeAttribute("xmlns", "http://www.w3.org/1999/xhtml");
  //      writer.writeAttribute("xml:lang", "en");
  //      writer.writeAttribute("lang", "en");
  //      //<head>
  //      writer.writeStartElement("head");
  //      //  <title></title>
  //      writer.writeStartElement("title");
  //      if (!StringUtils.isBlank(title)) {
  //        writer.writeCharacters(title);
  //      }
  //      writer.writeEndElement();
  //      //</head>
  //      writer.writeEndElement();
  //      //<body>
  //      writer.writeStartElement("body");
  //      //  <p class="resultCode">INVALID_QUERY</p>
  //      writer.writeStartElement("p");
  //      writer.writeAttribute("class", "resultCode");
  //      writer.writeCharacters(resultCode);
  //      writer.writeEndElement();
  //      
  //      //  <p class="resultMessage"></p>
  //      writer.writeStartElement("p");
  //      writer.writeAttribute("class", "resultMessage");
  //      writer.writeCharacters(resultMessage);
  //      writer.writeEndElement();
  //
  //      //  <p class="success">F</p>
  //      writer.writeStartElement("p");
  //      writer.writeAttribute("class", "success");
  //      writer.writeCharacters(success ? "T" : "F");
  //      writer.writeEndElement();
  //
  //      //now we invoke the callback and let the specific response happen
  //      wsLiteResponseHandler.writeResponse(writer);
  //      
  //      //  </body>
  //      writer.writeEndElement();
  //      //</html>
  //      writer.writeEndElement();
  //
  //      writer.writeEndDocument();
  //      
  //    } catch (IOException ioe) {
  //      //if IOException, there isnt much we can do
  //      throw new RuntimeException(ioe);
  //    } catch (XMLStreamException xse) {
  //      //if XMLStreamException, there isnt much we can do
  //      throw new RuntimeException(xse);
  //    } finally {
  //      try {
  //        writer.close();
  //      } catch (XMLStreamException xse) {
  //        //not much we can do if we cant even close it
  //        LOG.error(xse);
  //      }
  //    }
  //  }
}
