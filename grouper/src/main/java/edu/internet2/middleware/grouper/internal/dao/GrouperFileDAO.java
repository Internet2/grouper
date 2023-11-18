package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.file.GrouperFile;

public interface GrouperFileDAO extends GrouperDAO {
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the config
   */
  public GrouperFile findById(String id, boolean exceptionIfNotFound);

  /**
   * save the object to the database
   * @param grouperFile
   */
  public void saveOrUpdate(GrouperFile grouperFile);

  /**
   * delete the object from the database
   * @param grouperFile
   */
  public void delete(GrouperFile grouperFile);

}
