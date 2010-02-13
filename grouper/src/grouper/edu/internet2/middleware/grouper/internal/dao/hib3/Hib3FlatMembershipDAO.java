package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.flat.FlatMembership;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO;

/**
 * @author shilen
 * $Id$
 */
public class Hib3FlatMembershipDAO extends Hib3DAO implements FlatMembershipDAO {

  /**
   *
   */
  private static final String KLASS = Hib3FlatMembershipDAO.class.getName();


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#save(edu.internet2.middleware.grouper.membership.FlatMembership)
   */
  public void save(FlatMembership flatMembership) {
    HibernateSession.byObjectStatic().save(flatMembership);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#save(java.util.Set)
   */
  public void save(Set<FlatMembership> flatMemberships) {
    Iterator<FlatMembership> iter = flatMemberships.iterator();
    while (iter.hasNext()) {
      save(iter.next());
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#delete(edu.internet2.middleware.grouper.membership.FlatMembership)
   */
  public void delete(FlatMembership flatMembership) {
    HibernateSession.byObjectStatic().delete(flatMembership);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#delete(java.util.Set)
   */
  public void delete(Set<FlatMembership> flatMemberships) {
    Iterator<FlatMembership> iter = flatMemberships.iterator();
    while (iter.hasNext()) {
      delete(iter.next());
    }
  }
  
  /**
   * reset flat membership
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from FlatMembership").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findById(java.lang.String)
   */
  public FlatMembership findById(String flatMembershipId) {
    FlatMembership flatMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship from FlatMembership as flatMship where flatMship.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", flatMembershipId)
      .uniqueResult(FlatMembership.class);
    
    return flatMembership;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatMembershipDAO#findByOwnerAndMemberAndField(java.lang.String, java.lang.String, java.lang.String)
   */
  public FlatMembership findByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId) {
    FlatMembership flatMembership = HibernateSession
      .byHqlStatic()
      .createQuery("select flatMship from FlatMembership as flatMship where ownerId = :ownerId and " +
      		"memberId = :memberId and fieldId = :fieldId")
      .setCacheable(false).setCacheRegion(KLASS + ".FindByOwnerAndMemberAndField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .uniqueResult(FlatMembership.class);
    
    return flatMembership;
  }

}

