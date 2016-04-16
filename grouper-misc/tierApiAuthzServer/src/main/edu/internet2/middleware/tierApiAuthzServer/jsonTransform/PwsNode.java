/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.jsonTransform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.net.sf.json.JSONArray;
import edu.internet2.middleware.tierApiAuthzServerExt.net.sf.json.JSONNull;
import edu.internet2.middleware.tierApiAuthzServerExt.net.sf.json.JSONObject;
import edu.internet2.middleware.tierApiAuthzServerExt.net.sf.json.JSONSerializer;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.StringUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.math.NumberUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.LogFactory;


/**
 * node of the representation
 */
public class PwsNode {

  /**
   * see if has no value
   * @return true if has no value (e.g. null)
   */
  public boolean isHasNoValue() {
    
    if (this.arrayType) {
      return this.array == null || this.array.size() == 0;
    }
    
    if (this.pwsNodeType == PwsNodeType.object) {
      return this.object == null || this.object.size() == 0;
    }
    
    return this.getValue() == null;
  }
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(PwsNode.class);

  /**
   * type cast a node and assign to another node
   * @param toNode
   * @param fromNode
   * @param typeCastClass
   */
  public static void typeCast(PwsNode toNode, PwsNode fromNode, Class<?> typeCastClass) {

    Object value = fromNode.getValue();
    
    if (typeCastClass == Object.class) {
      
      if (StandardApiServerUtils.isBlank(value)) {
        toNode.setPwsNodeType(PwsNodeType.object);
        
        //clear out the sub objects
        Map<String, PwsNode> theObjects = toNode.getFields();
        if (theObjects != null) {
          theObjects.clear();
        }
        
      } else {
        throw new RuntimeException("Cant type cast a non null scalar to an object: " + fromNode);
      }
      
    } else if (typeCastClass == String.class) {
      
      toNode.setPwsNodeType(PwsNodeType.string);
      
      if (value == null) {
        toNode.setString(null);
      } else {
        toNode.setString(value.toString());
      }
      
    } else if (typeCastClass == Long.class) {
      
      toNode.setPwsNodeType(PwsNodeType.integer);
      
      if (value == null) {
        toNode.setInteger(null);
      } else {
        toNode.setInteger(StandardApiServerUtils.longObjectValue(value, true));
      }
      
    } else if (typeCastClass == Double.class) {
      
      toNode.setPwsNodeType(PwsNodeType.floating);
      
      if (value == null) {
        toNode.setFloating(null);
      } else {
        toNode.setFloating(StandardApiServerUtils.doubleObjectValue(value, true));
      }

    } else {
      
      throw new RuntimeException("Expecting type Object, Long, "
          + "Double, Boolean, String, but was: " + typeCastClass.getName());

    }

  }

  /**
   * return the subobjects
   * @return the subobjects
   */
  public Map<String, PwsNode> getFields() {
    
    //allow empty type if empty fields
    if (this.getPwsNodeType() == null && StandardApiServerUtils.length(this.object) == 0) {
      return this.object;
    }
    
    if (this.getPwsNodeType() != PwsNodeType.object) {
      throw new RuntimeException("Expecting object but was: " + this.getPwsNodeType());
    }
    
    return this.object;
  }

  /**
   * get the value of the object
   * @return the object
   */
  public Object getValue() {

    if (this.isArrayType()) {
      throw new RuntimeException("Not expecting array: " + this);
    }
    
    switch(this.getPwsNodeType()) {
      case bool:
        return this.getBool();
      case floating:
        return this.getFloating();
      case integer:
        return this.getInteger();
      case object:
        if (StandardApiServerUtils.length(this.object) == 0) {
          return null;
        }
        throw new RuntimeException("Not expecting non-empty object: " + this);
      case string:
        return this.getString();
      default:
        throw new RuntimeException("Not expecting node type: " + this.getPwsNodeType());
    }
    
  }
  
