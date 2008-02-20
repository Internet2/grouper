/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.List;

import org.hibernate.Session;

/**
 * @author mchyzer
 *
 */
public class HibUtils {

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
   * @param session from hibernate
   * @param object to evict that was just retrieved
   */
  public static void evict(Session session, Object object) {
    if (object instanceof List) {
      session.evict((List)object);
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
   * @param session from hibernate
   * @param list of objects from hibernate to evict
   */
  public static void evict(Session session, List<Object> list) {
    if (list == null) {
      return;
    }
  }
  
}
