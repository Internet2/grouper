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
/*
 * @author mchyzer
 * $Id: GroupAttributeNameValidationHookTest.java,v 1.2 2009-03-24 17:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import junit.textui.TestRunner;


/**
 *
 */
public class AttributeDefNameAttributeNameValidationHookTest extends GrouperTest {

  /**
   * edu attributeDefName 
   */
  private Stem edu;
  /**
   */
  private GrouperSession grouperSession = null;
  /**
   * root attributeDefName 
   */
  private Stem root;

  /**
   * @param name
   */
  public AttributeDefNameAttributeNameValidationHookTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameAttributeNameValidationHookTest("testBuiltInAttributeValidator"));
  }

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testBuiltInAttributeValidator() throws Exception {
  
    grouperSession     = SessionHelper.getRootSession();

    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");

    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("edu:attrDef")
        .assignAttributeDefType(AttributeDefType.attr).assignValueType(AttributeDefValueType.marker).assignToGroup(true).save();

    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName("edu:attrDefName").save();

    //put this in try/catch in case storing on setters
    //add the attribute
    attributeDefName.setDescription("whatever2");
    
    attributeDefName.store();
  
    try {
      try {
        
        AttributeDefNameAttributeNameValidationHook.attributeNamePatterns.put(AttributeDefName.FIELD_DESCRIPTION, Pattern.compile("^" + StemAttributeNameValidationHook.TEST_PATTERN + "$"));
        AttributeDefNameAttributeNameValidationHook.attributeNameRegexes.put(AttributeDefName.FIELD_DESCRIPTION, "^" + GroupAttributeNameValidationHook.TEST_PATTERN + "$");
        AttributeDefNameAttributeNameValidationHook.attributeNameVetoMessages.put(AttributeDefName.FIELD_DESCRIPTION, "Attribute description cannot have the value: '$attributeValue$'");
        
        //put this in try/catch in case storing on setters
        //add the attribute
        attributeDefName.setDescription("whatever");
        
        attributeDefName.store();
        fail("Should veto this invalid name");
      } catch (HookVeto hv) {
        //this is a success, it is supposed to veto  
      }
      
      attributeDefName.setDescription(GroupAttributeNameValidationHook.TEST_PATTERN);
      
      attributeDefName.store();
    
      //should be fine
    } finally {
      AttributeDefNameAttributeNameValidationHook.attributeNamePatterns.remove(AttributeDefName.FIELD_DESCRIPTION);
      AttributeDefNameAttributeNameValidationHook.attributeNameRegexes.remove(AttributeDefName.FIELD_DESCRIPTION);
      AttributeDefNameAttributeNameValidationHook.attributeNameVetoMessages.remove(AttributeDefName.FIELD_DESCRIPTION);
      
    }
  
  }

}
