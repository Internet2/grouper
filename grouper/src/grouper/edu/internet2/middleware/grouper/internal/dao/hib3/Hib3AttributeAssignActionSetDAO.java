package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.exception.AttributeAssignActionSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Data Access Object for attribute assign action set
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignActionSetDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeAssignActionSetDAO extends Hib3DAO implements AttributeAssignActionSetDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignActionSetDAO.class.getName();

  /**
   * reset the attribute def scopes
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("update AttributeAssignActionSet set parentAttrAssignActionSetId = null").executeUpdate();
    hibernateSession.byHql().createQuery("delete from AttributeAssignActionSet").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#findById(java.lang.String, boolean)
   */
  public AttributeAssignActionSet findById(String id, boolean exceptionIfNotFound) throws AttributeAssignActionSetNotFoundException {
    AttributeAssignActionSet attributeAssignActionSet = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignActionSet where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssignActionSet.class);
    if (attributeAssignActionSet == null && exceptionIfNotFound) {
      throw new AttributeAssignActionSetNotFoundException("Cant find attribute def name set by id: " + id);
    }
    return attributeAssignActionSet;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet)
   */
  public void saveOrUpdate(AttributeAssignActionSet attributeAssignActionSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignActionSet);
  }

  /**
   * @see AttributeAssignActionSetDAO#findByIfHasAttributeAssignActionId(String)
   */
  public Set<AttributeAssignActionSet> findByIfHasAttributeAssignActionId(String id) {
    Set<AttributeAssignActionSet> attributeAssignActionSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssignActionSet where ifHasAttrAssignActionId = :theId")
      .setString("theId", id).listSet(AttributeAssignActionSet.class);
    return attributeAssignActionSets;

  }

  /**
   * @see AttributeAssignActionSetDAO#findByThenHasAttributeAssignActionId(String)
   */
  public Set<AttributeAssignActionSet> findByThenHasAttributeAssignActionId(String id) {
    Set<AttributeAssignActionSet> attributeAssignActionSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssignActionSet where thenHasAttrAssignActionId = :theId")
      .setString("theId", id).listSet(AttributeAssignActionSet.class);
    return attributeAssignActionSets;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#findByIfThenHasAttributeAssignActionId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssignActionSet> findByIfThenHasAttributeAssignActionId(
      String attributeAssignActionSetForThens, String attributeAssignActionSetForIfs) {
    Set<AttributeAssignActionSet> attributeAssignActionSets = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssignActionSet from AttributeAssignActionSet as theAttributeAssignActionSet, AttributeAssignActionSet as theAttributeAssignActionSetThens, "
        + "AttributeAssignActionSet as theAttributeAssignActionSetIfs "
        + "where theAttributeAssignActionSetThens.thenHasAttrAssignActionId = :attributeAssignActionSetForThens "
        + "and theAttributeAssignActionSetIfs.ifHasAttrAssignActionId = :attributeAssignActionSetForIfs "
        + "and theAttributeAssignActionSet.ifHasAttrAssignActionId = theAttributeAssignActionSetThens.ifHasAttrAssignActionId "
        + "and theAttributeAssignActionSet.thenHasAttrAssignActionId = theAttributeAssignActionSetIfs.thenHasAttrAssignActionId "
    )
    .setString("attributeAssignActionSetForThens", attributeAssignActionSetForThens)
    .setString("attributeAssignActionSetForIfs", attributeAssignActionSetForIfs)
    .listSet(AttributeAssignActionSet.class);
  return attributeAssignActionSets;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#delete(edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet)
   */
  public void delete(final AttributeAssignActionSet attributeAssignActionSet) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            //set parent to null so mysql doest get mad
            //http://bugs.mysql.com/bug.php?id=15746
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
                "update AttributeAssignActionSet set parentAttrAssignActionSetId = null where id = :id")
                .setString("id", attributeAssignActionSet.getId()).executeUpdate();
            hibernateHandlerBean.getHibernateSession().byObject().delete(attributeAssignActionSet);
            return null;
          }
      
    });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
   */
  public AttributeAssignActionSet findByIfThenImmediate(String attributeAssignActionIdIf,
      String attributeAssignActionIdThen, boolean exceptionIfNotFound) throws AttributeAssignActionSetNotFoundException {
    AttributeAssignActionSet attributeAssignActionSet = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssignActionSet where ifHasAttrAssignActionId = :ifId " +
      "and thenHasAttrAssignActionId = :thenId")
      .setString("ifId", attributeAssignActionIdIf).setString("thenId", attributeAssignActionIdThen)
      .uniqueResult(AttributeAssignActionSet.class);
    if (attributeAssignActionSet == null && exceptionIfNotFound) {
      throw new AttributeAssignActionSetNotFoundException("AttributeAssignActionSet immediate if "
          + attributeAssignActionIdIf + ", then: " + attributeAssignActionIdThen);
    }
    return attributeAssignActionSet;

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#deleteByIfHasAttributeAssignAction(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void deleteByIfHasAttributeAssignAction(final AttributeAssignAction attributeAssignAction) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler()  {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            //do this since mysql cant handle self-referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update AttributeAssignActionSet set parentAttrAssignActionSetId = null where ifHasAttrAssignActionId = :id")
              .setString("id", attributeAssignAction.getId())
              .executeUpdate();    
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "delete from AttributeAssignActionSet where ifHasAttrAssignActionId = :id")
              .setString("id", attributeAssignAction.getId())
              .executeUpdate();    
            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#attributeAssignActionsImpliedByThis(java.lang.String)
   */
  public Set<AttributeAssignAction> attributeAssignActionsImpliedByThis(String attributeAssignActionId) {
    Set<AttributeAssignAction> attributeAssignActions = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeAssignActionSet as adns, AttributeAssignAction as adn " +
        "where adn.ifHasAttrAssignActionId = :theId and adn.id = adns.thenHasAttrAssignActionId " +
        "and adn.id != :theId order by adn.name")
        .setString("theId", attributeAssignActionId).listSet(AttributeAssignAction.class);
      return attributeAssignActions;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#attributeAssignActionsImpliedByThisImmediate(java.lang.String)
   */
  public Set<AttributeAssignAction> attributeAssignActionsImpliedByThisImmediate(String attributeAssignActionId) {
    Set<AttributeAssignAction> attributeAssignActions = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeAssignActionSet as adns, AttributeAssignAction as adn " +
        "where adn.ifHasAttrAssignActionId = :theId and adn.id = adns.thenHasAttrAssignActionId " +
        "and adn.id != :theId and adn.typeDb = 'immediate' order by adn.name")
        .setString("theId", attributeAssignActionId).listSet(AttributeAssignAction.class);
      return attributeAssignActions;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#attributeAssignActionsThatImplyThis(java.lang.String)
   */
  public Set<AttributeAssignAction> attributeAssignActionsThatImplyThis(String attributeAssignActionId) {
    Set<AttributeAssignAction> attributeAssignActions = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeAssignActionSet as adns, AttributeAssignAction as adn " +
        "where adn.thenHasAttrAssignActionId = :theId and adn.id = adns.ifHasAttrAssignActionId " +
        "and adn.id != :theId order by adn.name")
        .setString("theId", attributeAssignActionId).listSet(AttributeAssignAction.class);
      return attributeAssignActions;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO#attributeAssignActionsThatImplyThisImmediate(java.lang.String)
   */
  public Set<AttributeAssignAction> attributeAssignActionsThatImplyThisImmediate(String attributeAssignActionId) {
    Set<AttributeAssignAction> attributeAssignActions = HibernateSession.byHqlStatic().createQuery(
        "select distinct adn from AttributeAssignActionSet as adns, AttributeAssignAction as adn " +
        "where adn.thenHasAttrAssignActionId = :theId and adn.id = adns.ifHasAttrAssignActionId " +
        "and adn.id != :theId and adn.typeDb = 'immediate' order by adn.name")
        .setString("theId", attributeAssignActionId).listSet(AttributeAssignAction.class);
      return attributeAssignActions;
  }

} 

