/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.util.Collection;


/**
 * Hibernated object which can be imported into.  There can be multiple in the DB
 * based on business key (e.g. milti-assign)
 * @param <T> is the type of the object
 */
public interface XmlImportableMultiple<T> extends XmlImportableBase<T> {

  /**
   * retrieve from db by id or key.  throws exception if duplicate
   * @param idsToIgnore these are ids already processed, do not pick these
   * @return the object or null if not found
   */
  public T xmlRetrieveByIdOrKey(Collection<String> idsToIgnore);
  
}
