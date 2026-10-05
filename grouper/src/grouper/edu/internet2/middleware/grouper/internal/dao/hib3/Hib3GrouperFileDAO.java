package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.file.GrouperFile;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO;

public class Hib3GrouperFileDAO implements GrouperFileDAO {

  @SuppressWarnings("unused")
  private static final String KLASS = Hib3ConfigDAO.class.getName();

  /**
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from GrouperFile").executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#findById(String, boolean)
   */
  public GrouperFile findById(String id, boolean exceptionIfNotFound) {
    GrouperFile config = HibernateSession.byHqlStatic()
      .createQuery("from GrouperFile where id = :theId")
      .setString("theId", id).uniqueResult(GrouperFile.class);
    
    if (config == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find config by id: " + id);
    }
    
    return config;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#findByFilePath(String, boolean)
   */
  public GrouperFile findByFilePath(String filePath, boolean exceptionIfNotFound) {
    // file_path has a unique index so this is at most one row
    GrouperFile grouperFile = HibernateSession.byHqlStatic()
      .createQuery("from GrouperFile where filePath = :theFilePath")
      .setString("theFilePath", filePath).uniqueResult(GrouperFile.class);
    
    if (grouperFile == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find file by path: " + filePath);
    }
    
    return grouperFile;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#findIdBySystemNameAndFilePath(String, String)
   */
  public String findIdBySystemNameAndFilePath(String systemName, String filePath) {
    // select just the id so the (possibly large) contents are not loaded or put in the second level cache
    return HibernateSession.byHqlStatic()
      .createQuery("select theFile.id from GrouperFile theFile where theFile.systemName = :theSystemName and theFile.filePath = :theFilePath")
      .setString("theSystemName", systemName)
      .setString("theFilePath", filePath).uniqueResult(String.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#findFileNameById(String)
   */
  public String findFileNameById(String id) {
    // select just the file name so the (possibly large) contents are not loaded
    return HibernateSession.byHqlStatic()
      .createQuery("select theFile.fileName from GrouperFile theFile where theFile.id = :theId")
      .setString("theId", id).uniqueResult(String.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#deleteById(String)
   */
  public int deleteById(String id) {
    // bulk hql delete so the contents are not loaded, hibernate evicts the cache region for bulk deletes
    return HibernateSession.byHqlStatic()
      .createQuery("delete from GrouperFile where id = :theId")
      .setString("theId", id).executeUpdateInt();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#saveOrUpdate(GrouperFile)
   */
  public void saveOrUpdate(GrouperFile grouperFileDao) {
    // GRP-7439: timestamps are set here so every caller (reports, workflow, gsh template downloads) gets them
    long nowMicros = System.currentTimeMillis() * 1000L;

    // make sure updated always moves forward, even if two saves happen in the same millisecond
    Long previousUpdatedOnMicros = grouperFileDao.getUpdatedOnMicros();
    if (previousUpdatedOnMicros != null && nowMicros <= previousUpdatedOnMicros) {
      nowMicros = previousUpdatedOnMicros + 1;
    }

    // created is set once on insert (or on the first save of a row that predates the column), never changed
    if (grouperFileDao.getCreatedOnMicros() == null) {
      grouperFileDao.setCreatedOnMicros(nowMicros);
    }
    grouperFileDao.setUpdatedOnMicros(nowMicros);

    HibernateSession.byObjectStatic().saveOrUpdate(grouperFileDao);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#delete(GrouperFile)
   */
  public void delete(final GrouperFile grouperFile) {
    HibernateSession.byObjectStatic().delete(grouperFile);
  }

}
