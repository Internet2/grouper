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
 * $Id: ChangeLogTypeFinder.java,v 1.1 2009-05-08 05:28:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * find a type
 */
public class ChangeLogTypeFinder {
  /** 
   * every 10 minutes, get new elements
   */
  private static GrouperCache<MultiKey, ChangeLogType> types = null;

  /** 
   * sister cache to types cache
   */
  private static Map<String, ChangeLogType> typesById = null;

  /**
   * clear this out, so it will begin again
   */
  public static void clearCache() {
    types = null;
    typesById = null;
    updatedBuiltinTypes = false;
    ChangeLogTypeBuiltin.internal_clearCache();
  }
  
  /** 
   * Find an {@link ChangeLogType}.
   * <p/>
   * @param   changeLogCategory  Find {@link ChangeLogType} with this name.
   * @param   changeLogAction  Find {@link ChangeLogType} with this name.
   * @param exceptionIfNotFound 
   * @return  {@link GroupType}
   */
  public static ChangeLogType find(String changeLogCategory, String changeLogAction, boolean exceptionIfNotFound) {
    
    MultiKey multiKey = new MultiKey(changeLogCategory, changeLogAction);
    
    // First check to see if type is cached.
    if (types != null && types.containsKey(multiKey)) {
      return types.get(multiKey);
    }
    // If not, refresh known types as it may be new and try again. 
    internal_updateKnownTypes();
    if (types.containsKey(multiKey)) {
      return types.get(multiKey);
    }
    if (exceptionIfNotFound) {
      String msg = "Invalid change log type: category: " + changeLogCategory + ", action: " + changeLogAction;
      throw new RuntimeException(msg);
    }
    return null;
  }

  /** 
   * Find an {@link ChangeLogType}.
   * <p/>
   * @param   changeLogTypeId  Find {@link ChangeLogType} with this id.
   * @param exceptionIfNotFound 
   * @return  {@link GroupType}
   */
  public static ChangeLogType find(String changeLogTypeId, boolean exceptionIfNotFound) {
    
    // First check to see if type is cached.
    if (typesById != null && typesById.containsKey(changeLogTypeId)) {
      return typesById.get(changeLogTypeId);
    }
    // If not, refresh known types as it may be new and try again. 
    internal_updateKnownTypes();
    if (typesById.containsKey(changeLogTypeId)) {
      return typesById.get(changeLogTypeId);
    }
    if (exceptionIfNotFound) {
      String msg = "Invalid change log type id: " + changeLogTypeId;
      throw new RuntimeException(msg);
    }
    return null;
  }

  /**
   * update the internal cache
   */
  public synchronized static void internal_updateKnownTypes() {
    Set<ChangeLogType> changeLogTypes = GrouperDAOFactory.getFactory().getChangeLogType().findAll();
    GrouperCache<MultiKey, ChangeLogType> newTypes = new GrouperCache<MultiKey, ChangeLogType>(
        ChangeLogTypeFinder.class.getName() + ".typeCache", 10000, false, 60*10, 60*10, false);
    
    Map<String, ChangeLogType> newTypesById = new HashMap<String, ChangeLogType>();
    
    for (ChangeLogType changeLogType : GrouperUtil.nonNull(changeLogTypes)) {
      newTypes.put(new MultiKey(changeLogType.getChangeLogCategory(), changeLogType.getActionName()), changeLogType);
      newTypesById.put(changeLogType.getId(), changeLogType);
    }
    
    //add builtins if necessary
    internal_updateBuiltinTypesOnce(newTypes, newTypesById);
    
    types = newTypes;
    typesById = newTypesById;
  }
  
  /**
   * keep track if updated
   */
  private static boolean updatedBuiltinTypes = false;
  
  /**
   * update builtin types once
   * @param newTypes
   * @param newTypesById 
   */
  private static void internal_updateBuiltinTypesOnce(GrouperCache<MultiKey, ChangeLogType> newTypes,
      Map<String, ChangeLogType> newTypesById) {
    if (updatedBuiltinTypes && newTypes.getCache().getSize() != 0) {
      return;
    }
    
    for (ChangeLogTypeBuiltin changeLogTypeBuiltin : ChangeLogTypeBuiltin.values()) {
      internal_findOrReplaceChangeLogType(newTypes, newTypesById, changeLogTypeBuiltin.internal_changeLogTypeDefault());
    }
  }
  
  /**
   * 
   * @param newTypes
   * @param newTypesById 
   * @param changeLogType
   */
  private static void internal_findOrReplaceChangeLogType(GrouperCache<MultiKey, ChangeLogType> newTypes, 
      Map<String, ChangeLogType> newTypesById, ChangeLogType changeLogType) {
    MultiKey changeLogKey = new MultiKey(changeLogType.getChangeLogCategory(), changeLogType.getActionName());

    //if new
    if (!newTypes.containsKey(changeLogKey)) {
      GrouperDAOFactory.getFactory().getChangeLogType().saveOrUpdate(changeLogType);
      newTypes.put(changeLogKey, changeLogType);
      newTypesById.put(changeLogType.getId(), changeLogType);
    } else {

      ChangeLogType existingType = newTypes.get(changeLogKey);
      if (!existingType.equalsDeep(changeLogType)) {

        //if existing and different then copy the new object fields into the existing, and store
        existingType.copyArgFieldIntoThis(changeLogType);
        GrouperDAOFactory.getFactory().getChangeLogType().saveOrUpdate(existingType);
      }
    }
    
  }
  
  
}
