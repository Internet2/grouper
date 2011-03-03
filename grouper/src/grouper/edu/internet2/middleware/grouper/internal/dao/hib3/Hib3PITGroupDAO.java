package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITGroupDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITGroupDAO extends Hib3DAO implements PITGroupDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITGroupDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITGroup)
   */
  public void saveOrUpdate(PITGroup pitGroup) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGroup);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#delete(edu.internet2.middleware.grouper.pit.PITGroup)
   */
  public void delete(PITGroup pitGroup) {
    HibernateSession.byObjectStatic().delete(pitGroup);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<PITGroup> pitGroups) {
    HibernateSession.byObjectStatic().saveBatch(pitGroups);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITGroup where id not in (select g.uuid from Group as g)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findById(java.lang.String)
   */
  public PITGroup findById(String pitGroupId) {
    PITGroup pitGroup = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroup from PITGroup as pitGroup where pitGroup.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitGroupId)
      .uniqueResult(PITGroup.class);
    
    return pitGroup;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findByName(java.lang.String)
   */
  public Set<PITGroup> findByName(String groupName, boolean orderByStartTime) {
    String sql = "select pitGroup from PITGroup as pitGroup where pitGroup.nameDb = :name";
    
    if (orderByStartTime) {
      sql += " order by startTimeDb";
    }
    
    Set<PITGroup> pitGroups = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false).setCacheRegion(KLASS + ".FindByName")
      .setString("name", groupName)
      .listSet(PITGroup.class);
    
    return pitGroups;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITGroup where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#getAllGroupsMembershipSecure(java.lang.String, java.lang.String, java.lang.String, edu.internet2.middleware.grouper.pit.PITStem, edu.internet2.middleware.grouper.Stem.Scope, java.sql.Timestamp, java.sql.Timestamp, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<PITGroup> getAllGroupsMembershipSecure(String pitMemberId, String pitFieldId, 
      String scope, PITStem pitStem, Scope stemScope, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions) {

    if ((pitStem == null) != (stemScope == null)) {
      throw new RuntimeException("If pitStem is set, then stem scope must be set.  If pitStem isnt set, then stem scope must not be set.");
    }

    boolean hasScope = StringUtils.isNotBlank(scope);
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("thePITGroup.nameDb");
    }
    
    List<QuerySortField> querySortFields = queryOptions.getQuerySort().getQuerySortFields();

    //reset from friendly sort fields to non friendly
    for (QuerySortField querySortField : querySortFields) {
      if (StringUtils.equalsIgnoreCase(querySortField.getColumn(), "name")) {
        querySortField.setColumn("thePITGroup.nameDb");
      }
    }
  
    StringBuilder sql = new StringBuilder("select distinct thePITGroup from PITGroup thePITGroup, " +
        " PITMembershipView ms ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //make sure the session can read the privs
    Set<Privilege> inPrivSet = AccessPrivilege.READ_PRIVILEGES;
    
    //subject to check privileges for
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Subject accessSubject = grouperSession.getSubject();
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(accessSubject, byHqlStatic, 
        sql, "thePITGroup.id", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    if (hasScope) {
      sql.append(" thePITGroup.nameDb like :scope and ");
      byHqlStatic.setString("scope", scope + "%");
    }
    
    boolean subScope = false;
    
    if (pitStem != null) {
      switch (stemScope) {
        case ONE:

          sql.append(" thePITGroup.stemId = :stemId and ");
          byHqlStatic.setString("stemId", pitStem.getId());
          
          break;

        case SUB:
          
          // additional filtering after the query
          subScope = true;

          sql.append(" thePITGroup.nameDb like :stemSub and ");
          byHqlStatic.setString("stemSub", pitStem.getName() + ":%");

          break;
        default:
          throw new RuntimeException("Not expecting scope: " + stemScope);
      }
    }
    
    //this must be last due to and's
    sql.append(" ms.ownerGroupId = thePITGroup.id and ms.fieldId = :fieldId " +
      " and ms.memberId = :memberId ");
    
    if (pointInTimeFrom != null) {
      Long endDateAfter = pointInTimeFrom.getTime() * 1000;
      sql.append(" and (ms.membershipEndTimeDb is null or ms.membershipEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (ms.groupSetEndTimeDb is null or ms.groupSetEndTimeDb > '" + endDateAfter + "')");
    }
    
    if (pointInTimeTo != null) {
      Long startDateBefore = pointInTimeTo.getTime() * 1000;
      sql.append(" and ms.membershipStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and ms.groupSetStartTimeDb < '" + startDateBefore + "'");
    }
    
    byHqlStatic.createQuery(sql.toString())
      .setString("fieldId", pitFieldId)
      .setString("memberId", pitMemberId);

    Set<PITGroup> pitGroups = byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".GetAllGroupsMembershipSecure")
      .options(queryOptions)
      .listSet(PITGroup.class);


    if (subScope) {
      Set<PITGroup> pitGroupsCopy = new LinkedHashSet<PITGroup>(pitGroups);
      for (PITGroup pitGroup : pitGroupsCopy) {
        
        // make sure the pit group was active when the pit stem was active...
        if (pitGroup.getEndTimeDb() != null && pitGroup.getEndTimeDb() < pitStem.getStartTimeDb()) {
          pitGroups.remove(pitGroup);
        } else if (pitStem.getEndTime() != null && pitGroup.getStartTimeDb() > pitStem.getEndTimeDb()) {
          pitGroups.remove(pitGroup);
        }
      }
    }
    
    if (changedQuery || PrivilegeHelper.isWheelOrRoot(accessSubject)) {
      return pitGroups;
    }

    // TODO improve performance here...
    Set<PITGroup> filteredPITGroups = new LinkedHashSet<PITGroup>();
    for (PITGroup pitGroup : pitGroups) {
      if (pitGroup.isActive()) {
        Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(pitGroup.getId(), true);
        if (PrivilegeHelper.canRead(grouperSession.internal_getRootSession(), group, accessSubject)) {
          filteredPITGroups.add(pitGroup);
        }
      }
    }
    
    return filteredPITGroups;
  }
}

