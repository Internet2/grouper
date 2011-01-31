package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignDAO extends Hib3DAO implements PITAttributeAssignDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssign)
   */
  public void saveOrUpdate(PITAttributeAssign pitAttributeAssign) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssign);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssign)
   */
  public void delete(PITAttributeAssign pitAttributeAssign) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssign);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeAssign set ownerAttributeAssignId = null where ownerAttributeAssignId is not null and id not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeAssign where id not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findById(java.lang.String)
   */
  public PITAttributeAssign findById(String id) {
    PITAttributeAssign pitAttributeAssign = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssign.class);
    
    return pitAttributeAssign;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#updateOwnerAttributeAssignId(java.lang.String, java.lang.String)
   */
  public void updateOwnerAttributeAssignId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssign set ownerAttributeAssignId = :newId where ownerAttributeAssignId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerAttributeAssignId(java.lang.String)
   */
  public Set<PITAttributeAssign> findActiveByOwnerAttributeAssignId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerAttributeAssignId = :id and attrAssign.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByOwnerAttributeAssignId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#updateOwnerMembershipId(java.lang.String, java.lang.String)
   */
  public void updateOwnerMembershipId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssign set ownerMembershipId = :newId where ownerMembershipId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findActiveByOwnerMembershipId(java.lang.String)
   */
  public Set<PITAttributeAssign> findActiveByOwnerMembershipId(String id) {
    Set<PITAttributeAssign> assignments = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.ownerMembershipId = :id and attrAssign.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByOwnerMembershipId")
      .setString("id", id)
      .listSet(PITAttributeAssign.class);
    
    return assignments;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    HibernateSession.byHqlStatic()
      .createQuery("update PITAttributeAssign a set a.ownerAttributeAssignId = null where a.endTimeDb is not null and a.endTimeDb < :time and a.ownerAttributeAssignId is not null " +
      		"and not exists (select 1 from PITAttributeAssignValue v where v.attributeAssignId = a.id)")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssign a where a.endTimeDb is not null and a.endTimeDb < :time and a.ownerAttributeAssignId is null " +
      		"and not exists (select 1 from PITAttributeAssignValue v where v.attributeAssignId = a.id)")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
}