/*
 * @author mchyzer
 * $Id: GroupAttributeNameValidationHook.java,v 1.4 2009-03-15 08:18:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * you can retrict certain attributes of a group to be within a certain regex
 * 
 * </pre>
 */
public class GroupAttributeNameValidationHook extends GroupHooks {
  
  /**
   * for unit tests
   */
  public static final String TEST_ATTRIBUTE_NAME = "testAttribute123";

  /**
   * for unit tests
   */
  public static final String TEST_PATTERN = "qwertyuiop";

  /**
   * only register once
   */
  private static boolean registered = false;
  
  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   * @param addTestValidation if a test validation should be added
   */
  public static void registerHookIfNecessary(boolean addTestValidation) {
    
    if (registered) {
      return;
    }
    
    //see if there are config entries
    Properties grouperProperties = GrouperUtil.propertiesFromResourceName("grouper.properties");

    
    //group.attribute.validator.attributeName.0=extension
    //group.attribute.validator.regex.0=^[a-zA-Z0-9]+$
    //group.attribute.validator.vetoMessage.0=Group ID '$attributeValue$' is invalid since it must contain only alpha-numerics
    
    int index = 0;

    while (true) {
      String attributeName = StringUtils.trim(grouperProperties.getProperty(
          "group.attribute.validator.attributeName." + index));
      String regex = StringUtils.trim(grouperProperties.getProperty(
          "group.attribute.validator.regex." + index));
      String vetoMessage = StringUtils.trim(grouperProperties.getProperty(
          "group.attribute.validator.vetoMessage." + index));
      
      //if we are done checking
      if (StringUtils.isBlank(attributeName)) {
        if (!StringUtils.isBlank(regex) || !StringUtils.isBlank(vetoMessage)) {
          throw new RuntimeException("Dont configure a regex or vetoMessage without an attribute name! index: " + index
              + ", check the grouper.properties");
        }
        break;
      }
      //these are required
      if (StringUtils.isBlank(regex) || StringUtils.isBlank(vetoMessage)) {
        throw new RuntimeException("Regex and vetoMessage are required for attribute: '" + attributeName
            + "' index: " + index + ", check the grouper.properties file");
      }
      //see if already exists
      if (attributeNamePatterns.containsKey(attributeName)) {
        throw new RuntimeException("Attribute name already exists (duplicate): '" + attributeName
            + "' index: " + index + ", check the grouper.properties file");
      }
      //add all configs
      attributeNamePatterns.put(attributeName, Pattern.compile(regex));
      attributeNameRegexes.put(attributeName, regex);
      attributeNameVetoMessages.put(attributeName, vetoMessage);
      
      index++;
    }

    if (addTestValidation) {
      
      attributeNamePatterns.put(TEST_ATTRIBUTE_NAME, Pattern.compile("^" + TEST_PATTERN + "$"));
      attributeNameRegexes.put(TEST_ATTRIBUTE_NAME, "^" + TEST_PATTERN + "$");
      attributeNameVetoMessages.put(TEST_ATTRIBUTE_NAME, "Attribute testAttribute123 cannot have the value: '$attributeValue$'");
      
      index++;
    }
    
    //register the hook
    if (index > 0) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.GROUP.getPropertyFileKey(), GroupAttributeNameValidationHook.class);
    }
    
    registered = true;

  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    Group group = preInsertBean.getGroup();
    Map<String, String> attributesDb = GrouperUtil.nonNull(group.getAttributesDb());
    Set<String> attributesChanged = new HashSet<String>();
    for (String key: attributesDb.keySet()) {
      attributesChanged.add(Group.ATTRIBUTE_PREFIX + key);
    }
    groupPreChangeAttribute(group, attributesChanged);
  }

  /**
   * check that a new attribute value is ok
   * @param group 
   * @param attributeNamesToCheck 
   */
  private static void groupPreChangeAttribute(Group group, Set<String> attributeNamesToCheck) {

    //loop through fields, look for attributes
    for (String fieldName : attributeNamesToCheck) {

      //if attribute then process
      if (fieldName.startsWith(Group.ATTRIBUTE_PREFIX)) {
        String value = (String)group.fieldValue(fieldName);
        String attributeName = fieldName.substring(Group.ATTRIBUTE_PREFIX.length(), fieldName.length());
        groupPreChangeAttribute(attributeName, value);
      }
    }
  }

  /** cache of attribute names to patterns */
  private static Map<String, Pattern> attributeNamePatterns = new HashMap<String, Pattern>();

  /** cache of attribute names to regexs */
  private static Map<String, String> attributeNameRegexes = new HashMap<String, String>();
  
  /** cache of attribute names to error messages when a name doesnt match */
  private static Map<String, String> attributeNameVetoMessages = new HashMap<String, String>();
  
  /**
   * check that a new attribute value is ok
   * @param attributeName 
   * @param attributeValue 
   */
  private static void groupPreChangeAttribute(String attributeName, String attributeValue) {
    //see if there is a configuration about this attribute
    if (attributeNamePatterns.containsKey(attributeName)) {
      Pattern pattern = attributeNamePatterns.get(attributeName);
      if (pattern == null) {
        throw new RuntimeException("Regex pattern '" + attributeNameRegexes.get(attributeName) 
            + "'probably didnt compile for attribute: '" 
            + attributeName + "', check logs or grouper.properties");
      }
      Matcher matcher = pattern.matcher(attributeValue);
      if (!matcher.matches()) {
        
        String attributeNameErrorMessage = attributeNameVetoMessages.get(attributeName);

        //substitute the attribute name
        attributeNameErrorMessage = StringUtils.replace(attributeNameErrorMessage, "$attributeValue$", attributeValue);
        
        throw new HookVeto("veto.group.attribute.name.regex." + attributeName, attributeNameErrorMessage);
      }
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    Group group = preUpdateBean.getGroup();
    Set<String> fieldNames = group.dbVersionDifferentFields(false);
    groupPreChangeAttribute(group, fieldNames);
  }
}
