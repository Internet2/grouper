/**
 * @author Kate
 * $Id: DefaultJsonConverter.java,v 1.1 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.json;

import java.io.Writer;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;


/**
 * use grouper's default json library
 */
public class DefaultJsonConverter implements JsonConverter {

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertFromJson(java.lang.String, StringBuilder)
   */
  public Object convertFromJson(String json, StringBuilder warnings) {
    Object object = GrouperUtil.jsonConvertFrom(WsRestClassLookup.getAliasClassMap(), json);
    return object;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertToJson(java.lang.Object)
   */
  public String convertToJson(Object object) {
    String result = GrouperUtil.jsonConvertTo(object);
    return result;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertToJson(java.lang.Object, java.io.Writer)
   */
  public void convertToJson(Object object, Writer writer) {
    GrouperUtil.jsonConvertTo(object, writer);
  }

  
  
}
