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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.subject.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Subject;


/**
 * utility methods for subject api
 */
public class SubjectApiUtils {

  /**
   * convert a subject to string safely
   * @param subject
   * @return the string value of subject (might be null)
   */
  public static String subjectToString(Subject subject) {
    if (subject == null) {
      return null;
    }
    try {
      return "Subject id: " + subject.getId() + ", sourceId: " + subject.getSource().getId();
    } catch (RuntimeException e) {
      //might be subject not found if lazy subject
      return subject.toString();
    }
  }

  /**
   * Null safe array length or map
   *
   * @param arrayOrCollection
   * @return the length of the array (0 for null)
   */
  public static int length(Object arrayOrCollection) {
    if (arrayOrCollection == null) {
      return 0;
    }
    if (arrayOrCollection.getClass().isArray()) {
      return Array.getLength(arrayOrCollection);
    }
    if (arrayOrCollection instanceof Collection) {
      return ((Collection) arrayOrCollection).size();
    }
    if (arrayOrCollection instanceof Map) {
      return ((Map) arrayOrCollection).size();
    }
    // simple non array non collection object
    return 1;
  }

  /**
   * return a set of objects from varargs.
   *
   * @param <T> template type of the objects
   * @param objects
   * @return the set
   */
  public static <T> Set<T> toSet(T... objects) {
    if (objects == null) {
      return null;
    }
    Set<T> result = new LinkedHashSet<T>();
    for (T object : objects) {
      result.add(object);
    }
    return result;
  }

  /**
   * if there is no driver class specified, then try to derive it from the URL
   * @param connectionUrl
   * @param driverClassName
   * @return the driver class
   */
  public static String convertUrlToDriverClassIfNeeded(String connectionUrl, String driverClassName) {
    //default some of the stuff
    if (StringUtils.isBlank(driverClassName)) {
      
      if (isHsql(connectionUrl)) {
        driverClassName = "org.hsqldb.jdbcDriver";
      } else if (isMysql(connectionUrl)) {
        driverClassName = "com.mysql.jdbc.Driver";
      } else if (isOracle(connectionUrl)) {
        driverClassName = "oracle.jdbc.driver.OracleDriver";
      } else if (isPostgres(connectionUrl)) { 
        driverClassName = "org.postgresql.Driver";
      } else if (isSQLServer(connectionUrl)) {
        driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
      } else {
        
        //if this is blank we will figure it out later
        if (!StringUtils.isBlank(connectionUrl)) {
        
          String error = "Cannot determine the driver class from database URL: " + connectionUrl;
          System.err.println(error);
          log.error(error);
          return null;
        }
      }
    }
    return driverClassName;
  
  }

  /**
   * 
   */
  private static Log log = LogFactory.getLog(SubjectApiUtils.class);

