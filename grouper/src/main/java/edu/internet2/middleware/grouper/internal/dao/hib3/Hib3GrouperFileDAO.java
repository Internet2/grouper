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
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#saveOrUpdate(GrouperFile)
   */
  public void saveOrUpdate(GrouperFile grouperFileDao) {
    HibernateSession.byObjectStatic().saveOrUpdate(grouperFileDao);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GrouperFileDAO#delete(GrouperFile)
   */
  public void delete(final GrouperFile grouperFile) {
    HibernateSession.byObjectStatic().delete(grouperFile);
  }

}
