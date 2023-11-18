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
package edu.internet2.middleware.grouper.grouperUi.beans.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * option value in a select
 */
public class GuiOption implements Serializable {

  /** name of the javabean property: name */
  public static final String FIELD_NAME = "name";

  /** name of the javabean property: value */
  public static final String FIELD_VALUE = "value";

  /** name of the javabean property: css */
  public static final String FIELD_CSS = "css";

  /** name of an option */
  private String name;

  /** value of an option */
  private String value;

  /** css of an option */
  private String css;


  /** 
   * default constructor
   */
  public GuiOption() {
  }

  /**
   * constructor that includes all fields of bean
   * @param _name name of an option
   * @param _value value of an option
   * @param _css css of an option
   */
   public GuiOption (
      String _name,
      String _value,
      String _css  ) {
    this.setName(_name);
    this.setValue(_value);
    this.setCss(_css);
  }

  /**
   * setter for name: name of an option
   * @param _name is the data to set
   */
  public void setName(String _name) {
    this.name = _name;
  }

  /**
   * getter for name: name of an option
   * @return the value of the field
   */
  public String getName() {
    return this.name;
  }

  /**
   * setter for value: value of an option
   * @param _value is the data to set
   */
  public void setValue(String _value) {
    this.value = _value;
  }

  /**
   * getter for value: value of an option
   * @return the value of the field
   */
  public String getValue() {
    return this.value;
  }

  /**
   * setter for css: css of an option
   * @param _css is the data to set
   */
  public void setCss(String _css) {
    this.css = _css;
  }

  /**
   * getter for css: css of an option
   * @return the value of the field
   */
  public String getCss() {
    return this.css;
  }

  /**
   * assert a list contains a option value with a certain key
   * @param guiOptions to search in
   * @param keys key to look for
   */
  public static void assertContains(List<GuiOption> guiOptions, 
      List<String> keys) {
    if (keys != null) {
      //just try each key
      for (String key : keys) {
        assertContains(guiOptions, key);
      }
    }
  }

  /**
   * assert a list contains a option value with a certain key
   * @param guiOptions to search in
   * @param key key to look for
   */
  public static void assertContains(List<GuiOption> guiOptions, String key) {
    if (!contains(guiOptions, key)) {
      throw new RuntimeException("Cant find option: " + key + " in options: " 
        + GuiOption.toStringForLogging(guiOptions));
    }
  }

  /**
   * assert a list not contains a option value with a certain key
   * @param guiOptions to search in
   * @param keys key to look for
   */
  public static void assertNotContains(List<GuiOption> guiOptions, 
      List<String> keys) {
    if (keys != null) {
      //just try each key
      for (String key : keys) {
        assertNotContains(guiOptions, key);
      }
    }
  }

  /**
   * assert a list not contains a option value with a certain key
   * @param guiOptions to search in
   * @param key key to look for
   */
  public static void assertNotContains(List<GuiOption> guiOptions, String key) {
    if (contains(guiOptions, key)) {
      throw new RuntimeException("Shouldnt find option: " + key + " in options: " 
          + GuiOption.toStringForLogging(guiOptions));
    }
  }

