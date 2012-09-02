/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.StemSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.StemSetDAO;
import edu.internet2.middleware.grouper.stem.StemSet;

/**
 * Data Access Object for stem set
 * @author  shilen
 * @version $Id$
 */
public class Hib3StemSetDAO extends Hib3DAO implements StemSetDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3StemSetDAO.class.getName();

  /**
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    
    if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
      //do this since mysql cant handle self-referential foreign keys
      // restrict this only to mysql since in oracle this might cause unique constraint violations
      hibernateSession.byHql().createQuery("update StemSet set parentStemSetId = null where thenHasStemId not in (select uuid from Stem where name = ':')").executeUpdate();
    }
    
    hibernateSession.byHql().createQuery("delete from StemSet where thenHasStemId not in (select uuid from Stem where name = ':')").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#findById(java.lang.String, boolean)
   */
  public StemSet findById(String id, boolean exceptionIfNotFound) throws StemSetNotFoundException {
    StemSet stemSet = HibernateSession.byHqlStatic()
      .createQuery("from StemSet where id = :theId")
      .setString("theId", id).uniqueResult(StemSet.class);
    
    if (stemSet == null && exceptionIfNotFound) {
      throw new StemSetNotFoundException("Cant find stem set by id: " + id);
    }
    
    return stemSet;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.stem.StemSet)
   */
  public void saveOrUpdate(StemSet stemSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(stemSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#saveBatch(java.util.Collection)
   */
  public void saveBatch(Collection<StemSet> stemSets) {
    HibernateSession.byObjectStatic().saveBatch(stemSets);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#findByIfHasStemId(java.lang.String)
   */
  public Set<StemSet> findByIfHasStemId(String id) {
    Set<StemSet> stemSets = HibernateSession.byHqlStatic()
      .createQuery("from StemSet where ifHasStemId = :theId")
      .setString("theId", id).listSet(StemSet.class);
    return stemSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#findByThenHasStemId(java.lang.String)
   */
  public Set<StemSet> findByThenHasStemId(String id) {
    Set<StemSet> stemSets = HibernateSession.byHqlStatic()
      .createQuery("from StemSet where thenHasStemId = :theId")
      .setString("theId", id).listSet(StemSet.class);
    
    return stemSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#findNonSelfByThenHasStemId(java.lang.String)
   */
  public Set<StemSet> findNonSelfByThenHasStemId(String id) {
    Set<StemSet> stemSets = HibernateSession.byHqlStatic()
      .createQuery("from StemSet where thenHasStemId = :theId and depth > 0")
      .setString("theId", id).listSet(StemSet.class);
    
    return stemSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#delete(edu.internet2.middleware.grouper.stem.StemSet)
   */
  public void delete(final StemSet stemSet) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            if (stemSet.getParentStemSetId() != null && (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql())) {
              //set parent to null so mysql doest get mad
              //http://bugs.mysql.com/bug.php?id=15746
              hibernateHandlerBean.getHibernateSession().byHql()
                .createQuery("update StemSet set parentStemSetId = null where id = :id")
                .setString("id", stemSet.getId()).executeUpdate();
            }
            
            hibernateHandlerBean.getHibernateSession().byObject().delete(stemSet);
            return null;
          }
    });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#findByIfThenImmediate(java.lang.String, java.lang.String, boolean)
   */
  public StemSet findByIfThenImmediate(String stemIdIf, String stemIdThen, boolean exceptionIfNotFound) 
    throws StemSetNotFoundException {
    
    StemSet stemSet = HibernateSession.byHqlStatic()
      .createQuery("from StemSet where ifHasStemId = :ifId and thenHasStemId = :thenId and depth = 1")
      .setString("ifId", stemIdIf)
      .setString("thenId", stemIdThen)
      .uniqueResult(StemSet.class);
    
    if (stemSet == null && exceptionIfNotFound) {
      throw new StemSetNotFoundException("StemSet immediate if " + stemIdIf + ", then: " + stemIdThen);
    }
    
    return stemSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#findAllChildren(java.util.Collection, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<StemSet> findAllChildren(Collection<StemSet> stemSets, QueryOptions queryOptions) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    Set<String> stemSetIds = new LinkedHashSet<String>();
    for (StemSet stemSet : stemSets) {
      stemSetIds.add(stemSet.getId());
    }
    
    StringBuilder sql = new StringBuilder();
    sql.append("select ss from StemSet ss where ss.parentStemSetId in (");
    sql.append(HibUtils.convertToInClause(stemSetIds, byHqlStatic));
    sql.append(") ");
    
    Set<StemSet> result = byHqlStatic
      .createQuery(sql.toString())
      .options(queryOptions)
      .listSet(StemSet.class);
    
    return result; 
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.StemSetDAO#deleteByThenHasStemId(java.lang.String)
   */
  public void deleteByThenHasStemId(final String id) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
              //set parent to null so mysql doest get mad
              //http://bugs.mysql.com/bug.php?id=15746
              hibernateHandlerBean.getHibernateSession().byHql()
                .createQuery("update StemSet set parentStemSetId = null where thenHasStemId = :id")
                .setString("id", id).executeUpdate();
            }
            
            for (StemSet stemSet : findByThenHasStemId(id)) {
              stemSet.delete();
            }
            
            return null;
          }
    });
  }
}
