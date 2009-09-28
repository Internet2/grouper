package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignDAO.java,v 1.5 2009-09-28 06:05:11 mchyzer Exp $
 */
public class Hib3AttributeAssignDAO extends Hib3DAO implements AttributeAssignDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignDAO.class.getName();

  /**
   * reset the attribute assigns
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssign").executeUpdate();
  }

  /**
   * retrieve by id
   * @param id 
   * @param exceptionIfNotFound 
   * @return  the attribute assign
   */
  public AttributeAssign findById(String id, boolean exceptionIfNotFound) {
    AttributeAssign attributeAssign = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssign.class);
    if (attributeAssign == null && exceptionIfNotFound) {
      throw new AttributeAssignNotFoundException("Cant find attribute assign by id: " + id);
    }

    return attributeAssign;
  }

  /**
   * save or update
   * @param attributeAssign 
   */
  public void saveOrUpdate(AttributeAssign attributeAssign) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssign);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdAndAttributeDefNameId(String groupId, String attributeDefNameId) {
    
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerGroupId = :theOwnerGroupId")
      .setString("theAttributeDefNameId", attributeDefNameId)
      .setString("theOwnerGroupId", groupId)
      .listSet(AttributeAssign.class);

    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdAndAttributeDefId(String groupId,
      String attributeDefId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
      "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
      "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerGroupId = :theOwnerGroupId")
      .setString("theAttributeDefId", attributeDefId)
      .setString("theOwnerGroupId", groupId)
      .listSet(AttributeAssign.class);

    return attributeAssigns;
  }

  /**
   * delete
   * @param attributeAssign 
   */
  public void delete(AttributeAssign attributeAssign) {
    HibernateSession.byObjectStatic().delete(attributeAssign);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByGroupIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByGroupIdAndAttributeDefId(
      String groupId, String attributeDefId) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerGroupId = :theOwnerGroupId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerGroupId", groupId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByStemIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByStemIdAndAttributeDefId(
      String stemId, String attributeDefId) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerStemId = :theOwnerStemId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerStemId", stemId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByStemIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByStemIdAndAttributeDefId(String stemId,
      String attributeDefId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerStemId = :theOwnerStemId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerStemId", stemId)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByStemIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByStemIdAndAttributeDefNameId(String stemId,
      String attributeDefNameId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
    "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerStemId = :theOwnerStemId")
    .setString("theAttributeDefNameId", attributeDefNameId)
    .setString("theOwnerStemId", stemId)
    .listSet(AttributeAssign.class);

  return attributeAssigns;
  }



  
} 


