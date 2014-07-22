/**
 * Copyright 2014 Internet2
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
 */
package edu.internet2.middleware.grouperClientExt.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvents;


/**
 * @author mchyzer
 *
 */
public class JsonUtils {

  /**
     * convert an object from json.  note this works well if there are no collections, just real types, arrays, etc.
     * @param conversionMap is the class simple name to class of objects which are allowed to be brought back.
     * Note: only the top level object needs to be registered
     * @param json
     * @return the object
     */
    public static Object jsonConvertFrom(Map<String, Class<?>> conversionMap, String json) {
      
      //gson does not put the type of the object in the json, but we need that.  so when we convert,
      //put the type in there.  So we need to extract the type out when unmarshaling
      Matcher matcher = jsonPattern.matcher(json);
      
      if (!matcher.matches()) {
        throw new RuntimeException("Cant match this json, should start with simple class name: " + json);
      }
      
      String simpleClassName = matcher.group(1);
      String jsonBody = matcher.group(2);
      
      Class<?> theClass = conversionMap.get(simpleClassName);
      if (theClass == null) {
        throw new RuntimeException("Not allowed to unmarshal json: " + simpleClassName + ", " + json);
      }
  //    Gson gson = new GsonBuilder().create();
  //    Object object = gson.fromJson(jsonBody, theClass);
      JSONObject jsonObject = JSONObject.fromObject( jsonBody );
      Object object = JSONObject.toBean( jsonObject, theClass );  
  
      return object;
    }

  /**
   * convert an object from json.  note this works well if there are no collections, just real types, arrays, etc.
   * @param json is the json string, not wrapped with a simple class name
   * @param theClass is the class that the object should be coverted into.
   * Note: only the top level object needs to be registered
   * @return the object
   */
  public static Object jsonConvertFrom (String json, Class<?> theClass) {
    	JSONObject jsonObject = JSONObject.fromObject( json );
      Object object = JSONObject.toBean( jsonObject, theClass );  
      return object;
    
  }

  /**
     * convert an object to json.
     * @param object
     * @return the string of json
     */
    public static String jsonConvertTo(Object object) {
      if (object == null) {
        throw new NullPointerException();
      }
  //    Gson gson = new GsonBuilder().create();
  //    String json = gson.toJson(object);
  
  //    JSONObject jsonObject = net.sf.json.JSONObject.fromObject( object );  
  //    String json = jsonObject.toString();
  
      JsonConfig jsonConfig = new JsonConfig();  
      jsonConfig.setJsonPropertyFilter( new PropertyFilter(){  
         public boolean apply( Object source, String name, Object value ) {  
            return value == null; 
         }  
      });  
      JSONObject jsonObject = JSONObject.fromObject( object, jsonConfig );  
      String json = jsonObject.toString();
      
      return "{\"" + object.getClass().getSimpleName() + "\":" + json + "}";
    }

  /**
   * convert an object to json.  note this wraps the gson with the object simple name so it can be revived
   * @param object
   * @param writer 
   */
  public static void jsonConvertTo(Object object, Writer writer) {
    String json = jsonConvertTo(object);
    try {
      writer.write(json);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * convert an object to json without wrapping it with the simple class name.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertToNoWrap(Object object) {
      if (object == null) {
        throw new NullPointerException();
      }
  
      JsonConfig jsonConfig = new JsonConfig();  
      jsonConfig.setJsonPropertyFilter( new PropertyFilter(){  
         public boolean apply( Object source, String name, Object value ) {  
            return value == null; 
         }  
      });  
      JSONObject jsonObject = JSONObject.fromObject( object, jsonConfig );  
      String json = jsonObject.toString();
      
      return json;
    }

  /**
   * <pre>
   * detects the front of a json string, pops off the first field, and gives the body as the matcher
   * ^\s*\{\s*\"([^"]+)\"\s*:\s*\{(.*)}$
   * Example matching text:
   * {
   *  "XstreamPocGroup":{
   *    "somethingNotMarshaled":"whatever",
   *    "name":"myGroup",
   *    "someInt":5,
   *    "someBool":true,
   *    "members":[
   *      {
   *        "name":"John",
   *        "description":"John Smith - Employee"
   *      },
   *      {
   *        "name":"Mary",
   *        "description":"Mary Johnson - Student"
   *      }
   *    ]
   *  }
   * }
   * 
   * ^\s*          front of string and optional space
   * \{\s*         open bracket and optional space
   * \"([^"]+)\"   quote, simple name of class, quote
   * \s*:\s*       optional space, colon, optional space
   * \{(.*)}$      open bracket, the class info, close bracket, end of string
   * 
   * 
   * </pre>
   */
  private static Pattern jsonPattern = Pattern.compile("^\\s*\\{\\s*\\\"([^\"]+)\\\"\\s*:\\s*(.*)}$", Pattern.DOTALL);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    String jsonEventString = "{\"esbEvent\":[{\"displayName\":\"test:isc:ait:mchyzer:test\"," +
    		"\"eventType\":\"GROUP_ADD\",\"id\":\"917a3c80afee4e2c8d008e16e743eabe\"," +
    		"\"name\":\"test:isc:ait:mchyzer:test\",\"parentStemId\":\"091f20d5-a29b-4817-95f8-213b9573194d\"}]}";

    EsbEvents esbEvents = (EsbEvents)JsonUtils.jsonConvertFrom(jsonEventString, EsbEvents.class);

    System.out.println(esbEvents.getEsbEvent()[0].getEventType());
    
  }
  
}
