package edu.internet2.middleware.poc_secureUserData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
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
   * @param args
   */
  public static void main(String[] args) {
    syncMemberships();
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
    WsGroup[] wsGroups = new GcFindGroups().assignQueryFilter(wsQueryFilter).execute().getGroupResults();
    
    List<String> groupExtensionsFromGrouper = new ArrayList<String>();
    List<String> groupExtensions = new ArrayList<String>();
    
    //add all from grouper
    for (WsGroup wsGroup : wsGroups) {
      String groupExtension = wsGroup.getExtension();
      groupExtensions.add(groupExtension);
      groupExtensionsFromGrouper.add(groupExtension);
    }
    
    //add all from DB
    List<String> groupExtensionsFromDb = GcDbUtils.listSelect(String.class, 
        "select distinct group_extension from secureuserdata_row_permiss", 
        GrouperClientUtils.toList(DbType.STRING));
    groupExtensions.addAll(groupExtensionsFromDb);
    
    //cycle through and sync up memberships
    for (String groupExtension : groupExtensions) {
      
      //if its not in grouper, delete from db
      if (!groupExtensionsFromGrouper.contains(groupExtension)) {
        
        GcDbUtils.executeUpdate("delete from secureuserdata_memberships where group_extension = ?", groupExtension);
        continue;
      }
      
      Set<String> personidsInGrouper = new HashSet<String>();
      
      {
        //sync up the memberships from db with the memberships from grouper
        WsGetMembersResult wsGetMembersResult =
          new GcGetMembers().addSourceId("jdbc").addGroupName(rowGroupsFolder + ":" + groupExtension).execute().getResults()[0];
        WsSubject[] wsSubjects = wsGetMembersResult.getWsSubjects();
        for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {
          
          personidsInGrouper.add(wsSubject.getId());
          
        }
      }      
      
      Set<String> personIdsInDb = new HashSet<String>();
      
      List<SudMembership> membershipsInDbInGroup = SudMembership.retrieveAllInGroup(groupExtension);
      for (SudMembership sudMembership : GrouperClientUtils.nonNull(membershipsInDbInGroup)) {
        personIdsInDb.add(sudMembership.getPersonid());
      }
      
      //find deletes
      for (String personIdInDb : personIdsInDb) {
        if (!personidsInGrouper.contains(personIdInDb)) {
          
          //delete by query in case multiple
          GcDbUtils.executeUpdate("delete from secureuserdata_memberships where group_extension = ? and personid = ?", 
              GrouperClientUtils.toList((Object)groupExtension, personIdInDb));
        }
      }
      
      //find inserts
      for (String personidInGrouper : personidsInGrouper) {
        if (!personIdsInDb.contains(personidInGrouper)) {
          SudMembership sudMembership = new SudMembership();
          sudMembership.setGroupExtension(groupExtension);
          sudMembership.setPersonid(personidInGrouper);
        }
      }
      
    }
    
  }
}
