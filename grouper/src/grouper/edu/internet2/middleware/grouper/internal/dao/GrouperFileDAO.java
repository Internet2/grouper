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
   * find by file path (file_path is unique)
   * @param filePath
   * @param exceptionIfNotFound
   * @return the file or null if not found
   */
  public GrouperFile findByFilePath(String filePath, boolean exceptionIfNotFound);

  /**
   * find the id by system name and file path without loading the contents
   * @param systemName
   * @param filePath
   * @return the id or null if not found
   */
  public String findIdBySystemNameAndFilePath(String systemName, String filePath);

  /**
   * find the file name by id without loading the contents
   * @param id
   * @return the file name or null if not found
   */
  public String findFileNameById(String id);

  /**
   * delete by id without loading the contents
   * @param id
   * @return number of rows deleted
   */
  public int deleteById(String id);

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
