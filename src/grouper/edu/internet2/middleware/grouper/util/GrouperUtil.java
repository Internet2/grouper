/**
 * 
 */
package edu.internet2.middleware.grouper.util;

import java.util.Collection;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * utility methods for grouper
 * @author mchyzer
 *
 */
public class GrouperUtil {
  /**
   * fail safe toString for Exception blocks, and include the stack
   * if there is a problem with toString()
   * @param object
   * @return
   */
  public static String toStringSafe(Object object) {
    if (object == null) {
      return null;
    }
    
    try {
      //give size and type if collection
      if (object instanceof Collection) {
        Collection<Object> collection = (Collection<Object>) object;
        int collectionSize = collection.size();
        if (collectionSize == 0) {
          return "Empty " + object.getClass().getSimpleName();
        }
        Object first = collection.iterator().next();
        return object.getClass().getSimpleName() + " of size " 
          + collectionSize + " with first type: " + 
          (first == null ? null : first.getClass());
      }
    
      return object.toString();
    } catch (Exception e) {
      return "<<exception>> " + object.getClass() + ":\n" + ExceptionUtils.getFullStackTrace(e) + "\n";
    }
  }
}
