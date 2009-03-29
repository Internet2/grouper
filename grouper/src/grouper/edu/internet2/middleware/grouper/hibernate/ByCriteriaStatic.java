/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
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
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByCriteriaStatic, persistentClass: '");
    result.append(this.persistentClass).append("', criterions: ").append(this.criterions)
      .append("', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", tx type: ").append(this.grouperTransactionType);
    if (this.sort != null) {
      result.append(", sort: ").append(this.sort.sortString(false));
    }
    if (this.paging != null) {
      result.append(", ").append(this.paging.toString());
    }
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * criterions to query
   */
  private Criterion criterions = null;

  /**
   * class to execute criteria on
   */
  private Class<?> persistentClass = null;

  /** if we are sorting */
  private QuerySort sort = null;

  /**
   * add a sort to the query
   * @param querySort1
   * @return this for chaining
   */
  public ByCriteriaStatic sort(QuerySort querySort1) {
    this.sort = querySort1;
    return this;
  }

  /** paging */
  private QueryPaging paging = null;

  /**
   * add a paging to the query
   * @param queryPaging1
   * @return this for chaining
   */
  public ByCriteriaStatic paging(QueryPaging queryPaging1) {
    this.paging = queryPaging1;
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
      
      T result = (T)HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              Session session  = hibernateSession.getSession();
              Criteria criteria = ByCriteriaStatic.this.attachCriteriaInfo(session);
              Object object = criteria.uniqueResult();
              HibUtils.evict(hibernateSession, object, true);
              return object;
            }
        
      });
      
      return result;
    } catch (GrouperDAOException e) {
      LOG.error("Exception in uniqueResult: (" + returnType + "), " + this, e);
      throw e;
    } catch (RuntimeException e) {
      LOG.error("Exception in uniqueResult: " + this, e);
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
      
      List<T> result = (List<T>)HibernateSession.callbackHibernateSession(grouperTransactionTypeToUse,
          new HibernateHandler() {
  
            public Object callback(HibernateSession hibernateSession) {
              
              Session session  = hibernateSession.getSession();
              Criteria criteria = ByCriteriaStatic.this.attachCriteriaInfo(session);
              //not sure this can ever be null, but make sure not to make iterating results easier
              List<Object> list = GrouperUtil.nonNull(criteria.list());
              HibUtils.evict(hibernateSession, list, true);
              
              //now see if we should get the query count
              if (ByCriteriaStatic.this.paging != null && ByCriteriaStatic.this.paging.isDoTotalCount()) {
                
                //see if we already know the total size (if less than page size and first page)
                int resultSize = list.size();
                if (resultSize >= ByCriteriaStatic.this.paging.getPageSize() 
                    || ByCriteriaStatic.this.paging.getPageNumber() != 1) {
                  resultSize = -1;
                }
                
                //do this if we dont have a total, or if we are not caching the total
                if (ByCriteriaStatic.this.paging.getTotalRecordCount() < 0 
                    || !ByCriteriaStatic.this.paging.isCacheTotalCount() || resultSize > -1) {

                  //if we dont already know the size
                  if (resultSize == -1) {

                    queryCountQueries++;

                    Criteria countQuery = session.createCriteria(ByCriteriaStatic.this.persistentClass);

                    //turn it into a row count
                    countQuery.setProjection( Projections.projectionList()
                        .add( Projections.rowCount()));

                    //add criterions
                    if (ByCriteriaStatic.this.criterions != null) {
                      countQuery.add(ByCriteriaStatic.this.criterions);
                    }
                    resultSize = (Integer)countQuery.list().get(0);
                  }
                  ByCriteriaStatic.this.paging.setTotalRecordCount(resultSize);
                  //calculate the page stuff like how many pages etc
                  ByCriteriaStatic.this.paging.calculateIndexes();
                }
              }

              return list;
            }
      });
      
      return result;
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
    Criteria   query = session.createCriteria(this.persistentClass);
    //add criterions
    if (this.criterions != null) {
      query.add(this.criterions);
    }
    if (ByCriteriaStatic.this.cacheable != null) {
      query.setCacheable(ByCriteriaStatic.this.cacheable);
    }
    if (ByCriteriaStatic.this.cacheRegion != null) {
      query.setCacheRegion(ByCriteriaStatic.this.cacheRegion);
    }
    
    if (this.sort != null) {
      List<QuerySortField> sorts = this.sort.getQuerySortFields();
      
      for (QuerySortField theSort : GrouperUtil.nonNull(sorts)) {
        
        Order order = theSort.isAscending() ? Order.asc(theSort.getColumn()) : Order.desc(theSort.getColumn());
        
        query.addOrder(order);
        
      }
    }
    if (this.paging != null) {
      query.setFirstResult(this.paging.getFirstIndexOnPage());
      query.setMaxResults(this.paging.getPageSize());
    }

    return query;
    
  }
  
  /**
   * constructor
   *
   */
  ByCriteriaStatic() {}  
  
}