  /**
   * see if a scalar node equals this value (massage the type)
   * @param expectedValue 
   * @return the result
   */
  public boolean equalsScalar(Object expectedValue) {

    if (this.getPwsNodeType() == null && expectedValue == null) {
      return true;
    }

    if (this.getPwsNodeType() == null) {
      throw new RuntimeException("Trying to test a scalar equality?  "
          + "Should be initialized and not null node type! " 
          + this);
    }
    
    if (this.getPwsNodeType() == PwsNodeType.object) {
      throw new RuntimeException("Trying to test a scalar equality?  Should be a scalar! " 
          + this);
    }

    if (this.getPwsNodeType() == PwsNodeType.object) {
      throw new RuntimeException("Trying to test a scalar equality?  Should be a scalar! " 
          + this);
    }

    if (this.isArrayType()) {
      throw new RuntimeException("Why would an attribute selector target be an array?  Should be a scalar! " 
          + this);
    }

    //lets see if the value equals
    switch(this.getPwsNodeType()) {

      case bool:
        
        if (expectedValue == this.bool) {
          return true;
        }
        {
          //must be a boolean
          Boolean expectedBoolean = null;
          
          if (expectedValue instanceof Boolean) {
            expectedBoolean = (Boolean)expectedValue;
          } else if (expectedValue instanceof String) {
            expectedBoolean = StandardApiServerUtils.booleanObjectValue(expectedValue);
          } else {
            throw new RuntimeException("Expecting type of boolean or string, but was: " + expectedValue.getClass().getName() + ", " + expectedValue);
          }
  
          if (expectedBoolean == null || this.bool == null) {
            return false;
          }
          
          return this.bool.equals(expectedBoolean);
        }
        
      case floating:

        if (expectedValue == this.floating) {
          return true;
        }

        {
          //must be a boolean
          Double expectedDouble = null;
          
          if (expectedValue instanceof Long) {
            expectedDouble = ((Long)expectedValue).doubleValue();
          } else if (expectedValue instanceof Double) {
            expectedDouble = (Double)expectedValue;
          } else if (expectedValue instanceof String) {
            expectedDouble = StandardApiServerUtils.doubleObjectValue(expectedValue, true);
          } else {
            throw new RuntimeException("Expecting type of double, long, or string, but was: " + expectedValue.getClass().getName() + ", " + expectedValue);
          }
  
          if (expectedDouble == null || this.bool == null) {
            return false;
          }
          return NumberUtils.compare(this.floating, expectedDouble) == 0;
        }

      case integer:

        if (expectedValue == this.integer) {
          return true;
        }
        if (this.integer == null || expectedValue == null) {
          return false;
        }

        {
          //must be a boolean
          Long expectedLong = null;
          
          if (expectedValue instanceof Long) {
            expectedLong = (Long)expectedValue;
            return this.integer.equals(expectedLong);
          }
          
          if (expectedValue instanceof Double) {
            Double thisFloating = this.integer.doubleValue();
            return NumberUtils.compare(thisFloating, (Double)expectedValue) == 0;
          }

          //do this as double...
          if (expectedValue instanceof String) {
            Double expectedDouble = StandardApiServerUtils.doubleObjectValue(expectedValue, true);
            if (expectedDouble == null) {
              return false;
            }
            Double thisFloating = this.integer.doubleValue();
            return NumberUtils.compare(thisFloating, expectedDouble) == 0;
          }
          
          throw new RuntimeException("Expecting type of double, long, or string, but was: " + expectedValue.getClass().getName() + ", " + expectedValue);
        }
      case string:

        if (expectedValue == this.string) {
          return true;
        }
        if (this.string == null || expectedValue == null) {
          return false;
        }

        {
          //must be a boolean
          String expectedString = null;
          
          if (expectedValue instanceof String) {
            //string easy
            expectedString = (String)expectedValue;
          } else if (expectedValue instanceof Boolean) {
            
            //for boolean, lets not go to string, lets try (failsafe) to go to boolean
            Boolean expectedBoolean = (Boolean)expectedValue;
            Boolean thisBoolean = null;
            try {
              thisBoolean = StandardApiServerUtils.booleanObjectValue(this.string);
            } catch (Exception e) {
              LOG.debug("Cant parse boolean: '" + expectedValue + "'");
              return false;
            }

            if (expectedBoolean == thisBoolean) {
              return true;
            }
            
            if (expectedBoolean == null || thisBoolean == null) {
              return false;
            }
            
            return expectedBoolean.equals(thisBoolean);
            
          } else if (expectedValue instanceof Double) {
            expectedString = Double.toString((Double)expectedValue);
          } else if (expectedValue instanceof Long) {
            expectedString = Long.toString((Long)expectedValue);
          } else {
            throw new RuntimeException("Expecting type of double, long, or string, but was: " + expectedValue.getClass().getName() + ", " + expectedValue);
          }
          return StringUtils.equals(expectedString, this.string);
        }

        
      //object is already handled  
      case object:
      default:
        throw new RuntimeException("Not expecting object type: " + this.getPwsNodeType());
    }

  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("{");
    result.append("fromFieldName: ").append(this.fromFieldName);
    result.append(", nodeType: ").append(this.pwsNodeType == null ? null : this.pwsNodeType.name());
    if (this.arrayType) {
      result.append(", arrayType: ").append(this.arrayType);
      result.append(", arraySize: ").append(StandardApiServerUtils.length(this.array));
    } else {
      if (this.pwsNodeType != null) {
        switch(this.pwsNodeType) {
          case bool:
            result.append(", bool: ").append(this.bool);
            break;
          case floating:
            result.append(", floating: ").append(this.floating);
            break;
          case integer:
            result.append(", integer: ").append(this.integer);
            break;
          case object:
            result.append(", objectFieldSize: ").append(StandardApiServerUtils.length(this.object));
            break;
          case string:
            result.append(", string: '").append(StandardApiServerUtils.abbreviate(this.string, 20)).append("'");
            break;
          default:
            throw new RuntimeException("Not expecting type: " + this.pwsNodeType);  
        }
      }
    }
    result.append("}");
    return result.toString();
  }
  
