/*
 * @author mchyzer $Id: WsRestContentType.java,v 1.6 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzClient.contentType;

import java.io.File;

import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacResponseBeanBase;
import edu.internet2.middleware.tierApiAuthzClient.json.DefaultJsonConverter;
import edu.internet2.middleware.tierApiAuthzClient.json.JsonConverter;
import edu.internet2.middleware.tierApiAuthzClient.util.JsonIndenter;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientConfig;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * possible content types by grouper ws rest
 */
public enum AsacRestContentType {

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
    public <T extends AsacResponseBeanBase> T parseString(Class<T> expectedResultClass, String input, StringBuilder warnings) {

      JsonConverter jsonConverter = jsonConverter();
      
      try {
        return jsonConverter.convertFromJson(expectedResultClass, input, warnings);
      } catch (RuntimeException re) {
        LOG.error("Error unparsing string with converter: " + StandardApiClientUtils.className(jsonConverter) + ", " + input);
        throw new RuntimeException("Problem unparsing string with converter: " + StandardApiClientUtils.className(jsonConverter)
            + ", " + StandardApiClientUtils.indent(input, false), re);
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
            + StandardApiClientUtils.className(jsonConverter) + ", " + StandardApiClientUtils.className(object));
        throw new RuntimeException("Error converting json object with converter: " + StandardApiClientUtils.className(jsonConverter)
            + ", " + StandardApiClientUtils.className(object), re);
      }
    }

    @Override
    public String indent(String string) {
      return new JsonIndenter(string).result();
    }
  };

  /** xml header */
  public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  
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
    String jsonConverterClassName = StandardApiClientConfig.retrieveConfig().propertyValueString(
        "jsonConverter", DefaultJsonConverter.class.getName());
    Class<? extends JsonConverter> jsonConverterClass = StandardApiClientUtils.forName(jsonConverterClassName);
    JsonConverter jsonConverter = StandardApiClientUtils.newInstance(jsonConverterClass);
    return jsonConverter;
  }
  
  /**
   * test out a parse
   * @param args
   */
  public static void main(String[] args) {
    String jsonString = StandardApiClientUtils.readFileIntoString(new File("c:/temp/problem.json"));
    AsacRestContentType.json.parseString(AsacResponseBeanBase.class, jsonString, new StringBuilder());
  }
  
  /**
   * parse a string to an object
   * @param input
   * @param warnings is where warnings should be written to
   * @return the object
   * @param T is the class to return
   */
  public abstract <T extends AsacResponseBeanBase> T parseString(Class<T> expectedResultClass, String input, StringBuilder warnings);

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
  private AsacRestContentType(String theContentType) {
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
  private static final Log LOG = LogFactory.getLog(AsacRestContentType.class);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws AsasRestInvalidRequest problem
   */
  public static AsacRestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) {
    return StandardApiClientUtils.enumValueOfIgnoreCase(AsacRestContentType.class, string, exceptionOnNotFound);
  }

}
