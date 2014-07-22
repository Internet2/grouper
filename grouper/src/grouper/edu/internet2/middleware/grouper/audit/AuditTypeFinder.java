/**
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
 */
/*
 * @author mchyzer
 * $Id: AuditTypeFinder.java,v 1.4 2009-05-13 12:15:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

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
public class AuditTypeFinder {
  /** 
   * every 10 minutes, get new elements
   */
  private static GrouperCache<MultiKey, AuditType> types = null;

  /** 
   * sister cache to types cache
   */
  private static Map<String, AuditType> typesById = null;

  /**
   * clear this out, so it will begin again
   */
  public static void clearCache() {
    types = null;
    typesById = null;
    updatedBuiltinTypes = false;
    AuditTypeBuiltin.internal_clearCache();
  }
  
  /** 
   * Find an {@link AuditType}.
   * <p/>
   * @param   auditCategory  Find {@link AuditType} with this name.
   * @param   auditAction  Find {@link AuditType} with this name.
   * @param exceptionIfNotFound 
   * @return  {@link GroupType}
   */
  public static AuditType find(String auditCategory, String auditAction, boolean exceptionIfNotFound) {
    
    MultiKey multiKey = new MultiKey(auditCategory, auditAction);
    
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
      String msg = "Invalid audit type: category: " + auditCategory + ", action: " + auditAction;
      throw new RuntimeException(msg);
    }
    return null;
  }

  /** 
   * Find an {@link AuditType}.
   * <p/>
   * @param   auditCategory  Find {@link AuditType} with this name.
   * @return  {@link GroupType}
   */
  public static Set<AuditType> findByCategory(String auditCategory) {
    
    return GrouperDAOFactory.getFactory().getAuditType().findByCategory(auditCategory);
  }

  /** 
   * Find an {@link AuditType}.
   * <p/>
   * @param   auditTypeId  Find {@link AuditType} with this id.
   * @param exceptionIfNotFound 
   * @return  {@link GroupType}
   */
  public static AuditType find(String auditTypeId, boolean exceptionIfNotFound) {
    
    // First check to see if type is cached.
    if (typesById != null && typesById.containsKey(auditTypeId)) {
      return typesById.get(auditTypeId);
    }
    // If not, refresh known types as it may be new and try again. 
    internal_updateKnownTypes();
    if (typesById.containsKey(auditTypeId)) {
      return typesById.get(auditTypeId);
    }
    if (exceptionIfNotFound) {
      String msg = "Invalid audit type id: " + auditTypeId;
      throw new RuntimeException(msg);
    }
    return null;
  }

  /**
   * update the internal cache
   */
  public synchronized static void internal_updateKnownTypes() {
    Set<AuditType> auditTypes = GrouperDAOFactory.getFactory().getAuditType().findAll();
    GrouperCache<MultiKey, AuditType> newTypes = new GrouperCache<MultiKey, AuditType>(
        AuditTypeFinder.class.getName() + ".typeCache", 10000, false, 60*10, 60*10, false);
    
    Map<String, AuditType> newTypesById = new HashMap<String, AuditType>();
    
    for (AuditType auditType : GrouperUtil.nonNull(auditTypes)) {
      newTypes.put(new MultiKey(auditType.getAuditCategory(), auditType.getActionName()), auditType);
      newTypesById.put(auditType.getId(), auditType);
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
  private static void internal_updateBuiltinTypesOnce(GrouperCache<MultiKey, AuditType> newTypes,
      Map<String, AuditType> newTypesById) {
    if (updatedBuiltinTypes && newTypes.getCache().getSize() != 0) {
      return;
    }
    
    for (AuditTypeBuiltin auditTypeBuiltin : AuditTypeBuiltin.values()) {
      internal_findOrReplaceAuditType(newTypes, newTypesById, auditTypeBuiltin.internal_auditTypeDefault());
    }
  }
  
  /**
   * 
   * @param newTypes
   * @param newTypesById 
   * @param auditType
   */
  private static void internal_findOrReplaceAuditType(GrouperCache<MultiKey, AuditType> newTypes, 
      Map<String, AuditType> newTypesById, AuditType auditType) {
    MultiKey auditKey = new MultiKey(auditType.getAuditCategory(), auditType.getActionName());

    //if new
    if (!newTypes.containsKey(auditKey)) {
      GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(auditType);
      newTypes.put(auditKey, auditType);
      newTypesById.put(auditType.getId(), auditType);
    } else {

      AuditType existingType = newTypes.get(auditKey);
      if (!existingType.equalsDeep(auditType)) {

        //if existing and different then copy the new object fields into the existing, and store
        existingType.copyArgFieldIntoThis(auditType);
        GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(existingType);
      }
    }
    
  }
  
  
}
