package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.Session;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupSetDAO;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * @author shilen
 * @version $Id: Hib3GroupSetDAO.java,v 1.6 2009-09-17 15:33:05 shilen Exp $
 */
public class Hib3GroupSetDAO extends Hib3DAO implements GroupSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3GroupSetDAO.class.getName();


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#save(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public void save(GroupSet groupSet) {
    HibernateSession.byObjectStatic().save(groupSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#save(java.util.Set)
   */
  public void save(Set<GroupSet> groupSets) {
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      save(iter.next());
    }
  }
  
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#delete(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public void delete(GroupSet groupSet) {
    HibernateSession.byObjectStatic().delete(groupSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#delete(java.util.Set)
   */
  public void delete(Set<GroupSet> groupSets) {
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      delete(iter.next());
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#save(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public void update(GroupSet groupSet) {
    HibernateSession.byObjectStatic().update(groupSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#update(java.util.Set)
   */
  public void update(Set<GroupSet> groupSets) {
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      update(iter.next());
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#deleteByOwnerGroupAndField(java.lang.String, java.lang.String)
   */
  public void deleteByOwnerGroupAndField(String groupId, String field) {
    HibernateSession.byHqlStatic().createQuery(
        "delete from GroupSet as gs where ownerGroupId = :id and fieldId = :field")
        .setString("id", groupId)
        .setString("field", field)
        .executeUpdate();
  }
  


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#deleteByOwnerStem(java.lang.String)
   */
  public void deleteByOwnerStem(String stemId) {
    HibernateSession.byHqlStatic().createQuery(
        "delete from GroupSet as gs where ownerStemId = :id").setString("id", stemId)
        .executeUpdate();
  }

  
  /**
   * reset group set
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    Session hs = hibernateSession.getSession();

    // Find the root stem
    Stem rootStem = GrouperDAOFactory.getFactory().getStem().findByName(Stem.ROOT_INT, true);
    
    // set parent_id to null first to avoid foreign key constraint violations
    hs.createQuery("update GroupSet set parentId = null where ownerStemId not like :owner or ownerStemId is null or depth not like '0'")
        .setString("owner", rootStem.getUuid())
        .executeUpdate();
    
    hs.createQuery("delete from GroupSet where parentId = null")
        .executeUpdate();
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByGroupOwnerAndField(java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public Set<GroupSet> findAllByGroupOwnerAndField(String groupId, Field field) {
    Set<GroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.ownerGroupId = :owner and gs.fieldId = :fuuid")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByGroupOwnerAndField")
        .setString("owner", groupId)
        .setString("fuuid", field.getUuid())
        .listSet(GroupSet.class);

    return groupSets;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findParentGroupSet(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public GroupSet findParentGroupSet(GroupSet groupSet) {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindParentGroupSet")
      .setString("id", groupSet.getParentId())
      .uniqueResult(GroupSet.class);
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByMemberGroup(java.lang.String)
   */
  public Set<GroupSet> findAllByMemberGroup(String groupId) {
    Set<GroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.memberGroupId = :member and gs.type = 'effective'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByMemberGroup")
        .setString("member", groupId)
        .listSet(GroupSet.class);

    return groupSets;
  }
  


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByMemberGroupAndField(java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public Set<GroupSet> findAllByMemberGroupAndField(String memberId, Field field) {
    Set<GroupSet> groupSets = HibernateSession
    .byHqlStatic()
    .createQuery("select gs from GroupSet as gs where gs.memberGroupId = :member and gs.fieldId = :field and gs.type = 'effective'")
    .setCacheable(false).setCacheRegion(KLASS + ".FindAllByMemberGroupAndField")
    .setString("member", memberId)
    .setString("field", field.getUuid())
    .listSet(GroupSet.class);

return groupSets;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findById(java.lang.String)
   */
  public GroupSet findById(String groupSetId) {
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", groupSetId)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null) {
      throw new GroupSetNotFoundException("Didn't find groupSet with id: " + groupSetId);
    }
    
    return groupSet;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllChildren(edu.internet2.middleware.grouper.group.GroupSet)
   */
  public Set<GroupSet> findAllChildren(GroupSet groupSet) {
    Set<GroupSet> allChildren = new LinkedHashSet<GroupSet>();
    Set<GroupSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.parentId = :parent and gs.type = 'effective'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllChildren")
        .setString("parent", groupSet.getId())
        .listSet(GroupSet.class);
    
    Iterator<GroupSet> iter = children.iterator();
    
    while (iter.hasNext()) {
      GroupSet child = iter.next();
      allChildren.addAll(findAllChildren(child));
      allChildren.add(child);
    }
    
    return allChildren;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findImmediateChildByParentAndMemberGroup(edu.internet2.middleware.grouper.group.GroupSet, java.lang.String)
   */
  public GroupSet findImmediateChildByParentAndMemberGroup(GroupSet parentGroupSet,
      String memberGroupId) {
    
    int depth = parentGroupSet.getDepth() + 1;
    
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.parentId = :id and gs.memberGroupId = :memberGroupId and gs.depth = :depth")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildByParentAndMemberGroup")
      .setString("id", parentGroupSet.getId())
      .setString("memberGroupId", memberGroupId)
      .setInteger("depth", depth)
      .uniqueResult(GroupSet.class);
  }



  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findSelfGroup(java.lang.String, java.lang.String)
   */
  public GroupSet findSelfGroup(String groupId, String fieldId) {
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerGroupId = :id and memberGroupId = :id and fieldId = :field and depth='0'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindSelfGroup")
      .setString("id", groupId)
      .setString("field", fieldId)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null) {
      throw new GroupSetNotFoundException("Didn't find groupSet of depth 0 with owner and member: " + groupId);
    }
    
    return groupSet;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findSelfStem(java.lang.String, java.lang.String)
   */
  public GroupSet findSelfStem(String stemId, String fieldId) {
    GroupSet groupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerStemId = :id and memberStemId = :id and fieldId = :field and depth='0'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindSelfStem")
      .setString("id", stemId)
      .setString("field", fieldId)
      .uniqueResult(GroupSet.class);
    
    if (groupSet == null) {
      throw new GroupSetNotFoundException("Didn't find groupSet of depth 0 with owner and member: " + stemId);
    }
    
    return groupSet;
  }



  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#deleteByOwnerGroup(edu.internet2.middleware.grouper.Group)
   */
  public void deleteByOwnerGroup(Group group) {
    HibernateSession.byHqlStatic().createQuery(
      "delete from GroupSet as gs where ownerGroupId = :id")
      .setString("id", group.getUuid())
      .executeUpdate();    
  }



  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findImmediateByOwnerGroupAndMemberGroupAndField(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public GroupSet findImmediateByOwnerGroupAndMemberGroupAndField(String ownerGroupId,
      String memberGroupId, Field field) {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerGroupId = :ownerGroupId and gs.memberGroupId = :memberGroupId and fieldId = :field and type = 'effective' and depth = '1'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateByOwnerGroupAndMemberGroupAndField")
      .setString("ownerGroupId", ownerGroupId)
      .setString("memberGroupId", memberGroupId)
      .setString("field", field.getUuid())
      .uniqueResult(GroupSet.class);
  }



  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findImmediateByOwnerStemAndMemberGroupAndField(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public GroupSet findImmediateByOwnerStemAndMemberGroupAndField(String ownerStemId,
      String memberGroupId, Field field) {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select gs from GroupSet as gs where gs.ownerStemId = :ownerStemId and gs.memberGroupId = :memberGroupId and fieldId = :field and type = 'effective' and depth = '1'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateByOwnerStemAndMemberGroupAndField")
      .setString("ownerStemId", ownerStemId)
      .setString("memberGroupId", memberGroupId)
      .setString("field", field.getUuid())
      .uniqueResult(GroupSet.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findAllByCreator(edu.internet2.middleware.grouper.Member)
   */
  public Set<GroupSet> findAllByCreator(Member member) {
    Set<GroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from GroupSet as gs where gs.creatorId = :member")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByCreator")
        .setString("member", member.getUuid())
        .listSet(GroupSet.class);

    return groupSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findMissingSelfGroupSetsForGroups()
   */
  public Set<Object[]> findMissingSelfGroupSetsForGroups() {
    
    String sql = "select g, f from GroupTypeTuple as gtt, Field as f, Group as g " +
                 "where g.uuid = gtt.groupUuid " +
                 "and gtt.typeUuid = f.groupTypeUuid " +
                 "and (f.typeString = 'list' or f.typeString = 'access') " +
                 "and not exists " +
                 "(select gs.ownerGroupId from GroupSet as gs where gs.ownerGroupId = g.id and gs.fieldId = f.uuid and gs.depth='0')";
    
    Set<Object[]> missing = HibernateSession
        .byHqlStatic()
        .createQuery(sql)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindMissingSelfGroupSetsForGroups")
        .listSet(Object[].class);
    
    return missing;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupSetDAO#findMissingSelfGroupSetsForStems()
   */
  public Set<Object[]> findMissingSelfGroupSetsForStems() {
    
    String sql = "select s, f from Field as f, Stem as s " +
                 "where f.typeString = 'naming' " +
                 "and not exists " +
                 "(select gs.ownerStemId from GroupSet as gs where gs.ownerStemId = s.id and gs.fieldId = f.uuid and gs.depth='0')";
    
    Set<Object[]> missing = HibernateSession
        .byHqlStatic()
        .createQuery(sql)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindMissingSelfGroupSetsForStems")
        .listSet(Object[].class);
    
    return missing;
  }
}

