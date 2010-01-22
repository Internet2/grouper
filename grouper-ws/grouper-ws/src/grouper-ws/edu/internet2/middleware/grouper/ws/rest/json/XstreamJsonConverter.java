/**
 * @author mchyzer
 * $Id: XstreamJsonConverter.java,v 1.1 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.json;

import java.io.StringWriter;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestResponseContentType;


/**
 * legacy (deprecated) json converter, doesnt always work correctly, sometimes
 * can unmarshal things that it marshals
 */
public class XstreamJsonConverter implements JsonConverter {

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertFromJson(java.lang.String, StringBuilder)
   */
  public Object convertFromJson(String json, StringBuilder warnings) {
    XStream xStream = WsRestResponseContentType.xstream(true);
    Object object = xStream.fromXML(json);
    return object;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertToJson(java.lang.Object)
   */
  public String convertToJson(Object object) {
    StringWriter stringWriter = new StringWriter();
    convertToJson(object, stringWriter);
    return stringWriter.toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.json.JsonConverter#convertToJson(java.lang.Object, java.io.Writer)
   */
  public void convertToJson(Object object, Writer writer) {
    XStream xstream = WsRestResponseContentType.xstream(true);
    xstream.toXML(object, writer);
  }

}
