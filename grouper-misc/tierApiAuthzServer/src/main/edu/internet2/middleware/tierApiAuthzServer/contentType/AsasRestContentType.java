/*
 * @author mchyzer $Id: WsRestContentType.java,v 1.6 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzServer.contentType;

import java.io.File;

import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.json.DefaultJsonConverter;
import edu.internet2.middleware.tierApiAuthzServer.json.JsonConverter;
import edu.internet2.middleware.tierApiAuthzServer.util.JsonIndenter;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerConfig;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServer.util.XmlIndenter;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.LogFactory;

/**
 * possible content types by grouper ws rest
 */
public enum AsasRestContentType {

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
    @SuppressWarnings("unchecked")
    @Override
    public <T> T parseString(Class<T> theClass, String input, StringBuilder warnings) {

      JsonConverter jsonConverter = jsonConverter();
      
      try {
        return (T)jsonConverter.convertFromJson(theClass, input, warnings);
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
    AsasRestContentType.json.parseString(Object.class, jsonString, new StringBuilder());
  }
  
  /**
   * parse a string to an object
   * @param input
   * @param warnings is where warnings should be written to
   * @return the object
   * @param <T> is the template type of the object
   */
  public abstract <T> T parseString(Class<T> theClass, String input, StringBuilder warnings);

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
  private AsasRestContentType(String theContentType) {
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
  private static final Log LOG = LogFactory.getLog(AsasRestContentType.class);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws AsasRestInvalidRequest problem
   */
  public static AsasRestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(AsasRestContentType.class, string, exceptionOnNotFound);
  }

  /** content type thread local */
  private static ThreadLocal<AsasRestContentType> contentTypeThreadLocal = new ThreadLocal<AsasRestContentType>();

  /**
   * 
   * @param wsRestContentType
   */
  public static void assignContentType(AsasRestContentType wsRestContentType) {
    contentTypeThreadLocal.set(wsRestContentType);
  }
  
  /**
   * 
   * @param wsRestContentType
   */
  public static void clearContentType() {
    contentTypeThreadLocal.remove();
  }
  
  /**
   * 
   * @return wsRestContentType
   */
  public static AsasRestContentType retrieveContentType() {
    return contentTypeThreadLocal.get();
  }
  

}