  /**
   * see if the config file seems to be hsql
   * @param connectionUrl url to check against
   * @return see if hsql
   */
  public static boolean isHsql(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":hsqldb:");
  }

  /**
   * see if the config file seems to be mysql
   * @param connectionUrl
   * @return see if mysql
   */
  public static boolean isMysql(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":mysql:");
  }

  /**
   * see if the config file seems to be oracle
   * @param connectionUrl
   * @return see if oracle
   */
  public static boolean isOracle(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":oracle:");
  }

  /**
   * see if the config file seems to be postgres
   * @param connectionUrl
   * @return see if postgres
   */
  public static boolean isPostgres(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":postgresql:");
  }

  /**
   * see if the config file seems to be sql server
   * @param connectionUrl
   * @return see if sql server
   */
  public static boolean isSQLServer(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":sqlserver:");
  }

  /**
     * retrieve a batch by 0 index. Will return an array of size batchSize or
     * the remainder. the array will be full of elements. Note, this requires an
     * ordered input (so use linkedhashset not hashset if doing sets)
     * @param <T> template type
     * @param collection
     * @param batchSize
     * @param batchIndex
     * @return the list
     *         This never returns null, only empty list
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> batchList(List<T> collection, int batchSize,
        int batchIndex) {
  
      int numberOfBatches = batchNumberOfBatches(collection, batchSize);
      int arraySize = length(collection);
  
      // short circuit
      if (arraySize == 0) {
        return new ArrayList<T>();
      }
  
      List<T> theBatchObjects = new ArrayList<T>();
  
      // lets get the type of the first element if possible
  //    Object first = get(arrayOrCollection, 0);
  //
  //    Class theType = first == null ? Object.class : first.getClass();
  
      // if last batch
      if (batchIndex == numberOfBatches - 1) {
  
        // needs to work to 1-n
        //int thisBatchSize = 1 + ((arraySize - 1) % batchSize);
  
        int collectionIndex = 0;
        for (T t : collection) {
          if (collectionIndex++ < batchIndex * batchSize) {
            continue;
          }
          //just copy the rest
          //if (collectionIndex >= (batchIndex * batchSize) + arraySize) {
          //  break;
          //}
          //we are in the copy mode
          theBatchObjects.add(t);
        }
  
      } else {
        // if non-last batch
        //int newIndex = 0;
        int collectionIndex = 0;
        for (T t : collection) {
          if (collectionIndex < batchIndex * batchSize) {
            collectionIndex++;
            continue;
          }
          //done with batch
          if (collectionIndex >= (batchIndex + 1) * batchSize) {
            break;
          }
          theBatchObjects.add(t);
          collectionIndex++;
        }
      }
      return theBatchObjects;
    }

  /**
   * If batching this is the number of batches
   * @param collection
   * @param batchSize
   * @return the number of batches
   */
  public static int batchNumberOfBatches(Collection<?> collection, int batchSize) {
    int arrraySize = length(collection);
    return batchNumberOfBatches(arrraySize, batchSize);
  
  }

  /**
   * If batching this is the number of batches
   * @param count is size of set
   * @param batchSize
   * @return the number of batches
   */
  public static int batchNumberOfBatches(int count, int batchSize) {
    //not sure why this would be 0...
    if (batchSize == 0) {
      return 0;
    }
    int batches = 1 + ((count - 1) / batchSize);
    return batches;
  
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
   * make sure a collection is non null.  If null, then return an empty list
   * @param <T>
   * @param list
   * @return the list or empty list if null
   */
  public static <T> Collection<T> nonNull(Collection<T> list) {
    return list == null ? new ArrayList<T>() : list;
  }

  /**
   * make sure a list is non null.  If null, then return an empty list
   * @param <T>
   * @param list
   * @return the list or empty list if null
   */
  public static <T> List<T> nonNull(List<T> list) {
    return list == null ? new ArrayList<T>() : list;
  }

  /**
   * make sure it is non null, if null, then give new map
   *
   * @param <K> key of map
   * @param <V> value of map
   * @param map is map
   * @return set non null
   */
  public static <K,V> Map<K,V> nonNull(Map<K,V> map) {
    return map == null ? new HashMap<K,V>() : map;
  }

  /**
   * make sure a list is non null.  If null, then return an empty set
   * @param <T>
   * @param set
   * @return the set or empty set if null
   */
  public static <T> Set<T> nonNull(Set<T> set) {
    return set == null ? new HashSet<T>() : set;
  }

  /**
   * make sure a array is non null.  If null, then return an empty array.
   * @param <T>
   * @param array
   * @param theClass to make array from
   * @return the list or empty list if null
   */
  @SuppressWarnings({ "unchecked", "cast" })
  public static <T> T[] nonNull(T[] array, Class<?> theClass) {
    if (int.class.equals(theClass)) {
      return (T[])(Object)new int[0];
    }
    if (float.class.equals(theClass)) {
      return (T[])(Object)new float[0];
    }
    if (double.class.equals(theClass)) {
      return (T[])(Object)new double[0];
    }
    if (short.class.equals(theClass)) {
      return (T[])(Object)new short[0];
    }
    if (long.class.equals(theClass)) {
      return (T[])(Object)new long[0];
    }
    if (byte.class.equals(theClass)) {
      return (T[])(Object)new byte[0];
    }
    if (boolean.class.equals(theClass)) {
      return (T[])(Object)new boolean[0];
    }
    if (char.class.equals(theClass)) {
      return (T[])(Object)new char[0];
    }
    return array == null ? ((T[])Array.newInstance(theClass, 0)) : array;
  }

}
