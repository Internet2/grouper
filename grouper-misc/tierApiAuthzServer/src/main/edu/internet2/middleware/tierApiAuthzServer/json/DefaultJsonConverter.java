/**
 * @author Chris
 * $Id: DefaultJsonConverter.java,v 1.1 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzServer.json;

import java.io.IOException;
import java.io.Writer;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServer.ws.AsasRestClassLookup;



/**
 * use grouper's default json library
 */
public class DefaultJsonConverter implements JsonConverter {

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertFromJson(java.lang.String, StringBuilder)
   */
  public Object convertFromJson(Class<?> theClass, String json, StringBuilder warnings) {
    Object object = StandardApiServerUtils.jsonConvertFrom(json, theClass);
    return object;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertToJson(java.lang.Object)
   */
  public String convertToJson(Object object) {
    String result = StandardApiServerUtils.jsonConvertToNoWrap(object);
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
