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
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignDAO.java,v 1.10 2009-10-02 05:57:58 mchyzer Exp $
 */
public class Hib3AttributeAssignDAO extends Hib3DAO implements AttributeAssignDAO {
  
  /**
   * 
   */
  private static final String KLASS = Hib3AttributeAssignDAO.class.getName();

  /**
   * reset the attribute assigns
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssign where ownerAttributeAssignId is not null").executeUpdate();
    hibernateSession.byHql().createQuery("delete from AttributeAssign").executeUpdate();
  }

  /**
   * @param multiKey
   * @return string multikey
   */
  private static String attributeAssignFlashCacheMultiKeyToString(MultiKey multiKey) {
    
    if (multiKey == null) {
      return null;
    }
    
    StringBuilder result = new StringBuilder();
    
    String type = (String)multiKey.getKey(0);

    result.append("type: ").append(type).append(", source: ").append(multiKey.getKey(1)).append(", subjectId: ").append(multiKey.getKey(2));
    
    if (StringUtils.equals(type, "params")) {
      
      if (!StringUtils.isBlank((String)multiKey.getKey(3))) {
        result.append(", attributeDefId: ").append(multiKey.getKey(3));
      }
      if (!StringUtils.isBlank((String)multiKey.getKey(4))) {
        result.append(", attributeDefNameId: ").append(multiKey.getKey(4));
      }
      if (!StringUtils.isBlank((String)multiKey.getKey(5))) {
        result.append(", ownerAttributeAssignId: ").append(multiKey.getKey(5));
      }
      if (!StringUtils.isBlank((String)multiKey.getKey(6))) {
        result.append(", ownerAttributeDefId: ").append(multiKey.getKey(6));
      }
      if (!StringUtils.isBlank((String)multiKey.getKey(7))) {
        result.append(", ownerGroupId: ").append(multiKey.getKey(7));
      }
      if (!StringUtils.isBlank((String)multiKey.getKey(8))) {
        result.append(", ownerMemberId: ").append(multiKey.getKey(8));
      }
      if (!StringUtils.isBlank((String)multiKey.getKey(9))) {
        result.append(", ownerMembershipId: ").append(multiKey.getKey(9));
      }
      if (!StringUtils.isBlank((String)multiKey.getKey(10))) {
        result.append(", ownerStemId: ").append(multiKey.getKey(10));
      }
      
    } else if (StringUtils.equals(type, "id")) {

      result.append(", id: ").append(multiKey.getKey(3));
      
    }
    return result.toString();
  }
  
