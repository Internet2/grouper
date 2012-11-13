/**
 * @author Kate
 * $Id: JsonConverter.java,v 1.1 2009-11-20 07:15:38 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.json;

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
  public <T> T convertFromJson(Class<T> theClass, String json, StringBuilder warnings);
  
}
