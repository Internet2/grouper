/**
 * @author Chris
 * $Id: DefaultJsonConverter.java,v 1.1 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.json;

import java.io.IOException;
import java.io.Writer;

import edu.internet2.middleware.authzStandardApiClient.contentType.AsacRestContentType;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultResourceContainer;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;



/**
 * use grouper's default json library
 */
public class DefaultJsonConverter implements JsonConverter {

  public static void main(String[] args) {
    String string = "{\n"
        + "\"asasDefaultResource\":{\n"
        + "\"jsonDefaultUri\":\"http://localhost:8090/authzStandardApi/authzStandardApi.json\",\n"
        + "\"xmlDefaultUri\":\"http://localhost:8090/authzStandardApi/authzStandardApi.xml\"\n"
        + "},\n"
        + "\"meta\":{\n"
        + "\"lastModified\":\"2012-11-13T21:48:32.036Z\",\n"
        + "\"selfUri\":\"http://localhost:8090/authzStandardApi/authzStandardApi\",\n"
        + "\"statusCode\":\"SUCCESS\",\n"
        + "\"structureName\":\"defaultResourceContainer\",\n"
        + "\"success\":true\n"
        + "},\n"
        + "\"responseMeta\":{\n"
        + "\"httpStatusCode\":200,\n"
        + "\"millis\":3\n"
        + "},\n"
        + "\"serviceMeta\":{\n"
        + "\"serverVersion\":\"1.0\",\n"
        + "\"serviceRootUri\":\"http://localhost:8090/authzStandardApi/authzStandardApi\"\n"
        + "}\n"
        + "}";
    
    AsacDefaultResourceContainer asacDefaultResourceContainer = 
        AsacRestContentType.json.parseString(AsacDefaultResourceContainer.class, string, new StringBuilder());
    
    System.out.println(asacDefaultResourceContainer.getMeta().getSelfUri());
    
  }
  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertFromJson(java.lang.String, StringBuilder)
   */
  public <T> T convertFromJson(Class<T> theClass, String json, StringBuilder warnings) {
    return StandardApiClientUtils.jsonConvertFrom(json, theClass);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertToJson(java.lang.Object)
   */
  public String convertToJson(Object object) {
    String result = StandardApiClientUtils.jsonConvertToNoWrap(object);
    return result;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertToJson(java.lang.Object, java.io.Writer)
   */
  public void convertToJson(Object object, Writer writer) {
    String json = convertToJson(object);
    try {
      writer.write(json);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  
  
}
