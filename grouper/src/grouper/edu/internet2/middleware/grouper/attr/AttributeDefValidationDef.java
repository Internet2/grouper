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
package edu.internet2.middleware.grouper.attr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;


/**
 * definitions of validations
 * @author mchyzer
 */
public enum AttributeDefValidationDef implements AttributeDefValidationInterface {
  
  /** length in chars must be a certain value, not less not more */
  exactLength {

    /**
     * @see AttributeDefValidationDef#validate(Object, String, String)
     */
    @Override
    public String validate(Object input, String argument0, String argument1) {
      
      int length = argumentOneInt(this, argument0, argument1);
      
      if (length == length(input)) {
        return null;
      }
      return "Length should be " + length + " but instead is " + length(input);
    }
    
  },

  /** length in chars must be at least this amount */
  minLength {

    /**
     * @see AttributeDefValidationDef#validate(Object, String, String)
     */
    @Override
    public String validate(Object input, String argument0, String argument1) {
      
      int length = argumentOneInt(this, argument0, argument1);
      
      if (length <= length(input)) {
        return null;
      }
      return "Length should be at least " + length + " but instead is " + length(input);
    }
    
  },

  /** length in chars cannot be more than this amount */
  maxLength {

    /**
     * @see AttributeDefValidationDef#validate(Object, String, String)
     */
    @Override
    public String validate(Object input, String argument0, String argument1) {
      
      int length = argumentOneInt(this, argument0, argument1);
      
      if (length >= length(input)) {
        return null;
      }
      return "Length should be at most " + length + " but instead is " + length(input);
    }
    
  },

  /** validate based on regex */
  regex {

    /**
     * @see AttributeDefValidationDef#validate(Object, String, String)
     */
    @Override
    public String validate(Object input, String argument0, String argument1) {
      
      String regex = argumentOneString(this, argument0, argument1);
      
      input = input == null ? "" : input;
      
      String inputString = input.toString();
      
      if (!inputString.matches(regex)) {
        return "Input needs to match the regex: '" + regex + "'";
      }
      //has no problems
      return null;
    }
    
  },
  
  /** if the value is required when the attribute is assigned */
  required {

    /**
     * @see AttributeDefValidationDef#validate(Object, String, String)
     */
    @Override
    public String validate(Object input, String argument0, String argument1) {
      
      argumentNone(this, argument0, argument1);
      
      if (0 == length(input)) {
        return null;
      }
      return "This is a required field";

      
    }
    
  },
  
  /** formatting of dateTime, the mask.  Like a java SimpleDateFormat, e.g. mm/dd/ccyy */
  dateTimeMask {

    /**
     * 
     */
    @Override
    public String formatFromDb(Object input, String argument0, String argument1) {
      if (input == null) {
        return "";

      }
        
      if (!(input instanceof Long)) {
        throw new RuntimeException("Why is this not Long??? " 
            + input.getClass().getName());
      }
      
      long inputLong = (Long)input;
      String mask = argumentOneString(this, argument0, argument1);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mask);
      
      String result = simpleDateFormat.format(new Date(inputLong));
      return result;
    }

    /**
     * 
     */
    @Override
    public String validate(Object input, String argument0, String argument1) {
      if (input == null || input instanceof Long) {
        return null;
      }
      return "DateTime must be in the format: " + argument0 + ", but was: '" + input + "'"; 
    }

    /**
     * 
     */
    @Override
    public Object formatToDb(Object input, String argument0, String argument1) {
      
      if (input == null || input instanceof Long ) {
        return input;
      }
      
      if (!(input instanceof String)) {
        throw new RuntimeException("Why is this not null, integer or string??? " 
            + input.getClass().getName());
      }
      String inputString = (String)input;
      String mask = argumentOneString(this, argument0, argument1);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mask);
      
