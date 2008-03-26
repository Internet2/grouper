/*
 * @author mchyzer $Id: WsRestRequestContentType.java,v 1.2 2008-03-26 07:39:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * possible content types by grouper ws rest
 */
public enum WsRestRequestContentType {

  /** default xhtml content type
   * http request content type should be set to application/xhtml+xml
   */
  xhtml("application/xhtml+xml") {

    /**
     * parse a string to an object
     * @param input
     * @param warnings is where warnings should be written to
     * @return the object
     */
    @Override
    public Object parseString(String input, StringBuilder warnings) {
      WsXhtmlInputConverter wsXhtmlInputConverter = new WsXhtmlInputConverter();
      Object object = wsXhtmlInputConverter.parseXhtmlString(input);
      return object;
    }

    /**
     * based on the request type, calculate the response type
     * @return the response type
     */
    @Override
    public WsRestResponseContentType calculateResponseContentType() {
      return null;
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      StringWriter stringWriter = new StringWriter();
      WsRestResponseContentType.xhtml.writeString(object, stringWriter);
      return stringWriter.toString();
    }
  },
  /** http params set fields in a lite object
   * http request content type should not be set, or set to:
   * application/x-www-form-urlencoded
   */
  http(null) {

    /**
     * parse a string to an object
     * @param input
     * @param warnings is where warnings should be written to
     * @return the object
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object parseString(String input, StringBuilder warnings) {
      HttpServletRequest httpServletRequest = GrouperServiceJ2ee.retrieveHttpServletRequest();
      //marshal the object out of there
      Map<String, String> params = httpServletRequest.getParameterMap();
      
      //see if in query string in body
      if (!StringUtils.isBlank(input)) {
        params = GrouperServiceUtils.convertQueryStringToMap(input);
        //dont worry about params in request anymore
        httpServletRequest = null;
      }
      Object object = GrouperServiceUtils.marshalHttpParamsToObject(
          params, httpServletRequest, warnings);
      return object;
    }

    /**
     * based on the request type, calculate the response type
     * @return the response type
     */
    @Override
    public WsRestResponseContentType calculateResponseContentType() {
      
      //not specified, get from config file
      String configValue = GrouperWsConfig.getPropertyString(
          GrouperWsConfig.WS_REST_DEFAULT_RESPONSE_CONTENT_TYPE);
      
      //default to xhtml if not specified
      configValue = StringUtils.defaultIfEmpty(configValue, "xhtml");
      
      //convert to enum
      return WsRestResponseContentType.valueOfIgnoreCase(configValue, true);
    }
    
    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      return GrouperServiceUtils.marshalLiteBeanToQueryString(object, false, true);
    }

  },
  /** xml content type
   * http request content type should be set to text/xml
   */
  xml("text/xml") {

    /**
     * parse a string to an object
     * @param input
     * @param warnings is where warnings should be written to
     * @return the object
     */
    @Override
    public Object parseString(String input, StringBuilder warnings) {
      XStream xStream = WsRestResponseContentType.xstream(false);
      Object object = xStream.fromXML(input);
      return object;
    }

    /**
     * based on the request type, calculate the response type
     * @return the response type
     */
    @Override
    public WsRestResponseContentType calculateResponseContentType() {
      return WsRestResponseContentType.xml;
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      StringWriter stringWriter = new StringWriter();
      WsRestResponseContentType.xml.writeString(object, stringWriter);
      return stringWriter.toString();
    }
  },
  /** json content type
   * http request content type should be set to text/x-json
   */
  json("text/x-json") {

    /**
     * parse a string to an object
     * @param input
     * @param warnings is where warnings should be written to
     * @return the object
     */
    @Override
    public Object parseString(String input, StringBuilder warnings) {
      XStream xStream = WsRestResponseContentType.xstream(true);
      Object object = xStream.fromXML(input);
      return object;
    }

    /**
     * based on the request type, calculate the response type
     * @return the response type
     */
    @Override
    public WsRestResponseContentType calculateResponseContentType() {
      return WsRestResponseContentType.json;
    }
    
    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      StringWriter stringWriter = new StringWriter();
      WsRestResponseContentType.json.writeString(object, stringWriter);
      return stringWriter.toString();
    }

  };

  /**
   * based on the request type, calculate the response type
   * @return the response type or null if there is not a clear winner
   */
  public abstract WsRestResponseContentType calculateResponseContentType();
  
  /**
   * parse a string to an object
   * @param input
   * @param warnings is where warnings should be written to
   * @return the object
   */
  public abstract Object parseString(String input, StringBuilder warnings);

  /**
   * write a string representation to result string
   * @param object to write to output
   * @return the string representation
   */
  public abstract String writeString(Object object);

  /**
   * constructor with content type
   * @param theContentType
   */
  private WsRestRequestContentType(String theContentType) {
    this.contentType = theContentType;
  }
  
  /** content type of request */
  private String contentType;
  
  /**
   * content type header for http
   * @return the content type
   */
  public String getContentType() {
    return this.contentType;
  }
  
  /**
   * friendly content type error
   * @param contentTypeInRequest 
   * @return the friendly content type error
   */
  private static String contentTypeError(String contentTypeInRequest) {
    StringBuilder error = new StringBuilder(
    "Cant find http request Content-type header from request, received: '");
    error.append(StringUtils.defaultIfEmpty(contentTypeInRequest, "[none]"));
    error.append("', expecting one of: ");
    
    for (WsRestRequestContentType wsRestRequestContentType : 
        WsRestRequestContentType.values()) {
      String contentTypeString = StringUtils.defaultIfEmpty(wsRestRequestContentType.contentType,
          "[none] for http params");
      error.append(wsRestRequestContentType.name()).append(": ")
        .append(contentTypeString).append(", ");
    }
    return error.toString();
  }
  
  /**
   * find a request content type by content type
   * @param theContentType
   * @param requestBody is the request onverted to a body
   * @return the requestContentType
   */
  public static WsRestRequestContentType findByContentType(String theContentType, String requestBody) {
    
    //note, are doing a starts with since charset could be in there too... e.g. Content-Type: text/xml; charset=UTF-8
    
    //if form data, massage to none
    theContentType = StringUtils.trimToEmpty(theContentType).toLowerCase();

    //this means a form, which is http params, so massage to none
    if (theContentType.startsWith("application/x-www-form-urlencoded")) {
      theContentType = "";
    }
    
    requestBody = StringUtils.trimToEmpty(requestBody);
    if (StringUtils.isBlank(theContentType)) {
      if (requestBody.startsWith("<") || requestBody.startsWith("{")) {
        throw new WsInvalidQueryException("No request HTTP Content-type: header, " +
        		"but there was content detected in request: '" + StringUtils.abbreviate(requestBody, 20) + "', " 
        		+ contentTypeError(null));
      }
    }
    for (WsRestRequestContentType wsRestRequestContentType : WsRestRequestContentType.values()) {
      if (!StringUtils.isBlank(wsRestRequestContentType.getContentType()) 
          && theContentType.startsWith(wsRestRequestContentType.getContentType())) {
        return wsRestRequestContentType;
      }
      //handle null
      if (StringUtils.isBlank(theContentType) 
          && StringUtils.isBlank(wsRestRequestContentType.getContentType())) {
        return wsRestRequestContentType;
      }
      
    }
    throw new WsInvalidQueryException(contentTypeError(theContentType));
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if problem
   */
  public static WsRestRequestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (WsRestRequestContentType wsRestRequestContentType : WsRestRequestContentType
        .values()) {
      if (StringUtils.equalsIgnoreCase(string, wsRestRequestContentType.name())) {
        return wsRestRequestContentType;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find wsLiteRequestContentType from string: '").append(string);
    error.append("', expecting one of: ");
    for (WsRestRequestContentType wsLiteRequestContentType : WsRestRequestContentType
        .values()) {
      error.append(wsLiteRequestContentType.name()).append(", ");
    }
    throw new GrouperRestInvalidRequest(error.toString());
  }

}
