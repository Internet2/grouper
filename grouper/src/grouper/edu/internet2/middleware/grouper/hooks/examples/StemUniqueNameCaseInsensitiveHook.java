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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksStemBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * folder names will case insensitive be unique
 * 
 * set that with grouper.properties:
 * 
 * hooks.stem.class = edu.internet2.middleware.grouper.hooks.examples.StemUniqueNameCaseInsensitiveHook
 * 
 * or
 * 
 * grouperHook.StemUniqueNameCaseInsensitiveHook.autoRegister = true (default)
 * </pre>
 */
public class StemUniqueNameCaseInsensitiveHook extends StemHooks {
  
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
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.StemUniqueNameCaseInsensitiveHook.autoRegister", true)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.STEM.getPropertyFileKey(), 
          StemUniqueNameCaseInsensitiveHook.class);
    }
    
    registered = true;

  }

  /**
   * 
   */
  public static final String VETO_STEM_UNIQUE_NAME_CASE_INSENSITIVE = "veto.stem.unique.nameCaseInsensitive";

  /**
   * 
   */
  public static final String VETO_STEM_UNIQUE_ID_CASE_INSENSITIVE = "veto.stem.unique.idCaseInsensitive";

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreInsert(HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPreInsert(HooksContext hooksContext, HooksStemBean preInsertBean) {
    Stem stem = preInsertBean.getStem();
    verifyCaseInsensitiveName(stem);
  }

  /**
   * 
   * @param stem
   */
  public static void verifyCaseInsensitiveName(Stem stem) {

    
    //see if there is another stem with the same name case insensitive
    long count = HibernateSession.byHqlStatic().createQuery("select count(theStem) "
        + "from Stem as theStem where "
        + " (lower(theStem.nameDb) in (:theName, :theName2) or lower(theStem.alternateNameDb) in (:theName, :theName2) or lower(theStem.displayNameDb) = :theName3) "
        + "and theStem.uuid != :theUuid ")
        .setString("theName", stem.getName().toLowerCase())
        .setString("theName2", GrouperUtil.defaultIfEmpty(stem.getAlternateName(), stem.getName()).toLowerCase())
        .setString("theName3", stem.getDisplayName().toLowerCase())
        .setString("theUuid", stem.getId()).uniqueResult(long.class);
    
    if (count > 0) {
      count = HibernateSession.byHqlStatic().createQuery("select count(theStem) "
          + "from Stem as theStem where "
          + " (lower(theStem.nameDb) in (:theName, :theName2) or lower(theStem.alternateNameDb) in (:theName, :theName2)) "
          + "and theStem.uuid != :theUuid ")
          .setString("theName", stem.getName().toLowerCase())
          .setString("theName2", GrouperUtil.defaultIfEmpty(stem.getAlternateName(), stem.getName()).toLowerCase())
          .setString("theUuid", stem.getId()).uniqueResult(long.class);
      if (count > 0) {
        throw new HookVeto(VETO_STEM_UNIQUE_ID_CASE_INSENSITIVE, GrouperTextContainer.textOrNull("veto.stem.unique.idCaseInsensitive.default"));
      } else {
        throw new HookVeto(VETO_STEM_UNIQUE_NAME_CASE_INSENSITIVE, GrouperTextContainer.textOrNull("veto.stem.unique.nameCaseInsensitive.default"));
      }
    }
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreUpdate(HooksContext, HooksStemBean)
   */
  @Override
  public void stemPreUpdate(HooksContext hooksContext, HooksStemBean preUpdateBean) {
    Stem stem = preUpdateBean.getStem();
    if (stem.dbVersionDifferentFields().contains(Stem.FIELD_EXTENSION) || stem.dbVersionDifferentFields().contains(Stem.FIELD_NAME) 
        || stem.dbVersionDifferentFields().contains(Stem.FIELD_DISPLAY_EXTENSION) || stem.dbVersionDifferentFields().contains(Stem.FIELD_DISPLAY_NAME) 
        || stem.dbVersionDifferentFields().contains(Stem.FIELD_ALTERNATE_NAME_DB)) {
      verifyCaseInsensitiveName(stem);
    }
  }

  /**
   * 
   */
  public static void clearHook() {
    registered = false;
  }
  
}
