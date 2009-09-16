package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO;

/**
 * Data Access Object for attribute def name set
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameSetDAO.java,v 1.4 2009-09-16 05:50:52 mchyzer Exp $
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
    hibernateSession.byHql().createQuery("update AttributeDefNameSet set parentAttrDefNameSetId = null").executeUpdate();
    hibernateSession.byHql().createQuery("delete from AttributeDefNameSet").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findById(java.lang.String, boolean)
   */
  public AttributeDefNameSet findById(String id, boolean exceptionIfNotFound) {
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
  public void delete(AttributeDefNameSet attributeDefNameSet) {
    HibernateSession.byObjectStatic().delete(attributeDefNameSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
   */
  public AttributeDefNameSet findByIfThenImmediate(String attributeDefNameIdIf,
      String attributeDefNameIdThen, boolean exceptionIfNotFound) {
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where ifHasAttributeDefNameId = :ifId " +
      "and thenHasAttributeDefNameId = :thenId")
      .setString("ifId", attributeDefNameIdIf).setString("thenId", attributeDefNameIdThen)
      .uniqueResult(AttributeDefNameSet.class);
    if (attributeDefNameSet == null && exceptionIfNotFound) {
      throw new RuntimeException("AttributeDefNameSet immediate if "
          + attributeDefNameIdIf + ", then: " + attributeDefNameIdThen);
    }
    return attributeDefNameSet;

  }

} 

