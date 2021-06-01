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

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.AttributeDefHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * attribute definition will case insensitive be unique
 * 
 * set that with grouper.properties:
 * 
 * hooks.attributeDef.class = edu.internet2.middleware.grouper.hooks.examples.AttributeDefUniqueNameCaseInsensitiveHook
 * 
 * or
 * 
 * grouperHook.AttributeDefUniqueNameCaseInsensitiveHook.autoRegister = true (default)
 * </pre>
 */
public class AttributeDefUniqueNameCaseInsensitiveHook extends AttributeDefHooks {
  
  /**
   * only register once
   */
  private static boolean registered = false;
  
  /**
   * 
   */
  public static void clearHook() {
    registered = false;
  }

  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.AttributeDefUniqueNameCaseInsensitiveHook.autoRegister", true)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.ATTRIBUTE_DEF.getPropertyFileKey(), 
          AttributeDefUniqueNameCaseInsensitiveHook.class);
    }
    
    registered = true;

  }

  /**
   * 
   */
  public static final String VETO_ATTRIBUTE_DEF_UNIQUE_NAME_CASE_INSENSITIVE = "veto.attributeDef.unique.nameCaseInsensitive";

  /**
   * 
   */
  public static final String VETO_ATTRIBUTE_DEF_UNIQUE_ID_CASE_INSENSITIVE = "veto.attributeDef.unique.idCaseInsensitive";

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPreInsert(HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPreInsert(HooksContext hooksContext, HooksAttributeDefBean preInsertBean) {
    AttributeDef attributeDef = preInsertBean.getAttributeDef();
    verifyCaseInsensitiveName(attributeDef);
  }

  /**
   * 
   * @param attributeDef
   */
  public static void verifyCaseInsensitiveName(AttributeDef attributeDef) {
    
    //see if there is another attributeDef with the same name case insensitive
    long count = HibernateSession.byHqlStatic().createQuery("select count(theAttributeDef) "
        + "from AttributeDef as theAttributeDef where "
        + " lower(theAttributeDef.nameDb)  = :theName "
        + "and theAttributeDef.id != :theUuid ")
        .setString("theName", attributeDef.getName().toLowerCase())
        .setString("theUuid", attributeDef.getId()).uniqueResult(long.class);
    
    if (count > 0) {
      throw new HookVeto(VETO_ATTRIBUTE_DEF_UNIQUE_NAME_CASE_INSENSITIVE, GrouperTextContainer.textOrNull("veto.attributeDef.unique.idCaseInsensitive.default"));
    }
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPreUpdate(HooksContext, HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPreUpdate(HooksContext hooksContext, HooksAttributeDefBean preUpdateBean) {
    AttributeDef attributeDef = preUpdateBean.getAttributeDef();
    if (attributeDef.dbVersionDifferentFields().contains(AttributeDef.FIELD_EXTENSION) || attributeDef.dbVersionDifferentFields().contains(AttributeDef.FIELD_NAME)) {
      verifyCaseInsensitiveName(attributeDef);
    }
  }
  
}
