/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;


/**
 * Hibernated object which can be imported into 
 * @param <T> is the type of the object
 */
public interface XmlImportable<T> {

  /**
   * retrieve from db by id or key.  throws exception if duplicate
   * @return the object or null if not found
   */
  public T xmlRetrieveByIdOrKey();
  
  /**
   * see if the update cols are different (e.g. last updated)
   * @param other the one to compare with
   * @return true if so
   */
  public boolean xmlDifferentUpdateProperties(T other);
  
  /**
   * see if the non update cols are different (e.g. name)
   * @param other the one to compare with
   * @return true if so
   */
  public boolean xmlDifferentBusinessProperties(T other);

  /**
   * save the business properties (not update properties)
   * @param existingRecord null if insert, the object if exists in DB
   * generally just copy the hibernate version number, and last updated to the
   * object and store it
   */
  public void xmlSaveBusinessProperties(T existingRecord);

  /**
   * save the udpate properties (e.g. last updated).  Note, this is
   * done with a sql update statement, not with hibernate
   */
  public void xmlSaveUpdateProperties();
  
  /**
   * copy business (non update) properties to an existing record
   * @param existingRecord
   */
  public void xmlCopyBusinessPropertiesToExisting(T existingRecord);
  
}
