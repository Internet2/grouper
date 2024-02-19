/**
 * Copyright 2014 Internet2
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
 */
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.membershipRequire.GrouperMembershipRequireChangeDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataAliasDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignDao;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.RegistryDAO;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupUser;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * Basic Hibernate <code>Registry</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3RegistryDAO.java,v 1.24 2009-10-26 02:26:07 mchyzer Exp $
 * @since   @HEAD@
 */
class Hib3RegistryDAO implements RegistryDAO {

  /** */
  private static final boolean PRINT_DDL_TO_CONSOLE = false;
  /** */
  private static final boolean EXPORT_DDL_TO_DB     = true;
  
  /**
   * @param includeTypesAndFields 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void reset(final boolean includeTypesAndFields) 
    throws  GrouperDAOException {

    // this doesnt need tx right?
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
            new GcDbAccess().sql("delete from grouper_sql_cache_mship").executeSql();
            new GcDbAccess().sql("delete from grouper_sql_cache_group").executeSql();

            new GcDbAccess().sql("delete from grouper_failsafe").executeSql();
            new GcDbAccess().sql("delete from grouper_last_login").executeSql();
            new GcDbAccess().sql("delete from grouper_stem_view_privilege").executeSql();
            new GcDbAccess().sql("delete from grouper_prov_zoom_user").executeSql();
            new GcDbAccess().sql("delete from grouper_prov_duo_user").executeSql();
            GrouperMembershipRequireChangeDao.deleteAllRecords();
            hibernateSession.getSession().flush();
            
            Hib3TableIndexDAO.reset(hibernateSession);
            Hib3RoleSetDAO.reset(hibernateSession);
            Hib3AttributeAssignValueDAO.reset(hibernateSession);
            Hib3AttributeAssignActionSetDAO.reset(hibernateSession);
            Hib3AttributeDefScopeDAO.reset(hibernateSession);
            Hib3AttributeAssignDAO.reset(hibernateSession);
            Hib3AttributeAssignActionDAO.reset(hibernateSession);
            Hib3AttributeDefNameSetDAO.reset(hibernateSession);
            Hib3AttributeDefNameDAO.reset(hibernateSession);

            Hib3AuditEntryDAO.reset(hibernateSession);
            Hib3AuditTypeDAO.reset(hibernateSession);
            Hib3ChangeLogConsumerDAO.reset(hibernateSession);
            Hib3ChangeLogEntryDAO.reset(hibernateSession);
            Hib3ChangeLogTypeDAO.reset(hibernateSession);
            Hib3ConfigDAO.reset(hibernateSession);
            Hib3GrouperLoaderLog.reset(hibernateSession);
            Hib3GrouperPasswordRecentlyUsedDAO.reset(hibernateSession);
            Hib3GrouperPasswordDAO.reset(hibernateSession);
            
            GcGrouperSyncDependencyGroupGroup.reset();
            GcGrouperSyncDependencyGroupUser.reset();

            Hib3GroupSetDAO.reset(hibernateSession);
            Hib3MembershipDAO.reset(hibernateSession);
            Hib3AttributeDefDAO.reset(hibernateSession);            
            Hib3CompositeDAO.reset(hibernateSession);
            Hib3GroupDAO.reset(hibernateSession);
            Hib3StemSetDAO.reset(hibernateSession);
            Hib3StemDAO.reset(hibernateSession);
            Hib3FieldDAO.reset(hibernateSession);
            Hib3MessageDAO.reset(hibernateSession);
            Hib3GrouperFileDAO.reset(hibernateSession);

            GcGrouperSyncLog.reset();
            GcGrouperSyncMembership.reset();
            GcGrouperSyncGroup.reset();
            GcGrouperSyncMember.reset();
            GcGrouperSyncJob.reset();
            GcGrouperSync.reset();
            
            //we need to flush since the next query will run a sql
            hibernateSession.getSession().flush();
            Hib3MemberDAO.reset(hibernateSession);
            Hib3RegistrySubjectAttributeDAO.reset(hibernateSession);
            Hib3RegistrySubjectDAO.reset(hibernateSession);

            Hib3PITAttributeAssignValueDAO.reset(hibernateSession);
            Hib3PITRoleSetDAO.reset(hibernateSession);
            Hib3PITAttributeAssignActionSetDAO.reset(hibernateSession);
            Hib3PITAttributeAssignDAO.reset(hibernateSession);
            Hib3PITAttributeAssignActionDAO.reset(hibernateSession);
            Hib3PITAttributeDefNameSetDAO.reset(hibernateSession);
            Hib3PITAttributeDefNameDAO.reset(hibernateSession);
            
            Hib3PITMembershipDAO.reset(hibernateSession);
            Hib3PITGroupSetDAO.reset(hibernateSession);
            Hib3PITGroupDAO.reset(hibernateSession);
            Hib3PITAttributeDefDAO.reset(hibernateSession);
            Hib3PITStemDAO.reset(hibernateSession);
            Hib3PITFieldDAO.reset(hibernateSession);
            Hib3PITMemberDAO.reset(hibernateSession);
            Hib3PITConfigDAO.reset(hibernateSession);

            Hib3ExternalSubjectAttributeDAO.reset(hibernateSession);
            Hib3ExternalSubjectDAO.reset(hibernateSession);
            
            new GcDbAccess().sql("delete from grouper_recent_mships_conf").executeSql();
            new GcDbAccess().sql("delete from grouper_stem_view_privilege").executeSql();
            new GcDbAccess().sql("delete from grouper_last_login").executeSql();
            
            new edu.internet2.middleware.grouper.misc.AddMissingGroupSets().showResults(false).addAllMissingGroupSets();
            
            GrouperDataRowFieldAssignDao.reset();
            GrouperDataFieldAssignDao.reset();
            GrouperDataRowAssignDao.reset();
            GrouperDataAliasDao.reset();
            GrouperDataFieldDao.reset();
            GrouperDataRowDao.reset();
            GrouperDataProviderDao.reset();

            Hib3MemberDAO.reset(hibernateSession);
            Hib3PITMemberDAO.reset(hibernateSession);
            GrouperDictionaryDao.reset();
            
            return null;
          }
      
    });
    
  } 

} 

