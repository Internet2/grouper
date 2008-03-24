/*
 * @author mchyzer $Id: WsLiteRequestContentType.java,v 1.1 2008-03-24 20:19:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.contentType;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.lite.GrouperLiteInvalidRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * possible content types by grouper ws lite
 */
public enum WsLiteRequestContentType {

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
    public WsLiteResponseContentType calculateResponseContentType() {
      return WsLiteResponseContentType.xhtml;
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      StringWriter stringWriter = new StringWriter();
      WsLiteResponseContentType.xhtml.writeString(object, stringWriter);
      return stringWriter.toString();
    }
  },
  /** http params set fields in a simple object
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
      Object object = GrouperServiceUtils.marshalHttpParamsToObject(
          httpServletRequest.getParameterMap(), httpServletRequest, warnings);
      return object;
    }

    /**
     * based on the request type, calculate the response type
     * @return the response type
     */
    @Override
    public WsLiteResponseContentType calculateResponseContentType() {
      
      //not specified, get from config file
      String configValue = GrouperWsConfig.getPropertyString(
          GrouperWsConfig.WS_LITE_DEFAULT_RESPONSE_CONTENT_TYPE);
      
      //default to xhtml if not specified
      configValue = StringUtils.defaultIfEmpty(configValue, "xhtml");
      
      //convert to enum
      return WsLiteResponseContentType.valueOfIgnoreCase(configValue, true);
    }
    
    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      return GrouperServiceUtils.marshalSimpleBeanToQueryString(object, true, true);
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
      XStream xStream = WsLiteResponseContentType.xstream(false);
      Object object = xStream.fromXML(input);
      return object;
    }

    /**
     * based on the request type, calculate the response type
     * @return the response type
     */
    @Override
    public WsLiteResponseContentType calculateResponseContentType() {
      return WsLiteResponseContentType.xml;
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      StringWriter stringWriter = new StringWriter();
      WsLiteResponseContentType.xml.writeString(object, stringWriter);
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
      XStream xStream = WsLiteResponseContentType.xstream(true);
      Object object = xStream.fromXML(input);
      return object;
    }

    /**
     * based on the request type, calculate the response type
     * @return the response type
     */
    @Override
    public WsLiteResponseContentType calculateResponseContentType() {
      return WsLiteResponseContentType.json;
    }
    
    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      StringWriter stringWriter = new StringWriter();
      WsLiteResponseContentType.json.writeString(object, stringWriter);
      return stringWriter.toString();
    }

  };

  /**
   * based on the request type, calculate the response type
   * @return the response type
   */
  public abstract WsLiteResponseContentType calculateResponseContentType();
  
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
  private WsLiteRequestContentType(String theContentType) {
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
    
    for (WsLiteRequestContentType wsLiteRequestContentType : 
        WsLiteRequestContentType.values()) {
      String contentTypeString = StringUtils.defaultIfEmpty(wsLiteRequestContentType.contentType,
          "[none] for http params");
      error.append(wsLiteRequestContentType.name()).append(": ")
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
  public static WsLiteRequestContentType findByContentType(String theContentType, String requestBody) {
    
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
    for (WsLiteRequestContentType wsLiteRequestContentType : WsLiteRequestContentType.values()) {
      if (!StringUtils.isBlank(wsLiteRequestContentType.getContentType()) 
          && theContentType.startsWith(wsLiteRequestContentType.getContentType())) {
        return wsLiteRequestContentType;
      }
      //handle null
      if (StringUtils.isBlank(theContentType) 
          && StringUtils.isBlank(wsLiteRequestContentType.getContentType())) {
        return wsLiteRequestContentType;
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
   * @throws GrouperLiteInvalidRequest if problem
   */
  public static WsLiteRequestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperLiteInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (WsLiteRequestContentType wsLiteRequestContentType : WsLiteRequestContentType
        .values()) {
      if (StringUtils.equalsIgnoreCase(string, wsLiteRequestContentType.name())) {
        return wsLiteRequestContentType;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find wsLiteRequestContentType from string: '").append(string);
    error.append("', expecting one of: ");
    for (WsLiteRequestContentType wsLiteRequestContentType : WsLiteRequestContentType
        .values()) {
      error.append(wsLiteRequestContentType.name()).append(", ");
    }
    throw new GrouperLiteInvalidRequest(error.toString());
  }

}
