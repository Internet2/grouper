/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;


/**
 * Hibernated object which can be imported into 
 * @param <T> is the type of the object
 */
public interface XmlImportable<T> extends XmlImportableBase<T> {

  /**
   * retrieve from db by id or key.  throws exception if duplicate
   * @return the object or null if not found
   */
  public XmlImportable<T> xmlRetrieveByIdOrKey();
  
}
