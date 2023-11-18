package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public enum GrouperProvisioningObjectMetadataItemValueType {

  STRING {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof String) {
        return value;
      }
      
      return GrouperUtil.stringValue(value);
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      return true;
    }
    
  }, 
  
  INTEGER {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof Integer) {
        return value;
      }
      return GrouperUtil.intValue(value);
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.intValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      
      return true;
    }
  },
  
  FLOAT {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.floatObjectValue(value, true);      
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.floatValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      
      return true;
    }
   
    
  },
  
  BOOLEAN {

    @Override
    public Object convert(Object value) {
      
      return GrouperUtil.booleanObjectValue(value);
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.booleanValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      return true;
    }
    
  },
  
  TIMESTAMP {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.toTimestamp(value);
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.stringToTimestamp(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      
      return true;
    }
    
  },
  SET {

    @Override
    public Object convert(Object value) {
      if (value == null) {
        return value;
      }
      if (value instanceof Collection<?>) {
        return value;
      }
      
      if (value instanceof TextNode) {
        TextNode textNode = (TextNode) value;
        value = textNode.asText();
      }

      if (value instanceof String) {
        Set<String> set = GrouperUtil.splitTrimToSet(value.toString(), ",");
        Set<String> newSet  = new LinkedHashSet<>();
        
        for (String singleValue: set) {
          newSet.add(GrouperUtil.replace(singleValue, "U+002C", ","));
        }
        
        return newSet;
      }
      
      if (value instanceof ArrayNode) {
      
        ArrayNode arrayNode = (ArrayNode) value;
        
        Set<Object> set = new HashSet<>();
        for (int i=0;i<arrayNode.size();i++) {
          String val = arrayNode.get(i).asText();
          set.add(val);
        }
        
        return set;
       
      } 
      
      if (value.getClass().isArray()) {
        Set<Object> set = new HashSet<>();
        Object[] vals = (Object[]) value;
        
        for (Object val: vals) {
          set.add(val);
        }
        
        return set;
      }
      
      return null;
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      return true;
    }
    
  }, 
  ;
  
  /**
   * convert to type should be
   * @param value
   * @return
   */
  public abstract Object convert(Object value);
  
  /**
   * 
   * @param type
   * @param exceptionOnNotFound
   * @return field type
   */
  public static GrouperProvisioningObjectMetadataItemValueType valueOfIgnoreCase(String type, boolean exceptionOnNotFound) {

    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningObjectMetadataItemValueType.class, type, exceptionOnNotFound);
    
  }
  
  public abstract boolean canConvertToCorrectType(String valueFromUser);

}

