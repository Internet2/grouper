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
package edu.internet2.middleware.poc_secureUserData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGetPermissionAssignments;
import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsPermissionAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils.DbType;


/**
 * @author mchyzer
 * $Id$
 */

/**
 * main class that syncs the secure user data
 */
public class SudFullSync {
  
  /**
   * 
   */
  private static Logger LOG = Logger.getLogger(SudFullSync.class);


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    syncMemberships();
    syncRowAndColumnPermissions();
  }
  
  /**
   * sync up db memberships with whats in grouper
   */
  public static void syncMemberships() {
    
    //get the memberships from Grouper
    String rowGroupsFolder = GrouperClientUtils.propertiesValue("sud.rootFolder", true) + ":rowGroups";
    
    //get the group memberships there
    WsQueryFilter wsQueryFilter = new WsQueryFilter();
    wsQueryFilter.setStemName(rowGroupsFolder);
    wsQueryFilter.setStemNameScope("ONE_LEVEL");
    wsQueryFilter.setQueryFilterType("FIND_BY_STEM_NAME");
    WsGroup[] wsGroups = new GcFindGroups().assignQueryFilter(wsQueryFilter).execute().getGroupResults();
    
    Set<String> groupExtensionsFromGrouper = new TreeSet<String>();
    Set<String> groupExtensions = new TreeSet<String>();
    
    //add all from grouper
    for (WsGroup wsGroup : wsGroups) {
      String groupExtension = wsGroup.getExtension();
      groupExtensions.add(groupExtension);
      groupExtensionsFromGrouper.add(groupExtension);
    }
    
    //add all from DB
    Set<String> groupExtensionsFromDb = new TreeSet<String>(GcDbUtils.listSelect(String.class, 
        "select distinct group_extension from secureuserdata_memberships", 
        GrouperClientUtils.toList(DbType.STRING)));
    groupExtensions.addAll(groupExtensionsFromDb);
    
    //cycle through and sync up memberships
    for (String groupExtension : groupExtensions) {
      
      //if its not in grouper, delete from db
      if (!groupExtensionsFromGrouper.contains(groupExtension)) {
        
        int rows = GcDbUtils.executeUpdate("delete from secureuserdata_memberships where group_extension = ?", groupExtension);
        if (LOG.isInfoEnabled()) {
          LOG.info("Del " + rows + " mships of group: " + groupExtension);
        }
        continue;
      }
      
      Set<String> personidsInGrouper = new TreeSet<String>();
      
      {
        //sync up the memberships from db with the memberships from grouper
        WsGetMembersResult wsGetMembersResult =
          new GcGetMembers().addSourceId("jdbc").addGroupName(rowGroupsFolder + ":" + groupExtension).execute().getResults()[0];
        WsSubject[] wsSubjects = wsGetMembersResult.getWsSubjects();
        for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {
          
          personidsInGrouper.add(wsSubject.getId());
          
        }
      }      
      
      Set<String> personIdsInDb = new TreeSet<String>();
      
      List<SudMembership> membershipsInDbInGroup = SudMembership.retrieveAllInGroup(groupExtension);
      for (SudMembership sudMembership : GrouperClientUtils.nonNull(membershipsInDbInGroup)) {
        personIdsInDb.add(sudMembership.getPersonid());
      }
      
      //find deletes
      for (String personIdInDb : personIdsInDb) {
        if (!personidsInGrouper.contains(personIdInDb)) {
          
          //delete by query in case multiple
          int rows = GcDbUtils.executeUpdate("delete from secureuserdata_memberships where group_extension = ? and personid = ?", 
              GrouperClientUtils.toList((Object)groupExtension, personIdInDb));
          
          if (LOG.isInfoEnabled()) {
            LOG.info("Del " + rows + " mships of group: " 
                + groupExtension + ", personid: " + personIdInDb);
          }

        }
      }
      
      //find inserts
      for (String personidInGrouper : personidsInGrouper) {
        if (!personIdsInDb.contains(personidInGrouper)) {
          SudMembership sudMembership = new SudMembership();
          sudMembership.setGroupExtension(groupExtension);
          sudMembership.setPersonid(personidInGrouper);
          sudMembership.store();
          
          if (LOG.isInfoEnabled()) {
            LOG.info("Add mship for group: " 
                + groupExtension + ", personid: " + personidInGrouper);
          }

        }
      }
    }
  }

  /**
   * sync up db row permissions with whats in grouper
   */
  public static void syncRowAndColumnPermissions() {

    String permissionsFolderName = GrouperClientUtils.propertiesValue("sud.rootFolder", true) + ":permissions";

    WsGetPermissionAssignmentsResults wsPermissionAssignmentsResults = new GcGetPermissionAssignments()
      .addAttributeDefName(permissionsFolderName + ":rowOrColumnPermissionDef")
      .assignIncludeSubjectDetail(true).addAction("read").addAction("write")
      .assignPermissionProcessor("FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS").execute();

    //lets keep subjectId to subjectName map
    Map<String, WsSubject> subjectIdToSubjectMap = new HashMap<String, WsSubject>();
    
    //there is a bug in 2.0.0 where wsSubjects arexnt returned from WS: GRP-643
    {
      //sourceId, subjectId
      Set<MultiKey> subjectSet = new HashSet<MultiKey>();
      //get permissions from grouper
      for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(wsPermissionAssignmentsResults.getWsPermissionAssigns(), WsPermissionAssign.class)) {
        
        if (GrouperClientUtils.equals("g:gsa", wsPermissionAssign.getSourceId())) {
        
          subjectSet.add(new MultiKey(wsPermissionAssign.getSourceId(), wsPermissionAssign.getSubjectId()));
        }
      }      
      
      //lookup those subjects
      GcGetSubjects gcGetSubjects = new GcGetSubjects();
      for (MultiKey subject : subjectSet) {
        
        gcGetSubjects.addWsSubjectLookup(new WsSubjectLookup((String)subject.getKey(1), (String)subject.getKey(0), null));
        
      }
      WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
      
      for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsGetSubjectsResults.getWsSubjects(), WsSubject.class)) {
        subjectIdToSubjectMap.put(wsSubject.getId(), wsSubject);
      }
      
    }
    
    //this should be sufficient going forward after bug above is fixed
    for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsPermissionAssignmentsResults.getWsSubjects(), WsSubject.class)) {
      subjectIdToSubjectMap.put(wsSubject.getId(), wsSubject);
    }
    
    String rowsFolderName = permissionsFolderName + ":rows";
    String columnsFolderName = permissionsFolderName + ":columns";
    String schemaFolderName = GrouperClientUtils.propertiesValue("sud.rootFolder", true) + ":schemas";

    //MultiKey is schema_name, action, permissionExtension
    Set<MultiKey> rowPermissionsFromGrouper = new HashSet<MultiKey>();
    Set<MultiKey> columnPermissionsFromGrouper = new HashSet<MultiKey>();
    
    //get permissions from grouper
    for (WsPermissionAssign wsPermissionAssign : GrouperClientUtils.nonNull(wsPermissionAssignmentsResults.getWsPermissionAssigns(), WsPermissionAssign.class)) {
      
      String folderName = GrouperClientUtils.parentStemNameFromName(wsPermissionAssign.getAttributeDefNameName());
      String extension = GrouperClientUtils.extensionFromName(wsPermissionAssign.getAttributeDefNameName());
      
      //lets make sure the permission is for a group in the right folder
      if (!GrouperClientUtils.equals("g:gsa", wsPermissionAssign.getSourceId())) {
        continue;
      }
      
      WsSubject wsSubject = subjectIdToSubjectMap.get(wsPermissionAssign.getSubjectId());
      String groupFolderName = GrouperClientUtils.parentStemNameFromName(wsSubject.getName());
      
      String schemaName = GrouperClientUtils.defaultString(GrouperClientUtils.extensionFromName(wsSubject.getName())).toUpperCase();
      
      if (!GrouperClientUtils.equals(groupFolderName, schemaFolderName)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Wrong folder for schema name: " + groupFolderName + ", but should be: " + schemaFolderName);
        }
        continue;
      }
      
      //action needs to be read or write (should be based on query
      if (!GrouperClientUtils.equals("read", wsPermissionAssign.getAction()) && !GrouperClientUtils.equals("write", wsPermissionAssign.getAction())) {
        continue;
      }
      
      if (GrouperClientUtils.equals(folderName, rowsFolderName)) {
        
        if (!extension.startsWith("rows_")) {
          LOG.debug("Skipping since extension doesnt start with rows_ : " + extension);
        }
        
        String permissionName = extension.substring("rows_".length());
        
        MultiKey multiKey = new MultiKey(schemaName, wsPermissionAssign.getAction(), permissionName);
        rowPermissionsFromGrouper.add(multiKey);
      } else if (GrouperClientUtils.equals(folderName, columnsFolderName)) {
        if (!extension.startsWith("columns_")) {
          LOG.debug("Skipping since extension doesnt start with columns_ : " + extension);
        }
        
        String permissionName = extension.substring("columns_".length());
        
        MultiKey multiKey = new MultiKey(schemaName, wsPermissionAssign.getAction(), permissionName);
        columnPermissionsFromGrouper.add(multiKey);
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Permission is in the wrong folder, should be in rows or cols: " + folderName + ", " + wsPermissionAssign.getAttributeAssignId());
        }
      }
    }

    //add all from DB
    Set<MultiKey> rowPermissionsFromDb = new HashSet<MultiKey>();
    Set<MultiKey> columnPermissionsFromDb = new HashSet<MultiKey>();

    List<SudColPermission> sudColPermissions = SudColPermission.retrieveAllColPermissions();
    List<SudRowPermission> sudRowPermissions = SudRowPermission.retrieveAllRowPermissions();
    
    for (SudColPermission sudColPermission : sudColPermissions) {
      MultiKey multiKey = new MultiKey(sudColPermission.getSchemaName(), sudColPermission.getAction(), sudColPermission.getColset());
      columnPermissionsFromDb.add(multiKey);
    }
    
    for (SudRowPermission sudRowPermission : sudRowPermissions) {
      MultiKey multiKey = new MultiKey(sudRowPermission.getSchemaName(), sudRowPermission.getAction(), sudRowPermission.getGroupExtension());
      rowPermissionsFromDb.add(multiKey);
    }

    //find deletes
    for (MultiKey multiKey : rowPermissionsFromDb) {
      if (!rowPermissionsFromGrouper.contains(multiKey)) {
        //delete by query in case multiple
        int rows = GcDbUtils.executeUpdate("delete from secureuserdata_row_permiss where schema_name = ? and action = ? and group_extension = ?", 
            GrouperClientUtils.toList(multiKey.getKey(0), multiKey.getKey(1), multiKey.getKey(2)));
        
        if (LOG.isInfoEnabled()) {
          LOG.info("Del " + rows + " row permiss schema: " 
              + multiKey.getKey(0) + ", action: " + multiKey.getKey(1) + ", group: " + multiKey.getKey(2));
        }

      }
    }
    
    for (MultiKey multiKey : columnPermissionsFromDb) {
      if (!columnPermissionsFromGrouper.contains(multiKey)) {
        //delete by query in case multiple
        int rows = GcDbUtils.executeUpdate("delete from secureuserdata_col_permiss where schema_name = ? and action = ? and colset = ?", 
            GrouperClientUtils.toList(multiKey.getKey(0), multiKey.getKey(1), multiKey.getKey(2)));
        
        if (LOG.isInfoEnabled()) {
          LOG.info("Del " + rows + " col permiss schema: " 
              + multiKey.getKey(0) + ", action: " + multiKey.getKey(1) + ", cols: " + multiKey.getKey(2));
        }

      }
    }
    
    //inserts
    for (MultiKey multiKey : rowPermissionsFromGrouper) {
      if (!rowPermissionsFromDb.contains(multiKey)) {
        SudRowPermission sudRowPermission = new SudRowPermission();
        sudRowPermission.setSchemaName((String)multiKey.getKey(0));
        sudRowPermission.setAction((String)multiKey.getKey(1));
        sudRowPermission.setGroupExtension((String)multiKey.getKey(2));
        sudRowPermission.store();
          
        if (LOG.isInfoEnabled()) {
          LOG.info("Add row permiss schema: " 
            + multiKey.getKey(0) + ", action: " + multiKey.getKey(1) + ", group: " + multiKey.getKey(2));
        }

      }
    }
    
    for (MultiKey multiKey : columnPermissionsFromGrouper) {
      if (!columnPermissionsFromDb.contains(multiKey)) {
        SudColPermission sudColPermission = new SudColPermission();
        sudColPermission.setSchemaName((String)multiKey.getKey(0));
        sudColPermission.setAction((String)multiKey.getKey(1));
        sudColPermission.setColset((String)multiKey.getKey(2));
        sudColPermission.store();
          
        if (LOG.isInfoEnabled()) {
          LOG.info("Add col permiss schema: " 
            + multiKey.getKey(0) + ", action: " + multiKey.getKey(1) + ", cols: " + multiKey.getKey(2));
        }
      }
    }
    
  }

}