  /**
   * see if a list contains a key
   * @param guiOptions
   * @param key
   * @return true if it contains
   */
  public static boolean contains(List<GuiOption> guiOptions, String key) {
    GrouperUtil.assertion(key!=null, "key cant be null");
    if (guiOptions != null) {
      //loop through and check
      for (GuiOption guiOption : guiOptions) {
        if (StringUtils.equals(guiOption.getValue(), key)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * remove an option value by key.
   * @param guiOptions to remove from
   * @param keys to remove
   * @param shallowCloneFirst true to clone if this is a cached list
   * @param throwExceptionIfNotThere true to throw exception if the element wasnt even there
   * @return the list (or the same one if not cloning)
   */
  public static List<GuiOption> remove(
      List<GuiOption> guiOptions, List<String> keys, 
      boolean shallowCloneFirst, boolean throwExceptionIfNotThere) {
    //just clone once
    if (shallowCloneFirst) {
      guiOptions = GrouperUiUtils.cloneShallow(guiOptions);
    }
    if (keys != null) {
      for (String key : keys) {
        //doesnt matter if we reassign or not, but what the hay
        guiOptions = remove(guiOptions, key, false, throwExceptionIfNotThere);
      }
    }
    return guiOptions;
  }

  /**
   * remove an option value by key.
   * @param guiOptions to remove from
   * @param key to remove
   * @param shallowCloneFirst true to clone if this is a cached list
   * @param throwExceptionIfNotThere true to throw exception if the element wasnt even there
   * @return the list (or the same one if not cloning)
   */
  public static List<GuiOption> remove(
      List<GuiOption> guiOptions, String key, 
      boolean shallowCloneFirst, boolean throwExceptionIfNotThere) {
  
    if (shallowCloneFirst) {
      guiOptions = GrouperUiUtils.cloneShallow(guiOptions);
    }
    //keep track if we found one
    boolean foundOne = false;
    if (guiOptions != null) {
      
      Iterator<GuiOption> iterator = guiOptions.iterator();
      while (iterator.hasNext()) {
        GuiOption guiOption = iterator.next();
  
        //keep track, but dont break, since maybe multiple...
        if (StringUtils.equals(key, guiOption.getValue())) {
          iterator.remove();
          foundOne = true;
        }
      }
    }
    //maybe error
    if (throwExceptionIfNotThere && !foundOne) {
      throw new RuntimeException("Was supposed to find this key: " + key 
          + " and didnt in list: " + GuiOption.toStringForLogging(guiOptions));
    }
    
    return guiOptions;
    
  }

  /**
   * retain only certain options by key.  Throw exception if not there
   * @param guiOptions to remove from
   * @param keys to remove
   * @param shallowCloneFirst true to clone if this is a cached list
   * @return the list (or the same one if not cloning)
   */
  public static List<GuiOption> retainAll(
      List<GuiOption> guiOptions, List<String> keys, 
      boolean shallowCloneFirst) {
  
    List<GuiOption> originalList = guiOptions;
    
    if (shallowCloneFirst) {
      guiOptions = GrouperUiUtils.cloneShallow(guiOptions);
    }
    
    //clone keys in case caller will use again
    keys = GrouperUiUtils.cloneShallow(keys);
    
    //there need to be option values and keys
    GrouperUtil.assertion(guiOptions != null, "guiOptions cant be null");
    GrouperUtil.assertion(keys != null, "keys cant be null");
    
    if (guiOptions != null) {
      
      Iterator<GuiOption> iterator = guiOptions.iterator();
      while (iterator.hasNext()) {
        GuiOption guiOption = iterator.next();
        String key = guiOption.getValue();
        
        //keep track, but dont break, since maybe multiple...
        if (!keys.contains(key)) {
          iterator.remove();
        } else {
          //to keep track of keys, remove from keys too
          keys.remove(key);
        }
      }
      
    }
    //maybe error, should have removed all keys if found
    if (keys.size() > 0) {
      throw new RuntimeException("Was supposed to find these keys: " + GrouperUtil.toStringForLog(keys) 
          + " and didnt in list: " + GuiOption.toStringForLogging(originalList));
    }
    
    return guiOptions;
    
  }

  /**
   * print out a few option values
   * @param guiOptions
   * @return the string value
   */
  public static String toStringForLogging(List<GuiOption> guiOptions) {
    int length = GrouperUtil.length(guiOptions);
    //only print a max of 5
    int printLength = Math.min(length, 5);
    List<GuiOption> listToPrint = new ArrayList<GuiOption>();
    for (int i=0;i<printLength;i++) {
      listToPrint.add(guiOptions.get(i));
    }
    //delegate to object utils
    String addendum = length > printLength ? 
        ("(and " + (length - printLength) + " more)") : "";
    return GrouperUtil.toStringForLog(listToPrint) + addendum;
  }

}
