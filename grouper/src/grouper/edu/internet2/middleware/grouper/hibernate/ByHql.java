/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * for simple HQL, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * GrouperTransactionType.READONLY_OR_USE_EXISTING, and 
 * GrouperTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class ByHql extends HibernateDelegate implements HqlQuery {
  
  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value
   * @return this object for chaining
   */
  public ByHql setTimestamp(String bindVarName, Date value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Timestamp.class));
    return this;
  }
  

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(ByHql.class);

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
  public ByHql setGrouperTransactionType(GrouperTransactionType 
      theGrouperTransactionType) {
    this.grouperTransactionType = theGrouperTransactionType;
    return this;
  }
  
  
  /**
   * assign if this query is cacheable or not.
   * @param cacheable the cacheable to set
   * @return this object for chaining
   */
  public ByHql setCacheable(Boolean cacheable) {
    this.cacheable = cacheable;
    return this;
  }

  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByHql, query: '");
    result.append(this.query).append("', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", tx type: ").append(this.grouperTransactionType);
    if (this.queryOptions != null) {
      result.append(", options: ").append(this.queryOptions.toString());
    }
    //dont use bindVarParams() method so it doesnt lazy load
    if (this.bindVarNameParams != null) {
      int index = 0;
      int size = this.bindVarNameParams().size();
      for (HibernateParam hibernateParam : this.bindVarNameParams()) {
        result.append("Bind var[").append(index++).append("]: '");
        result.append(hibernateParam);
        if (index!=size-1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * map of params to attach to the query.
   * access this with the bindVarNameParams method
   */
  private List<HibernateParam> bindVarNameParams = null;

  /**
   * query to execute
   */
  private String query = null;

  /**
   * if we are sorting, paging, resultSize, etc 
   */
  private QueryOptions queryOptions = null;

  /**
   * set the query to run
   * @param theHqlQuery
   * @return this object for chaining
   */
  public ByHql createQuery(String theHqlQuery) {
    this.query = theHqlQuery;
    return this;
  }
  
  /**
   * lazy load params
   * @return the params map
   */
  private List<HibernateParam> bindVarNameParams() {
    if (this.bindVarNameParams == null) {
      this.bindVarNameParams = new ArrayList<HibernateParam>();
    }
    return this.bindVarNameParams;
  }
  
  /**
   * cache region for cache
   * @param cacheRegion the cacheRegion to set
   * @return this object for chaining
   */
  public ByHql setCacheRegion(String cacheRegion) {
    this.cacheRegion = cacheRegion;
    return this;
  }

  /**
   * append a certain number of params, and commas, and attach
   * the data.  Note any params before the in clause need to be already attached, 
   * since these will attach now (ordering issue)
   * @param query
   * @param params collection of params, note, this is for an inclause, so it cant be null
   * @return this for chaining
   */
  public ByHql setCollectionInClause(StringBuilder query, Collection<?> params) {
    ByHqlStatic.collectionInClauseHelper(this, query, params);
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value
   * @return this object for chaining
   */
  public ByHql setString(String bindVarName, String value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, String.class));
    return this;
  }
  
  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHql setLong(String bindVarName, Long value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Long.class));
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHql setInteger(String bindVarName, Integer value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Integer.class));
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
   * @param <T> is the template
   * @return the object or null if none found
   * @throws GrouperDAOException
   */
  public <T> T uniqueResult(@SuppressWarnings("unused") Class<T> returnType) throws GrouperDAOException {

    GrouperContext.incrementQueryCount();
    HibernateSession hibernateSession = this.getHibernateSession();
    Session session  = hibernateSession.getSession();
    Query query = ByHql.this.attachQueryInfo(session);
    T object = null;
    try {
      object = (T) query.uniqueResult();
    } catch (ObjectNotFoundException onfe) {
      //hibernate error when it couldnt find what was in cache perhaps, run the query again without caching
      if (this.cacheable != null && this.cacheable) {
        this.cacheable = false;
        query = ByHql.this.attachQueryInfo(session);
        object = (T) query.uniqueResult();
        //set this back
        this.cacheable = true;
      }
    }
    HibUtils.evict(hibernateSession, object, true);
    return object;
    
  }
  
  /**
   * <pre>
   * call hql executeUpdate, e.g. delete or update statement
   * 
   * </pre>
   * @throws GrouperDAOException 
   */
  public void executeUpdate() throws GrouperDAOException {
    GrouperContext.incrementQueryCount();
    HibernateSession hibernateSession = this.getHibernateSession();
    Session session  = hibernateSession.getSession();
    Query query = ByHql.this.attachQueryInfo(session);
    query.executeUpdate();
  }
  
  /** query count exec queries, used for testing */
  public static int queryCountQueries = 0;
  
  /**
   * <pre>
   * call hql list result
   * 
   * e.g.
   * 
   * List<Hib3GroupTypeTupleDAO> hib3GroupTypeTupleDAOs = 
   *  HibernateSession.byHqlStatic()
   *    .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
   *    .setCacheable(false).setString("group", uuid).list(Hib3GroupTypeTupleDAO.class);
   * </pre>
   * @param returnType type of the result (can typecast)
   * @param <T> is the template
   * @return the list or the empty list if not found (only null if not retrieving results)
   * @throws GrouperDAOException
   */
  public <T> List<T> list(@SuppressWarnings("unused") Class<T> returnType) {
    GrouperContext.incrementQueryCount();

    HibernateSession hibernateSession = this.getHibernateSession();
    Session session  = hibernateSession.getSession();
    List<T> list = null;
    
    //see if we are even retrieving the results
    if (this.queryOptions == null || this.queryOptions.isRetrieveResults()) {
      Query query = ByHql.this.attachQueryInfo(session);
      //not sure this can ever be null, but make sure not to make iterating results easier
      list = query.list();
      HibUtils.evict(hibernateSession,  list, true);
    }
    //no nulls
    list = GrouperUtil.nonNull(list);
    QueryPaging queryPaging = this.queryOptions == null ? null : this.queryOptions.getQueryPaging();
    
    //now see if we should get the query count
    boolean retrieveQueryCountNotForPaging = this.queryOptions != null && this.queryOptions.isRetrieveCount();
    boolean findQueryCount = (queryPaging != null && queryPaging.isDoTotalCount()) 
      || (retrieveQueryCountNotForPaging);
    if (findQueryCount) {
      
      int resultSize = -1;
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
      
      boolean needsPagingQuery = false;
      if (queryPaging != null && (queryPaging.getTotalRecordCount() <= 0 || !queryPaging.isCacheTotalCount())) {
        needsPagingQuery = true;
      }
      if (retrieveQueryCountNotForPaging) {
        needsPagingQuery = true;
      }
      //we already know the size
      if (resultSize != -1) {
        needsPagingQuery = false;
      }

      //do this if we dont have a total, or if we are not caching the total
      if (needsPagingQuery) {
        
        queryCountQueries++;
        String countQueryHql = HibUtils.convertHqlToCountHql(this.query);
        Query countQuery = session.createQuery(countQueryHql);
        attachBindValues(countQuery);
        Long theCount = (Long)countQuery.iterate().next();
        resultSize = theCount.intValue();
      }
      if (resultSize != -1) {
        if (queryPaging != null) {
          queryPaging.setTotalRecordCount(resultSize);

          //calculate the page stuff like how many pages etc
          queryPaging.calculateIndexes();
        }
        if (retrieveQueryCountNotForPaging) {
          this.queryOptions.setCount((long)resultSize);
        }
      }
    }

    return list;
    
  }
  
  /**
   * <pre>
   * call hql list result, and put the results in map with the key as one of the fields
   * 
   * </pre>
   * @param valueClass type of the result (can typecast)
   * @param keyClass is the type of the key of the map
   * @param <K> is the template of the value of the map
   * @param <V> is the template of the value of the map
   * @param keyPropertyName name of the javabeans property for the key in the map
   * @return the ordered set or the empty set if not found (never null)
   * @throws GrouperDAOException
   */
  public <K, V> Map<K, V> listMap(final Class<K> keyClass, final Class<V> valueClass, String keyPropertyName) throws GrouperDAOException {
    List<V> list = this.list(valueClass);
    Map<K,V> map = GrouperUtil.listToMap(list, keyClass, valueClass, keyPropertyName);
    return map;
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
   * @return the ordered set or the empty set if not found (never null)
   * @throws GrouperDAOException
   */
  public <S> Set<S> listSet(final Class<S> returnType) throws GrouperDAOException {
    Set<S> result = new LinkedHashSet<S>(this.list(returnType));
    return result;
  }



  /**
   * prepare query based on hql
   * @param session hib session
   * @return the query
   */
  private Query attachQueryInfo(Session session) {

    String theHql = this.query;

    QuerySort querySort = this.queryOptions == null ? 
        null : this.queryOptions.getQuerySort();
    if (querySort != null) {
      String sortString = querySort.sortString(false);
      if (!StringUtils.isBlank(sortString)) {
        theHql += " order by " + sortString;
      }
    }

    Query query = session.createQuery(theHql);

    QueryPaging queryPaging = this.queryOptions == null ? 
        null : this.queryOptions.getQueryPaging();
    if (queryPaging != null) {
      query.setFirstResult(queryPaging.getFirstIndexOnPage());
      query.setMaxResults(queryPaging.getPageSize());
    }

    boolean secondLevelCaching = HibUtils.secondLevelCaching(
        this.cacheable, this.queryOptions);
    query.setCacheable(secondLevelCaching);

    String secondLevelCacheRegion = HibUtils.secondLevelCacheRegion(this.cacheRegion, 
        this.queryOptions);
    if (!StringUtils.isBlank(secondLevelCacheRegion)) {
      query.setCacheRegion(secondLevelCacheRegion);
    }

    attachBindValues(query);
    return query;

  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHql setScalar(String bindVarName, Object value) {
    
    if (value instanceof Integer) {
      this.setInteger(bindVarName, (Integer)value);
    } else if (value instanceof String) {
      this.setString(bindVarName, (String)value);
    } else if (value instanceof Long) {
      this.setLong(bindVarName, (Long)value);
    } else if (value instanceof Date) {
      this.setTimestamp(bindVarName, (Date)value);
    } else {
      throw new RuntimeException("Unexpected value: " + value + ", " + (value == null ? null : value.getClass()));
    }
    
    return this;
    }


  /**
   * attach bind values to query
   * @param query
   */
  private void attachBindValues(Query query) {
    //note, dont call the method bindVarNameParams() so it doesnt lazyload...
    if (this.bindVarNameParams != null) {
      for (HibernateParam hibernateParam : this.bindVarNameParams()) {
        
        if (String.class.equals(hibernateParam.getType())) {
          query.setString(hibernateParam.getName(), (String)hibernateParam.getValue());
        } else if (Timestamp.class.equals(hibernateParam.getType())) {
          query.setTimestamp(hibernateParam.getName(), (Date)hibernateParam.getValue());
        } else if (Long.class.equals(hibernateParam.getType())) {
          if (hibernateParam.getValue() == null) {
            query.setBigDecimal(hibernateParam.getName(), null);
          } else {
            query.setLong(hibernateParam.getName(), (Long)hibernateParam.getValue());
          }
        } else if (Integer.class.equals(hibernateParam.getType())) {
          if (hibernateParam.getValue() == null) {
            query.setBigDecimal(hibernateParam.getName(), null);
          } else {
            query.setInteger(hibernateParam.getName(), (Integer)hibernateParam.getValue());
          }
        } else {
          throw new RuntimeException("Invalid bind var type: " 
              + hibernateParam );
        }
      }
    }
  }


  /**
   * @param theHibernateSession
   */
  public ByHql(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }


  
  /**
   * @param bindVarNameParams1 the bindVarNameParams to set
   */
  void setBindVarNameParams(List<HibernateParam> bindVarNameParams1) {
    this.bindVarNameParams = bindVarNameParams1;
  }


  
  /**
   * @param query1 the query to set
   */
  void setQuery(String query1) {
    this.query = query1;
  }


  /**
   * add a paging/sorting/resultSetSize, etc to the query
   * @param queryOptions1
   * @return this for chaining
   */
  public ByHql options(QueryOptions queryOptions1) {
    this.queryOptions = queryOptions1;
    return this;
  }
  
  
  
}