  /**
   * field name we are coming from (for debugging reasons)
   */
  private String fromFieldName;
  
  /**
   * link back up to the parent node
   */
  private PwsNode fromNode;
  
  /**
   * link back up to the parent node
   * @return the fromNode
   */
  public PwsNode getFromNode() {
    return this.fromNode;
  }
  
  /**
   * link back up to the parent node
   * @param fromNode1 the fromNode to set
   */
  public void setFromNode(PwsNode fromNode1) {
    this.fromNode = fromNode1;
  }

  /**
   * field name we are coming from (for debugging reasons)
   * @return the fromFieldName
   */
  public String getFromFieldName() {
    return this.fromFieldName;
  }
  
  /**
   * field name we are coming from (for debugging reasons)
   * @param fromFieldName1 the fromFieldName to set
   */
  public void setFromFieldName(String fromFieldName1) {
    this.fromFieldName = fromFieldName1;
  }

  /**
   * 
   * @param someInteger
   */
  public PwsNode(Long someInteger) {
    this.setPwsNodeType(PwsNodeType.integer);
    this.integer = someInteger;
  }
  
  /**
   * 
   * @param someInteger
   */
  public PwsNode(Integer someInteger) {
    this.setPwsNodeType(PwsNodeType.integer);
    this.integer = someInteger == null ? null : someInteger.longValue();
  }
  
  /**
   * construct with boolean
   * @param someBoolean
   */
  public PwsNode(Boolean someBoolean) {
    this.setPwsNodeType(PwsNodeType.bool);
    this.bool = someBoolean;
  }
  
  /**
   * construct with floating
   * @param someFloating
   */
  public PwsNode(Double someFloating) {
    this.setPwsNodeType(PwsNodeType.floating);
    this.floating = someFloating;
  }
  
  /**
   * 
   * @param someString
   */
  public PwsNode(String someString) {
    this.setPwsNodeType(PwsNodeType.string);
    this.string = someString;
  }
  
