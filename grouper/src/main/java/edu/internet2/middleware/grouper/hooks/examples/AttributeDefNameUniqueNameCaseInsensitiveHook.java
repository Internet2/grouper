/*******************************************************************************
 * Copyright 2015 Internet2
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
/*
 * @author mchyzer
 * $Id: GroupAttributeNameValidationHook.java,v 1.6 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * folder names will case insensitive be unique
 * 
 * set that with grouper.properties:
 * 
 * hooks.attributeDefName.class = edu.internet2.middleware.grouper.hooks.examples.AttributeDefNameUniqueNameCaseInsensitiveHook
 * 
 * or
 * 
 * grouperHook.AttributeDefNameUniqueNameCaseInsensitiveHook.autoRegister = true (default)
 * </pre>
 */
public class AttributeDefNameUniqueNameCaseInsensitiveHook extends AttributeDefNameHooks {
  
  /**
   * only register once
   */
  private static boolean registered = false;
  
  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.AttributeDefNameUniqueNameCaseInsensitiveHook.autoRegister", true)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.ATTRIBUTE_DEF_NAME.getPropertyFileKey(), 
          AttributeDefNameUniqueNameCaseInsensitiveHook.class);
    }
    
    registered = true;

  }

  /**
   * 
   */
  public static final String VETO_ATTRIBUTE_DEF_NAME_UNIQUE_NAME_CASE_INSENSITIVE = "veto.attributeDefName.unique.nameCaseInsensitive";

  /**
   * 
   */
  public static final String VETO_ATTRIBUTE_DEF_NAME_UNIQUE_ID_CASE_INSENSITIVE = "veto.attributeDefName.unique.idCaseInsensitive";

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePreInsert(HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePreInsert(HooksContext hooksContext, HooksAttributeDefNameBean preInsertBean) {
    AttributeDefName attributeDefName = preInsertBean.getAttributeDefName();
    verifyCaseInsensitiveName(attributeDefName);
  }

  /**
   * 
   * @param attributeDefName
   */
  public static void verifyCaseInsensitiveName(AttributeDefName attributeDefName) {

    
    //see if there is another attribute def name with the same name case insensitive
    long count = HibernateSession.byHqlStatic().createQuery("select count(theAttributeDefName) "
        + "from AttributeDefName as theAttributeDefName where "
        + " (lower(theAttributeDefName.nameDb) = :theName or lower(theAttributeDefName.displayNameDb) = :theName2) "
        + "and theAttributeDefName.id != :theUuid ")
        .setString("theName", attributeDefName.getName().toLowerCase())
        .setString("theName2", attributeDefName.getDisplayName().toLowerCase())
        .setString("theUuid", attributeDefName.getId()).uniqueResult(long.class);
    
    if (count > 0) {
      count = HibernateSession.byHqlStatic().createQuery("select count(theAttributeDefName) "
          + "from AttributeDefName as theAttributeDefName where "
          + " (lower(theAttributeDefName.nameDb) = :theName) "
          + "and theAttributeDefName.id != :theUuid ")
          .setString("theName", attributeDefName.getName().toLowerCase())
          .setString("theUuid", attributeDefName.getId()).uniqueResult(long.class);
      if (count > 0) {
        throw new HookVeto(VETO_ATTRIBUTE_DEF_NAME_UNIQUE_ID_CASE_INSENSITIVE, GrouperTextContainer.textOrNull("veto.attributeDefName.unique.idCaseInsensitive.default"));
      } else {
        throw new HookVeto(VETO_ATTRIBUTE_DEF_NAME_UNIQUE_NAME_CASE_INSENSITIVE, GrouperTextContainer.textOrNull("veto.attributeDefName.unique.nameCaseInsensitive.default"));
      }
    }
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePreUpdate(HooksContext, HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePreUpdate(HooksContext hooksContext, HooksAttributeDefNameBean preUpdateBean) {
    AttributeDefName attributeDefName = preUpdateBean.getAttributeDefName();
    if (attributeDefName.dbVersionDifferentFields().contains(AttributeDefName.FIELD_EXTENSION) || attributeDefName.dbVersionDifferentFields().contains(AttributeDefName.FIELD_NAME) 
        || attributeDefName.dbVersionDifferentFields().contains(AttributeDefName.FIELD_DISPLAY_EXTENSION) || attributeDefName.dbVersionDifferentFields().contains(AttributeDefName.FIELD_DISPLAY_NAME) 
        ) {
      verifyCaseInsensitiveName(attributeDefName);
    }
  }
  
}
