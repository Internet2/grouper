package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignDAO.java,v 1.10 2009-10-02 05:57:58 mchyzer Exp $
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByMemberIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByMemberIdAndAttributeDefId(
      String memberId, String attributeDefId) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMemberId = :theOwnerMemberId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerMemberId", memberId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMemberIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMemberIdAndAttributeDefId(String memberId,
      String attributeDefId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMemberId = :theOwnerMemberId")
        .setString("theAttributeDefId", attributeDefId)
        .setString("theOwnerMemberId", memberId)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMemberIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMemberIdAndAttributeDefNameId(String memberId,
      String attributeDefNameId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerMemberId = :theOwnerMemberId")
      .setString("theAttributeDefNameId", attributeDefNameId)
      .setString("theOwnerMemberId", memberId)
      .listSet(AttributeAssign.class);

    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByAttributeDefIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByAttributeDefIdAndAttributeDefId(
      String attributeDefIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttributeDefId", attributeDefIdToAssignTo)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttributeDefIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttributeDefIdAndAttributeDefId(
      String attributeDefIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttributeDefId", attributeDefIdToAssignTo)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttributeDefIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttributeDefIdAndAttributeDefNameId(
      String attributeDefIdToAssignTo, String attributeDefNameIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerAttributeDefId = :theOwnerAttributeDefId")
      .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
      .setString("theOwnerAttributeDefId", attributeDefIdToAssignTo)
      .listSet(AttributeAssign.class);

    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByMembershipIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByMembershipIdAndAttributeDefId(
      String membershipIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMembershipId = :theOwnerMembershipId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMembershipId", membershipIdToAssignTo)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMembershipIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMembershipIdAndAttributeDefId(
      String membershipIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMembershipId = :theOwnerMembershipId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMembershipId", membershipIdToAssignTo)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByMembershipIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByMembershipIdAndAttributeDefNameId(
      String membershipIdToAssignTo, String attributeDefNameIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerMembershipId = :theOwnerMembershipId")
      .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
      .setString("theOwnerMembershipId", membershipIdToAssignTo)
      .listSet(AttributeAssign.class);
  
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByAttrAssignIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByAttrAssignIdAndAttributeDefId(
      String attrAssignIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeAssignId = :theOwnerAttrAssignId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttrAssignId", attrAssignIdToAssignTo)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttrAssignIdAndAttributeDefId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttrAssignIdAndAttributeDefId(
      String attrAssignIdToAssignTo, String attributeDefIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerAttributeAssignId = :theOwnerAttrAssignId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerAttrAssignId", attrAssignIdToAssignTo)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttrAssignIdAndAttributeDefNameId(java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByAttrAssignIdAndAttributeDefNameId(
      String attrAssignIdToAssignTo, String attributeDefNameIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerAttributeAssignId = :theOwnerAttrAssignId")
      .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
      .setString("theOwnerAttrAssignId", attrAssignIdToAssignTo)
      .listSet(AttributeAssign.class);
  
    return attributeAssigns;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findAttributeDefNamesByGroupIdMemberIdAndAttributeDefId(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<AttributeDefName> findAttributeDefNamesByGroupIdMemberIdAndAttributeDefId(
      String groupId, String memberId, String attributeDefIdToAssign) {
    Set<AttributeDefName> attributeDefs = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeDefName from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId " +
        "and theAttributeAssign.ownerMemberId = :theOwnerMemberId " +
        "and theAttributeAssign.ownerGroupId = :theOwnerGroupId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMemberId", memberId)
        .setString("theOwnerGroupId", groupId)
        .listSet(AttributeDefName.class);

      return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdMemberIdAndAttributeDefId(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdMemberIdAndAttributeDefId(String groupId,
      String memberId, String attributeDefIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign, AttributeDefName theAttributeDefName " +
        "where theAttributeDefName.attributeDefId = :theAttributeDefId " +
        "and theAttributeDefName.id = theAttributeAssign.attributeDefNameId and theAttributeAssign.ownerMemberId = :theOwnerMemberId " +
        "and theAttributeAssign.ownerGroupId = :theOwnerGroupId")
        .setString("theAttributeDefId", attributeDefIdToAssign)
        .setString("theOwnerMemberId", memberId)
        .setString("theOwnerGroupId", groupId)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByGroupIdMemberIdAndAttributeDefNameId(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<AttributeAssign> findByGroupIdMemberIdAndAttributeDefNameId(String groupId,
      String memberId, String attributeDefNameIdToAssign) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssign where attributeDefNameId = :theAttributeDefNameId and ownerGroupId = :theOwnerGroupId " +
      "and ownerMemberId = :theOwnerMemberId")
      .setString("theAttributeDefNameId", attributeDefNameIdToAssign)
      .setString("theOwnerGroupId", groupId)
      .setString("theOwnerMemberId", memberId)
      .listSet(AttributeAssign.class);
  
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByUuidOrKey(java.util.Collection, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.Long, java.lang.Long, java.lang.String)
   */
  public AttributeAssign findByUuidOrKey(Collection<String> idsToIgnore, String id,
      String attributeDefNameId, String attributeAssignActionId, String ownerAttributeAssignId,
      String ownerAttributeDefId, String ownerGroupId, String ownerMemberId,
      String ownerMembershipId, String ownerStemId, boolean exceptionIfNull,
      Long disabledTimeDb, Long enabledTimeDb, String notes) throws GrouperDAOException {
    try {
      Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssign as theAttributeAssign where\n " +
            "theAttributeAssign.id = :theId or (theAttributeAssign.attributeDefNameId = :theAttributeDefNameId and\n " +
            "theAttributeAssign.attributeAssignActionId = :theAttributeAssignActionId and\n " +
            "(theAttributeAssign.ownerAttributeAssignId is null or theAttributeAssign.ownerAttributeAssignId = :theOwnerAttributeAssignId) and \n " +
            "(theAttributeAssign.ownerAttributeDefId is null or theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId) and \n " +
            "(theAttributeAssign.ownerGroupId is null or theAttributeAssign.ownerGroupId = :theOwnerGroupId) and \n " +
            "(theAttributeAssign.ownerMemberId is null or theAttributeAssign.ownerMemberId = :theOwnerMemberId) and \n " +
            "(theAttributeAssign.ownerMembershipId is null or theAttributeAssign.ownerMembershipId = :theOwnerMembershipId) and \n " +
            "(theAttributeAssign.ownerStemId is null or theAttributeAssign.ownerStemId = :theOwnerStemId) ) ")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrKey")
        .setString("theId", id)
        .setString("theAttributeAssignActionId", attributeAssignActionId)
        .setString("theAttributeDefNameId", attributeDefNameId)
        .setString("theOwnerAttributeAssignId", ownerAttributeAssignId)
        .setString("theOwnerAttributeDefId", ownerAttributeDefId)
        .setString("theOwnerGroupId", ownerGroupId)
        .setString("theOwnerMemberId", ownerMemberId)
        .setString("theOwnerMembershipId", ownerMembershipId)
        .setString("theOwnerStemId", ownerStemId)
        .listSet(AttributeAssign.class);
      if (GrouperUtil.length(attributeAssigns) == 0) {
        if (exceptionIfNull) {
          throw new RuntimeException("Can't find attributeAssign by id: '" + id + "' or attributeDefNameId '" + attributeDefNameId 
              + "', ownerAttributeAssignId: " + ownerAttributeAssignId
              + ", ownerAttributeDefId: " + ownerAttributeDefId
              + ", ownerGroupId: " + ownerGroupId
              + ", ownerMemberId: " + ownerMemberId
              + ", ownerMembershipId: " + ownerMembershipId
              + ", ownerStemId: " + ownerStemId
              );
        }
        return null;
      }
      
      idsToIgnore = GrouperUtil.nonNull(idsToIgnore);
      
      //lets remove ones we have already processed or will process
      Iterator<AttributeAssign> iterator = attributeAssigns.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssign attributeAssign = iterator.next();
        if (idsToIgnore.contains(attributeAssign.getId())) {
          iterator.remove();
        }
      }
      
      //first case, the ID matches
      iterator = attributeAssigns.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssign attributeAssign = iterator.next();
        if (StringUtils.equals(id, attributeAssign.getId())) {
          return attributeAssign;
        }
      }

      //second case, the value matches
      iterator = attributeAssigns.iterator();
      
      //tree map in reverse order
      Map<Integer, AttributeAssign> heuristicMap = new TreeMap<Integer, AttributeAssign>(new Comparator<Integer>() {

        public int compare(Integer o1, Integer o2) {
          if (o1 == o2) {
            return 0;
          }
          if (o1==null) {
            return o2;
          }
          if (o2== null) {
            return o1;
          }
          return -o1.compareTo(o2);
        }
      });
      
      while (iterator.hasNext()) {
        int score = 0;
        AttributeAssign attributeAssign = iterator.next();

        if (StringUtils.equals(notes, attributeAssign.getNotes())) {
          score++;
        }
        if (GrouperUtil.equals(disabledTimeDb, attributeAssign.getDisabledTimeDb())) {
          score++;
        }
        if (GrouperUtil.equals(enabledTimeDb, attributeAssign.getEnabledDb())) {
          score++;
        }
        
        heuristicMap.put(score, attributeAssign);
      }
      
      //ok, if there is one left, return it, the best heuristic first
      if (heuristicMap.size() > 0) {
        return heuristicMap.get(heuristicMap.keySet().iterator().next());
      }
      
      //cant find one
      return null;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeAssign by id: '" + id + "' or attributeDefNameId '" + attributeDefNameId 
              + "', ownerAttributeAssignId: " + ownerAttributeAssignId
              + ", ownerAttributeDefId: " + ownerAttributeDefId
              + ", ownerGroupId: " + ownerGroupId
              + ", ownerMemberId: " + ownerMemberId
              + ", ownerMembershipId: " + ownerMembershipId
              + ", ownerStemId: " + ownerStemId + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.assign.AttributeAssign)
   */
  public void saveUpdateProperties(AttributeAssign attributeAssign) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdatedDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeAssign.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeAssign.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeAssign.getLastUpdatedDb())
        .setString("theContextId", attributeAssign.getContextId())
        .setString("theId", attributeAssign.getId()).executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerAttributeAssignId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerAttributeAssignId(String ownerAttributeAssignId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerAttributeAssignId = :theOwnerAttrAssignId")
        .setString("theOwnerAttrAssignId", ownerAttributeAssignId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerGroupId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerGroupId(String ownerGroupId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerGroupId = :theOwnerGroupId")
        .setString("theOwnerGroupId", ownerGroupId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerMemberId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerMemberId(String ownerMemberId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerMemberId = :theOwnerMemberId")
        .setString("theOwnerMemberId", ownerMemberId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerMembershipId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerMembershipId(String ownerMembershipId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerMembershipId = :theOwnerMembershipId")
        .setString("theOwnerMembershipId", ownerMembershipId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerStemId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerStemId(String ownerStemId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerStemId = :theOwnerStemId")
        .setString("theOwnerStemId", ownerStemId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByActionId(java.lang.String)
   */
  public Set<AttributeAssign> findByActionId(String actionId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic()
      .createQuery("from AttributeAssign as theAttributeAssign where\n " +
          "theAttributeAssign.attributeAssignActionId = :theAttributeAssignActionId ")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByActionId")
      .setString("theAttributeAssignActionId", actionId)
      .listSet(AttributeAssign.class);
    return attributeAssigns;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByOwnerAttributeDefId(java.lang.String)
   */
  public Set<AttributeAssign> findByOwnerAttributeDefId(String ownerAttributeDefId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select distinct theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.ownerAttributeDefId = :theOwnerAttributeDefId")
        .setString("theOwnerAttributeDefId", ownerAttributeDefId)
        .listSet(AttributeAssign.class);
    return attributeAssigns;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO#findByAttributeDefNameId(java.lang.String)
   */
  public Set<AttributeAssign> findByAttributeDefNameId(String attributeDefNameId) {
    Set<AttributeAssign> attributeAssigns = HibernateSession.byHqlStatic().createQuery(
        "select theAttributeAssign from AttributeAssign theAttributeAssign " +
        "where theAttributeAssign.attributeDefNameId = :theAttributeDefNameId")
        .setString("theAttributeDefNameId", attributeDefNameId)
        .listSet(AttributeAssign.class);

      return attributeAssigns;
  }



  
} 


