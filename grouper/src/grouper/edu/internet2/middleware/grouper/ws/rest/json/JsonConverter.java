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
 * @author Kate
 * $Id: JsonConverter.java,v 1.1 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.json;

import java.io.Writer;



/**
 * convert objects to json and back.  The implementation does not need to log things, 
 * they will be logged in the caller
 */
public interface JsonConverter {

  /**
   * convert an object to json.  Note, there are only certian aliases which
   * are allowed to be converted from json, so make sure to marshal the container
   * object name somewhere (e.g. in the top level json object)
   * @param object
   * @return the json
   */
  public String convertToJson(Object object);
  
  /**
   * convert an object to json.  Note, there are only certian aliases which
   * are allowed to be converted from json, so make sure to marshal the container
   * object name somewhere (e.g. in the top level json object)
   * @param object to convert to json
   * @param writer write the json here
   */
  public void convertToJson(Object object, Writer writer);
  
  /**
   * convert a json string to an object.  note that only certain object are allowed to be
   * marshaled from json, the aliases in WsRestClassLookup.getAliasClassMap() 
   * @param json
   * @param warnings put warnings here
   * @return the object
   */
  public Object convertFromJson(String json, StringBuilder warnings);
  
}