  /**
   * @param attributeDefId 
   * @param attributeDefNameId 
   * @param ownerAttributeAssignId 
   * @param ownerAttributeDefId 
   * @param ownerGroupId 
   * @param ownerMemberId 
   * @param ownerMembershipId 
   * @param ownerStemId 
   * @return multikey
   */
  private static MultiKey attributeAssignFlashCacheMultiKey(String attributeDefId, String attributeDefNameId, 
      String ownerAttributeAssignId, String ownerAttributeDefId,
      String ownerGroupId, String ownerMemberId, String ownerMembershipId,
      String ownerStemId) {
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_ASSIGNS_CACHE, true)) {
      return null;
    }
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    
    Subject grouperSessionSubject = null;
    
    if (grouperSession != null) {
      grouperSessionSubject = SubjectFinder.findRootSubject();
    } else {
      grouperSessionSubject = grouperSession.getSubject();
    }

    return new MultiKey(new Object[] {"params", grouperSessionSubject.getSourceId(), grouperSessionSubject.getId(), 
        attributeDefId, attributeDefNameId, 
        ownerAttributeAssignId, ownerAttributeDefId,
        ownerGroupId, ownerMemberId, ownerMembershipId,
        ownerStemId});
  }
  
  /**
   * @param id
   * @return multikey
   */
  private static MultiKey attributeAssignFlashCacheMultiKey(String id) {
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_ASSIGNS_CACHE, true)) {
      return null;
    }
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    
    Subject grouperSessionSubject = null;
    
    if (grouperSession != null) {
      grouperSessionSubject = SubjectFinder.findRootSubject();
    } else {
      grouperSessionSubject = grouperSession.getSubject();
    }

    return new MultiKey(new Object[] {"id", grouperSessionSubject.getSourceId(), grouperSessionSubject.getId(), 
        id});
  }
  
  /**
   * cache stuff in attributeAssign by subjectSourceId, subjectId, and all params
   */
  private static GrouperCache<MultiKey, Set<AttributeAssign>> attributeAssignFlashCache = new GrouperCache(
      "edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeAssignDAO.attributeAssignFlashCache");

  /**
   * 
   */
  private static final String GROUPER_FLASHCACHE_FIND_ATTRIBUTE_ASSIGNS_CACHE = "grouperFlashCache.find.attributeAssignsCache";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3AttributeAssignDAO.class);
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAllEnabledDisabledMismatch()
   */
  public Set<AttributeAssign> findAllEnabledDisabledMismatch() {
    long now = System.currentTimeMillis();

    StringBuilder sql = new StringBuilder(
        "select ats from AttributeAssign as ats where  "
          + "(ats.enabledDb = 'F' and ats.enabledTimeDb is null and ats.disabledTimeDb is null) "  
          + " or (ats.enabledDb = 'F' and ats.enabledTimeDb is null and ats.disabledTimeDb > :now) "
          + " or (ats.enabledDb = 'F' and ats.enabledTimeDb < :now and ats.disabledTimeDb is null) "
          + " or (ats.enabledDb = 'F' and ats.enabledTimeDb < :now and ats.disabledTimeDb > :now) "
          + " or (ats.enabledDb = 'T' and ats.disabledTimeDb < :now) "
          + " or (ats.enabledDb = 'T' and ats.enabledTimeDb > :now) "
          + " or (ats.enabledDb <> 'T' and ats.enabledDb <> 'F') "
          + " or (ats.enabledDb is null) "
     );

    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setLong( "now",  now )
      .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * retrieve by id
   * @param id 
   * @param exceptionIfNotFound 
   * @return  the attribute assign
   */
  public AttributeAssign findById(String id, boolean exceptionIfNotFound) {
    return findById(id, exceptionIfNotFound, true);
  }
  
  /**
   * retrieve by id
   * @param id 
   * @param exceptionIfNotFound 
   * @param useCache
   * @return  the attribute assign
   */
  public AttributeAssign findById(String id, boolean exceptionIfNotFound, boolean useCache) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    if (useCache) {
      byHqlStatic.setCacheable(true)
        .setCacheRegion(KLASS + ".FindById");
    }
    
    AttributeAssign attributeAssign = byHqlStatic.createQuery(
        "from AttributeAssign where id = :theId")
      .setString("theId", id)
      .uniqueResult(AttributeAssign.class);
    
    if (attributeAssign == null && exceptionIfNotFound) {
      throw new AttributeAssignNotFoundException("Cant find attribute assign by id: " + id);
    }

    return attributeAssign;
  }

  
  /**
   * retrieve by ids.  note, this is not a secure method, will return any results queried
   * @param id 
   * @param exceptionIfNotFound 
   * @param useCache
   * @return the attribute assigns, will not return null
   */
  public Set<AttributeAssign> findByIds(Collection<String> ids, Boolean enabled, boolean useCache) {
    int idsSize = GrouperUtil.length(ids);
    
    Set<AttributeAssign> results = new HashSet<AttributeAssign>();
    
    if (idsSize == 0) {
      return results;
    }
    
    List<String> idsList = new ArrayList<String>(ids);
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, 100);
    
    //if there are more than 100, batch these up and return them
    for (int i=0;i<numberOfBatches; i++) {
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      
      if (useCache) {
        byHqlStatic.setCacheable(true)
          .setCacheRegion(KLASS + ".FindById");
      }

      StringBuilder sql = new StringBuilder("from AttributeAssign where ");
      
      List<String> currentBatch = GrouperUtil.batchList(idsList, 100, i);
      
      sql.append(" id in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(") ");
    
      Set<AttributeAssign> localResult = byHqlStatic.createQuery(sql.toString())
        .listSet(AttributeAssign.class);
      
      results.addAll(localResult);
    }

    return results;
  }

  /**
   * save or update
   * @param attributeAssign 
   */
  public void saveOrUpdate(AttributeAssign attributeAssign) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssign);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdAndAttributeDefNameId(String groupId, String attributeDefNameId) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(null, attributeDefNameId, null, null, groupId, null, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerGroupId = :theOwnerGroupId and attributeAssignTypeDb = 'group'")
      .setString("theAttributeDefNameId", attributeDefNameId)
      .setString("theOwnerGroupId", groupId)
      .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdAndAttributeDefId(String groupId,
      String attributeDefId) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(attributeDefId, null, null, null, groupId, null, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerGroupId = :theOwnerGroupId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerGroupId", groupId)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

    
  }

  /**
   * delete
   * @param attributeAssign 
   */
  public void delete(AttributeAssign attributeAssign) {
    HibernateSession.byObjectStatic().delete(attributeAssign);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByGroupIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByGroupIdAndAttributeDefId(
      String groupId, String attributeDefId) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerGroupId = :theOwnerGroupId and theAttributeAssign.enabledDb = 'T' ")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerGroupId", groupId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByStemIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByStemIdAndAttributeDefId(
      String stemId, String attributeDefId) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerStemId = :theOwnerStemId and theAttributeAssign.enabledDb = 'T' ")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerStemId", stemId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByStemIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByStemIdAndAttributeDefId(String stemId,
      String attributeDefId) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(attributeDefId, null, null, null, null, null, null, stemId);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerStemId = :theOwnerStemId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerStemId", stemId)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByStemIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByStemIdAndAttributeDefNameId(String stemId,
      String attributeDefNameId) {
  
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(null, attributeDefNameId, null, null, null, null, null, stemId);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerStemId = :theOwnerStemId")
        .setString("theAttributeDefNameId", attributeDefNameId)
        .setString("theOwnerStemId", stemId)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByMemberIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByMemberIdAndAttributeDefId(
      String memberId, String attributeDefId) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMemberId = :theOwnerMemberId and theAttributeAssign.enabledDb = 'T' ")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerMemberId", memberId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMemberIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMemberIdAndAttributeDefId(String memberId,
      String attributeDefId) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(attributeDefId, null, null, null, null, memberId, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMemberId = :theOwnerMemberId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerMemberId", memberId)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMemberIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMemberIdAndAttributeDefNameId(String memberId,
      String attributeDefNameId) {

    MultiKey multiKey = attributeAssignFlashCacheMultiKey(null, attributeDefNameId, null, null, null, memberId, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerMemberId = :theOwnerMemberId")
        .setString("theAttributeDefNameId", attributeDefNameId)
        .setString("theOwnerMemberId", memberId)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByAttributeDefIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByAttributeDefIdAndAttributeDefId(
      String attributeDefIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId and theAttributeAssign.enabledDb = 'T' ")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttributeDefId", attributeDefIdToAssignTo)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttributeDefIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttributeDefIdAndAttributeDefId(
      String attributeDefIdToAssignTo, String attributeDefIdToAssign) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(attributeDefIdToAssign, null, null, attributeDefIdToAssignTo, null, null, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttributeDefId", attributeDefIdToAssignTo)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttributeDefIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttributeDefIdAndAttributeDefNameId(
      String attributeDefIdToAssignTo, String attributeDefNameIdToAssign) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(null, attributeDefNameIdToAssign, null, attributeDefIdToAssignTo, null, null, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerAttributeDefId = :theOwnerAttributeDefId")
        .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
        .setString("theOwnerAttributeDefId", attributeDefIdToAssignTo)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByMembershipIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByMembershipIdAndAttributeDefId(
      String membershipIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMembershipId = :theOwnerMembershipId and theAttributeAssign.enabledDb = 'T' ")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMembershipId", membershipIdToAssignTo)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMembershipIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMembershipIdAndAttributeDefId(
      String membershipIdToAssignTo, String attributeDefIdToAssign) {
  
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(attributeDefIdToAssign, null, null, null, null, null, membershipIdToAssignTo, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMembershipId = :theOwnerMembershipId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMembershipId", membershipIdToAssignTo)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMembershipIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMembershipIdAndAttributeDefNameId(
      String membershipIdToAssignTo, String attributeDefNameIdToAssign) {

    MultiKey multiKey = attributeAssignFlashCacheMultiKey(null, attributeDefNameIdToAssign, null, null, null, null, membershipIdToAssignTo, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerMembershipId = :theOwnerMembershipId")
        .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
        .setString("theOwnerMembershipId", membershipIdToAssignTo)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByAttrAssignIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByAttrAssignIdAndAttributeDefId(
      String attrAssignIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeAssignId = :theOwnerAttrAssignId and theAttributeAssign.enabledDb = 'T' ")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttrAssignId", attrAssignIdToAssignTo)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttrAssignIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttrAssignIdAndAttributeDefId(
      String attrAssignIdToAssignTo, String attributeDefIdToAssign) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(attributeDefIdToAssign, null, attrAssignIdToAssignTo, null, null, null, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeAssignId = :theOwnerAttrAssignId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttrAssignId", attrAssignIdToAssignTo)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttrAssignIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttrAssignIdAndAttributeDefNameId(
      String attrAssignIdToAssignTo, String attributeDefNameIdToAssign) {

    MultiKey multiKey = attributeAssignFlashCacheMultiKey(null, attributeDefNameIdToAssign, attrAssignIdToAssignTo, null, null, null, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerAttributeAssignId = :theOwnerAttrAssignId")
        .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
        .setString("theOwnerAttrAssignId", attrAssignIdToAssignTo)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByGroupIdMemberIdAndAttributeDefId(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByGroupIdMemberIdAndAttributeDefId(
      String groupId, String memberId, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId " +
        "and theAttributeAssign.ownerMemberId = :theOwnerMemberId " +
        "and theAttributeAssign.ownerGroupId = :theOwnerGroupId and theAttributeAssign.enabledDb = 'T' ")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMemberId", memberId)
        .setString("theOwnerGroupId", groupId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdMemberIdAndAttributeDefId(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdMemberIdAndAttributeDefId(String groupId,
      String memberId, String attributeDefIdToAssign) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(attributeDefIdToAssign, null, null, null, groupId, memberId, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMemberId = :theOwnerMemberId " +
        "and theAttributeAssign.ownerGroupId = :theOwnerGroupId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMemberId", memberId)
        .setString("theOwnerGroupId", groupId)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdMemberIdAndAttributeDefNameId(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdMemberIdAndAttributeDefNameId(String groupId,
      String memberId, String attributeDefNameIdToAssign) {
    
    MultiKey multiKey = attributeAssignFlashCacheMultiKey(null, attributeDefNameIdToAssign, null, null, groupId, memberId, null, null);
    Set<AttributeAssign> attributeAssigns = attributeAssignFlashCacheRetrieve(multiKey, null);
    if (attributeAssigns != null) {
      return attributeAssigns;
    }      
    
    attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerGroupId = :theOwnerGroupId " +
        "and ownerMemberId = :theOwnerMemberId")
        .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
        .setString("theOwnerGroupId", groupId)
        .setString("theOwnerMemberId", memberId)
        .listSet(AttributeAssign.class);

    attributeAssignFlashCacheAddIfSupposedTo(multiKey, attributeAssigns);
    
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByUuidOrKey(java.util.Collection, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.Long, java.lang.Long, java.lang.String, boolean)
   */
  public AttributeAssign findByUuidOrKey(Collection<String> idsToIgnore, String id,
      String attributeDefNameId, String attributeAssignActionId, String ownerAttributeAssignId,
      String ownerAttributeDefId, String ownerGroupId, String ownerMemberId,
      String ownerMembershipId, String ownerStemId, boolean exceptionIfNotFound,
      Long disabledTimeDb, Long enabledTimeDb, String notes, boolean disallowed) throws GrouperDAOException {
    try {
      Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssign as theAttributeAssign where\n " +
            "theAttributeAssign.id = :theId or (theAttributeAssign.attributeDefNameId = :theAttributeDefNameId and\n " +
            "theAttributeAssign.attributeAssignActionId = :theAttributeAssignActionId and\n " +
            "(" + (disallowed ? "theAttributeAssign.disallowedDb is null or " : "") + " theAttributeAssign.disallowedDb = :theDisallowedDb) and\n " +
            "(theAttributeAssign.ownerAttributeAssignId is null or theAttributeAssign.ownerAttributeAssignId = :theOwnerAttributeAssignId) and \n " +
            "(theAttributeAssign.ownerAttributeDefId is null or theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId) and \n " +
            "(theAttributeAssign.ownerGroupId is null or theAttributeAssign.ownerGroupId = :theOwnerGroupId) and \n " +
            "(theAttributeAssign.ownerMemberId is null or theAttributeAssign.ownerMemberId = :theOwnerMemberId) and \n " +
            "(theAttributeAssign.ownerMembershipId is null or theAttributeAssign.ownerMembershipId = :theOwnerMembershipId) and \n " +
            "(theAttributeAssign.ownerStemId is null or theAttributeAssign.ownerStemId = :theOwnerStemId) ) ")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrKey")
        .setString("theId", id)
        .setString("theAttributeAssignActionId", attributeAssignActionId)
        .setString("theAttributeDefNameId", attributeDefNameId)
        .setString("theOwnerAttributeAssignId", ownerAttributeAssignId)
        .setString("theOwnerAttributeDefId", ownerAttributeDefId)
        .setString("theOwnerGroupId", ownerGroupId)
        .setString("theOwnerMemberId", ownerMemberId)
        .setString("theOwnerMembershipId", ownerMembershipId)
        .setString("theOwnerStemId", ownerStemId)
        .setString("theDisallowedDb", disallowed ? "T" : "F")
        .listSet(AttributeAssign.class);
      if (GrouperUtil.length(attributeAssigns) == 0) {
        if (exceptionIfNotFound) {
          throw new RuntimeException("Can't find attributeAssign by id: '" + id + "' or attributeDefNameId '" + attributeDefNameId 
              + "', ownerAttributeAssignId: " + ownerAttributeAssignId
              + ", ownerAttributeDefId: " + ownerAttributeDefId
              + ", ownerGroupId: " + ownerGroupId
              + ", ownerMemberId: " + ownerMemberId
              + ", ownerMembershipId: " + ownerMembershipId
              + ", ownerStemId: " + ownerStemId
              );
        }
        return null;
      }
      
      idsToIgnore = GrouperUtil.nonNull(idsToIgnore);
      
      //lets remove ones we have already processed or will process
      Iterator<AttributeAssign> iterator = attributeAssigns.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssign attributeAssign = iterator.next();
        if (idsToIgnore.contains(attributeAssign.getId())) {
          iterator.remove();
        }
      }
      
      //first case, the ID matches
      iterator = attributeAssigns.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssign attributeAssign = iterator.next();
        if (StringUtils.equals(id, attributeAssign.getId())) {
          return attributeAssign;
        }
      }

      //second case, the value matches
      iterator = attributeAssigns.iterator();
      
      //tree map in reverse order
      Map<Integer, AttributeAssign> heuristicMap = new TreeMap<Integer, AttributeAssign>(new Comparator<Integer>() {

        public int compare(Integer o1, Integer o2) {
          if (o1 == o2) {
            return 0;
          }
          if (o1==null) {
            return o2;
          }
          if (o2== null) {
            return o1;
          }
          return -o1.compareTo(o2);
        }
      });
      
      while (iterator.hasNext()) {
        int score = 0;
        AttributeAssign attributeAssign = iterator.next();

        if (StringUtils.equals(notes, attributeAssign.getNotes())) {
          score++;
        }
        if (GrouperUtil.equals(disabledTimeDb, attributeAssign.getDisabledTimeDb())) {
          score++;
        }
        if (GrouperUtil.equals(enabledTimeDb, attributeAssign.getEnabledDb())) {
          score++;
        }
        
        heuristicMap.put(score, attributeAssign);
      }
      
      //ok, if there is one left, return it, the best heuristic first
      if (heuristicMap.size() > 0) {
        return heuristicMap.get(heuristicMap.keySet().iterator().next());
      }
      
      //cant find one
      return null;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeAssign by id: '" + id + "' or attributeDefNameId '" + attributeDefNameId 
              + "', ownerAttributeAssignId: " + ownerAttributeAssignId
              + ", ownerAttributeDefId: " + ownerAttributeDefId
              + ", ownerGroupId: " + ownerGroupId
              + ", ownerMemberId: " + ownerMemberId
              + ", ownerMembershipId: " + ownerMembershipId
              + ", ownerStemId: " + ownerStemId + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.assign.AttributeAssign)
   */
  public void saveUpdateProperties(AttributeAssign attributeAssign) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdatedDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeAssign.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeAssign.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeAssign.getLastUpdatedDb())
        .setString("theContextId", attributeAssign.getContextId())
        .setString("theId", attributeAssign.getId()).executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerAttributeAssignId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerAttributeAssignId(String ownerAttributeAssignId) {
    return findByOwnerAttributeAssignId(ownerAttributeAssignId, null);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerAttributeAssignId(java.lang.String, QueryOptions)
   */
  public Set<AttributeAssign> findByOwnerAttributeAssignId(String ownerAttributeAssignId, QueryOptions queryOptions) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerAttributeAssignId = :theOwnerAttrAssignId")
        .setString("theOwnerAttrAssignId", ownerAttributeAssignId)
        .options(queryOptions)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerGroupId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerGroupId(String ownerGroupId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerGroupId = :theOwnerGroupId")
        .setString("theOwnerGroupId", ownerGroupId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerMemberId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerMemberId(String ownerMemberId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerMemberId = :theOwnerMemberId")
        .setString("theOwnerMemberId", ownerMemberId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerMembershipId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerMembershipId(String ownerMembershipId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerMembershipId = :theOwnerMembershipId")
        .setString("theOwnerMembershipId", ownerMembershipId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerStemId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerStemId(String ownerStemId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerStemId = :theOwnerStemId")
        .setString("theOwnerStemId", ownerStemId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByActionId(java.lang.String)
   */
  public Set<AttributeAssign> findByActionId(String actionId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic()
      .createQuery("from AttributeAssign as theAttributeAssign where\n " +
          "theAttributeAssign.attributeAssignActionId = :theAttributeAssignActionId ")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByActionId")
      .setString("theAttributeAssignActionId", actionId)
      .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerAttributeDefId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerAttributeDefId(String ownerAttributeDefId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId")
        .setString("theOwnerAttributeDefId", ownerAttributeDefId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttributeDefNameId(java.lang.String)
   */
  public Set<AttributeAssign> findByAttributeDefNameId(String attributeDefNameId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.attributeDefNameId = :theAttributeDefNameId")
        .setString("theAttributeDefNameId", attributeDefNameId)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findGroupAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean)
   */
  public Set<AttributeAssign> findGroupAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments) {
    
    return findGroupAttributeAssignments(attributeAssignIds, attributeDefIds, attributeDefNameIds, groupIds, actions, enabled, includeAssignmentsOnAssignments, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findGroupAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType)
   */
  public Set<AttributeAssign> findGroupAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments,
      AttributeDefType attributeDefType) {
    return findGroupAttributeAssignments(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        groupIds, actions, enabled, includeAssignmentsOnAssignments,
        attributeDefType, null, null);
  }
  
  /**
   * make sure if sending a value you are sending a value type, and add table if necessary
   * @param byHqlStatic
   * @param sqlTables
   * @param sqlWhereClause should not be empty
   * @param attributeDefValueType
   * @param theValue
   * @param attributeAssignAlias is usually "aa"
   */
  public static void queryByValueAddTablesWhereClause(ByHqlStatic byHqlStatic, StringBuilder sqlTables, StringBuilder sqlWhereClause, 
      AttributeDefValueType attributeDefValueType, Object theValue) {

    String bindVariableName = GrouperUtil.uniqueId();
    
    if (theValue != null && attributeDefValueType == null) {
      throw new RuntimeException("Why is attributeDefValueType null if you are querying by value???");
    }

    if (attributeDefValueType != null) {
      
      if (sqlTables != null) {
        sqlTables.append(", AttributeAssignValue aav ");
      }
      
      if (sqlWhereClause.length() > 0) {
        sqlWhereClause.append(" and ");
      }
      sqlWhereClause.append(" aa.id = aav.attributeAssignId ");
      switch(attributeDefValueType) {
        case floating:
          sqlWhereClause.append(" and aav.valueFloating = :" + bindVariableName + " ");
          Double theDouble = (Double)attributeDefValueType.convertToObject(theValue);
          byHqlStatic.setDouble(bindVariableName, theDouble);
          break;
        case integer:
          sqlWhereClause.append(" and aav.valueInteger = :" + bindVariableName + " ");
          Long theLong = (Long)attributeDefValueType.convertToObject(theValue);
          byHqlStatic.setLong(bindVariableName, theLong);
          break;
          
        case marker:
          //this should throw exception
          throw new RuntimeException("Why are you querying by value on a marker attribute???");
        case memberId:
          theValue = attributeDefValueType.convertToObject(theValue);
          sqlWhereClause.append(" and aav.valueMemberId = :" + bindVariableName + " ");
          byHqlStatic.setString(bindVariableName, (String)theValue);
          break;
          
        case string:
          theValue = attributeDefValueType.convertToObject(theValue);
          sqlWhereClause.append(" and aav.valueString = :" + bindVariableName + " ");
          byHqlStatic.setString(bindVariableName, (String)theValue);
          break;
          
        case timestamp:
          
          sqlWhereClause.append(" and aav.valueInteger = :" + bindVariableName + " ");
          theValue = attributeDefValueType.convertToObject(theValue);
          
          if (theValue != null) {
            theValue = ((Timestamp)theValue).getTime();
          }
          byHqlStatic.setLong(bindVariableName, (Long)theValue);
          break;
          
          
        default:
          throw new RuntimeException("Not expecting attributeDefValueType: " + attributeDefValueType);
      }
    }
    
  }
  
  /**
   * make sure if sending a value you are sending a value type, and add table if necessary
   * @param byHqlStatic
   * @param sqlTables
   * @param sqlWhereClause should not be empty
   * @param attributeDefValueType
   * @param theValues
   * @param attributeAssignAlias is usually "aa"
   */
  public static void queryByValuesAddTablesWhereClause(ByHqlStatic byHqlStatic, StringBuilder sqlTables, StringBuilder sqlWhereClause, 
      AttributeDefValueType attributeDefValueType, Set<Object> theValues, String attributeAssignAlias) {

    String bindVariableName = GrouperUtil.uniqueId();

    if (theValues != null && attributeDefValueType == null) {
      throw new RuntimeException("Why is attributeDefValueType null if you are querying by value???");
    }

    if (attributeDefValueType != null) {
      
      if (sqlTables != null) {
        sqlTables.append(", AttributeAssignValue aav ");
      }
      
      if (sqlWhereClause.length() > 0) {
        sqlWhereClause.append(" and ");
      }
      sqlWhereClause.append(" " + attributeAssignAlias + ".id = aav.attributeAssignId ");
      
      switch(attributeDefValueType) {
        case floating:
          sqlWhereClause.append(" and aav.valueFloating ");
          break;
        case integer:
          sqlWhereClause.append(" and aav.valueInteger ");
          break;
          
        case marker:
          //this should throw exception
          throw new RuntimeException("Why are you querying by value on a marker attribute???");
        case memberId:
          sqlWhereClause.append(" and aav.valueMemberId ");
          break;
          
        case string:
          sqlWhereClause.append(" and aav.valueString ");
          break;
          
        case timestamp:
          
          sqlWhereClause.append(" and aav.valueInteger ");
          break;
          
          
        default:
          throw new RuntimeException("Not expecting attributeDefValueType: " + attributeDefValueType);
      }

      if (GrouperUtil.length(theValues) > 100) {
        throw new RuntimeException("Too many values for theValues: " + GrouperUtil.length(theValues));
      }

      List<Object> theValuesList = new ArrayList<Object>(theValues);
      sqlWhereClause.append(" in ( ");
      for (int i=0; i<GrouperUtil.length(theValuesList); i++) {
        if (i!=0) {
          sqlWhereClause.append(", ");
        }
        sqlWhereClause.append(" :" + bindVariableName);
        sqlWhereClause.append(i);
        
      }
      sqlWhereClause.append(" ) ");
      
      for (int i=0; i<GrouperUtil.length(theValuesList); i++) {
        Object theValueLocal = theValuesList.get(i);
        String alias = bindVariableName + i;
        switch(attributeDefValueType) {
          case floating:
            
            Double theDouble = (Double)attributeDefValueType.convertToObject(theValueLocal);
            byHqlStatic.setDouble(alias, theDouble);
  
            break;
          case integer:
            Long theLong = (Long)attributeDefValueType.convertToObject(theValueLocal);
            byHqlStatic.setLong(alias, theLong);
            break;
            
          case marker:
            //this should throw exception
            throw new RuntimeException("Why are you querying by value on a marker attribute???");
          case memberId:
            theValueLocal = attributeDefValueType.convertToObject(theValueLocal);
            byHqlStatic.setString(alias, (String)theValueLocal);
            break;
            
          case string:
            theValueLocal = attributeDefValueType.convertToObject(theValueLocal);
            byHqlStatic.setString(alias, (String)theValueLocal);
            break;
            
          case timestamp:
            
            theValueLocal = attributeDefValueType.convertToObject(theValueLocal);
            
            if (theValueLocal != null) {
              theValueLocal = ((Timestamp)theValueLocal).getTime();
            }
            byHqlStatic.setLong(alias, (Long)theValueLocal);
            break;
            
            
          default:
            throw new RuntimeException("Not expecting attributeDefValueType: " + attributeDefValueType);
        }
      }
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findGroupAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType, AttributeDefValueType, Object, boolean)
   */
  public Set<AttributeAssign> findGroupAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments,
      AttributeDefType attributeDefType, AttributeDefValueType attributeDefValueType, Object theValue,
      boolean attributeCheckReadOnAttributeDef) {
    return findGroupAttributeAssignmentsHelper(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        groupIds, actions, enabled, includeAssignmentsOnAssignments,
        attributeDefType, attributeDefValueType, theValue, attributeCheckReadOnAttributeDef);

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findGroupAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType, AttributeDefValueType, Object, boolean)
   */
  private Set<AttributeAssign> findGroupAttributeAssignmentsHelper(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments,
      AttributeDefType attributeDefType, AttributeDefValueType attributeDefValueType, Object theValue,
      Boolean attributeCheckReadOnAttributeDef) {
      
    attributeCheckReadOnAttributeDef = GrouperUtil.defaultIfNull(attributeCheckReadOnAttributeDef, true);
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int groupIdsSize = GrouperUtil.length(groupIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && groupIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or groupId(s) and/or groupNames and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + groupIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " groupIdsSize " + groupIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");    
    
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }

    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    StringBuilder sqlWhereClause = new StringBuilder(
        " aa.attributeDefNameId = adn.id ");
    
    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'group' ");
    
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    boolean changedQuery = false;
    
    if (attributeCheckReadOnAttributeDef) {
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
      changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSessionSubject, byHqlStatic, 
          sqlTables, "aa.ownerGroupId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
    }
    

    StringBuilder sql;
    
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }
    
    sql = sqlTables.append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }

    if (attributeDefType != null) {
      sql.append(" and adn.attributeDefId = ad.id and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (groupIdsSize > 0) {
      sql.append(" and aa.ownerGroupId in (");
      sql.append(HibUtils.convertToInClause(groupIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindGroupAttributeAssignments");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    //if -1, lets not check
    long size = -1;
    
    if (maxAssignments >= 0) {

      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = size == 0 ? new LinkedHashSet<AttributeAssign>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);

    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    if (attributeCheckReadOnAttributeDef) {
      results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    }
    
    //if looking for assignments on assignments, do that now
    if (includeAssignmentsOnAssignments) {
      Set<AttributeAssign> assignmentsOnAssignments = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAssignmentsOnAssignments(results, AttributeAssignType.group_asgn, enabled, attributeCheckReadOnAttributeDef);
      results.addAll(assignmentsOnAssignments);
    }
    
    //we should be down to the secure list
    return results;
      
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findStemAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean)
   */
  public Set<AttributeAssign> findStemAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> stemIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments) {

    return findStemAttributeAssignments(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        stemIds, actions, enabled, includeAssignmentsOnAssignments, 
        null, null, null);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findStemAttributeAssignmentsOnAssignments(Collection, Collection, Collection, Collection, Collection, Boolean, AttributeDefType, AttributeDefValueType, Object, boolean, Collection, Collection, Collection, Collection, boolean)
   */
  public Set<AttributeAssign> findStemAttributeAssignmentsOnAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> stemIds, Collection<String> actions, 
      Boolean enabled, 
      AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType,
      Object theValue, boolean includeAssignmentsFromAssignments,
      Collection<String> ownerAttributeAssignIds,
      Collection<String> ownerAttributeDefIds, 
      Collection<String> ownerAttributeDefNameIds,
      Collection<String> ownerActions, boolean useCache) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int stemIdsSize = GrouperUtil.length(stemIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    int ownerAttributeAssignIdsSize = GrouperUtil.length(ownerAttributeAssignIds);
    int ownerAttributeDefIdsSize = GrouperUtil.length(ownerAttributeDefIds);
    int ownerAttributeDefNameIdsSize = GrouperUtil.length(ownerAttributeDefNameIds);
    int ownerActionsSize = GrouperUtil.length(ownerActions);
    
    if (attributeAssignIdsSize == 0 && stemIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0
        && ownerAttributeAssignIdsSize == 0 && ownerAttributeDefIdsSize == 0 && ownerAttributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) " +
      		"and/or stemId(s) and/or stemName(s) and/or attributeDefNameIds and/or ownerAttributeAssignIds" +
      		" and/or ownerAttributeDefIdsSize and/or ownerAttributeDefNameIdsSize");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + stemIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize 
        + ownerAttributeAssignIdsSize + ownerAttributeDefIdsSize + ownerAttributeDefNameIdsSize + ownerActionsSize > 100 ) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " stemIdsSize " + stemIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize 
          + " or actionsSize " + actionsSize + " or ownerAttributeAssignIdsSize " + ownerAttributeAssignIdsSize
          + " or ownerAttributeDefIdsSize " + ownerAttributeDefIdsSize 
          + " ownerAttributeDefNameIdsSize " + ownerAttributeDefNameIdsSize
          + " or ownerActionsSize " + ownerActionsSize);
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";

    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeAssign ownerAa, AttributeDefName ownerAdn ");

    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }

    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }

    if (ownerActionsSize > 0) {
      sqlTables.append(", AttributeAssignAction ownerAaa ");
    }

    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and ownerAa.attributeDefNameId = ownerAdn.id and aa.ownerAttributeAssignId = ownerAa.id ");

    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'stem_asgn' ");

    sqlWhereClause.append(" and ownerAa.attributeAssignTypeDb = 'stem' ");
    
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    //need to check the assignment on assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);

    //need to check the assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ownerAdn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);

    StringBuilder sql = new StringBuilder(sqlTables.toString());
    
    //need to check underlying object
    boolean changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sql, "ownerAa.ownerStemId", NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES);
    
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append(sqlWhereClause);

    attributeAssignAssignQueryStart(attributeAssignIds, attributeDefIds,
        attributeDefNameIds, actions, enabled, attributeDefType, ownerAttributeAssignIds,
        ownerAttributeDefIds, ownerAttributeDefNameIds, ownerActions,
        attributeAssignIdsSize, actionsSize, attributeDefIdsSize,
        attributeDefNameIdsSize, ownerAttributeAssignIdsSize, ownerAttributeDefIdsSize,
        ownerAttributeDefNameIdsSize, ownerActionsSize, byHqlStatic, sql);

    if (stemIdsSize > 0) {
      sql.append(" and ownerAa.ownerStemId in (");
      sql.append(HibUtils.convertToInClause(stemIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(useCache)
      .setCacheRegion(KLASS + ".FindStemAttributeAssignments");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    Set<AttributeAssign> results = attributeAssignmentAssignmentRunQuery(
        includeAssignmentsFromAssignments, useCache, byHqlStatic, selectPrefix,
        countPrefix, grouperSession, grouperSessionSubject, sql, maxAssignments);

    //we should be down to the secure list
    return results;
      
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMemberAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean)
   */
  public Set<AttributeAssign> findMemberAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> memberIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments) {
    
    return findMemberAttributeAssignments(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        memberIds, actions, enabled, includeAssignmentsOnAssignments, 
        null, null, null);
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMemberAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType, AttributeDefValueType, Object)
   */
  public Set<AttributeAssign> findMemberAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> memberIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments, 
      AttributeDefType attributeDefType, 
      AttributeDefValueType attributeDefValueType, Object theValue) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int memberIdsSize = GrouperUtil.length(memberIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && memberIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or memberId(s) [subjectIds or subjectIdentifiers] and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + memberIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " memberIdsSize " + memberIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }

    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'member' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();

    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    StringBuilder sql;
    sql = sqlTables.append(" where ").append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }

    if (attributeDefType != null) {
      sql.append(" and adn.attributeDefId = ad.id and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }

    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (memberIdsSize > 0) {
      sql.append(" and aa.ownerMemberId in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = size == 0 ? new LinkedHashSet<AttributeAssign>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    
    //if looking for assignments on assignments, do that now
    if (includeAssignmentsOnAssignments) {
      Set<AttributeAssign> assignmentsOnAssignments = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAssignmentsOnAssignments(results, AttributeAssignType.mem_asgn, enabled);
      results.addAll(assignmentsOnAssignments);
    }

    //we should be down to the secure list
    return results;
      
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean)
   */
  public Set<AttributeAssign> findAttributeDefAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> attributeDefAssignToIds, Collection<String> actions, 
      Boolean enabled, boolean includeAssignmentsOnAssignments) {
    
    return findAttributeDefAttributeAssignments(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        attributeDefAssignToIds, actions, 
        enabled, includeAssignmentsOnAssignments, null, null, 
        null);
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType, AttributeDefValueType, Object)
   */
  public Set<AttributeAssign> findAttributeDefAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> attributeDefAssignToIds, Collection<String> actions, 
      Boolean enabled, boolean includeAssignmentsOnAssignments, AttributeDefType attributeDefType, 
      AttributeDefValueType attributeDefValueType, 
      Object theValue) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int attributeDefAssignToIdsSize = GrouperUtil.length(attributeDefAssignToIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && attributeDefAssignToIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or attributeDefAssignToId(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + attributeDefAssignToIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " attributeDefAssignToIdsSize " + attributeDefAssignToIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }

    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }

    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'attr_def' ");
    
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "aa.ownerAttributeDefId", AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES);
      
    StringBuilder sql = sqlTables.append(" where ").append(sqlWhereClause);
    
    if (attributeDefType != null) {
      sql.append(" and adn.attributeDefId = ad.id and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefAssignToIdsSize > 0) {
      sql.append(" and aa.ownerAttributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefAssignToIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAttributeDefAttributeAssignments");

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    //if -1, lets not check
    if (maxAssignments >= 0) {

      long size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);

    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    
    //if looking for assignments on assignments, do that now
    if (includeAssignmentsOnAssignments) {
      Set<AttributeAssign> assignmentsOnAssignments = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAssignmentsOnAssignments(results, AttributeAssignType.attr_def_asgn, enabled);
      results.addAll(assignmentsOnAssignments);
    }

    //we should be down to the secure list
    return results;
      
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMembershipAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean)
   */
  public Set<AttributeAssign> findMembershipAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> membershipIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments) {

    return findMembershipAttributeAssignments(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        membershipIds, actions, enabled, 
        includeAssignmentsOnAssignments, null, null, null);
    
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMembershipAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType, AttributeDefValueType, Object)
   */
  public Set<AttributeAssign> findMembershipAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> membershipIds, Collection<String> actions, Boolean enabled, 
      boolean includeAssignmentsOnAssignments, AttributeDefType attributeDefType, AttributeDefValueType attributeDefValueType, 
      Object theValue) {
    
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int membershipIdsSize = GrouperUtil.length(membershipIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && membershipIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or membershipId(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + membershipIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " membershipIdsSize " + membershipIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, ImmediateMembershipEntry ime ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }
    
    Field membersField = FieldFinder.find("members", true);
    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'imm_mem' and " +
    		"aa.ownerMembershipId = ime.immediateMembershipId and ime.fieldId = '" + membersField.getUuid() + "' " +
    				" and ime.enabledDb = 'T' ");
    
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();

    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);

    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "ime.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);

    StringBuilder sql;
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }

    sql = sqlTables.append(sqlWhereClause);

    if (membershipIdsSize > 0) {
      sql.append(" and aa.ownerMembershipId in (");
      sql.append(HibUtils.convertToInClause(membershipIds, byHqlStatic));
      sql.append(") ");
    }

    if (attributeDefType != null) {
      sql.append(" and adn.attributeDefId = ad.id and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }

    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMembershipAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = size == 0 ? new LinkedHashSet<AttributeAssign>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    
    //if looking for assignments on assignments, do that now
    if (includeAssignmentsOnAssignments) {
      Set<AttributeAssign> assignmentsOnAssignments = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAssignmentsOnAssignments(results, AttributeAssignType.imm_mem_asgn, enabled);
      results.addAll(assignmentsOnAssignments);
    }

    //we should be down to the secure list
    return results;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAnyMembershipAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean)
   */
  public Set<AttributeAssign> findAnyMembershipAttributeAssignments(
      Collection<String> attributeAssignIds, Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<MultiKey> groupIdsAndMemberIds,
      Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments) {
    return findAnyMembershipAttributeAssignments(attributeAssignIds, attributeDefIds, attributeDefNameIds, groupIdsAndMemberIds, actions, enabled, includeAssignmentsOnAssignments, null);
  }

  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAnyMembershipAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType)
   */
  public Set<AttributeAssign> findAnyMembershipAttributeAssignments(
      Collection<String> attributeAssignIds, Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<MultiKey> groupIdsAndMemberIds,
      Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments, AttributeDefType attributeDefType) {
    return findAnyMembershipAttributeAssignments(
        attributeAssignIds, attributeDefIds,
        attributeDefNameIds, groupIdsAndMemberIds,
        actions, enabled, includeAssignmentsOnAssignments, attributeDefType, 
        null, null);
  }
  
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAnyMembershipAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType, AttributeDefValueType, Object)
   */
  public Set<AttributeAssign> findAnyMembershipAttributeAssignments(
      Collection<String> attributeAssignIds, Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<MultiKey> groupIdsAndMemberIds,
      Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments, AttributeDefType attributeDefType, 
      AttributeDefValueType attributeDefValueType, Object theValue) {
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int groupIdsAndMemberIdsSize = GrouperUtil.length(groupIdsAndMemberIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);

    if (attributeAssignIdsSize == 0 && groupIdsAndMemberIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or membershipId(s) [groupIds/Names subjectIds/Identifiers] and/or attributeDefNameIds");
    }
    
    //too many bind vars
    //note, each groupIdsAndMemberIds is a multikey with a groupId and memberId
    if (attributeAssignIdsSize + (groupIdsAndMemberIdsSize*2) + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " groupIdsAndMemberIdsSize " + groupIdsAndMemberIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, MembershipEntry me ");
    
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }

    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    Field membersField = FieldFinder.find("members", true);
    
    StringBuilder sqlWhereClause = new StringBuilder(
        " aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'any_mem' and " +
        "aa.ownerGroupId = me.ownerGroupId and aa.ownerMemberId = me.memberUuid and me.fieldId = '" + membersField.getUuid() + "' " +
        		" and me.enabledDb = 'T' ");
    
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();

    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);

    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "aa.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);

    StringBuilder sql;
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }
    sql = sqlTables.append(sqlWhereClause);

    if (attributeDefType != null) {
      sql.append(" and adn.attributeDefId = ad.id and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeDefType != null) {
      sql.append(" and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (groupIdsAndMemberIdsSize > 0) {
      HibUtils.convertToMultiKeyInClause(groupIdsAndMemberIds, byHqlStatic, GrouperUtil.toSet("aa.ownerGroupId", "aa.ownerMemberId"), sql);
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAnyMembershipAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = size == 0 ? new LinkedHashSet<AttributeAssign>()
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    
    //if looking for assignments on assignments, do that now
    if (includeAssignmentsOnAssignments) {
      Set<AttributeAssign> assignmentsOnAssignments = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAssignmentsOnAssignments(results, AttributeAssignType.any_mem_asgn, enabled);
      results.addAll(assignmentsOnAssignments);
    }

    //we should be down to the secure list
    return results;
  }

  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAssignmentsOnAssignments(java.util.Collection, edu.internet2.middleware.grouper.attr.assign.AttributeAssignType, java.lang.Boolean)
   */
  public Set<AttributeAssign> findAssignmentsOnAssignments(
      Collection<AttributeAssign> attributeAssigns,
      AttributeAssignType attributeAssignType, Boolean enabled) {
    return findAssignmentsOnAssignments(attributeAssigns, attributeAssignType, enabled, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAssignmentsOnAssignments(java.util.Collection, edu.internet2.middleware.grouper.attr.assign.AttributeAssignType, java.lang.Boolean)
   */
  public Set<AttributeAssign> findAssignmentsOnAssignments(
      Collection<AttributeAssign> attributeAssigns,
      AttributeAssignType attributeAssignType, Boolean enabled, Boolean attributeCheckReadOnAttributeDef) {
    
    Set<String> attributeAssignIds = new HashSet<String>();
    
    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {
      attributeAssignIds.add(attributeAssign.getId());
    }
    
    return findAssignmentsOnAssignmentsByIds(attributeAssignIds, attributeAssignType, null, enabled, attributeCheckReadOnAttributeDef);
  }

  /**
   * @see AttributeAssignDAO#findAssignmentsOnAssignmentsByIds(Collection, AttributeAssignType, AttributeDefType, Boolean)
   */
  public Set<AttributeAssign> findAssignmentsOnAssignmentsByIds(Collection<String> attributeAssignIds, 
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType, Boolean enabled) {
    return findAssignmentsOnAssignmentsByIds(attributeAssignIds, attributeAssignType, attributeDefType, enabled, null);
  }

  /**
   * @see AttributeAssignDAO#findAssignmentsOnAssignmentsByIds(Collection, AttributeAssignType, AttributeDefType, Boolean, Boolean)
   */
  public Set<AttributeAssign> findAssignmentsOnAssignmentsByIds(Collection<String> attributeAssignIds, 
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType, Boolean enabled, Boolean attributeCheckReadOnAttributeDef) {

    attributeCheckReadOnAttributeDef = GrouperUtil.defaultIfNull(attributeCheckReadOnAttributeDef, true);
    
    int attributeAssignsSize = GrouperUtil.length(attributeAssignIds);

    Set<AttributeAssign> results = new LinkedHashSet<AttributeAssign>();
    
    if (attributeAssignsSize == 0) {
      return results;
    }
    
    //remove dupes
    Set<String> attributeAssignIdsSet = attributeAssignIds instanceof Set ? (Set)attributeAssignIds : new LinkedHashSet<String>(attributeAssignIds);
    
    //get in ordered list
    List<String> attributeAssignIdsList = new ArrayList<String>(attributeAssignIdsSet);
    
    attributeAssignsSize = GrouperUtil.length(attributeAssignIdsList);
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignsSize, 100);

    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    

    for (int i=0;i<numberOfBatches; i++) {
      
      List<String> currentBatch = GrouperUtil.batchList(attributeAssignIdsList, 100, i);
      
      int currentBatchSize = GrouperUtil.length(currentBatch);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      String selectPrefix = "select distinct aa ";
      
      StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeDef ad ");
      
      StringBuilder sqlWhereClause = new StringBuilder(
          " aa.attributeDefNameId = adn.id and adn.attributeDefId = ad.id ");
      
      if (attributeAssignType != null) {
        sqlWhereClause.append(" and aa.attributeAssignTypeDb = '" + attributeAssignType.name() + "' ");
      }
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      
      Subject grouperSessionSubject = grouperSession.getSubject();
      
      if (attributeCheckReadOnAttributeDef) {
        grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
            grouperSessionSubject, byHqlStatic, 
            sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
      }
      
      StringBuilder sql;
      sql = sqlTables.append(" where ").append(sqlWhereClause);
      
      if (attributeDefType != null) {
        sql.append(" and ad.attributeDefTypeDb = :theAttributeDefType ");
        byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
      }
      if (enabled != null && enabled) {
        sql.append(" and aa.enabledDb = 'T' ");
      }
      if (enabled != null && !enabled) {
        sql.append(" and aa.enabledDb = 'F' ");
      }
      if (currentBatchSize > 0) {
        
        sql.append(" and aa.ownerAttributeAssignId in (");
        sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
        sql.append(") ");
      }
      
      byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAssignmentsOnAssignments");

      //if -1, lets not check
      Set<AttributeAssign> tempResults = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);

      //nothing to filter
      if (GrouperUtil.length(tempResults) > 0 && attributeCheckReadOnAttributeDef) {
        //if the hql didnt filter, we need to do that here
        tempResults = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, tempResults);
      }
      
      results.addAll(tempResults);
      
      if (maxAssignments >= 0) {

        //see if too many
        if (results.size() > maxAssignments) {
          throw new RuntimeException("Too many results: " + results.size());
        }
        
      }
    }
    
    
    //we should be down to the secure list
    return results;

    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefsByAttributeDefNameId(java.lang.String)
   */
  public Set<AttributeDef> findAttributeDefsByAttributeDefNameId(
      String attributeDefNameId) {
    Set<AttributeDef> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDef " +
        "from AttributeAssign theAttributeAssign, AttributeDef theAttributeDef " +
        "where theAttributeAssign.attributeDefNameId = :theAttributeDefNameId " +
        "and theAttributeAssign.ownerAttributeDefId = theAttributeDef.id " +
        "and theAttributeAssign.enabledDb = 'T'")
        .setString("theAttributeDefNameId", attributeDefNameId)
        .listSet(AttributeDef.class);
    return attributeDefs;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findGroupAttributeDefNames(Collection, Collection, Collection, Collection, Collection, Boolean)
   */
  public Set<AttributeDefName> findGroupAttributeDefNames(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, Boolean enabled) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int groupIdsSize = GrouperUtil.length(groupIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && groupIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or groupId(s) and/or groupNames and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + groupIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " groupIdsSize " + groupIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct adn ";
    String countPrefix = "select count(distinct adn) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'group' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "aa.ownerGroupId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
  
    StringBuilder sql;
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }
    sql = sqlTables.append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (groupIdsSize > 0) {
      sql.append(" and aa.ownerGroupId in (");
      sql.append(HibUtils.convertToInClause(groupIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindGroupAttributeDefNames");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    //if -1, lets not check
    long size = -1;
    
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeDefName> results = size == 0 ? new LinkedHashSet<AttributeDefName>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeDefName.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = filterAttributeDefNames(grouperSession, results);
    
    //we should be down to the secure list
    return results;
      
  }

  /**
   * 
   * @param grouperSession
   * @param attibuteDefNames
   * @return the names which are ok to view
   */
  public Set<AttributeDefName> filterAttributeDefNames(GrouperSession grouperSession, Set<AttributeDefName> attibuteDefNames) {
    
    if (GrouperUtil.length(attibuteDefNames) == 0) {
      return attibuteDefNames;
    }
    
    Set<AttributeDef> attributeDefs = new HashSet<AttributeDef>();
    
    //this can probably be done more efficiently
    for (AttributeDefName attributeDefName : attibuteDefNames) {
      attributeDefs.add(attributeDefName.getAttributeDef());
    }

    attributeDefs = grouperSession.getAttributeDefResolver().postHqlFilterAttrDefs(
        attributeDefs, grouperSession.getSubject(),AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    Iterator<AttributeDefName> iterator = attibuteDefNames.iterator();
    
    while(iterator.hasNext()) {
      AttributeDefName attributeDefName = iterator.next();
      if (!attributeDefs.contains(attributeDefName.getAttributeDef())) {
        iterator.remove();
      }
    }
    
    return attibuteDefNames;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMemberAttributeDefNames(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean)
   */
  public Set<AttributeDefName> findMemberAttributeDefNames(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> memberIds, Collection<String> actions, Boolean enabled) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int memberIdsSize = GrouperUtil.length(memberIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && memberIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or memberId(s) [subjectIds or subjectIdentifiers] and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + memberIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " memberIdsSize " + memberIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct adn ";
    String countPrefix = "select count(distinct adn) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'member' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    StringBuilder sql;
    sql = sqlTables.append(" where ").append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (memberIdsSize > 0) {
      sql.append(" and aa.ownerMemberId in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberAttributeDefNames");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeDefName> results = size == 0 ? new LinkedHashSet<AttributeDefName>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeDefName.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = filterAttributeDefNames(grouperSession, results);
    
    //we should be down to the secure list
    return results;
      
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMembershipAttributeDefNames(Collection, Collection, Collection, Collection, Collection, Boolean)
   */
  public Set<AttributeDefName> findMembershipAttributeDefNames(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> membershipIds, Collection<String> actions, Boolean enabled) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int membershipIdsSize = GrouperUtil.length(membershipIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && membershipIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or membershipId(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + membershipIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " membershipIdsSize " + membershipIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct adn ";
    String countPrefix = "select count(distinct adn) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, ImmediateMembershipEntry ime ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    Field membersField = FieldFinder.find("members", true);
    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'imm_mem' and " +
    		"aa.ownerMembershipId = ime.immediateMembershipId and ime.fieldId = '" + membersField.getUuid() + "' " +
    				" and ime.enabledDb = 'T' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
  
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "ime.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);
  
    StringBuilder sql;
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }
    sql = sqlTables.append(sqlWhereClause);
  
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (membershipIdsSize > 0) {
      sql.append(" and aa.ownerMembershipId in (");
      sql.append(HibUtils.convertToInClause(membershipIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMembershipAttributeAttributeDefNames");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeDefName> results = size == 0 ? new LinkedHashSet<AttributeDefName>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeDefName.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = filterAttributeDefNames(grouperSession, results);
    
    //we should be down to the secure list
    return results;
      
  }

  /**
   * find attribute assigns by ids, as root (no security).  order by attribute type def name, so they are in order
   * @param attributeTypeDefNameId attribute def name of the type on the owner
   * @param queryOptions 
   * @return attributes grouped by the type assignment
   */
  public Map<AttributeAssign, Set<AttributeAssignValueContainer>> findByAttributeTypeDefNameId(String attributeTypeDefNameId, QueryOptions queryOptions) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //SELECT * 
    //FROM grouper_attribute_assign gaa_attr, grouper_attribute_assign gaa_type,
    //grouper_attribute_def_name gadn, grouper_attribute_assign_value gaav
    //WHERE gaa_attr.owner_attribute_assign_id = gaa_type.id
    //AND gaa_attr.attribute_def_name_id = gadn.id
    //AND gaav.attribute_assign_id = gaa_attr.id
    
    //lets do all this in one query
    StringBuilder sql = new StringBuilder("select distinct aa_type, aa_attr, adn, aav " +
    		" from AttributeAssign aa_type, AttributeAssign aa_attr, AttributeDefName adn, AttributeAssignValue aav " +
    		" where aa_attr.attributeDefNameId = adn.id " +
    		"   and aa_attr.ownerAttributeAssignId = aa_type.id " +
    		"   and aav.attributeAssignId = aa_attr.id " +
    		"   and aa_type.attributeDefNameId = :attributeTypeDefNameId " +
    		"   and aa_attr.enabledDb = 'T' " +
    		"   and aa_type.enabledDb = 'T' order by aa_type.id ");
    
    byHqlStatic.options(queryOptions)
      .setString("attributeTypeDefNameId", attributeTypeDefNameId)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByAttributeTypeDefNameId");

    Set<Object[]> resultsObjectArrays = byHqlStatic.createQuery(sql.toString()).listSet(Object[].class);
    
    Map<AttributeAssign, Set<AttributeAssignValueContainer>> result = new LinkedHashMap<AttributeAssign, Set<AttributeAssignValueContainer>>();

    String currentAttributeTypeAssignId = null;

    Set<AttributeAssignValueContainer> currentContainers = null;
    
    for (Object[] resultsObjectArray : resultsObjectArrays) {
      AttributeAssignValueContainer attributeAssignValueContainer = new AttributeAssignValueContainer();
      attributeAssignValueContainer.setAttributeTypeAssign((AttributeAssign)resultsObjectArray[0]);
      attributeAssignValueContainer.setAttributeValueAssign((AttributeAssign)resultsObjectArray[1]);
      attributeAssignValueContainer.setAttributeDefName((AttributeDefName)resultsObjectArray[2]);
      attributeAssignValueContainer.setAttributeAssignValue((AttributeAssignValue)resultsObjectArray[3]);
      
      //first one
      if (currentContainers == null) {
        currentContainers = new HashSet<AttributeAssignValueContainer>();
        result.put(attributeAssignValueContainer.getAttributeTypeAssign(), currentContainers);
      }
      
      //first one
      if (currentAttributeTypeAssignId == null) {
        currentAttributeTypeAssignId = attributeAssignValueContainer.getAttributeTypeAssign().getId();
      }
      
      //if on same batch
      if (StringUtils.equals(currentAttributeTypeAssignId, attributeAssignValueContainer.getAttributeTypeAssign().getId())) {
        //same batch, continue
        currentContainers.add(attributeAssignValueContainer);
        continue;
      }
      
      //if not on same batch
      currentAttributeTypeAssignId = attributeAssignValueContainer.getAttributeTypeAssign().getId();
      currentContainers = new HashSet<AttributeAssignValueContainer>();
      currentContainers.add(attributeAssignValueContainer);
      result.put(attributeAssignValueContainer.getAttributeTypeAssign(), currentContainers);

    }
    
    //lets take out invalid ones
    Iterator<AttributeAssign> iterator = result.keySet().iterator();
    OUTER: while (iterator.hasNext()) {
      
      currentContainers = result.get(iterator.next());
      for (AttributeAssignValueContainer attributeAssignValueContainer : currentContainers) {
        if (StringUtils.equals(RuleUtils.ruleValidName(), attributeAssignValueContainer.getAttributeDefName().getName())) {
          if (StringUtils.equals("T", attributeAssignValueContainer.getAttributeAssignValue().getValueString())) {
            //leave this in
            continue OUTER;
          }
        }
      }
      //invalid, take out
      iterator.remove();
    }
    
    return result;
  }

  /**
   * find attribute assigns by ids, as root (no security).  this is for one type assignment.
   * @param attributeTypeAssignId type assign id
   * @return attributes
   */
  public Set<AttributeAssignValueContainer> findByAssignTypeId(
      String attributeTypeAssignId) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //SELECT * 
    //FROM grouper_attribute_assign gaa_attr, grouper_attribute_assign gaa_type,
    //grouper_attribute_def_name gadn, grouper_attribute_assign_value gaav
    //WHERE gaa_attr.owner_attribute_assign_id = gaa_type.id
    //AND gaa_attr.attribute_def_name_id = gadn.id
    //AND gaav.attribute_assign_id = gaa_attr.id
    
    //lets do all this in one query
    StringBuilder sql = new StringBuilder("select distinct aa_type, aa_attr, adn, aav " +
        " from AttributeAssign aa_type, AttributeAssign aa_attr, AttributeDefName adn, AttributeAssignValue aav " +
        " where aa_attr.attributeDefNameId = adn.id " +
        "   and aa_attr.ownerAttributeAssignId = aa_type.id " +
        "   and aav.attributeAssignId = aa_attr.id " +
        "   and aa_attr.enabledDb = 'T' " +
        "   and aa_type.id = :attributeTypeAssignId " +
        "   and aa_type.enabledDb = 'T' ");
    
    byHqlStatic
      .setString("attributeTypeAssignId", attributeTypeAssignId)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByAttributeTypeDefNameIdAndAssignTypeId");

    Set<Object[]> resultsObjectArrays = byHqlStatic.createQuery(sql.toString()).listSet(Object[].class);
    
    Set<AttributeAssignValueContainer> currentContainers = new HashSet<AttributeAssignValueContainer>();
    
    for (Object[] resultsObjectArray : resultsObjectArrays) {
      AttributeAssignValueContainer attributeAssignValueContainer = new AttributeAssignValueContainer();
      attributeAssignValueContainer.setAttributeTypeAssign((AttributeAssign)resultsObjectArray[0]);
      attributeAssignValueContainer.setAttributeValueAssign((AttributeAssign)resultsObjectArray[1]);
      attributeAssignValueContainer.setAttributeDefName((AttributeDefName)resultsObjectArray[2]);
      attributeAssignValueContainer.setAttributeAssignValue((AttributeAssignValue)resultsObjectArray[3]);
      
      //same batch, continue
      currentContainers.add(attributeAssignValueContainer);

    }
    
    return currentContainers;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findStemAttributeDefNames(Collection, Collection, Collection, Collection, Collection, Boolean)
   */
  public Set<AttributeDefName> findStemAttributeDefNames(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> stemIds, Collection<String> actions, Boolean enabled) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int stemIdsSize = GrouperUtil.length(stemIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && stemIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or stemId(s) and/or stemName(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + stemIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " stemIdsSize " + stemIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct adn ";
    String countPrefix = "select count(distinct adn) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'stem' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "aa.ownerStemId", NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES);
  
    StringBuilder sql;
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }
    sql = sqlTables.append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (stemIdsSize > 0) {
      sql.append(" and aa.ownerStemId in (");
      sql.append(HibUtils.convertToInClause(stemIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindStemAttributeDefNames");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeDefName> results = size == 0 ? new LinkedHashSet<AttributeDefName>()
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeDefName.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = filterAttributeDefNames(grouperSession, results);
    
    //we should be down to the secure list
    return results;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAnyMembershipAttributeDefNames(Collection, Collection, Collection, Collection, Collection, Boolean)
   */
  public Set<AttributeDefName> findAnyMembershipAttributeDefNames(
      Collection<String> attributeAssignIds, Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<MultiKey> groupIdsAndMemberIds,
      Collection<String> actions, Boolean enabled) {
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int groupIdsAndMemberIdsSize = GrouperUtil.length(groupIdsAndMemberIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && groupIdsAndMemberIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or membershipId(s) [groupIds/Names subjectIds/Identifiers] and/or attributeDefNameIds");
    }
    
    //too many bind vars
    //note, each groupIdsAndMemberIds is a multikey with a groupId and memberId
    if (attributeAssignIdsSize + (groupIdsAndMemberIdsSize*2) + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " groupIdsAndMemberIdsSize " + groupIdsAndMemberIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct adn ";
    String countPrefix = "select count(distinct adn) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, MembershipEntry me ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    Field membersField = FieldFinder.find("members", true);
    
    StringBuilder sqlWhereClause = new StringBuilder(
        " aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'any_mem' and " +
        "aa.ownerGroupId = me.ownerGroupId and aa.ownerMemberId = me.memberUuid and me.fieldId = '" + membersField.getUuid() + "' " +
        		" and me.enabledDb = 'T' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
  
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "aa.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);
  
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }

    StringBuilder sql;
    sql = sqlTables.append(sqlWhereClause);
  
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (groupIdsAndMemberIdsSize > 0) {
      HibUtils.convertToMultiKeyInClause(groupIdsAndMemberIds, byHqlStatic, GrouperUtil.toSet("aa.ownerGroupId", "aa.ownerMemberId"), sql);
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAnyMembershipAttributeDefNames");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeDefName> results = size == 0 ? new LinkedHashSet<AttributeDefName>()
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeDefName.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = filterAttributeDefNames(grouperSession, results);
    
    //we should be down to the secure list
    return results;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefAttributeDefNames(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean)
   */
  public Set<AttributeDefName> findAttributeDefAttributeDefNames(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> attributeDefAssignToIds, Collection<String> actions, 
      Boolean enabled) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int attributeDefAssignToIdsSize = GrouperUtil.length(attributeDefAssignToIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && attributeDefAssignToIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or attributeDefAssignToId(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + attributeDefAssignToIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " attributeDefAssignToIdsSize " + attributeDefAssignToIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct adn ";
    String countPrefix = "select count(distinct adn) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'attr_def' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "aa.ownerAttributeDefId", AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES);
      
    StringBuilder sql = sqlTables.append(" where ").append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefAssignToIdsSize > 0) {
      sql.append(" and aa.ownerAttributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefAssignToIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAttributeDefAttributeDefNames");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      long size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeDefName> results = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeDefName.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = filterAttributeDefNames(grouperSession, results);
    
    //we should be down to the secure list
    return results;
      
  }
  
  /**
   * @see AttributeAssignDAO#findByAttributeDefNameAndValueString(String, String, QueryOptions)
   */
  public Set<AttributeAssign> findByAttributeDefNameAndValueString(
      String attributeDefNameId, String value, QueryOptions queryOptions) {
    
    try {
      Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic()
        .createQuery("select distinct theAttributeAssign from AttributeAssign as theAttributeAssign, " +
        		" AttributeAssignValue as theAttributeAssignValue where " +
            " theAttributeAssignValue.attributeAssignId = theAttributeAssign.id and " +
            " theAttributeAssignValue.valueString = :theValue and theAttributeAssign.enabledDb='T' " +
            " and theAttributeAssign.attributeDefNameId = :theAttributeDefNameId ")
        .options(queryOptions)
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByAttributeDefNameAndValueString")
        .setString("theValue", value)
        .setString("theAttributeDefNameId", attributeDefNameId)
        .listSet(AttributeAssign.class);
      
      //return result
      return attributeAssigns;
    } catch (GrouperDAOException e) {
      String error = "Problem find by value '" + value 
             + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    
  }

  /**
   * @see AttributeAssignDAO#findAttributeAssignments(AttributeAssignType, String, String, String, String, String, String, String, Boolean, boolean)
   */
  public Set<AttributeAssign> findAttributeAssignments(
      AttributeAssignType attributeAssignType, String attributeDefId,
      String attributeDefNameId, String ownerGroupId, String ownerStemId,
      String ownerMemberId, String ownerAttributeDefId,
      String ownerMembershipId,
      Boolean enabled, boolean includeAssignmentsOnAssignments) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (attributeAssignType == null) {
      throw new NullPointerException("attributeAssignType cannot be null");
    }
    
    StringBuilder sqlWhereClause = new StringBuilder(
        " aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = '" + attributeAssignType.name() + "' ");

    //if we are an immediate, then to filter on group/member we need the membership object
    if (attributeAssignType == AttributeAssignType.imm_mem) {
      if (!StringUtils.isBlank(ownerGroupId) || !StringUtils.isBlank(ownerMemberId)) {
        sqlTables.append(", ImmediateMembershipEntry ime ");
        sqlWhereClause.append(" and aa.ownerMembershipId = ime.immediateMembershipId ");
      }
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = false;
      
    if (attributeAssignType == AttributeAssignType.any_mem) {
      changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSessionSubject, byHqlStatic, 
          //sqlTables, "aa.ownerGroupId", AccessPrivilege.VIEW_PRIVILEGES);  why was this view??
          sqlTables, "aa.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);
    }
    
    if (attributeAssignType == AttributeAssignType.group) {
      changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSessionSubject, byHqlStatic, 
          sqlTables, "aa.ownerGroupId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
    }
  
    if (attributeAssignType == AttributeAssignType.stem) {
      changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(
          grouperSessionSubject, byHqlStatic, 
         // sqlTables, "aa.ownerStemId", NamingPrivilege.CREATE_PRIVILEGES);   why was this create??
          sqlTables, "aa.ownerStemId", NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES);
    }
  
    if (attributeAssignType == AttributeAssignType.imm_mem) {
      changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSessionSubject, byHqlStatic, 
          //sqlTables, "ime.ownerGroupId", AccessPrivilege.VIEW_PRIVILEGES);   why was this view??
          sqlTables, "ime.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);
    }
    if (attributeAssignType == AttributeAssignType.attr_def) {
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
          grouperSessionSubject, byHqlStatic, 
          //sqlTables, sqlWhereClause, "aa.ownerAttributeDefId", AttributeDefPrivilege.MANAGE_PRIVILEGES); why was this manage??
          sqlTables, sqlWhereClause, "aa.ownerAttributeDefId", AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES);
    }
  
    StringBuilder sql;
    if (!changedQuery) {
      sqlTables.append(" where ");
    } else {
      sqlTables.append(" and ");
    }
    sql = sqlTables.append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (!StringUtils.isBlank(ownerGroupId)) {
      if (attributeAssignType == AttributeAssignType.group || attributeAssignType == AttributeAssignType.any_mem) {
        sql.append(" and aa.ownerGroupId = :theOwnerGroupId ");
        byHqlStatic.setString("theOwnerGroupId", ownerGroupId);
      } else if (attributeAssignType == AttributeAssignType.imm_mem) {
        sql.append(" and ime.ownerGroupId = :theOwnerGroupId ");
        byHqlStatic.setString("theOwnerGroupId", ownerGroupId);
      } else {
        throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
      }
    }
    if (!StringUtils.isBlank(ownerStemId)) {
      sql.append(" and aa.ownerStemId = :theOwnerStemId ");
      byHqlStatic.setString("theOwnerStemId", ownerStemId);
    }
    if (!StringUtils.isBlank(ownerMemberId)) {
      if (attributeAssignType == AttributeAssignType.group || attributeAssignType == AttributeAssignType.any_mem) {
        sql.append(" and aa.ownerMemberId = :theOwnerMemberId ");
        byHqlStatic.setString("theOwnerMemberId", ownerMemberId);
      } else if (attributeAssignType == AttributeAssignType.imm_mem) {
        sql.append(" and ime.memberUuid = :theOwnerMemberId ");
        byHqlStatic.setString("theOwnerMemberId", ownerMemberId);
      } else {
        throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
      }
    }
    if (!StringUtils.isBlank(attributeDefId)) {
      sql.append(" and adn.attributeDefId = :theAttributeDefId ");
      byHqlStatic.setString("theAttributeDefId", attributeDefId);
    }
    if (!StringUtils.isBlank(ownerMembershipId)) {
      sql.append(" and ime.immediateMembershipId = :theImmediateMembershipId ");
      byHqlStatic.setString("theImmediateMembershipId", ownerMembershipId);
    }
    if (!StringUtils.isBlank(attributeDefNameId)) {
      sql.append(" and adn.id = :theAttributeDefNameId ");
      byHqlStatic.setString("theAttributeDefNameId", attributeDefNameId);
    }
    if (!StringUtils.isBlank(ownerAttributeDefId)) {
      sql.append(" and aa.ownerAttributeDefId = :theOwnerAttributeDefId ");
      byHqlStatic.setString("theOwnerAttributeDefId", ownerAttributeDefId);
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    //if -1, lets not check
    long size = -1;
    
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = size == 0 ? new LinkedHashSet<AttributeAssign>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    
    //we should be down to the secure list
    return results;

    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAssignmentsOnAssignmentsAttributeDefNames(java.util.Collection, edu.internet2.middleware.grouper.attr.assign.AttributeAssignType, java.lang.Boolean)
   */
  public Set<AttributeDefName> findAssignmentsOnAssignmentsAttributeDefNames(
      Collection<AttributeAssign> attributeAssigns,
      AttributeAssignType attributeAssignType, Boolean enabled) {
    
    int attributeAssignsSize = GrouperUtil.length(attributeAssigns);
  
    Set<AttributeDefName> results = new LinkedHashSet<AttributeDefName>();
    
    if (attributeAssignsSize == 0) {
      return results;
    }
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignsSize, 100);
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    List<AttributeAssign> attributeAssignsList = GrouperUtil.listFromCollection(attributeAssigns);
  
    for (int i=0;i<numberOfBatches; i++) {
      
      List<AttributeAssign> currentBatch = GrouperUtil.batchList(attributeAssignsList, 100, i);
      
      int currentBatchSize = GrouperUtil.length(currentBatch);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
      String selectPrefix = "select distinct adn ";
      
      StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
      
      StringBuilder sqlWhereClause = new StringBuilder(
          " aa.attributeDefNameId = adn.id ");
      
      if (attributeAssignType != null) {
        sqlWhereClause.append(" and aa.attributeAssignTypeDb = '" + attributeAssignType.name() + "' ");
      }
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      
      Subject grouperSessionSubject = grouperSession.getSubject();
      
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
      
      StringBuilder sql;
      sql = sqlTables.append(" where ").append(sqlWhereClause);
      
      if (enabled != null && enabled) {
        sql.append(" and aa.enabledDb = 'T' ");
      }
      if (enabled != null && !enabled) {
        sql.append(" and aa.enabledDb = 'F' ");
      }
      if (currentBatchSize > 0) {
        
        //convert to a list of ids
        Set<String> ids = new LinkedHashSet<String>();
        for (AttributeAssign attributeAssign : currentBatch) {
          ids.add(attributeAssign.getId());
        }
        
        sql.append(" and aa.ownerAttributeAssignId in (");
        sql.append(HibUtils.convertToInClause(ids, byHqlStatic));
        sql.append(") ");
      }
      byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAssignmentsOnAssignmentsAttributeDefNames");
  
      //if -1, lets not check
      Set<AttributeDefName> tempResults = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeDefName.class);
  
      //nothing to filter
      if (GrouperUtil.length(tempResults) > 0) {
        //if the hql didnt filter, we need to do that here
        tempResults = filterAttributeDefNames(grouperSession, tempResults);
      }
      
      results.addAll(tempResults);
      
      if (maxAssignments >= 0) {
  
        //see if too many
        if (results.size() > maxAssignments) {
          throw new RuntimeException("Too many results: " + results.size());
        }
        
      }
    }
    
    //we should be down to the secure list
    return results;
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findStemAttributeAssignments(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, boolean, AttributeDefType, AttributeDefValueType, Object)
   */
  public Set<AttributeAssign> findStemAttributeAssignments(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> stemIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments,
      AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType, Object theValue) {
    return findStemAttributeAssignmentsHelper(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        stemIds, actions, enabled, includeAssignmentsOnAssignments, 
        attributeDefType, attributeDefValueType, theValue, null);

  }

  /**
   * 
   * @param attributeAssignIds
   * @param attributeDefIds
   * @param attributeDefNameIds
   * @param stemIds
   * @param actions
   * @param enabled
   * @param includeAssignmentsOnAssignments
   * @param attributeDefType
   * @param attributeDefValueType
   * @param theValue
   * @return
   */
  private Set<AttributeAssign> findStemAttributeAssignmentsHelper(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> stemIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments,
      AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType, Object theValue, Boolean attributeCheckReadOnAttributeDef) {

    attributeCheckReadOnAttributeDef = GrouperUtil.defaultIfNull(attributeCheckReadOnAttributeDef, true);
    
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int stemIdsSize = GrouperUtil.length(stemIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    
    if (attributeAssignIdsSize == 0 && stemIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) and/or stemId(s) and/or stemName(s) and/or attributeDefNameIds");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + stemIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize > 100) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " stemIdsSize " + stemIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize + " or actionsSize " + actionsSize );
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn ");
    
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }
    
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id ");
    
    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'stem' ");
    
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
    
    boolean changedQuery = false;
    
    if (attributeCheckReadOnAttributeDef) {
      changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "aa.ownerStemId", NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES);
    }
    
    StringBuilder sql;
    
    if (changedQuery) {
      sqlTables.append(" and ");
    } else {
      sqlTables.append(" where ");
    }
    
    sql = sqlTables.append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }

    if (attributeDefType != null) {
      sql.append(" and adn.attributeDefId = ad.id and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }

    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (stemIdsSize > 0) {
      sql.append(" and aa.ownerStemId in (");
      sql.append(HibUtils.convertToInClause(stemIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindStemAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = size == 0 ? new LinkedHashSet<AttributeAssign>()
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    if (attributeCheckReadOnAttributeDef) {
      results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    }
    
    //if looking for assignments on assignments, do that now
    if (includeAssignmentsOnAssignments) {
      Set<AttributeAssign> assignmentsOnAssignments = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAssignmentsOnAssignments(results, AttributeAssignType.stem_asgn, enabled, attributeCheckReadOnAttributeDef);
      results.addAll(assignmentsOnAssignments);
    }
  
    //we should be down to the secure list
    return results;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAssignmentsFromAssignments(java.util.Collection, edu.internet2.middleware.grouper.attr.assign.AttributeAssignType, java.lang.Boolean)
   */
  public Set<AttributeAssign> findAssignmentsFromAssignments(
      Collection<AttributeAssign> attributeAssigns,
      AttributeAssignType attributeAssignType, Boolean enabled, boolean useCache) {

    Set<String> attributeAssignIds = new HashSet<String>();

    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {
      attributeAssignIds.add(attributeAssign.getOwnerAttributeAssignId());
    }

    return findByIds(attributeAssignIds, enabled, useCache);
  }

  /**
   * @see AttributeAssignDAO#findAssignmentsFromAssignmentsByIds(Collection, AttributeAssignType, AttributeDefType, Boolean)
   */
  public Set<AttributeAssign> findAssignmentsFromAssignmentsByIds(Collection<String> attributeAssignIds, 
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType, Boolean enabled) {
  
    int attributeAssignsSize = GrouperUtil.length(attributeAssignIds);
  
    Set<AttributeAssign> results = new LinkedHashSet<AttributeAssign>();
    
    if (attributeAssignsSize == 0) {
      return results;
    }
    
    //remove dupes
    Set<String> attributeAssignIdsSet = attributeAssignIds instanceof Set ? (Set)attributeAssignIds : new LinkedHashSet<String>(attributeAssignIds);
    
    //get in ordered list
    List<String> attributeAssignIdsList = new ArrayList<String>(attributeAssignIdsSet);
    
    attributeAssignsSize = GrouperUtil.length(attributeAssignIdsList);
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignsSize, 100);
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
  
    for (int i=0;i<numberOfBatches; i++) {
      
      List<String> currentBatch = GrouperUtil.batchList(attributeAssignIdsList, 100, i);
      
      int currentBatchSize = GrouperUtil.length(currentBatch);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
      String selectPrefix = "select distinct aa ";
      
      StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeDef ad ");
      
      StringBuilder sqlWhereClause = new StringBuilder(
          " aa.attributeDefNameId = adn.id and adn.attributeDefId = ad.id ");
      
      if (attributeAssignType != null) {
        sqlWhereClause.append(" and aa.attributeAssignTypeDb = '" + attributeAssignType.name() + "' ");
      }
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      
      Subject grouperSessionSubject = grouperSession.getSubject();
      
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
      
      StringBuilder sql;
      sql = sqlTables.append(" where ").append(sqlWhereClause);
      
      if (attributeDefType != null) {
        sql.append(" and ad.attributeDefTypeDb = :theAttributeDefType ");
        byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
      }
      if (enabled != null && enabled) {
        sql.append(" and aa.enabledDb = 'T' ");
      }
      if (enabled != null && !enabled) {
        sql.append(" and aa.enabledDb = 'F' ");
      }
      if (currentBatchSize > 0) {
        
        sql.append(" and aa.ownerAttributeAssignId in (");
        sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
        sql.append(") ");
      }
      
      byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAssignmentsOnAssignments");
  
      //if -1, lets not check
      Set<AttributeAssign> tempResults = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);
  
      //nothing to filter
      if (GrouperUtil.length(tempResults) > 0) {
        //if the hql didnt filter, we need to do that here
        tempResults = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, tempResults);
      }
      
      results.addAll(tempResults);
      
      if (maxAssignments >= 0) {
  
        //see if too many
        if (results.size() > maxAssignments) {
          throw new RuntimeException("Too many results: " + results.size());
        }
        
      }
    }
    
    
    //we should be down to the secure list
    return results;
  
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMemberAttributeAssignmentsOnAssignments(Collection, Collection, Collection, Collection, Collection, Boolean, AttributeDefType, AttributeDefValueType, Object, boolean, Collection, Collection, Collection, Collection, boolean)
   */
  public Set<AttributeAssign> findMemberAttributeAssignmentsOnAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> memberIds, Collection<String> actions, 
      Boolean enabled, AttributeDefType attributeDefType, AttributeDefValueType attributeDefValueType,
      Object theValue, boolean includeAssignmentsFromAssignments,
      Collection<String> ownerAttributeAssignIds,
      Collection<String> ownerAttributeDefIds, 
      Collection<String> ownerAttributeDefNameIds,
      Collection<String> ownerActions, boolean useCache) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int memberIdsSize = GrouperUtil.length(memberIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    int ownerAttributeAssignIdsSize = GrouperUtil.length(ownerAttributeAssignIds);
    int ownerAttributeDefIdsSize = GrouperUtil.length(ownerAttributeDefIds);
    int ownerAttributeDefNameIdsSize = GrouperUtil.length(ownerAttributeDefNameIds);
    int ownerActionsSize = GrouperUtil.length(ownerActions);
    
    if (attributeAssignIdsSize == 0 && memberIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0
        && ownerAttributeAssignIdsSize == 0 && ownerAttributeDefIdsSize == 0 && ownerAttributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) " +
      		"and/or memberId(s) and/or stemName(s) and/or attributeDefNameIds and/or ownerAttributeAssignIds" +
      		" and/or ownerAttributeDefIdsSize and/or ownerAttributeDefNameIdsSize");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + memberIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize 
        + ownerAttributeAssignIdsSize + ownerAttributeDefIdsSize + ownerAttributeDefNameIdsSize + ownerActionsSize > 100 ) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " memberIdsSize " + memberIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize 
          + " or actionsSize " + actionsSize + " or ownerAttributeAssignIdsSize " + ownerAttributeAssignIdsSize
          + " or ownerAttributeDefIdsSize " + ownerAttributeDefIdsSize 
          + " ownerAttributeDefNameIdsSize " + ownerAttributeDefNameIdsSize
          + " or ownerActionsSize " + ownerActionsSize);
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
  
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeAssign ownerAa, AttributeDefName ownerAdn ");
  
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
    
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }

    if (ownerActionsSize > 0) {
      sqlTables.append(", AttributeAssignAction ownerAaa ");
    }
  
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and ownerAa.attributeDefNameId = ownerAdn.id and aa.ownerAttributeAssignId = ownerAa.id ");
  
    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'mem_asgn' ");
  
    sqlWhereClause.append(" and ownerAa.attributeAssignTypeDb = 'member' ");
  
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    //need to check the assignment on assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    //need to check the assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ownerAdn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    StringBuilder sql;
    sql = sqlTables.append(" where ").append(sqlWhereClause);
  
    attributeAssignAssignQueryStart(attributeAssignIds, attributeDefIds,
        attributeDefNameIds, actions, enabled, attributeDefType, ownerAttributeAssignIds,
        ownerAttributeDefIds, ownerAttributeDefNameIds, ownerActions,
        attributeAssignIdsSize, actionsSize, attributeDefIdsSize,
        attributeDefNameIdsSize, ownerAttributeAssignIdsSize, ownerAttributeDefIdsSize,
        ownerAttributeDefNameIdsSize, ownerActionsSize, byHqlStatic, sql);
    if (memberIdsSize > 0) {
      sql.append(" and ownerAa.ownerMemberId in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(useCache)
      .setCacheRegion(KLASS + ".FindMemberAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    Set<AttributeAssign> results = attributeAssignmentAssignmentRunQuery(
        includeAssignmentsFromAssignments, useCache, byHqlStatic, selectPrefix,
        countPrefix, grouperSession, grouperSessionSubject, sql, maxAssignments);
  
    //we should be down to the secure list
    return results;
      
  }

  /**
   * run query
   * @param includeAssignmentsFromAssignments
   * @param useCache
   * @param byHqlStatic
   * @param selectPrefix
   * @param countPrefix
   * @param grouperSession
   * @param grouperSessionSubject
   * @param sql
   * @param maxAssignments
   * @return results
   */
  private Set<AttributeAssign> attributeAssignmentAssignmentRunQuery(
      boolean includeAssignmentsFromAssignments, boolean useCache,
      ByHqlStatic byHqlStatic, String selectPrefix, String countPrefix,
      GrouperSession grouperSession, Subject grouperSessionSubject, StringBuilder sql,
      int maxAssignments) {
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssign> results = size == 0 ? new LinkedHashSet<AttributeAssign>()
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssign.class);
  
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return results;
    }
    
    //if the hql didnt filter, we need to do that here
    results = grouperSession.getAttributeDefResolver().postHqlFilterAttributeAssigns(grouperSessionSubject, results);
    
    //if looking for assignments on assignments, do that now
    if (includeAssignmentsFromAssignments) {
      Set<AttributeAssign> assignmentsFromAssignments = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findAssignmentsFromAssignments(results, AttributeAssignType.member, null, useCache);
      results.addAll(assignmentsFromAssignments);
    }
    return results;
  }

  private void attributeAssignAssignQueryStart(Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, Collection<String> attributeDefNameIds,
      Collection<String> actions, Boolean enabled, AttributeDefType attributeDefType,
      Collection<String> ownerAttributeAssignIds,
      Collection<String> ownerAttributeDefIds,
      Collection<String> ownerAttributeDefNameIds, Collection<String> ownerActions,
      int attributeAssignIdsSize, int actionsSize, int attributeDefIdsSize,
      int attributeDefNameIdsSize, int ownerAttributeAssignIdsSize,
      int ownerAttributeDefIdsSize, int ownerAttributeDefNameIdsSize,
      int ownerActionsSize, ByHqlStatic byHqlStatic, StringBuilder sql) {
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
      sql.append(" and ownerAa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      //CH dont need the owner assignment to not be enabled... just one or the other
      sql.append(" and (aa.enabledDb = 'F' or ownerAa.enabledDb = 'F') ");
    }
    if (attributeAssignIdsSize > 0) {
      sql.append(" and aa.id in (");
      sql.append(HibUtils.convertToInClause(attributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (ownerAttributeAssignIdsSize > 0) {
      sql.append(" and ownerAa.id in (");
      sql.append(HibUtils.convertToInClause(ownerAttributeAssignIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefType != null) {
      sql.append(" and adn.attributeDefId = ad.id and ad.attributeDefTypeDb = :theAttributeDefType ");
      byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
    }

    if (actionsSize > 0) {
      sql.append(" and adn.attributeDefId = aaa.attributeDefId and aaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(actions, byHqlStatic));
      sql.append(") ");
    }
    if (ownerActionsSize > 0) {
      sql.append(" and ownerAdn.attributeDefId = ownerAaa.attributeDefId and ownerAaa.nameDb in (");
      sql.append(HibUtils.convertToInClause(ownerActions, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefIdsSize > 0) {
      sql.append(" and adn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (ownerAttributeDefIdsSize > 0) {
      sql.append(" and ownerAdn.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(ownerAttributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    if (attributeDefNameIdsSize > 0) {
      sql.append(" and adn.id in (");
      sql.append(HibUtils.convertToInClause(attributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
    if (ownerAttributeDefNameIdsSize > 0) {
      sql.append(" and ownerAdn.id in (");
      sql.append(HibUtils.convertToInClause(ownerAttributeDefNameIds, byHqlStatic));
      sql.append(") ");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findGroupAttributeAssignmentsOnAssignments(Collection, Collection, Collection, Collection, Collection, Boolean, AttributeDefType, AttributeDefValueType, Object, boolean, Collection, Collection, Collection, Collection, boolean)
   */
  public Set<AttributeAssign> findGroupAttributeAssignmentsOnAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, 
      Boolean enabled, 
      AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType,
      Object theValue, boolean includeAssignmentsFromAssignments,
      Collection<String> ownerAttributeAssignIds,
      Collection<String> ownerAttributeDefIds, 
      Collection<String> ownerAttributeDefNameIds,
      Collection<String> ownerActions, boolean useCache) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int groupIdsSize = GrouperUtil.length(groupIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    int ownerAttributeAssignIdsSize = GrouperUtil.length(ownerAttributeAssignIds);
    int ownerAttributeDefIdsSize = GrouperUtil.length(ownerAttributeDefIds);
    int ownerAttributeDefNameIdsSize = GrouperUtil.length(ownerAttributeDefNameIds);
    int ownerActionsSize = GrouperUtil.length(ownerActions);
    
    if (attributeAssignIdsSize == 0 && groupIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0
        && ownerAttributeAssignIdsSize == 0 && ownerAttributeDefIdsSize == 0 && ownerAttributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) " +
      		"and/or groupId(s) and/or groupName(s) and/or attributeDefNameIds and/or ownerAttributeAssignIds" +
      		" and/or ownerAttributeDefIdsSize and/or ownerAttributeDefNameIdsSize");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + groupIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize 
        + ownerAttributeAssignIdsSize + ownerAttributeDefIdsSize + ownerAttributeDefNameIdsSize + ownerActionsSize > 100 ) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " groupIdsSize " + groupIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize 
          + " or actionsSize " + actionsSize + " or ownerAttributeAssignIdsSize " + ownerAttributeAssignIdsSize
          + " or ownerAttributeDefIdsSize " + ownerAttributeDefIdsSize 
          + " ownerAttributeDefNameIdsSize " + ownerAttributeDefNameIdsSize
          + " or ownerActionsSize " + ownerActionsSize);
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
  
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeAssign ownerAa, AttributeDefName ownerAdn ");
  
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }
  
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
  
    if (ownerActionsSize > 0) {
      sqlTables.append(", AttributeAssignAction ownerAaa ");
    }
  
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and ownerAa.attributeDefNameId = ownerAdn.id and aa.ownerAttributeAssignId = ownerAa.id ");
  
    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'group_asgn' ");
  
    sqlWhereClause.append(" and ownerAa.attributeAssignTypeDb = 'group' ");
  
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    //need to check the assignment on assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    //need to check the assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ownerAdn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "ownerAa.ownerGroupId", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
    
    StringBuilder sql;
    sql = sqlTables.append(changedQuery ? " and " : " where ").append(sqlWhereClause);
  
    attributeAssignAssignQueryStart(attributeAssignIds, attributeDefIds,
        attributeDefNameIds, actions, enabled, attributeDefType, ownerAttributeAssignIds,
        ownerAttributeDefIds, ownerAttributeDefNameIds, ownerActions,
        attributeAssignIdsSize, actionsSize, attributeDefIdsSize,
        attributeDefNameIdsSize, ownerAttributeAssignIdsSize, ownerAttributeDefIdsSize,
        ownerAttributeDefNameIdsSize, ownerActionsSize, byHqlStatic, sql);
  
    if (groupIdsSize > 0) {
      sql.append(" and ownerAa.ownerGroupId in (");
      sql.append(HibUtils.convertToInClause(groupIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(useCache)
      .setCacheRegion(KLASS + ".FindGroupAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    Set<AttributeAssign> results = attributeAssignmentAssignmentRunQuery(
        includeAssignmentsFromAssignments, useCache, byHqlStatic, selectPrefix,
        countPrefix, grouperSession, grouperSessionSubject, sql, maxAssignments);
  
    //we should be down to the secure list
    return results;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefAttributeAssignmentsOnAssignments(Collection, Collection, Collection, Collection, Collection, Boolean, AttributeDefType, AttributeDefValueType, Object, boolean, Collection, Collection, Collection, Collection, boolean)
   */
  public Set<AttributeAssign> findAttributeDefAttributeAssignmentsOnAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> mainOwnerAttributeDefIds, Collection<String> actions, 
      Boolean enabled, 
      AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType,
      Object theValue, boolean includeAssignmentsFromAssignments,
      Collection<String> ownerAttributeAssignIds,
      Collection<String> ownerAttributeDefIds, 
      Collection<String> ownerAttributeDefNameIds,
      Collection<String> ownerActions, boolean useCache) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int mainOwnerAttributeDefIdsSize = GrouperUtil.length(mainOwnerAttributeDefIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    int ownerAttributeAssignIdsSize = GrouperUtil.length(ownerAttributeAssignIds);
    int ownerAttributeDefIdsSize = GrouperUtil.length(ownerAttributeDefIds);
    int ownerAttributeDefNameIdsSize = GrouperUtil.length(ownerAttributeDefNameIds);
    int ownerActionsSize = GrouperUtil.length(ownerActions);
    
    if (attributeAssignIdsSize == 0 && mainOwnerAttributeDefIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0
        && ownerAttributeAssignIdsSize == 0 && ownerAttributeDefIdsSize == 0 && ownerAttributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) " +
      		"and/or mainOwnerAttributeDefIds(s) and/or attributeDefNameIds and/or ownerAttributeAssignIds" +
      		" and/or ownerAttributeDefIdsSize and/or ownerAttributeDefNameIdsSize");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + mainOwnerAttributeDefIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize 
        + ownerAttributeAssignIdsSize + ownerAttributeDefIdsSize + ownerAttributeDefNameIdsSize + ownerActionsSize > 100 ) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " mainOwnerAttributeDefIds " + mainOwnerAttributeDefIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize 
          + " or actionsSize " + actionsSize + " or ownerAttributeAssignIdsSize " + ownerAttributeAssignIdsSize
          + " or ownerAttributeDefIdsSize " + ownerAttributeDefIdsSize 
          + " ownerAttributeDefNameIdsSize " + ownerAttributeDefNameIdsSize
          + " or ownerActionsSize " + ownerActionsSize);
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
  
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeAssign ownerAa, AttributeDefName ownerAdn ");
  
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }
  
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
  
    if (ownerActionsSize > 0) {
      sqlTables.append(", AttributeAssignAction ownerAaa ");
    }
  
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and ownerAa.attributeDefNameId = ownerAdn.id and aa.ownerAttributeAssignId = ownerAa.id ");
  
    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'attr_def_asgn' ");
  
    sqlWhereClause.append(" and ownerAa.attributeAssignTypeDb = 'attr_def' ");
  
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    //need to check the assignment on assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    //need to check the assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ownerAdn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ownerAa.ownerAttributeDefId", AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES);

    StringBuilder sql;
    sql = sqlTables.append(" where ").append(sqlWhereClause);
  
    attributeAssignAssignQueryStart(attributeAssignIds, attributeDefIds,
        attributeDefNameIds, actions, enabled, attributeDefType, ownerAttributeAssignIds,
        ownerAttributeDefIds, ownerAttributeDefNameIds, ownerActions,
        attributeAssignIdsSize, actionsSize, attributeDefIdsSize,
        attributeDefNameIdsSize, ownerAttributeAssignIdsSize, ownerAttributeDefIdsSize,
        ownerAttributeDefNameIdsSize, ownerActionsSize, byHqlStatic, sql);
  
    if (mainOwnerAttributeDefIdsSize > 0) {
      sql.append(" and ownerAa.ownerAttributeDefId in (");
      sql.append(HibUtils.convertToInClause(mainOwnerAttributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(useCache)
      .setCacheRegion(KLASS + ".FindAttributeDefAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    Set<AttributeAssign> results = attributeAssignmentAssignmentRunQuery(
        includeAssignmentsFromAssignments, useCache, byHqlStatic, selectPrefix,
        countPrefix, grouperSession, grouperSessionSubject, sql, maxAssignments);
  
    //we should be down to the secure list
    return results;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findMembershipAttributeAssignmentsOnAssignments(Collection, Collection, Collection, Collection, Collection, Boolean, AttributeDefType, AttributeDefValueType, Object, boolean, Collection, Collection, Collection, Collection, boolean)
   */
  public Set<AttributeAssign> findMembershipAttributeAssignmentsOnAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> membershipIds, Collection<String> actions, 
      Boolean enabled, 
      AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType,
      Object theValue, boolean includeAssignmentsFromAssignments,
      Collection<String> ownerAttributeAssignIds,
      Collection<String> ownerAttributeDefIds, 
      Collection<String> ownerAttributeDefNameIds,
      Collection<String> ownerActions, boolean useCache) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int membershipIdsSize = GrouperUtil.length(membershipIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    int ownerAttributeAssignIdsSize = GrouperUtil.length(ownerAttributeAssignIds);
    int ownerAttributeDefIdsSize = GrouperUtil.length(ownerAttributeDefIds);
    int ownerAttributeDefNameIdsSize = GrouperUtil.length(ownerAttributeDefNameIds);
    int ownerActionsSize = GrouperUtil.length(ownerActions);
    
    if (attributeAssignIdsSize == 0 && membershipIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0
        && ownerAttributeAssignIdsSize == 0 && ownerAttributeDefIdsSize == 0 && ownerAttributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) " +
      		"and/or membershipId(s) and/or attributeDefNameIds and/or ownerAttributeAssignIds" +
      		" and/or ownerAttributeDefIdsSize and/or ownerAttributeDefNameIdsSize");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + membershipIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize 
        + ownerAttributeAssignIdsSize + ownerAttributeDefIdsSize + ownerAttributeDefNameIdsSize + ownerActionsSize > 100 ) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " membershipIdsSize " + membershipIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize 
          + " or actionsSize " + actionsSize + " or ownerAttributeAssignIdsSize " + ownerAttributeAssignIdsSize
          + " or ownerAttributeDefIdsSize " + ownerAttributeDefIdsSize 
          + " ownerAttributeDefNameIdsSize " + ownerAttributeDefNameIdsSize
          + " or ownerActionsSize " + ownerActionsSize);
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
  
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeAssign ownerAa, AttributeDefName ownerAdn, ImmediateMembershipEntry ime ");
  
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }
  
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
  
    if (ownerActionsSize > 0) {
      sqlTables.append(", AttributeAssignAction ownerAaa ");
    }
  
    Field membersField = FieldFinder.find("members", true);

    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and ownerAa.attributeDefNameId = ownerAdn.id and aa.ownerAttributeAssignId = ownerAa.id and "
        + " ownerAa.ownerMembershipId = ime.immediateMembershipId and ime.fieldId = '" + membersField.getUuid() + "' " +
        " and ime.enabledDb = 'T' ");
  
    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'imm_mem_asgn' ");
  
    sqlWhereClause.append(" and ownerAa.attributeAssignTypeDb = 'imm_mem' ");
  
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    //need to check the assignment on assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    //need to check the assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ownerAdn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "ime.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);
    
    StringBuilder sql;
    sql = sqlTables.append(changedQuery ? " and " : " where ").append(sqlWhereClause);
  
    attributeAssignAssignQueryStart(attributeAssignIds, attributeDefIds,
        attributeDefNameIds, actions, enabled, attributeDefType, ownerAttributeAssignIds,
        ownerAttributeDefIds, ownerAttributeDefNameIds, ownerActions,
        attributeAssignIdsSize, actionsSize, attributeDefIdsSize,
        attributeDefNameIdsSize, ownerAttributeAssignIdsSize, ownerAttributeDefIdsSize,
        ownerAttributeDefNameIdsSize, ownerActionsSize, byHqlStatic, sql);
  
    if (membershipIdsSize > 0) {
      sql.append(" and ownerAa.ownerMembershipId in (");
      sql.append(HibUtils.convertToInClause(membershipIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(useCache)
      .setCacheRegion(KLASS + ".FindMembershipAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    Set<AttributeAssign> results = attributeAssignmentAssignmentRunQuery(
        includeAssignmentsFromAssignments, useCache, byHqlStatic, selectPrefix,
        countPrefix, grouperSession, grouperSessionSubject, sql, maxAssignments);
  
    //we should be down to the secure list
    return results;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAnyMembershipAttributeAssignmentsOnAssignments(Collection, Collection, Collection, Collection, Collection, Boolean, AttributeDefType, AttributeDefValueType, Object, boolean, Collection, Collection, Collection, Collection, boolean)
   */
  public Set<AttributeAssign> findAnyMembershipAttributeAssignmentsOnAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<MultiKey> groupIdsAndMemberIds, Collection<String> actions, 
      Boolean enabled, 
      AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType,
      Object theValue, boolean includeAssignmentsFromAssignments,
      Collection<String> ownerAttributeAssignIds,
      Collection<String> ownerAttributeDefIds, 
      Collection<String> ownerAttributeDefNameIds,
      Collection<String> ownerActions, boolean useCache) {
      
    int attributeAssignIdsSize = GrouperUtil.length(attributeAssignIds);
    int groupIdsAndMemberIdsSize = GrouperUtil.length(groupIdsAndMemberIds);
    int actionsSize = GrouperUtil.length(actions);
    int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
    int attributeDefNameIdsSize = GrouperUtil.length(attributeDefNameIds);
    int ownerAttributeAssignIdsSize = GrouperUtil.length(ownerAttributeAssignIds);
    int ownerAttributeDefIdsSize = GrouperUtil.length(ownerAttributeDefIds);
    int ownerAttributeDefNameIdsSize = GrouperUtil.length(ownerAttributeDefNameIds);
    int ownerActionsSize = GrouperUtil.length(ownerActions);
    
    if (attributeAssignIdsSize == 0 && groupIdsAndMemberIdsSize == 0 && attributeDefIdsSize == 0 && attributeDefNameIdsSize == 0
        && ownerAttributeAssignIdsSize == 0 && ownerAttributeDefIdsSize == 0 && ownerAttributeDefNameIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in attributeAssignIds and/or attributeDefId(s) " +
      		"and/or groupId(s)/memberId(s) and/or attributeDefNameIds and/or ownerAttributeAssignIds" +
      		" and/or ownerAttributeDefIdsSize and/or ownerAttributeDefNameIdsSize");
    }
    
    //too many bind vars
    if (attributeAssignIdsSize + groupIdsAndMemberIdsSize + attributeDefIdsSize + attributeDefNameIdsSize + actionsSize 
        + ownerAttributeAssignIdsSize + ownerAttributeDefIdsSize + ownerAttributeDefNameIdsSize + ownerActionsSize > 100 ) {
      throw new RuntimeException("Too many attributeAssignIdsSize " + attributeAssignIdsSize 
          + " groupIdsAndMemberIdsSize " + groupIdsAndMemberIdsSize + " or attributeDefIdsSize " 
          + attributeDefIdsSize + " or attributeDefNameIds " + attributeDefNameIdsSize 
          + " or actionsSize " + actionsSize + " or ownerAttributeAssignIdsSize " + ownerAttributeAssignIdsSize
          + " or ownerAttributeDefIdsSize " + ownerAttributeDefIdsSize 
          + " ownerAttributeDefNameIdsSize " + ownerAttributeDefNameIdsSize
          + " or ownerActionsSize " + ownerActionsSize);
    }
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aa ";
    String countPrefix = "select count(distinct aa) ";
  
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeAssign ownerAa, AttributeDefName ownerAdn, MembershipEntry me ");
  
    if (attributeDefType != null) {
      sqlTables.append(", AttributeDef ad ");
    }
  
    if (actionsSize > 0) {
      sqlTables.append(", AttributeAssignAction aaa ");
    }
  
    if (ownerActionsSize > 0) {
      sqlTables.append(", AttributeAssignAction ownerAaa ");
    }
  
    Field membersField = FieldFinder.find("members", true);
  
    StringBuilder sqlWhereClause = new StringBuilder(
    		" aa.attributeDefNameId = adn.id and ownerAa.attributeDefNameId = ownerAdn.id and aa.ownerAttributeAssignId = ownerAa.id and "
        + " ownerAa.ownerGroupId = me.ownerGroupId and ownerAa.ownerMemberId = me.memberUuid and me.fieldId = '" + membersField.getUuid() + "' " +
            " and me.enabledDb = 'T' ");
  
    sqlWhereClause.append(" and aa.attributeAssignTypeDb = 'any_mem_asgn' ");
  
    sqlWhereClause.append(" and ownerAa.attributeAssignTypeDb = 'any_mem' ");
  
    queryByValueAddTablesWhereClause(byHqlStatic, sqlTables, sqlWhereClause, attributeDefValueType, theValue);
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    //need to check the assignment on assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    //need to check the assignment
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "ownerAdn.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
  
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, "me.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);
    
    StringBuilder sql;
    sql = sqlTables.append(changedQuery ? " and " : " where ").append(sqlWhereClause);
  
    attributeAssignAssignQueryStart(attributeAssignIds, attributeDefIds,
        attributeDefNameIds, actions, enabled, attributeDefType, ownerAttributeAssignIds,
        ownerAttributeDefIds, ownerAttributeDefNameIds, ownerActions,
        attributeAssignIdsSize, actionsSize, attributeDefIdsSize,
        attributeDefNameIdsSize, ownerAttributeAssignIdsSize, ownerAttributeDefIdsSize,
        ownerAttributeDefNameIdsSize, ownerActionsSize, byHqlStatic, sql);
  
    if (groupIdsAndMemberIdsSize > 0) {
      HibUtils.convertToMultiKeyInClause(groupIdsAndMemberIds, byHqlStatic, GrouperUtil.toSet("ownerAa.ownerGroupId", "ownerAa.ownerMemberId"), sql);
    }
    byHqlStatic
      .setCacheable(useCache)
      .setCacheRegion(KLASS + ".FindMembershipAttributeAssignments");
  
    int maxAssignments = GrouperConfig.retrieveConfig().propertyValueInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    Set<AttributeAssign> results = attributeAssignmentAssignmentRunQuery(
        includeAssignmentsFromAssignments, useCache, byHqlStatic, selectPrefix,
        countPrefix, grouperSession, grouperSessionSubject, sql, maxAssignments);
  
    //we should be down to the secure list
    return results;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findLegacyGroupTypeAssignmentsByGroupId(java.lang.String)
   */
  public Map<String, AttributeAssign> findLegacyGroupTypeAssignmentsByGroupId(String groupId) {
    
    if (StringUtils.isBlank(groupId)) {
      throw new RuntimeException("groupId cant be blank");
    }
    
    Map<String, AttributeAssign> results = new LinkedHashMap<String, AttributeAssign>();
              
    String sql = "select assignmentOnGroup, name, def " +
        "from AttributeAssign as assignmentOnGroup, " +
        "AttributeDefName as name, " +
        "AttributeDef as def, " +
        "Stem as stem " +
        "where assignmentOnGroup.ownerGroupId = :groupId and " +
        "assignmentOnGroup.attributeDefNameId = name.id and " +
        "name.attributeDefId = def.id and " +
        "name.stemId = stem.uuid and " +
        "stem.nameDb = :legacyStemName";
    
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");          

    Set<Object[]> rows = HibernateSession.byHqlStatic().createQuery(sql)
      .setString("groupId", groupId)
      .setString("legacyStemName", stemName)
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindLegacyGroupTypeAssignmentsByGroupId")
      .listSet(Object[].class);
        
    for (Object[] row : rows) {
      AttributeAssign assignment = (AttributeAssign)row[0];
      AttributeDefName name = (AttributeDefName)row[1];
      AttributeDef def = (AttributeDef)row[2];
      
      assignment.internalSetAttributeDef(def);
      assignment.internalSetAttributeDefName(name);
      name.internalSetAttributeDef(def);
      
      if (name.getName().startsWith(stemName) && name.getExtension().startsWith(groupTypePrefix)) {
        String groupTypeName = name.getExtension().substring(groupTypePrefix.length());
        results.put(groupTypeName, assignment);
      }
    }
    
    return results;
  }

  /**
   * @see AttributeAssignDAO#delete(Attribute
   */
  @Override
  public Set<AttributeAssign> findGroupAttributeAssignments(
      Collection<String> attributeAssignIds, Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> groupIds,
      Collection<String> actions, Boolean enabled,
      boolean includeAssignmentsOnAssignments, AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType, Object theValue) {
    return findGroupAttributeAssignmentsHelper(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        groupIds, actions, enabled, includeAssignmentsOnAssignments,
        attributeDefType, attributeDefValueType, theValue, null);
  }

  /**
   * @see AttributeAssignDAO#findStemAttributeAssignments(Collection, Collection, Collection, Collection, Collection, Boolean, boolean, AttributeDefType, AttributeDefValueType, Object, boolean)
   */
  @Override
  public Set<AttributeAssign> findStemAttributeAssignments(
      Collection<String> attributeAssignIds, Collection<String> attributeDefIds,
      Collection<String> attributeDefNameIds, Collection<String> stemIds,
      Collection<String> actions, Boolean enabled,
      boolean includeAssignmentsOnAssignments, AttributeDefType attributeDefType,
      AttributeDefValueType attributeDefValueType, Object theValue,
      boolean attributeCheckReadOnAttributeDef) {
    return findStemAttributeAssignmentsHelper(attributeAssignIds,
        attributeDefIds, attributeDefNameIds,
        stemIds, actions, enabled, includeAssignmentsOnAssignments, 
        attributeDefType, attributeDefValueType, theValue, attributeCheckReadOnAttributeDef);
  }

  /**
   * remove this from all caches
   * @param attributeDef
   */
  public static void attributeDefNameCacheRemove(AttributeDef attributeDef) {
    attributeAssignFlashCache.clear();
  }

  /**
   * see if this is cacheable
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean attributeAssignFlashCacheable(QueryOptions queryOptions) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_ASSIGNS_CACHE, true)) {
      return false;
    }
  
    if (queryOptions != null 
        && queryOptions.getSecondLevelCache() != null && !queryOptions.getSecondLevelCache()) {
      return false;
    }
    
    return true;
  }

  /**
   * add attributeAssign to cache if not null
   * @param multiKey 
   * @param attributeAssigns
   */
  private static void attributeAssignFlashCacheAddIfSupposedTo(MultiKey multiKey, Set<AttributeAssign> attributeAssigns) {
    if (multiKey == null || attributeAssigns == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_ASSIGNS_CACHE, true)) {
      return;
    }
    
    attributeAssignFlashCache.put(multiKey, attributeAssigns);
    
    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {

      multiKey = attributeAssignFlashCacheMultiKey(attributeAssign.getId());
      if (multiKey != null) {
        attributeAssignFlashCache.put(multiKey, GrouperUtil.toSet(attributeAssign));
      }
    }
  }

  /**
   * get a attributeDefName from flash cache
   * @param multiKey 
   * @param queryOptions
   * @return the attributeAssigns or null
   */
  private static Set<AttributeAssign> attributeAssignFlashCacheRetrieve(MultiKey multiKey, QueryOptions queryOptions) {
    if (multiKey != null && attributeAssignFlashCacheable(queryOptions)) {
      //see if its already in the cache
      Set<AttributeAssign> attributeAssigns = attributeAssignFlashCache.get(multiKey);
      if (attributeAssigns != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attribute assigns flash cache: " + attributeAssignFlashCacheMultiKeyToString(multiKey));
        }
        return attributeAssigns;
      }
    }
    return null;
  }
  
} 