      try {
        Date date = simpleDateFormat.parse(inputString);
        return date.getTime();
      } catch (ParseException pe) {
        //dont worry
      }
      return input;
    }
    
  },
  
  /** dont trim the value before saving.  note: the logic will look at this one */
  dontTrim,
  
  /** make the string caps */
  stringToUpper {

    @Override
    public String formatFromDb(Object input, String argument0, String argument1) {
      argumentNone(this, argument0, argument1);
      
      input = input == null ? "" : input;
      
      String inputString = input.toString();

      return inputString.toUpperCase();
    }

    @Override
    public Object formatToDb(Object input, String argument0, String argument1) {
      argumentNone(this, argument0, argument1);
      input = input == null ? "" : input;
      
      String inputString = input.toString();

      return inputString.toUpperCase();
    }
    
  },
  
  /** make the string to lower */
  stringToLower  {

    @Override
    public String formatFromDb(Object input, String argument0, String argument1) {
      argumentNone(this, argument0, argument1);
      input = input == null ? "" : input;
      
      String inputString = input.toString();

      return inputString.toLowerCase();
    }

    @Override
    public Object formatToDb(Object input, String argument0, String argument1) {
      argumentNone(this, argument0, argument1);
      input = input == null ? "" : input;
      
      String inputString = input.toString();

      return inputString.toLowerCase();
    }
    
  },
  
  /** capitalize words so each is lower and starts with upper */
  stringCapitalizeWords {

    @Override
    public String formatFromDb(Object input, String argument0, String argument1) {
      argumentNone(this, argument0, argument1);
      input = input == null ? "" : input;
      
      String inputString = input.toString();

      return WordUtils.capitalizeFully(inputString);
    }

    @Override
    public Object formatToDb(Object input, String argument0, String argument1) {
      argumentNone(this, argument0, argument1);
      input = input == null ? "" : input;
      
      String inputString = input.toString();

      return inputString.toLowerCase();
    }
    
  };
  
  /**
   * @see AttributeDefValidationDef#formatFromDb(Object, String, String)
   */
  public String formatFromDb(Object input, String argument0, String argument1) {
    throw AttributeDefValidationNotImplemented.instance();
  }

  /**
   * @see AttributeDefValidationDef#formatToDb(String, String, String)
   */
  public Object formatToDb(Object input, String argument0, String argument1) {
    throw AttributeDefValidationNotImplemented.instance();
  }
  
  /**
   * @see AttributeDefValidationDef#validate(Object, String, String)
   */
  public String validate(Object input, String argument0, String argument1) {
    throw AttributeDefValidationNotImplemented.instance();
  }
  
  /**
   * should have 1 argument, as int
   * @param argument0
   * @param argument1
   * @return the int
   */
  public static int argumentOneInt(
      AttributeDefValidationInterface attributeDefValidationInterface, 
      String argument0, String argument1) {
    //make sure one arg
    argumentOne(attributeDefValidationInterface, argument0, argument1);
    return argumentInt(attributeDefValidationInterface, argument0, 0);
    
  }
  
  /**
   * should have 1 argument, as string
   * @param argument0
   * @param argument1
   * @return the string
   */
  public static String argumentOneString(
      AttributeDefValidationInterface attributeDefValidationInterface, 
      String argument0, String argument1) {
    //make sure one arg
    argumentOne(attributeDefValidationInterface, argument0, argument1);
    return argument0;
    
  }
  
  /**
   * make sure this argument is an int
   * @param attributeDefValidationInterface
   * @param argument
   * @param argumentIndex
   */
  public static int argumentInt(AttributeDefValidationInterface attributeDefValidationInterface, 
      String argument, int argumentIndex) {
    
    try {
      Integer integer = Integer.valueOf(argument);
      return integer.intValue();
    } catch (Exception e) {
      throw new RuntimeException("Cant convert " + attributeDefValidationInterface.name() 
          + " arg " + argumentIndex + ", " + argument, e);
    }
  }

  /**
   * make sure there is one argument
   */
  public static void argumentOne(AttributeDefValidationInterface attributeDefValidationInterface, 
      String argument0, String argument1) {
    if (StringUtils.isNotBlank(argument1)) {
      throw new RuntimeException(attributeDefValidationInterface.name() 
          + " should not have a second argument: " + argument1);
    }
    if (StringUtils.isBlank(argument0)) {
      throw new RuntimeException(attributeDefValidationInterface.name() 
          + " should have a first argument");
    }
  }
  
  /**
   * make sure there are no argument
   */
  public static void argumentNone(AttributeDefValidationInterface attributeDefValidationInterface, 
      String argument0, String argument1) {
    if (StringUtils.isNotBlank(argument1)) {
      throw new RuntimeException(attributeDefValidationInterface.name() 
          + " should not have a second argument: " + argument1);
    }
    if (StringUtils.isNotBlank(argument0)) {
      throw new RuntimeException(attributeDefValidationInterface.name() 
          + " should not have a first argument: " + argument0);
    }
  }
  
  /**
   * 
   * @param object
   * @return
   */
  public static int length(Object object) {
    if (object == null || "".equals(object)) {
      return 0;
    }
    return object.toString().length();
  }
  
}