  /**
   * which type of node this is
   * @return the pwsNodeType
   */
  public PwsNodeType getPwsNodeType() {
    return this.pwsNodeType;
  }

  
  /**
   * which type of node this is
   * @param pwsNodeType1 the pwsNodeType to set
   */
  public void setPwsNodeType(PwsNodeType pwsNodeType1) {
    this.pwsNodeType = pwsNodeType1;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    PwsNode base = new PwsNode(PwsNodeType.object);
    base.assignField("someInteger", new PwsNode(45L));
    base.assignField("someFloat", new PwsNode(34.567));
    base.assignField("someFloatInt", new PwsNode(34D));
    base.assignField("someBoolTrue", new PwsNode(true));
    base.assignField("someBoolFalse", new PwsNode(false));
    base.assignField("someString", new PwsNode("some string"));
    base.assignField("nullString", new PwsNode((String)null));
    base.assignField("nullInteger", new PwsNode((Long)null));
    
    PwsNode sub = new PwsNode(PwsNodeType.object);
    sub.assignField("subInteger", new PwsNode(37L));
    sub.assignField("subString", new PwsNode("sub string"));

    base.assignField("sub", sub);

    PwsNode sub1 = new PwsNode(PwsNodeType.object);
    sub1.assignField("subInteger", new PwsNode(37L));
    sub1.assignField("subString", new PwsNode("sub string"));
    PwsNode sub2 = new PwsNode(PwsNodeType.object);
    sub2.assignField("subInteger", new PwsNode(37L));
    sub2.assignField("subString", new PwsNode("sub string"));
    
    PwsNode arraySub = new PwsNode(PwsNodeType.object);
    arraySub.setArrayType(true);
    
    arraySub.addArrayItem(sub1);
    arraySub.addArrayItem(sub2);
    
    base.assignField("arraySub", arraySub);

    PwsNode arrayInteger = new PwsNode(PwsNodeType.integer);
    arrayInteger.setArrayType(true);
    arrayInteger.addArrayItem(new PwsNode(28L));
    arrayInteger.addArrayItem(new PwsNode(17L));
    arrayInteger.addArrayItem(new PwsNode(9L));

    base.assignField("arrayInteger", arrayInteger);

    PwsNode arrayString = new PwsNode(PwsNodeType.string);
    arrayString.setArrayType(true);
    arrayString.addArrayItem(new PwsNode("abc"));
    arrayString.addArrayItem(new PwsNode("123"));
    arrayString.addArrayItem(new PwsNode("true"));

    base.assignField("arrayString", arrayString);

    String json = base.toJson();

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    
    System.out.println(json);
    
    PwsNode anotherNode = PwsNode.fromJson(json);
    
    json = anotherNode.toJson();
    
    System.out.println("-------------");
    
    System.out.println(json);
    
  }

  /**
   * convert from object to pws object
   * @param fromNode parent node
   * @param fromFieldName field name this is assigned to (for debugging)
   * @param value
   * @return the PwsObject
   */
  private static PwsNode convertFromJsonObject(PwsNode fromNode, String fromFieldName, Object value) {
    
    if (value == null) {
      PwsNode result = new PwsNode(PwsNodeType.object);
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    }
      
    if (value instanceof Boolean) {
      
      PwsNode result = new PwsNode((Boolean)value);
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    }

    if (value instanceof Double || value instanceof Float) {
      
      PwsNode result = new PwsNode(((Number)value).doubleValue());
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    }

    if (value instanceof Integer || value instanceof Long) {

      PwsNode result = new PwsNode(((Number)value).longValue());
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    } 

    if (value instanceof String) {

      PwsNode result = new PwsNode((String)value);
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    } 

    if (value instanceof JSONArray) {
      
      JSONArray jsonArray = (JSONArray)value;
      
      PwsNode pwsNode = new PwsNode();
      pwsNode.setArrayType(true);
      pwsNode.setFromNode(fromNode);
      pwsNode.setFromFieldName(fromFieldName);
      
      boolean foundType = false;
      
      for (int i=0;i<jsonArray.size();i++) {
        
        Object arrayObject = jsonArray.get(i);
        
        PwsNode arrayNode = convertFromJsonObject(fromNode, fromFieldName, arrayObject);
        if (!foundType) {
          pwsNode.setPwsNodeType(arrayNode.getPwsNodeType());
          foundType = true;
        }
        pwsNode.addArrayItem(arrayNode);
      }
      return pwsNode;
    } 

    if (value instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject)value;
      PwsNode pwsNode = new PwsNode();
      pwsNode.setFromNode(fromNode);
      pwsNode.setFromFieldName(fromFieldName);
      pwsNode.setPwsNodeType(PwsNodeType.object);
      for (String key : (Set<String>)jsonObject.keySet()) {
        
        Object fieldValue = jsonObject.get(key);
        PwsNode fieldNode = convertFromJsonObject(pwsNode, key, fieldValue);
        pwsNode.assignField(key, fieldNode);

      }
      return pwsNode;
    }
    
