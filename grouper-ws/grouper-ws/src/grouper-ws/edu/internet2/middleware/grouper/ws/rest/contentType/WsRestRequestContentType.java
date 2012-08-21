/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer $Id: WsRestRequestContentType.java,v 1.6 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.XStream;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.json.DefaultJsonConverter;
import edu.internet2.middleware.grouper.ws.rest.json.JsonConverter;
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
      return WsRestResponseContentType.xhtml;
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
      String configValue = GrouperWsConfig.retrieveConfig().propertyValueString(
          GrouperWsConfig.WS_REST_DEFAULT_RESPONSE_CONTENT_TYPE);
      
      //default to xhtml if not specified
      configValue = StringUtils.defaultIfEmpty(configValue, "json");
      
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
  /** 
   * json content type, uses the pluggable json converter
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

      JsonConverter jsonConverter = jsonConverter();
      
      try {
        return jsonConverter.convertFromJson(input, warnings);
      } catch (RuntimeException re) {
        LOG.error("Error unparsing string with converter: " + GrouperUtil.className(jsonConverter) + ", " + input);
        throw new RuntimeException("Problem unparsing string with converter: " + GrouperUtil.className(jsonConverter)
            + ", " + GrouperUtil.indent(input, false), re);
      }
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
      JsonConverter jsonConverter = jsonConverter();
      
      try {
        String jsonString = jsonConverter.convertToJson(object);
        return jsonString;
      } catch (RuntimeException re) {
        LOG.error("Error converting json object with converter: " 
            + GrouperUtil.className(jsonConverter) + ", " + GrouperUtil.className(object));
        throw new RuntimeException("Error converting json object with converter: " + GrouperUtil.className(jsonConverter)
            + ", " + GrouperUtil.className(object), re);
      }
    }

  };

  /**
   * instantiate the json convert configured in the grouper-ws.properties file
   * @return the json converter
   */
  @SuppressWarnings("unchecked")
  public static JsonConverter jsonConverter() {
    String jsonConverterClassName = GrouperWsConfig.getPropertyString(
        "jsonConverter", DefaultJsonConverter.class.getName());
    Class<? extends JsonConverter> jsonConverterClass = GrouperUtil.forName(jsonConverterClassName);
    JsonConverter jsonConverter = GrouperUtil.newInstance(jsonConverterClass);
    return jsonConverter;
  }
  
  /**
   * based on the request type, calculate the response type
   * @return the response type or null if there is not a clear winner
   */
  public abstract WsRestResponseContentType calculateResponseContentType();

  /**
   * test out a parse
   * @param args
   */
  public static void main(String[] args) {
    String jsonString = GrouperUtil.readFileIntoString(new File("c:/temp/problem.json"));
    WsRestRequestContentType.json.parseString(jsonString, new StringBuilder());
  }
  
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
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsRestRequestContentType.class);

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
    String theContentTypeBeforeSemi = StringUtils.trim(GrouperUtil.prefixOrSuffix(theContentType, ";", true));
    for (WsRestRequestContentType wsRestRequestContentType : WsRestRequestContentType.values()) {
      if (!StringUtils.isBlank(wsRestRequestContentType.getContentType())) {
        //get before the semi 
        String wsRestRequestContentTypeBeforeSemi = StringUtils.trim(GrouperUtil.prefixOrSuffix(
            wsRestRequestContentType.getContentType(), ";", true));
        if (StringUtils.equals(theContentTypeBeforeSemi, wsRestRequestContentTypeBeforeSemi)) {
          return wsRestRequestContentType;
        }
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
    return GrouperServiceUtils.enumValueOfIgnoreCase(WsRestRequestContentType.class, string, exceptionOnNotFound);
  }

}
