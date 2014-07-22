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
/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * for simple criteria queries, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * GrouperTransactionType.READONLY_OR_USE_EXISTING, and 
 * GrouperTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class ByCriteriaStatic {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ByCriteriaStatic.class);

  /** assign a transaction type, default use the transaction modes
   * GrouperTransactionType.READONLY_OR_USE_EXISTING, and 
   * GrouperTransactionType.READ_WRITE_OR_USE_EXISTING depending on
   * if a transaction is needed */
  private GrouperTransactionType grouperTransactionType = null;
  
  /**
   * assign if this query is cacheable or not.
   */
  private Boolean cacheable = null;
  
  /**
   * assign a different grouperTransactionType (e.g. for autonomous transactions)
   * @param theGrouperTransactionType
   * @return the same object for chaining
   */
  public ByCriteriaStatic setGrouperTransactionType(GrouperTransactionType 
      theGrouperTransactionType) {
    this.grouperTransactionType = theGrouperTransactionType;
    return this;
  }
  
  
  /**
   * assign if this query is cacheable or not.
   * @param cacheable the cacheable to set
   * @return this object for chaining
   */
  public ByCriteriaStatic setCacheable(Boolean cacheable) {
    this.cacheable = cacheable;
    return this;
  }

  /**
   * <pre>
   * call hql list result, and put the results in an ordered set
   * 
   * e.g.
   * 
   * Set<GroupTypeTupleDTO> groupTypeTupleDTOs = 
   *  HibernateSession.byHqlStatic()
   *    .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
   *    .setCacheable(false).setString("group", uuid).listSet(Hib3GroupTypeTupleDAO.class);
   * </pre>
   * @param returnType type of the result (can typecast)
   * @param <S> is the template
   * @param theCriterions 
   * @return the ordered set or the empty set if not found (never null)
   * @throws GrouperDAOException
   */
  public <S> Set<S> listSet(Class<S> returnType, Criterion theCriterions) throws GrouperDAOException {
    Set<S> result = new LinkedHashSet<S>(this.list(returnType, theCriterions));
    return result;
  }

  /**
   * <pre>
   * call criteria list result, and put the results in map with the key as one of the fields
   * 
   * </pre>
   * @param valueClass type of the result (can typecast)
   * @param theCriterions are the criteria for the query
   * @param keyClass is the type of the key of the map
   * @param <K> is the template of the key of the map
   * @param <V> is the template of the value of the map
   * @param keyPropertyName name of the javabeans property for the key in the map
   * @return the ordered set or the empty set if not found (never null)
   * @throws GrouperDAOException
   */
  public <K, V> Map<K, V> listMap(final Class<K> keyClass, final Class<V> valueClass, Criterion theCriterions, String keyPropertyName) throws GrouperDAOException {
    List<V> list = this.list(valueClass, theCriterions);
    Map<K,V> map = GrouperUtil.listToMap(list, keyClass, valueClass, keyPropertyName);
    return map;
  }

  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByCriteriaStatic, persistentClass: '");
    result.append(this.persistentClass).append("', criterions: ").append(this.criterions)
      .append("', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", entityName: ").append(this.entityName);
    result.append(", tx type: ").append(this.grouperTransactionType);
    if (this.queryOptions != null) {
      result.append(", options: ").append(this.queryOptions.toString());
    }
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * alias for class
   */
  private String alias = null;

  /**
   * criterions to query
   */
  private Criterion criterions = null;

  /**
   * class to execute criteria on
   */
  private Class<?> persistentClass = null;

  /** if we are sorting, paging, resultSize, etc */
  private QueryOptions queryOptions = null;

  /**
   * assign the entity name to refer to this mapping (multiple mappings per object)
   */
  private String entityName = null;

  /**
   * add a paging/sorting/resultSetSize, etc to the query
   * @param queryOptions1
   * @return this for chaining
   */
  public ByCriteriaStatic options(QueryOptions queryOptions1) {
    this.queryOptions = queryOptions1;
    return this;
  }

  /**
   * cache region for cache
   * @param cacheRegion the cacheRegion to set
   * @return this object for chaining
   */
  public ByCriteriaStatic setCacheRegion(String cacheRegion) {
    this.cacheRegion = cacheRegion;
    return this;
  }

  /**
   * alias for queried class
   * @param theAlias the cacheRegion to set
   * @return this object for chaining
   */
  public ByCriteriaStatic setAlias(String theAlias) {
    this.alias = theAlias;
    return this;
  }

  /**
   * <pre>
   * call hql unique result (returns one or null)
   * 
   * e.g.
   * 
   * Hib3GroupDAO hib3GroupDAO = HibernateSession.byHqlStatic()
   * .createQuery("from Hib3GroupDAO as g where g.uuid = :uuid")
   *  .setCacheable(false)
   *  .setCacheRegion(KLASS + ".Exists")
   *  .setString("uuid", uuid).uniqueResult(Hib3GroupDAO.class);
   * 
   * </pre>
   * @param returnType type of the result (in future can use this for typecasting)
   * @param theCriterions are the criterions to use (pack multiple with HibUtils.listCrit())
   * @param <T> is the template
   * @return the object or null if none found
   * @throws GrouperDAOException
   */
  public <T> T uniqueResult(Class<T> returnType, Criterion theCriterions) throws GrouperDAOException {
    this.persistentClass = returnType;
    this.criterions = theCriterions;
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READONLY_OR_USE_EXISTING);
      
      T result = (T)HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              Session session  = hibernateSession.getSession();
              Criteria criteria = ByCriteriaStatic.this.attachCriteriaInfo(session);
              GrouperContext.incrementQueryCount();
              Object object = criteria.uniqueResult();
              HibUtils.evict(hibernateSession, object, true);
              return object;
            }
        
      });
      
      return result;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in uniqueResult: (" + returnType + "), " + this;

      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /** query count exec queries, used for testing */
  public static int queryCountQueries = 0;
  
  /**
   * <pre>
   * call hql unique result (returns one or null)
   * 
   * e.g.
   * 
   * List<Hib3GroupTypeTupleDAO> hib3GroupTypeTupleDAOs = 
   *  HibernateSession.byHqlStatic()
   *    .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
   *    .setCacheable(false).setString("group", uuid).list(Hib3GroupTypeTupleDAO.class);
   * </pre>
   * @param returnType type of the result (can typecast)
   * @param theCriterions are the criterions to use (pack multiple with HibUtils.listCrit())
   * @param <T> is the template
   * @return the list or the empty list if not found (never null)
   * @throws GrouperDAOException
   */
  public <T> List<T> list(Class<T> returnType, Criterion theCriterions) throws GrouperDAOException {
    this.persistentClass = returnType;
    this.criterions = theCriterions;
    try {
      GrouperTransactionType grouperTransactionTypeToUse = 
        (GrouperTransactionType)ObjectUtils.defaultIfNull(this.grouperTransactionType, 
            GrouperTransactionType.READONLY_OR_USE_EXISTING);
      
      List<T> result = (List<T>)HibernateSession.callbackHibernateSession(
          grouperTransactionTypeToUse, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              Session session  = hibernateSession.getSession();
              List<T> list = null;
              
              //see if we are even retrieving the results
              if (ByCriteriaStatic.this.queryOptions == null || ByCriteriaStatic.this.queryOptions.isRetrieveResults()) {
              Criteria criteria = ByCriteriaStatic.this.attachCriteriaInfo(session);
              GrouperContext.incrementQueryCount();
              //not sure this can ever be null, but make sure not to make iterating results easier
                list = GrouperUtil.nonNull(criteria.list());
              HibUtils.evict(hibernateSession, list, true);
              }
              //no nulls
              list = GrouperUtil.nonNull(list);
              QueryPaging queryPaging = ByCriteriaStatic.this.queryOptions == null ? null : ByCriteriaStatic.this.queryOptions.getQueryPaging();
              
              //now see if we should get the query count
              boolean retrieveQueryCountNotForPaging = ByCriteriaStatic.this.queryOptions != null && ByCriteriaStatic.this.queryOptions.isRetrieveCount();
              boolean findQueryCount = (queryPaging != null && queryPaging.isDoTotalCount()) 
                || (retrieveQueryCountNotForPaging);
              if (findQueryCount) {
                
                long resultSize = -1;
                if (queryPaging != null) {
                  //see if we already know the total size (if less than page size and first page)
                  resultSize = GrouperUtil.length(list);
                  if (resultSize >= queryPaging.getPageSize()) {
                    resultSize = -1;
                  } else {
                    //we are on the last page, see how many records came before us, add those in
                    resultSize += (queryPaging.getPageSize() * (queryPaging.getPageNumber() - 1)); 
                  }
                }
                
                //do this if we dont have a total, or if we are not caching the total
                if ((queryPaging != null && (queryPaging.getTotalRecordCount() < 0 || !queryPaging.isCacheTotalCount())) 
                    || resultSize > -1 || retrieveQueryCountNotForPaging) {
                  
                  //if we dont already know the size
                  if (resultSize == -1) {
                    queryCountQueries++;

                    Criteria countQuery = StringUtils.isBlank(ByCriteriaStatic.this.alias) ? 
                        session.createCriteria(ByCriteriaStatic.this.persistentClass) 
                        : session.createCriteria(ByCriteriaStatic.this.alias);

                    //turn it into a row count
                    countQuery.setProjection( Projections.projectionList()
                        .add( Projections.rowCount()));

                    //add criterions
                    if (ByCriteriaStatic.this.criterions != null) {
                      countQuery.add(ByCriteriaStatic.this.criterions);
                    }
                    resultSize = (Long)countQuery.list().get(0);
                  }
                  
                  if (queryPaging != null) {
                    queryPaging.setTotalRecordCount((int)resultSize);
            
                    //calculate the page stuff like how many pages etc
                    queryPaging.calculateIndexes();
                  }
                  if (retrieveQueryCountNotForPaging) {
                    ByCriteriaStatic.this.queryOptions.setCount(resultSize);
                  }
                }
              }

              return list;
            }
        
      });
      
      return result;
    } catch (GrouperStaleObjectStateException e) {
      throw e;
    } catch (GrouperDAOException e) {
      GrouperUtil.injectInException(e, "Exception in list: (" + returnType + "), " + this);
      throw e;
    } catch (RuntimeException e) {
      GrouperUtil.injectInException(e, "Exception in list: (" + returnType + "), " + this);
      throw e;
    }
    
  }

  /**
   * prepare query based on criteria
   * @param session hib session
   * @return the query
   */
  private Criteria attachCriteriaInfo(Session session) {
    Criteria query = null;
    
    if (StringUtils.isBlank(this.entityName)) {
      if (StringUtils.isBlank(this.alias)) {
        query = session.createCriteria(this.persistentClass);
      } else {
        query = session.createCriteria(this.persistentClass, alias);
      }
    } else {
      query = session.createCriteria(this.entityName);
    }
    
    //add criterions
    if (this.criterions != null) {
      query.add(this.criterions);
    }
    boolean secondLevelCaching = HibUtils.secondLevelCaching(
        ByCriteriaStatic.this.cacheable, ByCriteriaStatic.this.queryOptions);
    query.setCacheable(secondLevelCaching);

    if (secondLevelCaching) {
      String secondLevelCacheRegion = HibUtils.secondLevelCacheRegion(ByCriteriaStatic.this.cacheRegion, 
          ByCriteriaStatic.this.queryOptions);
      if (!StringUtils.isBlank(secondLevelCacheRegion)) {
        query.setCacheRegion(secondLevelCacheRegion);
      }
    }
    
    QuerySort querySort = this.queryOptions == null ? null : this.queryOptions.getQuerySort();
    if (querySort != null) {
      List<QuerySortField> sorts = querySort.getQuerySortFields();
      
      for (QuerySortField theSort : GrouperUtil.nonNull(sorts)) {
        
        Order order = theSort.isAscending() ? Order.asc(theSort.getColumn()) : Order.desc(theSort.getColumn());
        
        query.addOrder(order);
        
    }
    }
    QueryPaging queryPaging = this.queryOptions == null ? null : this.queryOptions.getQueryPaging();
    if (queryPaging != null) {
      query.setFirstResult(queryPaging.getFirstIndexOnPage());
      query.setMaxResults(queryPaging.getPageSize());
    }

    return query;
    
  }
  
  /**
   * entity name if the object is mapped to more than one table
   * @param theEntityName the entity name of the object
   * @return this object for chaining
   */
  public ByCriteriaStatic setEntityName(String theEntityName) {
    this.entityName = theEntityName;
    return this;
  }


  /**
   * constructor
   *
   */
  ByCriteriaStatic() {}  
  
}