    throw new RuntimeException(" value type not supported: " + value.getClass().getName());
  }
  
  /**
   * parse a json string into a PWS node
   * @param json 
   * @return the PwsNode or null if no json
   */
  public static PwsNode fromJson(String json) {
    if (!StandardApiServerUtils.isBlank(json)) {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( json ); 
      PwsNode pwsNode = convertFromJsonObject(null, null, jsonObject);
      return pwsNode;
    }
    return null;
  }
  
  /**
   * convert this object to json
   * @return the json
   */
  public String toJson() {
    
    JSONObject jsonObject = this.toJsonObjectHelper();
    
    return jsonObject.toString();
    
  }
  
  /**
   * 
   * @param value
   */
  public void assignValueAndType(Object value) {
    if (value != null) {
      if (value instanceof Boolean) {
        this.setPwsNodeType(PwsNodeType.bool);
        this.setBool((Boolean)value);
      } else if (value instanceof Long) {
        this.setPwsNodeType(PwsNodeType.integer);
        this.setInteger((Long)value);
      } else if (value instanceof Double) {
        this.setPwsNodeType(PwsNodeType.floating);
        this.setFloating((Double)value);
      } else if (value instanceof String) {
        this.setPwsNodeType(PwsNodeType.string);
        this.setString((String)value);
      } else {
        throw new RuntimeException("Only expecting type of null, "
            + "Boolean, Long, Double, String, but received: " + value.getClass().getName());
      }
    }
  }

  /**
   * assign an object from the fromObject to this object
   * @param theFromNode
   */
  public void cloneNode(PwsNode theFromNode) {
    this.cloneNodeHelper(theFromNode);
  }

  /**
   * assign an object from the fromObject to this object
   * @param theFromNode
   */
  private void cloneNodeHelper(PwsNode theFromNode) {

    //copy over data
    this.arrayType = theFromNode.arrayType;
    this.bool = theFromNode.bool;
    this.floating = theFromNode.floating;
    //name and node might be different
    //this.fromFieldName = fromNode.fromFieldName
    //this.fromNode = theFromNode.fromNode;
    this.integer = theFromNode.integer;
    this.pwsNodeType = theFromNode.pwsNodeType;
    this.string = theFromNode.string;

    //we need to clone the field
    if (this.pwsNodeType == PwsNodeType.object && theFromNode.object != null && theFromNode.object.keySet() != null) {
      
      //go through each field in the object
      for (String fieldName : theFromNode.object.keySet()) {
        
        PwsNode field = theFromNode.object.get(fieldName);
        PwsNode clonedField = null;
        if (field != null) {
          clonedField = new PwsNode();
          clonedField.setFromFieldName(fieldName);
          clonedField.setFromNode(this);
          clonedField.cloneNode(field);
        }
        this.assignField(fieldName, clonedField);
      }
      
    }
    
    if (this.arrayType && theFromNode.array != null && theFromNode.array.size() > 0) {
      //we need to clone each item in the array
      for (PwsNode arrayElementNode : theFromNode.array) {
        PwsNode clonedField = null;
        if (arrayElementNode != null) {
          clonedField = new PwsNode();
          //should this be an array index???
          clonedField.setFromFieldName(this.getFromFieldName());
          clonedField.setFromNode(this);
          clonedField.cloneNode(arrayElementNode);
        }
        this.addArrayItem(clonedField);
      }
    }
    
  }

  /**
   * get the array if this is array type
   * @return the array
   */
  public List<PwsNode> getArray() {
    if (!this.isArrayType()) {
      throw new RuntimeException("Not array type");
    }
    return this.array;
  }
  
  /**
   * 
   * @return the json object
   */
  private JSONObject toJsonObjectHelper() {
    
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType);
    }
    
    JSONObject jsonObject = new JSONObject();

    //lets go through the fields
    if (this.object != null && this.object.keySet() != null) {
      for (String fieldName : this.object.keySet()) {
        
        PwsNode field = this.object.get(fieldName);
  
        if (field.isArrayType()) {
  
          if (field.array == null) {
            jsonObject.element(fieldName, JSONNull.getInstance());
          } else {
          
            JSONArray jsonArray = new JSONArray();
  
            for (PwsNode item : field.array) {
  
              if (item.isArrayType()) {
                throw new RuntimeException("Doesnt currently support array of arrays: " + fieldName);
              }
              
              switch (field.getPwsNodeType()) {
                case integer:
                  Long theInteger = item.getInteger();
                  jsonArray.add(theInteger);
                  break;
                case bool:
                  Boolean theBoolean = item.getBool();
                  jsonArray.add(theBoolean);
                  break;
                case floating:
                  Double theFloating = item.getFloating();
                  jsonArray.add(theFloating);
                  break;
                case string:
                  String theString = item.getString();
                  jsonArray.add(theString);
                  break;
                case object:
                  JSONObject jsonItem = item == null ? null : item.toJsonObjectHelper();
                  jsonArray.add(jsonItem);
                  break;
                default: 
                  throw new RuntimeException("Not expecting pws node type: " + field.getPwsNodeType());
              }
  
            }
  
            jsonObject.element(fieldName, jsonArray);
  
            
          }
          
        } else {
          switch (field.getPwsNodeType()) {
            case integer:
              Long theInteger = field.getInteger();
              if (theInteger != null) {
                jsonObject.element(fieldName, theInteger.longValue());
              } else {
                jsonObject.element(fieldName, JSONNull.getInstance());
              }
              break;
            case bool:
              Boolean theBoolean = field.getBool();
              if (theBoolean != null) {
                jsonObject.element(fieldName, theBoolean.booleanValue());
              } else {
                jsonObject.element(fieldName, JSONNull.getInstance());
              }
              break;
            case floating:
              Double theFloating = field.getFloating();
              if (theFloating != null) {
                jsonObject.element(fieldName, theFloating.doubleValue());
              } else {
                jsonObject.element(fieldName, JSONNull.getInstance());
              }
              break;
            case string:
              String theString = field.getString();
              if (theString != null) {
                jsonObject.element(fieldName, theString);
              } else {
                jsonObject.element(fieldName, JSONNull.getInstance());
              }
              break;
            case object:
              
              if (field.object == null) {
                jsonObject.element(fieldName, JSONNull.getInstance());
              } else {
                JSONObject fieldObject = field.toJsonObjectHelper();
                jsonObject.element(fieldName, fieldObject);
              }
              break;
            default: 
              throw new RuntimeException("Not expecting pws node type: " + field.getPwsNodeType());
          }
        }      
        
      }
    }
    return jsonObject;
  }
  
  /**
   * this doesnt have to be called to assign a field, but if you want an empty object instead of null, call this
   */
  public void initObjectIfNull() {
    if (this.object == null) {
      this.object = new LinkedHashMap<String, PwsNode>();
    }
  }
  
  /**
   * if this is an array
   * @return the arrayType
   */
  public boolean isArrayType() {
    return this.arrayType;
  }

  
  /**
   * if this is an array
   * @param arrayType1 the arrayType to set
   */
  public void setArrayType(boolean arrayType1) {
    this.arrayType = arrayType1;
  }

  /**
   * type of node
   */
  public static enum PwsNodeType {
    
    /** scalar string value */
    string,
    
    /** scalar integer value */
    integer,
    
    /** scalar boolean value */
    bool,
    
    /** scalar floating point value */
    floating,
    
    /** has fields and nodes */
    object,
  }

  /**
   * if this is an array
   */
  private boolean arrayType;
  
  /**
   * node
   */
  public PwsNode() {
    
  }
  
  /**
   * node with type
   * @param pwsNodeType1
   */
  public PwsNode(PwsNodeType pwsNodeType1) {
    this.pwsNodeType = pwsNodeType1;
  }
  
  /**
   * which type of node this is
   */
  private PwsNodeType pwsNodeType;

  /**
   * if it is an object, these are the fields
   */
  private Map<String, PwsNode> object;

  /**
   * if it is an array, these are the objects in the array
   */
  private List<PwsNode> array;

  /**
   * add an array item
   * @param pwsNode to add
   */
  public void addArrayItem(PwsNode pwsNode) {
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array" + ", " + this);
    }
    
    if (pwsNode != null) {
      if (this.pwsNodeType != pwsNode.getPwsNodeType()) {
        throw new RuntimeException("expecting array type of " + this.pwsNodeType 
            + ", but assigning: " + pwsNode.getPwsNodeType() + ", " + this);
      }
    }
    
    if (this.array == null) {
      this.array = new ArrayList<PwsNode>();
    }
    
    this.array.add(pwsNode);
  }

  /**
   * add an array item
   * @param index
   * @param pwsNode to add
   */
  public void assignArrayItem(int index, PwsNode pwsNode) {
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array");
    }
    
    if (pwsNode != null) {
      if (this.pwsNodeType != pwsNode.getPwsNodeType()) {
        throw new RuntimeException("expecting array type of " + this.pwsNodeType 
            + ", but assigning: " + pwsNode.getPwsNodeType() + ", " + this);
      }
    }
    
    if (this.array == null) {
      this.array = new ArrayList<PwsNode>();
    }

    int length = StandardApiServerUtils.length(this.array);
    if (length - 1 < index) {
      throw new RuntimeException("Trying to get index: " + index + ", but array is only length: " + length + ", " + this);
    }
    
    this.array.set(index, pwsNode);
  }

  /**
   * get the array length
   * @return the array length
   */
  public int getArrayLength() {
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array");
    }

    return StandardApiServerUtils.length(this.array);
  }

  /**
   * retrieve an array index
   * @param index 0 indexed
   * @return the node
   */
  public PwsNode retrieveArrayItem(int index) {
    
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array" + ", " + this);
    }

    int length = StandardApiServerUtils.length(this.array);
    if (length - 1 < index) {
      throw new RuntimeException("Trying to get index: " + index + ", but array is only length: " + length + ", " + this);
    }
    
    return this.array.get(index);
  }

  /**
   * retrieve an array index string
   * @param attributePath 
   * @param attributeValue 
   * @return the node or null if not found, exception if multiple found
   */
  public PwsNode retrieveArrayItemByAttributeValue(String attributePath, String attributeValue) {
    return retrieveArrayItemByAttributeValueHelper(attributePath, attributeValue);
  }

  /**
   * retrieve an array index
   * @param attributePath 
   * @param attributeValue 
   * @return the node or null if not found, exception if multiple found
   */
  public PwsNode retrieveArrayItemByAttributeValue(String attributePath, Long attributeValue) {
    return retrieveArrayItemByAttributeValueHelper(attributePath, attributeValue);
  }

  /**
   * retrieve an array index
   * @param attributePath 
   * @param attributeValue 
   * @return the node or null if not found, exception if multiple found
   */
  public PwsNode retrieveArrayItemByAttributeValue(String attributePath, Double attributeValue) {
    return retrieveArrayItemByAttributeValueHelper(attributePath, attributeValue);
  }

  /**
   * retrieve an array index
   * @param attributePath 
   * @param attributeValue 
   * @return the node or null if not found, exception if multiple found
   */
  public PwsNode retrieveArrayItemByAttributeValue(String attributePath, Boolean attributeValue) {
    return retrieveArrayItemByAttributeValueHelper(attributePath, attributeValue);
  }

  
  /**
   * retrieve an array index
   * @param attributePath 
   * @param attributeValue 
   * @return the node or null if not found, exception if multiple found
   */
  private PwsNode retrieveArrayItemByAttributeValueHelper(String attributePath, Object attributeValue) {
    
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array: " + this);
    }

    int length = StandardApiServerUtils.length(this.array);
    if (length == 0) {
      return null;
    }
    
    List<PwsOperationStep> pwsOperationSteps = PwsOperationStep.parseExpression(null, attributePath);
    
    PwsNode foundNode = null;

    for (PwsNode item : this.array) {

      //loop through the array and get the field by attribute name
      PwsNodeEvaluationResult pwsNodeEvaluationResult = PwsNodeEvaluation.evaluate(item, pwsOperationSteps, false);
      
      PwsNode fieldItem = pwsNodeEvaluationResult.getPwsNode();

      if (fieldItem != null) {
        boolean equal = fieldItem.equalsScalar(attributeValue);
        
        if (equal) {
          if (foundNode != null) {
            throw new RuntimeException("There are more than one array item to match the attribute path: '" 
                + attributePath + "', and attribute value: " + attributeValue + ", node: " + this );
          }
          foundNode = item;
        }
      }
    }
    return foundNode;

  }

  /**
   * assign a field.  Note if null will not remove
   * @param fieldName
   * @param pwsNode
   */
  public void assignField(String fieldName, PwsNode pwsNode) {
    this.assignField(fieldName, pwsNode, false);
  }

  /**
   * assign a field.  Note if null will not remove
   * @param fieldName
   * @param pwsNode
   * @param allowNull
   */
  public void assignField(String fieldName, PwsNode pwsNode, boolean allowNull) {
    
    if (!allowNull && (pwsNode == null)) {
      return;
    }

    if (!allowNull && pwsNode.pwsNodeType != null && pwsNode.pwsNodeType != PwsNodeType.object && pwsNode.isHasNoValue() ) {
      return;
    }
    
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType + ", " + this);
    }
    
    if (this.object == null) {
      this.object = new LinkedHashMap<String, PwsNode>();
    }
    
    this.object.put(fieldName, pwsNode);
    
  }

  /**
   * retrieve a field from an object
   * @param fieldName
   * @return the field or null if not there
   */
  public PwsNode retrieveField(String fieldName) {
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType + ", " + fieldName + ", " + this);
    }

    if (this.object != null) {
      return this.object.get(fieldName);
    }
    return null;
    
  }

  /**
   * retrieve the field names from an object, might be null
   * @return the set of string field names
   */
  public Set<String> getFieldNames() {
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType + ", " + this);
    }
    
    if (this.object != null) {
      return this.object.keySet();
    }
    return null;
    
  }
  
  /**
   * remove a field from the object
   * @param fieldName
   * @return the PwsNode that was removed or null if none
   */
  public PwsNode removeField(String fieldName) {
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType + ", " + this);
    }
    
    if (this.object != null) {
      return this.object.remove(fieldName);
    }
    return null;
  }
  
  /**
   * string value 
   */
  private String string;
  
  /**
   * integer value
   */
  private Long integer;

  /**
   * boolean value
   */
  private Boolean bool;
  
  /**
   * floating value
   */
  private Double floating;
  
  /**
   * @return the bool
   */
  public Boolean getBool() {
    
    if (this.getPwsNodeType() != PwsNodeType.bool) {
      throw new RuntimeException("Expecting bool but was: " + this.getPwsNodeType() + ", " + this);
    }
    
    return this.bool;
  }

  /**
   * @param bool1 the bool to set
   */
  public void setBool(Boolean bool1) {
    if (this.pwsNodeType != PwsNodeType.bool) {
      throw new RuntimeException("expecting node type of bool: " + this.pwsNodeType + ", " + this);
    }
    this.bool = bool1;
  }

  
  /**
   * @return the floating
   */
  public Double getFloating() {
    
    if (this.getPwsNodeType() != PwsNodeType.floating) {
      throw new RuntimeException("Expecting floating but was: " + this.getPwsNodeType() + ", " + this);
    }
    
    return this.floating;
  }
  
  /**
   * @param floating1 the floating to set
   */
  public void setFloating(Double floating1) {
    if (this.pwsNodeType != PwsNodeType.floating) {
      throw new RuntimeException("expecting node type of floating: " + this.pwsNodeType + ", " + this);
    }
    this.floating = floating1;
  }

  /**
   * @return the string
   */
  public String getString() {
    
    if (this.getPwsNodeType() != PwsNodeType.string) {
      throw new RuntimeException("Expecting string but was: " + this.getPwsNodeType() + ", " + this);
    }
    
    return this.string;
  }

  
  /**
   * @param string1 the string to set
   */
  public void setString(String string1) {
    if (this.pwsNodeType != PwsNodeType.string) {
      throw new RuntimeException("expecting node type of string: " + this.pwsNodeType + ", " + this);
    }
    
    this.string = string1;
  }

  
  /**
   * @return the theInteger
   */
  public Long getInteger() {
    
    if (this.getPwsNodeType() != PwsNodeType.integer) {
      throw new RuntimeException("Expecting integer but was: " + this.getPwsNodeType() + ", " + this);
    }
    
    return this.integer;
  }

  
  /**
   * @param theInteger1 the theInteger to set
   */
  public void setInteger(Long theInteger1) {

    if (this.pwsNodeType != PwsNodeType.integer) {
      throw new RuntimeException("expecting node type of integer: " + this.pwsNodeType + ", " + this);
    }

    this.integer = theInteger1;
  }
  
  
  
}
