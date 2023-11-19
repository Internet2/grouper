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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.type.ByteType;
import org.hibernate.type.CharacterType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.ObjectType;
import org.hibernate.type.ShortType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.TrueFalseType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * @author mchyzer
 *
 */
public class HibUtils {

  /**
   * attach bind values to query
   * @param query
   */
  public static void attachBindValues(Query query, List<HibernateParam> bindVarNameParams) {
    //note, dont call the method bindVarNameParams() so it doesnt lazyload...
    if (bindVarNameParams != null) {
      for (HibernateParam hibernateParam : bindVarNameParams) {
        
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
        } else if (Double.class.equals(hibernateParam.getType())) {
          if (hibernateParam.getValue() == null) {
            query.setBigDecimal(hibernateParam.getName(), null);
          } else {
            query.setDouble(hibernateParam.getName(), (Double)hibernateParam.getValue());
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
   * Whether the type of the class means that it should be handled as a primitive
   * query vs a hibernate object query.
   * @param clazz is the class to check.
   * @return true if so.
   */
  static boolean handleAsPrimitive(Class clazz) {
    
    if (clazz.isArray()) {
      clazz = clazz.getComponentType();
      
      if (clazz == Object.class) {
        return true;
      }
    }
    
    if (clazz.isPrimitive() || clazz == java.lang.String.class
        || clazz == BigDecimal.class || clazz == Integer.class
        || clazz == java.sql.Date.class || clazz == java.util.Date.class
        || clazz == java.sql.Time.class || clazz == java.sql.Timestamp.class
        || clazz == java.sql.Clob.class || clazz == java.sql.Blob.class
        || clazz == java.sql.Ref.class || clazz == Boolean.class || clazz == Byte.class
        || clazz == Short.class || clazz == Integer.class || clazz == Float.class
        || clazz == Double.class || clazz == Long.class) {
      return true;
    }
    return false;
  }

  /**
   * Convert the params to friendly strings
   * 
   * @param params
   * @param types
   * @return the string of the params (for logging)
   */
  @SuppressWarnings("unchecked")
  public static String paramsToString(Object params, Object types) {
    //nothing to do if nothing to do
    if (params == null && types == null) {
      return "null";
    }

    List<Object> paramsList = GrouperUtil.toList(params);
    List<Type> typesList = (List<Type>)(Object)GrouperUtil.toList(types);

    int paramLength = GrouperUtil.length(paramsList);
    int typeLength = GrouperUtil.length(typesList);

    if (paramLength != typeLength) {
      throw new RuntimeException("The params length " + paramLength
          + " must equal the types length " + typeLength);
    }
    StringBuilder result = new StringBuilder();
    //loop through, set the params
    Type currentType = null;
    for (int i = 0; i < paramLength; i++) {
      try {
        currentType = typesList.get(i);
        result.append(currentType.toLoggableString(paramsList.get(i), null)).append(",");
      } catch (HibernateException he) {
        result.append("<error>");
      }
    }

    return result.toString();
  }

  /**
   * Parses an alias out of a sql query.
   * @param text is the text to find the alias in.
   * @param exceptionIfNotFound if true, when an alias is not found, and exception will be thrown.
   * @return the alias.
   */
  public static String parseAlias(String text, boolean exceptionIfNotFound) {
    Pattern pattern = Pattern.compile("\\{.*?\\}");
    Matcher matcher = pattern.matcher(text);
    String alias = null;
    if (matcher.find()) {
      alias = matcher.group();
      alias = StringUtils.replace(alias, "{", "");
      alias = StringUtils.replace(alias, "}", "");
      if (alias != null && alias.indexOf(".") > -1) {
        alias = alias.substring(0, alias.indexOf("."));
      }
      return alias;
    }

    if (exceptionIfNotFound) {
      throw new RuntimeException("Cannot find a sql alias in the text: " + text
          + "An alias must be in the format {anything} or {anything.anything}. "
          + "Each sql query must contain at least one alias.");
    }
    return null;
  }

  /**
   * if in an hql or sql query, depending on the value, pass is or = back
   * @param value
   * @param bindVar 
   * @return the query comparator
   */
  public static String equalsOrIs(Object value, String bindVar) {
    //if sent with colon, remove
    if (!StringUtils.isBlank(bindVar) && bindVar.startsWith(":")) {
      bindVar = bindVar.substring(1);
    }
    return value == null ? " is null " : (" = :" + bindVar + " ");
  }

  /**
   * if should allow cache
   */
  private static InheritableThreadLocal<Boolean> cachingEnabledThreadLocal = new InheritableThreadLocal<Boolean>();

  /**
   * if should disallow cache
   * <pre>
   *  boolean needsUnassignment = HibUtils.assignDisallowCacheThreadLocal();
   *  Set<AttributeAssign> attributeAssigns = null;
   *  try {
   *    attributeAssigns = retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
   *  } finally {
   *    if (needsUnassignment) {
   *      HibUtils.clearDisallowCacheThreadLocal();
   *    }
   *  }
   * 
   * </pre>
   * @return if assigned
   */
  public static boolean assignDisallowCacheThreadLocal() {
    Boolean caching = cachingEnabledThreadLocal.get();
    if (caching != null && !caching) {
      return false;
    }
    cachingEnabledThreadLocal.set(Boolean.FALSE);
    return true;
  }

  /**
   * dont disallow cache anymore
   */
  public static void clearDisallowCacheThreadLocal() {
    cachingEnabledThreadLocal.remove();
  }
  
  /**
   * 
   * @param cacheable
   * @param queryOptions
   * @return if caching
   */
  public static boolean secondLevelCaching(Boolean cacheable, QueryOptions queryOptions) {

    {
      //if set in threadlocal then dont cache
      Boolean cachingEnabled = cachingEnabledThreadLocal.get();
      if (cachingEnabled != null && !cachingEnabled) {
        return false;
      }
      
    }
    
    HibernateSession hibernateSession = HibernateSession._internal_hibernateSession();
    
    //if hibernate session says no, then no
    if (hibernateSession != null && !hibernateSession.isCachingEnabled()) {
      return false;
    }
    
    //cant find answer
    if (cacheable == null && (queryOptions == null || queryOptions.getSecondLevelCache() == null)) {
      return false;
    }
    
    //if no options, but has cacheable
    if (queryOptions == null || queryOptions.getSecondLevelCache() == null) {
      return cacheable;
    }

    //this one trumps all if not null
    return queryOptions.getSecondLevelCache();
  }

  /**
   * 
   * @param cacheRegion
   * @param queryOptions
   * @return if caching
   */
  public static String secondLevelCacheRegion(String cacheRegion, QueryOptions queryOptions) {
    if (StringUtils.isBlank(cacheRegion) && (queryOptions == null || StringUtils.isBlank(queryOptions.getSecondLevelCacheRegion()))) {
      return null;
    }
    //if no options, but has cacheable
    if (queryOptions == null || StringUtils.isBlank(queryOptions.getSecondLevelCacheRegion())) {
      return cacheRegion;
    }
    //this one trumps all if not null
    return queryOptions.getSecondLevelCacheRegion();
    
  }

  /**
   * pattern to detect if a query starts with "from".  e.g. from Field
   */
  private static Pattern startsWithFrom = Pattern.compile("^\\s*from.*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  
  /**
   * pattern to detect if a query has a select and "from".  e.g. select field from Field field
   */
  private static Pattern hasSelectAndFrom = Pattern.compile("^\\s*select(.*?)(from.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  
  /**
   * convert an hql to a count hql
   * @param hql
   * @return the hql of the count query
   */
  public static String convertHqlToCountHql(String hql) {
    
    if (startsWithFrom.matcher(hql).matches()) {
      return "select count(*) " + hql;
    }
    Matcher selectAndFromMatcher = hasSelectAndFrom.matcher(hql);
    if (selectAndFromMatcher.matches()) {
      String selectPart = selectAndFromMatcher.group(1);
      String endOfQuery = selectAndFromMatcher.group(2);
      // Caused by: org.hibernate.hql.internal.ast.QuerySyntaxException: expecting CLOSE, found ',' near line 1, column 16 [select count(aa,theGroup)
      if (selectPart != null && selectPart.contains(",")) {
        return "select count(*) " + endOfQuery;
      }
      return "select count( " + selectPart + " ) " + endOfQuery;
    }
    throw new RuntimeException("Cant convert query to count query: " + hql);
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(HibUtils.class);

  /**
   * 
   * @param scrollableResults
   */
  public static void closeQuietly(ScrollableResults scrollableResults) {
    if (scrollableResults != null) {
      try {
        scrollableResults.close();
      } catch (Exception e) {
        //just log, something bad is happening
        LOG.info("Problem closing scrollable results", e);
      }
    }
  }
  
  /**
   * find the property index based on property name
   * @param propertyNames
   * @param propertyName e.g. userId
   * @return the index (0 based) in the data arrays where the object is
   */
  public static int propertyIndex(String[] propertyNames, String propertyName) {
    int propertiesSize = GrouperUtil.length(propertyNames);
    for (int i=0;i<propertiesSize;i++) {
      if (StringUtils.equals(propertyNames[i], propertyName)) {
        return i;
      }
    }
    throw new RuntimeException("Cant find property: " + propertyName 
        + " in list: " + GrouperUtil.toStringForLog(propertyNames));
  }

  /**
   * assign a property in hibernates arrays of states
   * @param state
   * @param propertyNames
   * @param propertyName
   * @param propertyValue
   */
  public static void assignProperty(Object[] state, String[] propertyNames, 
      String propertyName, Object propertyValue) {
    //first find which index
    int propertyIndex = propertyIndex(propertyNames, propertyName);
    //next assign the value
    state[propertyIndex] = propertyValue;
  }
  
  /**
   * find a property value in hibernates arrays of states
   * @param state
   * @param propertyNames
   * @param propertyName
   * @return the object
   */
  public static Object propertyValue(Object[] state, String[] propertyNames, 
      String propertyName) {
    //first find which index
    int propertyIndex = propertyIndex(propertyNames, propertyName);
    //next get the value
    return state[propertyIndex];
  }
  
  /**
   * close a prepared statement
   * @param preparedStatement
   */
  public static void closeQuietly(PreparedStatement preparedStatement) {
    try {
      if (preparedStatement != null) {
        preparedStatement.close();
      }
    } catch (Exception e) {
      //forget about it
    }
  }

  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession, can be null if not known
   * @param object to evict that was just retrieved, can be list or array
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession,
      Object object, boolean onlyEvictIfNotNew) {
    evict(hibernateSession, object, onlyEvictIfNotNew, true);
  }
  
  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession, can be null if not known
   * @param object to evict that was just retrieved, can be list or array
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   * @param evictBeforeFlush if evict before flush (dont do this if iterating through list)
   */
  public static void evict(HibernateSession hibernateSession,
      Object object, boolean onlyEvictIfNotNew, boolean evictBeforeFlush) {
    if (object instanceof Collection) {
      HibUtils.evict(hibernateSession, (Collection)object, onlyEvictIfNotNew);
      return;
    }
    
    //dont worry about it if new and only evicting if not new
    if (hibernateSession != null && hibernateSession.isNewHibernateSession() && onlyEvictIfNotNew) {
      return;
    }
    
    //if array, loop through
    if (object != null && object.getClass().isArray()) {
      if (evictBeforeFlush) {
        hibernateSession.getSession().flush();
      }
      for (int i=0;i<Array.getLength(object);i++) {
        HibUtils.evict(hibernateSession, Array.get(object, i), onlyEvictIfNotNew, false);
      }
      return;
    }
    
    //not sure it could ever be null...
    if (object != null) {
      if (evictBeforeFlush) {
        hibernateSession.getSession().flush();
      }
      
      try {
        hibernateSession.getSession().evict(object);
      } catch (IllegalArgumentException e) {
        // ignore
        
        // In hibernate 5, seems to sometimes throw:
        // java.lang.IllegalArgumentException: Non-entity object instance passed to evict : cd620430b6204390be5e55e375a5cdb7
        // at org.hibernate.event.internal.DefaultEvictEventListener.onEvict(DefaultEvictEventListener.java:95)
        
        // comment in hibernate 5 code:
        
        // see if the passed object is even an entity, and if not throw an exception
        //              this is different than legacy Hibernate behavior, but what JPA 2.1 is calling for
        //              with EntityManager.detach

        // TODO need to look at later
      }
    }
  }
  
  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession
   * @param list of objects from hibernate to evict
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession,
      Collection<Object> list, boolean onlyEvictIfNotNew) {
    if (list == null) {
      return;
    }
    hibernateSession.getSession().flush();
    for (Object object : list) {
      evict(hibernateSession, object, onlyEvictIfNotNew, false);
    }
  }

  /** 
   * Logger
   */
  //private static FastLogger log = new FastLogger(HibUtils.class);
  
  /**
   * make a list of criterions.  e.g. listCrit(crit1, crit2, etc).  will AND them together
   * this is null and empty safe
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  public static Criterion listCrit(Criterion... criterions) {
    return listCritHelper(Restrictions.conjunction(), criterions);
  }

  /**
   * make a list of criterions.  e.g. listCrit(critList).  will AND them together
   * this is null and empty safe
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  public static Criterion listCrit(List<Criterion> criterions) {
    return listCritHelper(Restrictions.conjunction(), GrouperUtil.toArray(criterions, Criterion.class));
  }

  /**
   * make a list of criterions.  e.g. listCrit(crit1, crit2, etc).  will OR them together
   * this is null and empty safe
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  public static Criterion listCritOr(Criterion... criterions) {
    return listCritHelper(Restrictions.disjunction(), criterions);
  }

  /**
   * make a list of criterions.  e.g. listCrit(crits).  will OR them together
   * this is null and empty safe
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  public static Criterion listCritOr(List<Criterion> criterions) {
    return listCritHelper(Restrictions.disjunction(), GrouperUtil.toArray(criterions, Criterion.class));
  }

  /**
   * make a list of criterions.  e.g. listCrit(crit1, crit2, etc).  will AND or OR them together
   * this is null and empty safe
   * @param junction either conjunction or disjunction
   * @param criterions
   * @return the criterion containing the list or null if none passed in
   */
  private static Criterion listCritHelper(Junction junction, Criterion... criterions) {
    int criterionsLength = GrouperUtil.length(criterions);
    if (criterionsLength == 0) {
      return null;
    }
    
    //if one no need for junction
    if (criterionsLength == 1 && criterions[0] != null) {
      return criterions[0];
    }
    
    //count to see if any added
    int resultsCount = 0;
    for (int i=0;i<criterionsLength;i++) {
      Criterion current = criterions[i];
      if (current != null) {
        resultsCount++;
        junction.add(current);
      }
    }
    if (resultsCount == 0) {
      return null;
    }
    return junction;
    
  }
  
  /**
   * close a connection null safe and dont throw exception
   * @param connection
   */
  public static void closeQuietly(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * close a resultSet null safe and dont throw exception
   * @param resultSet
   */
  public static void closeQuietly(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * close a session null safe and dont throw exception
   * @param session
   */
  public static void closeQuietly(Session session) {
    if (session != null) {
      try {
        session.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * close a statement null safe and dont throw exception
   * @param statement
   */
  public static void closeQuietly(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * rollback a connection quietly
   * @param connection
   */
  public static void rollbackQuietly(Connection connection) {
    if (connection != null) {
      try {
        connection.rollback();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * rollback a transaction quietly
   * @param transaction
   */
  public static void rollbackQuietly(Transaction transaction) {
    if (transaction != null && transaction.getStatus().isOneOf(TransactionStatus.ACTIVE)) {
      try {
        transaction.rollback();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * Attach params for a prepared statement.  The type of the params and types must be the
   * same (e.g. either both array or list, but not one is Array, and the other list
   * @param statement
   * @param params either null, Object, Object[], or List of Objects
   * @throws HibernateException
   * @throws SQLException
   * @deprecated doesnt work with postgres
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public static void attachParams(PreparedStatement statement, Object params)
      throws HibernateException, SQLException {
    if (GrouperUtil.length(params) == 0) {
      return;
    }
    List<Object> paramList = listObject(params);
    List<Type> typeList = hibernateTypes(paramList);
    attachParams(statement, paramList, typeList);
  }

  /**
   * Attach params for a prepared statement.  The type of the params and types must be the
   * same (e.g. either both array or list, but not one is Array, and the other list
   * @param statement
   * @param params either null, Object, Object[], or List of Objects
   * @param types either null, Type, Type[], or List of Objects
   * @throws HibernateException
   * @throws SQLException
   */
  static void attachParams(PreparedStatement statement, Object params, Object types)
      throws HibernateException, SQLException {
    int paramLength = GrouperUtil.length(params);
    int typeLength = GrouperUtil.length(types);
    
    //nothing to do if nothing to do
    if (paramLength == 0 && typeLength == 0) {
      return;
    }
  
      if (paramLength != typeLength) {
      throw new RuntimeException("The params length must equal the types length and params " +
      "and types must either both or neither be null");
      }
    
      List paramList = listObject(params);
      List typeList = listObject(types);
  
      //loop through, set the params
      Type currentType = null;
      for (int i = 0; i < paramLength; i++) {
        //not sure why the session implementer is null, if this ever fails for a type, 
        //might want to not use hibernate and brute force it
        currentType = (Type) typeList.get(i);
        currentType.nullSafeSet(statement, paramList.get(i), i + 1, null);
      }
  
  }

  /**
   * convert an object to a list of objects
   * @param object
   * @return the list of objects
   */
  @SuppressWarnings("unchecked")
  public static List<Object> listObject(Object object) {
    
    //if its already a list then we are all good
    if (object instanceof List) {
      return (List<Object>)object; 
    }
    
    return GrouperUtil.toList(object);
  
  }

  /**
   * convert types to a list of types
   *       return ObjectType.INSTANCE;
   * DoubleType.INSTANCE, LongType.INSTANCE, FloatType.INSTANCE, ByteType.INSTANCE, TrueFalseType.INSTANCE, 
   * CharacterType.INSTANCE, ShortType.INSTANCE, DateType.INSTANCE, TimestampType.INSTANCE, StringType.INSTANCE;
   * @param types
   * @return the list of objects
   */
  @SuppressWarnings("unchecked")
  public static List<Type> listType(Type... types) {
    
    if (types == null) {
      return null;
    }
    
    ArrayList<Type> typeList = new ArrayList<Type>();
    
    for(Type type : types) {
      typeList.add(type);
    }
    
    return typeList;
  
  }

  /**
   * Returns a Hibernate Type for the given java type. Handles both primitives and Objects.
   * Will throw an exception if the given object is null or if a type cannot be found for it.
   * @param o is the object to find the Type for.
   * @return the Type.
   * @deprecated doesnt work with postgres
   */
  @Deprecated
  public static Type hibernateType(Object o) {
    if (o == null) {
      //its possible to bind null (e.g. for an update), so just use object to do this
      return ObjectType.INSTANCE;
    }
    Class clazz = o.getClass();
  
    if (clazz == int.class || o instanceof Integer) {
      return IntegerType.INSTANCE;
    } else if (clazz == double.class || clazz == Double.class) {
      return DoubleType.INSTANCE;
    } else if (clazz == long.class || clazz == Long.class) {
      return LongType.INSTANCE;
    } else if (clazz == float.class || clazz == Float.class) {
      return FloatType.INSTANCE;
    } else if (clazz == byte.class || clazz == Byte.class) {
      return ByteType.INSTANCE;
    } else if (clazz == boolean.class || clazz == Boolean.class) {
      return TrueFalseType.INSTANCE;
    } else if (clazz == char.class || clazz == Character.class) {
      return CharacterType.INSTANCE;
    } else if (clazz == short.class || clazz == Short.class) {
      return ShortType.INSTANCE;
    } else if (clazz == java.util.Date.class || clazz == java.sql.Date.class) {
      //return Hibernate.TIMESTAMP;
      return DateType.INSTANCE;
    } else if (clazz == Timestamp.class) {
      return TimestampType.INSTANCE;
    } else if (clazz == String.class) {
      return StringType.INSTANCE;
    }
    throw new RuntimeException(
        "Cannot find a hibernate type to associate with java type " + clazz);
  }
  
  /**
   * Returns a list of Hibernate types corresponding to the given params.
   * @param params are the objects to get the types for. Can be list, Object, or array.
   * @return the corresponding types.
   * @deprecated doesnt work with postgres
   */
  @Deprecated
  public static List<Type> hibernateTypes(List<Object> params) {
  
    int length = GrouperUtil.length(params);
  
    //if null, make sure the same (or exception later)
    if (length == 0) {
      return null;
    }
  
    // Get the types.
    // Create a list of hibernate types.
    List<Type> types = new ArrayList<Type>();
  
    for (int i = 0; i < length; i++) {
      Object o = params.get(i);
      types.add(hibernateType(o));
    }
    return types;
  }

  /**
   * convert a collection of strings (no parens) to an in clause
   * @param collection
   * @param scalarable to set the string
   * @return the string of in clause (without parens)
   */
  public static String convertToInClause(Collection<String> collection, HqlQuery scalarable) {
    
    String unique = GrouperUtil.uniqueId();
    
    StringBuilder result = new StringBuilder();
    int collectionSize = collection.size();
    int i = 0;
    for (String string : collection) {
      String var = unique + i;
      result.append(":" + var);

      //add to query
      scalarable.setString(var, string);
      if (i < collectionSize-1) {
        result.append(", ");
      }
      i++;
    }
    return result.toString();
  }
  
  /**
   * convert a collection of strings (no parens) to an in clause
   * @param collection
   * @param scalarable to set the string
   * @return the string of in clause (without parens)
   */
  public static String convertToInClauseAnyType(Collection<?> collection, HqlQuery scalarable) {
    
    String unique = GrouperUtil.uniqueId();
    
    StringBuilder result = new StringBuilder();
    int collectionSize = collection.size();
    int i = 0;
    for (Object object : collection) {
      String var = unique + i;
      result.append(":" + var);

      //add to query
      scalarable.setScalar(var, object);
      if (i < collectionSize-1) {
        result.append(", ");
      }
      i++;
    }
    return result.toString();
  }
  
  /**
   * @param memberAlias is the alias of the table for members, e.g. gm
   * @param subjects collection of subjects
   * @param hqlQuery so far
   * @return the query, e.g.  ((gm.subject_id = '123' and gm.subject_source = 'jdbc') 
   *   or (gm.subject_id = '234' and gm.subject_source = 'jdbc' ))
   */
  public static String convertToSubjectInClause(Collection<Subject> subjects, HqlQuery hqlQuery, String memberAlias) {
    
    //    ((gm.subject_id = '123' and gm.subject_source = 'jdbc') 
    //      or (gm.subject_id = '234' and gm.subject_source = 'jdbc' ))
    
    String unique = GrouperUtil.uniqueId();
    
    StringBuilder result = new StringBuilder(" ( ");
    int collectionSize = subjects.size();
    int i = 0;
    for (Subject subject : subjects) {
      String subjectVar = unique + "_subj" + i;
      String sourceVar = unique + "_source" + i;
      result.append(" ( ").append(memberAlias).append(".subjectIdDb = :").append(subjectVar);
      result.append(" and ").append(memberAlias).append(".subjectSourceIdDb = :").append(sourceVar).append(" ) ");

      //add to query
      hqlQuery.setString(subjectVar, subject.getId());
      hqlQuery.setString(sourceVar, subject.getSourceId());
      if (i < collectionSize-1) {
        result.append("\n or ");
      }
      i++;
    }
    result.append(" ) ");
    return result.toString();

    
  }

  /**
   * escape the quotes from sql string
   * @param input
   * @return the escaped string
   */
  public static String escapeSqlString(String input) {
    if (input == null) {
      return input;
    }
    
    return StringUtils.replace(input, "'", "''");
    
  }
  
  /**
   * convert a collection of strings (no parens) to an in clause
   * @param collection
   * @return the string of in clause (without parens)
   */
  public static String convertToInClauseForSqlStatic(Collection<String> collection) {
    
    StringBuilder result = new StringBuilder();
    int collectionSize = collection.size();
    for (int i = 0; i < collectionSize; i++) {
      result.append("?");

      if (i < collectionSize - 1) {
        result.append(", ");
      }
    }
    return result.toString();
  }


  /**
   * Creates a Hibernate Criterion that breaks a (possibly very long) list of values into
   * an ORed series of IN statements, respecting the IN-statement-size-limit provided.
   *
   * See: https://thephilosophicalfacts.wordpress.com/2015/06/22/hibernate-criteria-list-size-is-more-than-1000-values/
   *
   * @param propertyName
   * @param values
   * @param inStatementSizeLimit
   * @return
   */
  public static Criterion buildInCriterion(String propertyName, List<?> values, int inStatementSizeLimit) {
    Criterion criterion = null;
    int listSize = values.size();
    for (int i = 0; i < listSize; i += inStatementSizeLimit) {
      List<?> subList;
      if (listSize > i + inStatementSizeLimit) {
        subList = values.subList(i, (i + inStatementSizeLimit));
      } else {
        subList = values.subList(i, listSize);
      }
      if (criterion != null) {
        criterion = Restrictions.or(criterion, Restrictions.in(propertyName, subList));
      } else {
        criterion = Restrictions.in(propertyName, subList);
      }
    }
    return criterion;
  }


  /**
   * convert a collection of multikeys to an in clause with multiple args.  currently this only
   * works with strings, though we could add support for more types in future
   * @param collection
   * @param scalarable to set the string
   * @param columnNames names of columns in multikey
   * @param whereClause
   */
  public static void convertToMultiKeyInClause(Collection<MultiKey> collection, HqlQuery scalarable, 
      Collection<String> columnNames, StringBuilder whereClause) {
    
    String unique = GrouperUtil.uniqueId();
    
    int collectionSize = collection.size();
    int columnNamesSize = columnNames.size();
    int i = 0;
    
    if (GrouperUtil.length(collection) == 0) {
      return;
    }
    
    whereClause.append(" and ( ");
    
    for (MultiKey multiKey : collection) {
      
      whereClause.append(" ( ");
      
      int j = 0;
      
      for (String columnName : columnNames) {

        String var = unique + i + "_" + j;

        whereClause.append(" ").append(columnName).append(" = :").append(var).append(" ");
        
        //add to query
        scalarable.setString(var, (String)multiKey.getKey(j));

        if (j < columnNamesSize-1) {
          whereClause.append(" and ");
        }
        j++;

      }
      
      whereClause.append(" ) ");

      if (i < collectionSize-1) {
        whereClause.append(" or ");
      }
      i++;
    }
    whereClause.append(" ) ");
  }
  
  /**
   * @param fields
   * @param hqlQuery 
   * @param sql before the and statement
   * @param fieldColumnName 
   */
  public static void convertFieldsToSqlInString(Collection<Field> fields, 
      HqlQuery hqlQuery, StringBuilder sql, String fieldColumnName) {

    if (GrouperUtil.length(fields) > 0) {
      if (GrouperUtil.length(fields) == 1) {
        String bindVar = "fieldId_" + GrouperUtil.uniqueId();
        sql.append(" and ").append(fieldColumnName).append(" = :").append(bindVar).append(" ");
        hqlQuery.setString(bindVar, fields.iterator().next().getUuid());
      } else {
        sql.append(" and ").append(fieldColumnName).append(" in ( ");
        Set<String> fieldIds = new HashSet<String>();
        for (Field field : fields) {
          fieldIds.add(field.getUuid());
        }
        String inClause = HibUtils.convertToInClause(fieldIds, hqlQuery);
        sql.append(inClause).append(" ) ");
        
      }
    }
  }      
  
  /**
   * @param sources
   * @param hqlQuery 
   * @param sql before the and statement
   * @param sourceColumnName 
   */
  public static void convertSourcesToSqlInString(Set<Source> sources, 
      HqlQuery hqlQuery, StringBuilder sql, String sourceColumnName) {
    if (sources == null || sources.size() == 0) {
      return;
    }
    
    //simplify if 1
    if (sources.size() == 1) {
      String bindVar = "sourceId_" + GrouperUtil.uniqueId();
      sql.append(" and ").append(sourceColumnName).append(" = :").append(bindVar).append(" ");
      hqlQuery.setString(bindVar, sources.iterator().next().getId());
      return;
    }
    
    List<String> sourcesStrings = new ArrayList<String>();
    for (Source source : sources) {
      sourcesStrings.add(source.getId());
    }
    
    String questions = HibUtils.convertToInClause(sourcesStrings, hqlQuery);
    
    sql.append(" and ").append(sourceColumnName).append(" in ( ").append(questions).append(" ) ");
    
  }
  
  /**
   * e.g. ('g:gsa', 'jdbc')
   * @param sources
   * @return the in string, of sources sorted alphabetically
   */
  public static String convertSourcesToSqlInString(Set<Source> sources) {
    if (sources == null || sources.size() == 0) {
      return null;
    }
    
    //simplify if 1
    if (sources.size() == 1) {
      return " ('" + sources.iterator().next().getId() + "') ";
    }
    
    List<String> sourcesStrings = new ArrayList<String>();
    for (Source source : sources) {
      sourcesStrings.add(source.getId());
    }
    
    //sort 
    Collections.sort(sourcesStrings);
    
    StringBuilder result = new StringBuilder();
    result.append(" (");
    for (int i=0;i<sourcesStrings.size();i++) {
      result.append("'").append(sourcesStrings.get(i)).append("'");
      if (i != sourcesStrings.size()-1) {
        result.append(", ");
      }
    }
    result.append(") ");
    return result.toString();
  }
  

}
