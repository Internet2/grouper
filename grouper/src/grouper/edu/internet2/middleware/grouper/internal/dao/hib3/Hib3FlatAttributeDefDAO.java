package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.flat.FlatAttributeDef;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FlatAttributeDefDAO;

/**
 * @author shilen
 * $Id$
 */
public class Hib3FlatAttributeDefDAO extends Hib3DAO implements FlatAttributeDefDAO {

  /**
   *
   */
  private static final String KLASS = Hib3FlatAttributeDefDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatAttributeDefDAO#save(edu.internet2.middleware.grouper.flat.FlatAttributeDef)
   */
  public void save(FlatAttributeDef flatAttributeDef) {
    HibernateSession.byObjectStatic().save(flatAttributeDef);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatAttributeDefDAO#delete(edu.internet2.middleware.grouper.flat.FlatAttributeDef)
   */
  public void delete(FlatAttributeDef flatAttributeDef) {
    HibernateSession.byObjectStatic().delete(flatAttributeDef);
  }
  
  public void saveBatch(Set<FlatAttributeDef> flatAttributeDefs) {
    HibernateSession.byObjectStatic().saveBatch(flatAttributeDefs);
  }
  
  /**
   * reset flat attribute def 
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from FlatAttributeDef").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatAttributeDefDAO#findById(java.lang.String)
   */
  public FlatAttributeDef findById(String flatAttributeDefId) {
    FlatAttributeDef flatAttributeDef = HibernateSession
      .byHqlStatic()
      .createQuery("select flatAttributeDef from FlatAttributeDef as flatAttributeDef where flatAttributeDef.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", flatAttributeDefId)
      .uniqueResult(FlatAttributeDef.class);
    
    return flatAttributeDef;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatAttributeDefDAO#removeAttributeDefForeignKey(java.lang.String)
   */
  public void removeAttributeDefForeignKey(String flatAttributeDefId) {
    HibernateSession.byHqlStatic()
      .createQuery("update FlatAttributeDef set attributeDefId = null where id = :id")
      .setString("id", flatAttributeDefId)
      .executeUpdate();
  }

  public Set<AttributeDef> findMissingFlatAttributeDefs() {
    Set<AttributeDef> attrDefs = HibernateSession
      .byHqlStatic()
      .createQuery("select a from AttributeDef a where not exists (select 1 from FlatAttributeDef flatAttributeDef where flatAttributeDef.id=a.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type where temp.string01 = a.id " +
          "and type.actionName='addAttributeDef' and type.changeLogCategory='attributeDef' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingFlatAttributeDefs")
      .listSet(AttributeDef.class);
    
    return attrDefs;
  }
  
  public Set<FlatAttributeDef> findBadFlatAttributeDefs() {
    Set<FlatAttributeDef> attrDefs = HibernateSession
      .byHqlStatic()
      .createQuery("select flatAttributeDef from FlatAttributeDef flatAttributeDef where not exists (select 1 from AttributeDef a where flatAttributeDef.id=a.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type where temp.string01 = flatAttributeDef.id " +
          "and type.actionName='deleteAttributeDef' and type.changeLogCategory='attributeDef' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBadFlatAttributeDefs")
      .listSet(FlatAttributeDef.class);
    
    return attrDefs;
  }
}

