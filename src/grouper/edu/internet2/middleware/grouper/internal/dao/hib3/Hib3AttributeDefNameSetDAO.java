package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Data Access Object for attribute def name set
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameSetDAO.java,v 1.8 2009-11-07 14:13:09 shilen Exp $
 */
public class Hib3AttributeDefNameSetDAO extends Hib3DAO implements AttributeDefNameSetDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefNameSetDAO.class.getName();

  /**
   * reset the attribute def scopes
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    if (GrouperDdlUtils.isMysql()) {
      //do this since mysql cant handle self-referential foreign keys
      // restrict this only to mysql since in oracle this might cause unique constraint violations
      hibernateSession.byHql().createQuery("update AttributeDefNameSet set parentAttrDefNameSetId = null").executeUpdate();
    }
    
    hibernateSession.byHql().createQuery("delete from AttributeDefNameSet").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findById(java.lang.String, boolean)
   */
  public AttributeDefNameSet findById(String id, boolean exceptionIfNotFound) throws AttributeDefNameSetNotFoundException {
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefNameSet where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefNameSet.class);
    if (attributeDefNameSet == null && exceptionIfNotFound) {
      throw new AttributeDefNameSetNotFoundException("Cant find attribute def name set by id: " + id);
    }
    return attributeDefNameSet;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDefNameSet)
   */
  public void saveOrUpdate(AttributeDefNameSet attributeDefNameSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefNameSet);
  }

  /**
   * @see AttributeDefNameSetDAO#findByIfHasAttributeDefNameId(String)
   */
  public Set<AttributeDefNameSet> findByIfHasAttributeDefNameId(String id) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where ifHasAttributeDefNameId = :theId")
      .setString("theId", id).listSet(AttributeDefNameSet.class);
    return attributeDefNameSets;

  }

  /**
   * @see AttributeDefNameSetDAO#findByThenHasAttributeDefNameId(String)
   */
  public Set<AttributeDefNameSet> findByThenHasAttributeDefNameId(String id) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where thenHasAttributeDefNameId = :theId")
      .setString("theId", id).listSet(AttributeDefNameSet.class);
    return attributeDefNameSets;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findByIfThenHasAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefNameSet> findByIfThenHasAttributeDefNameId(
      String attributeDefNameSetForThens, String attributeDefNameSetForIfs) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefNameSet from AttributeDefNameSet as theAttributeDefNameSet, AttributeDefNameSet as theAttributeDefNameSetThens, "
        + "AttributeDefNameSet as theAttributeDefNameSetIfs "
        + "where theAttributeDefNameSetThens.thenHasAttributeDefNameId = :attributeDefNameSetForThens "
        + "and theAttributeDefNameSetIfs.ifHasAttributeDefNameId = :attributeDefNameSetForIfs "
        + "and theAttributeDefNameSet.ifHasAttributeDefNameId = theAttributeDefNameSetThens.ifHasAttributeDefNameId "
        + "and theAttributeDefNameSet.thenHasAttributeDefNameId = theAttributeDefNameSetIfs.thenHasAttributeDefNameId "
    )
    .setString("attributeDefNameSetForThens", attributeDefNameSetForThens)
    .setString("attributeDefNameSetForIfs", attributeDefNameSetForIfs)
    .listSet(AttributeDefNameSet.class);
  return attributeDefNameSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#delete(edu.internet2.middleware.grouper.attr.AttributeDefNameSet)
   */
  public void delete(final AttributeDefNameSet attributeDefNameSet) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            if (GrouperDdlUtils.isMysql()) {
              //set parent to null so mysql doest get mad
              //http://bugs.mysql.com/bug.php?id=15746
              hibernateHandlerBean.getHibernateSession().byHql().createQuery(
                  "update AttributeDefNameSet set parentAttrDefNameSetId = null where id = :id")
                  .setString("id", attributeDefNameSet.getId()).executeUpdate();
            }
            
            hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDefNameSet);
            return null;
          }
      
    });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
   */
  public AttributeDefNameSet findByIfThenImmediate(String attributeDefNameIdIf,
      String attributeDefNameIdThen, boolean exceptionIfNotFound) throws AttributeDefNameSetNotFoundException {
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where ifHasAttributeDefNameId = :ifId " +
      "and thenHasAttributeDefNameId = :thenId")
      .setString("ifId", attributeDefNameIdIf).setString("thenId", attributeDefNameIdThen)
      .uniqueResult(AttributeDefNameSet.class);
    if (attributeDefNameSet == null && exceptionIfNotFound) {
      throw new AttributeDefNameSetNotFoundException("AttributeDefNameSet immediate if "
          + attributeDefNameIdIf + ", then: " + attributeDefNameIdThen);
    }
    return attributeDefNameSet;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#deleteByIfHasAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  public void deleteByIfHasAttributeDefName(final AttributeDefName attributeDefName) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler()  {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            if (GrouperDdlUtils.isMysql()) {
              //do this since mysql cant handle self-referential foreign keys
              hibernateHandlerBean.getHibernateSession().byHql().createQuery(
                "update AttributeDefNameSet set parentAttrDefNameSetId = null where ifHasAttributeDefNameId = :id")
                .setString("id", attributeDefName.getId())
                .executeUpdate();    
            }
            
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "delete from AttributeDefNameSet where ifHasAttributeDefNameId = :id")
              .setString("id", attributeDefName.getId())
              .executeUpdate();    
            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesImpliedByThis(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesImpliedByThis(String attributeDefNameId) {
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeDefNameSet as adns, AttributeDefName as adn " +
        "where adn.ifHasAttributeDefNameId = :theId and adn.id = adns.thenHasAttributeDefNameId " +
        "and adn.id != :theId order by adn.name")
        .setString("theId", attributeDefNameId).listSet(AttributeDefName.class);
      return attributeDefNames;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesImpliedByThisImmediate(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate(String attributeDefNameId) {
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeDefNameSet as adns, AttributeDefName as adn " +
        "where adn.ifHasAttributeDefNameId = :theId and adn.id = adns.thenHasAttributeDefNameId " +
        "and adn.id != :theId and adn.typeDb = 'immediate' order by adn.name")
        .setString("theId", attributeDefNameId).listSet(AttributeDefName.class);
      return attributeDefNames;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesThatImplyThis(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesThatImplyThis(String attributeDefNameId) {
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeDefNameSet as adns, AttributeDefName as adn " +
        "where adn.thenHasAttributeDefNameId = :theId and adn.id = adns.ifHasAttributeDefNameId " +
        "and adn.id != :theId order by adn.name")
        .setString("theId", attributeDefNameId).listSet(AttributeDefName.class);
      return attributeDefNames;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#attributeDefNamesThatImplyThisImmediate(java.lang.String)
   */
  public Set<AttributeDefName> attributeDefNamesThatImplyThisImmediate(String attributeDefNameId) {
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeDefNameSet as adns, AttributeDefName as adn " +
        "where adn.thenHasAttributeDefNameId = :theId and adn.id = adns.ifHasAttributeDefNameId " +
        "and adn.id != :theId and adn.typeDb = 'immediate' order by adn.name")
        .setString("theId", attributeDefNameId).listSet(AttributeDefName.class);
      return attributeDefNames;
  }

} 

