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
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignValueNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignValueDAO.java,v 1.2 2009-09-28 05:06:46 mchyzer Exp $
 */
public class Hib3AttributeAssignValueDAO extends Hib3DAO implements AttributeAssignValueDAO {
  
  /**
   * 
   */
  private static final String KLASS = Hib3AttributeAssignValueDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssignValue").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findById(java.lang.String, boolean)
   */
  public AttributeAssignValue findById(String id, boolean exceptionIfNotFound) {
    AttributeAssignValue attributeAssignValue = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignValue where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssignValue.class);
    if (attributeAssignValue == null && exceptionIfNotFound) {
      throw new AttributeAssignValueNotFoundException("Cant find attribute assign value by id: " + id);
   }

    return attributeAssignValue;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.value.AttributeAssignValue)
   */
  public void saveOrUpdate(AttributeAssignValue attributeAssignValue) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findByUuidOrKey(java.util.Collection, java.lang.String, java.lang.String, boolean, java.lang.Long, java.lang.String, java.lang.String)
   */
  public AttributeAssignValue findByUuidOrKey(Collection<String> idsToIgnore, String id,
      String attributeAssignId, boolean exceptionIfNull, Long valueInteger,
      String valueMemberId, String valueString) throws GrouperDAOException {
    try {
      Set<AttributeAssignValue> attributeAssignValues = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssignValue as theAttributeAssignValue where " +
            "theAttributeAssignValue.id = :theId or theAttributeAssignValue.attributeAssignId = :theAttributeAssignId")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theAttributeAssignId", attributeAssignId)
        .listSet(AttributeAssignValue.class);
      if (GrouperUtil.length(attributeAssignValues) == 0) {
        if (exceptionIfNull) {
          throw new RuntimeException("Can't find attributeAssignValue by id: '" + id + "' or attributeAssignId '" + attributeAssignId 
              + "'");
        }
        return null;
      }
      
      idsToIgnore = GrouperUtil.nonNull(idsToIgnore);
      
      //lets remove ones we have already processed or will process
      Iterator<AttributeAssignValue> iterator = attributeAssignValues.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssignValue attributeAssignValue = iterator.next();
        if (idsToIgnore.contains(attributeAssignValue.getId())) {
          iterator.remove();
        }
      }
      
      //first case, the ID matches
      iterator = attributeAssignValues.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssignValue attributeAssignValue = iterator.next();
        if (StringUtils.equals(id, attributeAssignValue.getId())) {
          return attributeAssignValue;
        }
      }

      //second case, the value matches
      iterator = attributeAssignValues.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssignValue attributeAssignValue = iterator.next();
        if (StringUtils.equals(valueString, attributeAssignValue.getValueString())
            && StringUtils.equals(valueMemberId, attributeAssignValue.getValueMemberId())
            && GrouperUtil.equals(valueInteger, attributeAssignValue.getValueInteger())) {
          return attributeAssignValue;
        }
      }
      
      //ok, if there is one left, return it
      if (attributeAssignValues.size() > 0) {
        return attributeAssignValues.iterator().next();
      }
      
      //cant find one
      return null;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeAssignValue by id: '" + id + "' or attributeAssignId '" + attributeAssignId 
            + "', valueString: " + valueString 
            + "', valueInteger: " + valueInteger + ", valueMemberId: " + valueMemberId + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.value.AttributeAssignValue)
   */
  public void saveUpdateProperties(AttributeAssignValue attributeAssignValue) {

    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeAssignValue " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdatedDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeAssignValue.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeAssignValue.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeAssignValue.getLastUpdatedDb())
        .setString("theContextId", attributeAssignValue.getContextId())
        .setString("theId", attributeAssignValue.getId()).executeUpdate();

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#delete(edu.internet2.middleware.grouper.attr.value.AttributeAssignValue)
   */
  public void delete(AttributeAssignValue attributeAssignValue) {
    HibernateSession.byObjectStatic().delete(attributeAssignValue);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findByAttributeAssignId(java.lang.String)
   */
  public Set<AttributeAssignValue> findByAttributeAssignId(String attributeAssignId) {
    return findByAttributeAssignId(attributeAssignId, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findByAttributeAssignId(java.lang.String, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<AttributeAssignValue> findByAttributeAssignId(String attributeAssignId,
      QueryOptions queryOptions) {
    try {
      Set<AttributeAssignValue> attributeAssignValues = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssignValue as theAttributeAssignValue where " +
            "theAttributeAssignValue.attributeAssignId = :theAttributeAssignId")
        .options(queryOptions)
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByAttributeAssignId")
        .setString("theAttributeAssignId", attributeAssignId)
        .listSet(AttributeAssignValue.class);
      
      //return result
      return attributeAssignValues;
    } catch (GrouperDAOException e) {
      String error = "Problem find attributeAssignValue by attributeAssignId '" + attributeAssignId 
             + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

  }

  /**
   * find all assignments and values for a member
   */
  public Map<AttributeAssign, Set<AttributeAssignValue>> findMemberAttributeAssignmentValues(
      Collection<String> memberIds, Boolean enabled) {

    int memberIdsSize = GrouperUtil.length(memberIds);
    
    if (memberIdsSize == 0) {
      throw new RuntimeException("Illegal query, you need to pass in memberId(s) [subjectIds or subjectIdentifiers]");
    }
    
    //too many bind vars
    if (memberIdsSize > 100) {
      throw new RuntimeException("Too many memberIdsSize " + memberIdsSize);
    }
  
    //lets get the attribute assigns
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignments(null, null, null, memberIds, null, enabled, false);
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    String selectPrefix = "select distinct aav ";
    String countPrefix = "select count(distinct aav) ";
    
    StringBuilder sqlTables = new StringBuilder(" from AttributeAssign aa, AttributeDefName adn, AttributeAssignValue aav ");
    
    StringBuilder sqlWhereClause = new StringBuilder(
        " aav.attributeAssignId = aa.id and aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'member' ");
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
      grouperSessionSubject, byHqlStatic, 
      sqlTables, sqlWhereClause, "adn.attributeDefId", AttributeDefPrivilege.READ_PRIVILEGES);
    
    StringBuilder sql;
    sql = sqlTables.append(" where ").append(sqlWhereClause);
    
    if (enabled != null && enabled) {
      sql.append(" and aa.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and aa.enabledDb = 'F' ");
    }
    if (memberIdsSize > 0) {
      sql.append(" and aa.ownerMemberId in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
    }
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberAttributeAssignmentValues");
  
    int maxAssignments = GrouperConfig.getPropertyInt("ws.findAttrAssignments.maxResultSize", 30000);
    
    long size = -1;
    
    //if -1, lets not check
    if (maxAssignments >= 0) {
  
      size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
      
      //see if too many
      if (size > maxAssignments) {
        throw new RuntimeException("Too many results: " + size);
      }
      
    }
    
    Set<AttributeAssignValue> results = size == 0 ? new LinkedHashSet<AttributeAssignValue>() 
        : byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(AttributeAssignValue.class);
  
    //hash these by id
    Map<String, AttributeAssign> attributeAssignMap = new HashMap<String, AttributeAssign>();

    Map<AttributeAssign, Set<AttributeAssignValue>> resultMap = new LinkedHashMap<AttributeAssign, Set<AttributeAssignValue>>();
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      attributeAssignMap.put(attributeAssign.getId(), attributeAssign);
      resultMap.put(attributeAssign, new HashSet<AttributeAssignValue>());
    }
    
    
    //nothing to filter
    if (GrouperUtil.length(results) == 0) {
      return resultMap;
    }
    
    //lets add stuff to the map
    for (AttributeAssignValue attributeAssignValue : results) {
      AttributeAssign attributeAssign = attributeAssignMap.get(attributeAssignValue.getAttributeAssignId());
      
      //if its null then we arent allowed to see it or something...
      if (attributeAssign == null) {
        continue;
      }
      Set<AttributeAssignValue> values = resultMap.get(attributeAssign);
      values.add(attributeAssignValue);
    }
    
    return resultMap;
  }

  /**
   * @see AttributeAssignValueDAO#findByAttributeAssignIds(Collection)
   */
  public Set<AttributeAssignValue> findByAttributeAssignIds(
      Collection<String> totalAttributeAssignIds) {

    if (GrouperUtil.length(totalAttributeAssignIds) == 0) {
      return new HashSet<AttributeAssignValue>();
    }

    Set<AttributeAssignValue> results = new LinkedHashSet<AttributeAssignValue>();
    
    int attributeAssignBatches = GrouperUtil.batchNumberOfBatches(totalAttributeAssignIds, 100);
    List<String> totalAttributeAssignIdsList = GrouperUtil.listFromCollection(totalAttributeAssignIds);
    //could be more than 100 so batch them up
    for (int index = 0; index < attributeAssignBatches; index++) {
      
      List<String> currentAttributeAssignIds = GrouperUtil.batchList(totalAttributeAssignIdsList, 100, index);

    
      ByHqlStatic byHqlStatic =  HibernateSession.byHqlStatic();
      
      StringBuilder sql = new StringBuilder("from AttributeAssignValue as theAttributeAssignValue where ");
      
      sql.append(" theAttributeAssignValue.attributeAssignId in (");
      sql.append(HibUtils.convertToInClause(currentAttributeAssignIds, byHqlStatic));
      sql.append(") ");
      
      
      Set<AttributeAssignValue> attributeAssignValues = byHqlStatic.createQuery(sql.toString())
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByAttributeAssignIds")
        .listSet(AttributeAssignValue.class);
      
      results.addAll(GrouperUtil.nonNull(attributeAssignValues));
      
    }
    
    //return result
    return results;
  }

  /**
   * @see AttributeAssignValueDAO#findByValueString(String)
   */
  public Set<AttributeAssignValue> findByValueString(String value) {
    
    if (StringUtils.isBlank(value)) {
      throw new RuntimeException("value cant be blank");
    }
    
    Set<AttributeAssignValue> results = new LinkedHashSet<AttributeAssignValue>();
    
    ByHqlStatic byHqlStatic =  HibernateSession.byHqlStatic();
      
    StringBuilder sql = new StringBuilder("select distinct theAttributeAssignValue " +
    		"from AttributeAssignValue as theAttributeAssignValue where ");
    sql.append(" theAttributeAssignValue.valueString = :theValueString");
    byHqlStatic.setString("theValueString", value);
    
    Set<AttributeAssignValue> attributeAssignValues = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByValueString")
      .listSet(AttributeAssignValue.class);
        
    //return attributeAssignValues
    return attributeAssignValues;
  }

} 

