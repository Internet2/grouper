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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksStemBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


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
 * </pre>
 */
public class StemUniqueNameCaseInsensitiveHook extends StemHooks {
  
  /**
   * 
   */
  public static final String VETO_STEM_UNIQUE_NAME_CASE_INSENSITIVE = "veto.stem.unique.nameCaseInsensitive";

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
        + " (lower(theStem.nameDb) = :theName or lower(theStem.alternateNameDb) = :theName) "
        + "and theStem.uuid != :theUuid ")
        .setString("theName", stem.getName().toLowerCase())
        .setString("theUuid", stem.getId()).uniqueResult(long.class);
    
//    boolean searchByParentFolder = GrouperConfig.retrieveConfig().propertyValueBoolean("stemUniqueNameCaseInsensitiveHook.searchByParentFolder", false);
//
//    if (searchByParentFolder) {
//      String parentFolderName = GrouperUtil.parentStemNameFromName(stem.getName());
//
//      count = HibernateSession.byHqlStatic()
//          .createQuery("select count(theStem) from Stem theStem, Stem parentStem "
//              + "where lower(theStem.extensionDb) = :theExtension "
//              + "and parentStem.nameDb = :parentStemName "
//              + " and theStem.uuid != :theUuid "
//              + "and theStem.parentUuid = parentStem.uuid ")
//          .setString("theExtension", StringUtils.defaultString(GrouperUtil.extensionFromName(stem.getName())).toLowerCase())
//          .setString("parentStemName", parentFolderName)
//          .setString("theUuid", stem.getId())
//          .uniqueResult(long.class);
//
//    } else {

    if (count > 0) {
      throw new HookVeto(VETO_STEM_UNIQUE_NAME_CASE_INSENSITIVE, "The folder ID is already in use, please use a different ID");
    }

    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreUpdate(HooksContext, HooksStemBean)
   */
  @Override
  public void stemPreUpdate(HooksContext hooksContext, HooksStemBean preUpdateBean) {
    Stem stem = preUpdateBean.getStem();
    if (stem.dbVersionDifferentFields().contains(Group.FIELD_EXTENSION) || stem.dbVersionDifferentFields().contains(Group.FIELD_NAME)) {
      verifyCaseInsensitiveName(stem);
    }
  }
  
}
