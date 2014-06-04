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
