/*
 * @author mchyzer $Id: WsRestContentType.java,v 1.6 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiServer.contentType;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.authzStandardApiServer.json.DefaultJsonConverter;
import edu.internet2.middleware.authzStandardApiServer.json.JsonConverter;
import edu.internet2.middleware.authzStandardApiServer.util.JsonIndenter;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerConfig;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;
import edu.internet2.middleware.authzStandardApiServer.util.XmlIndenter;
import edu.internet2.middleware.authzStandardApiServer.ws.AsasRestClassLookup;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.CompactWriter;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.XppDriver;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.mapper.MapperWrapper;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.logging.LogFactory;

/**
 * possible content types by grouper ws rest
 */
public enum WsRestContentType {

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
      //TODO fix this
      if (true) {
        throw new RuntimeException("Need to fix this");
      }
      return StandardApiServerUtils.xmlConvertFrom(input, null);
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      return StandardApiServerUtils.xmlConvertTo(object);
    }

    @Override
    public String indent(String string) {
      return new XmlIndenter(string).result();
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
        LOG.error("Error unparsing string with converter: " + StandardApiServerUtils.className(jsonConverter) + ", " + input);
        throw new RuntimeException("Problem unparsing string with converter: " + StandardApiServerUtils.className(jsonConverter)
            + ", " + StandardApiServerUtils.indent(input, false), re);
      }
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
            + StandardApiServerUtils.className(jsonConverter) + ", " + StandardApiServerUtils.className(object));
        throw new RuntimeException("Error converting json object with converter: " + StandardApiServerUtils.className(jsonConverter)
            + ", " + StandardApiServerUtils.className(object), re);
      }
    }

    @Override
    public String indent(String string) {
      return new JsonIndenter(string).result();
    }
  };

  /**
   * indent the content
   * @return the indented content
   */
  public abstract String indent(String string);
  
  /**
   * instantiate the json convert configured in the grouper-ws.properties file
   * @return the json converter
   */
  @SuppressWarnings("unchecked")
  public static JsonConverter jsonConverter() {
    String jsonConverterClassName = StandardApiServerConfig.retrieveConfig().propertyValueString(
        "jsonConverter", DefaultJsonConverter.class.getName());
    Class<? extends JsonConverter> jsonConverterClass = StandardApiServerUtils.forName(jsonConverterClassName);
    JsonConverter jsonConverter = StandardApiServerUtils.newInstance(jsonConverterClass);
    return jsonConverter;
  }
  
  /**
   * test out a parse
   * @param args
   */
  public static void main(String[] args) {
    String jsonString = StandardApiServerUtils.readFileIntoString(new File("c:/temp/problem.json"));
    WsRestContentType.json.parseString(jsonString, new StringBuilder());
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
  private WsRestContentType(String theContentType) {
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
  private static final Log LOG = LogFactory.getLog(WsRestContentType.class);

  /**
   * friendly content type error
   * @param contentTypeInRequest 
   * @return the friendly content type error
   */
  private static String contentTypeError(String contentTypeInRequest) {
    StringBuilder error = new StringBuilder(
    "Cant find http request Content-type header from request, received: '");
    error.append(StandardApiServerUtils.defaultIfEmpty(contentTypeInRequest, "[none]"));
    error.append("', expecting one of: ");
    
    for (WsRestContentType wsRestRequestContentType : 
        WsRestContentType.values()) {
      String contentTypeString = StandardApiServerUtils.defaultIfEmpty(wsRestRequestContentType.contentType,
          "[none] for http params");
      error.append(wsRestRequestContentType.name()).append(": ")
        .append(contentTypeString).append(", ");
    }
    return error.toString();
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws AsasRestInvalidRequest problem
   */
  public static WsRestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(WsRestContentType.class, string, exceptionOnNotFound);
  }

}
