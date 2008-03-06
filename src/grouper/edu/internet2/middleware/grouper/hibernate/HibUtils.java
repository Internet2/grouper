/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class HibUtils {

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
   * @param session from hibernate
   * @param object to evict that was just retrieved, can be list or array
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession, Session session, 
      Object object, boolean onlyEvictIfNotNew) {
    if (object instanceof List) {
      HibUtils.evict(hibernateSession, session, (List)object, onlyEvictIfNotNew);
      return;
    }
    
    //dont worry about it if new and only evicting if not new
    if (hibernateSession != null && hibernateSession.isNewHibernateSession() && onlyEvictIfNotNew) {
      return;
    }
    
    //if array, loop through
    if (object != null && object.getClass().isArray()) {
      for (int i=0;i<Array.getLength(object);i++) {
        HibUtils.evict(hibernateSession, session, Array.get(object, i), onlyEvictIfNotNew);
      }
      return;
    }
    
    //not sure it could ever be null...
    if (object != null) {
      session.evict(object);
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
   * @param session from hibernate
   * @param list of objects from hibernate to evict
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession, Session session, 
      List<Object> list, boolean onlyEvictIfNotNew) {
    if (list == null) {
      return;
    }
    for (Object object : list) {
      evict(hibernateSession, session, object, onlyEvictIfNotNew);
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
  
}
