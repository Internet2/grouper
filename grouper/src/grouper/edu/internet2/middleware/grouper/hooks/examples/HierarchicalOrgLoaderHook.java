/**
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
 */
/*
 * @author mchyzer
 * $Id: HierarchicalOrgLoaderHook.java,v 1.4 2009-10-18 16:30:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.LoaderHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLoaderBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class HierarchicalOrgLoaderHook extends LoaderHooks {

  /**
   * one node of the ord hierarchy
   */
  private static class OrgHierarchyNode {
    
    /** org id */
    private String orgId;
  
    /** org name */
    private String orgName;
    
    /** org display name */
    private String orgDisplayName;
    
    /** org parent id */
    private String orgParentId;
  
    /**
     * org id 
     * @return the org id
     */
    public String getOrgId() {
      return this.orgId;
    }
  
    /**
     * org id
     * @param orgId1
     */
    public void setOrgId(String orgId1) {
      this.orgId = orgId1;
    }
  
    /**
     * org name
     * @return org name
     */
    public String getOrgName() {
      return this.orgName;
    }
  
    /**
     * org name
     * @param orgName1
     */
    public void setOrgName(String orgName1) {
      this.orgName = orgName1;
    }
  
    /**
     * parent id
     * @return parent id
     */
    public String getOrgParentId() {
      return this.orgParentId;
    }
  
    /**
     * parent id
     * @param orgParentId1
     */
    public void setOrgParentId(String orgParentId1) {
      this.orgParentId = orgParentId1;
    }
    
    /**
     * return the parent of this node by the map of all nodes
     * @param allNodes
     * @return the node or null if this is the parent
     */
    public OrgHierarchyNode getParent(Map<String, OrgHierarchyNode> allNodes) {
      if (StringUtils.isBlank(this.orgParentId)) {
        return null;
      }
      OrgHierarchyNode parent = allNodes.get(this.orgParentId);
      if (parent == null) {
        throw new RuntimeException("Cant find parent: " + this.orgParentId);
      }
      return parent;
    }
  
    /**
     * return the parent of this node by the map of all nodes
     * @param allNodes
     * @return the node or null if this is the parent
     */
    public boolean hasChildren(Map<String, OrgHierarchyNode> allNodes) {
      for (OrgHierarchyNode orgHierarchyNode : allNodes.values()) {
        if (StringUtils.equals(orgHierarchyNode.getOrgParentId(), this.orgId)) {
          return true;
        }
      }
      return false;
    }
  
    /**
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "Org node: id: " + this.orgId + ", name: " + this.orgName + ", parentId: " + this.orgParentId; 
    }
  
    
    /**
     * 
     * @param allNodes 
     * @return the string
     * @see java.lang.Object#toString()
     */
    public String toString(Map<String, OrgHierarchyNode> allNodes) {
      return "Org node: id: " + this.orgId + ", name: " 
          + this.orgName + ", parentId: " + this.orgParentId + ", groupName: " + this.groupName(allNodes); 
    }
  
    /**
     * get the sorname for this group
     * @param allNodes
     * @return the sor name
     */
    public String groupSorName(Map<String, OrgHierarchyNode> allNodes) {
      String name = this.groupName(allNodes);
      String sorSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.extension.suffix");
      sorSuffix = StringUtils.defaultIfEmpty(sorSuffix, "_systemOfRecord");
      String sorName = name + sorSuffix;
      return sorName;
    }
    
    /**
     * stem name for this org
     * @param allNodes
     * @return the stem name
     */
    public String stemName(Map<String, OrgHierarchyNode> allNodes) {
      //get a list of nodes
      List<OrgHierarchyNode> hierarchy = new ArrayList<OrgHierarchyNode>();
      
      OrgHierarchyNode parent = this;
      
      while (true) {
        parent = parent.getParent(allNodes);
        if (parent == null) {
          break;
        }
        //insert this to the front
        hierarchy.add(0, parent);
      }
      
      //now generate the group name
      String parentStem = GrouperConfig.retrieveConfig().propertyValueString("orgs.parentStemName");
      if (StringUtils.isBlank(parentStem)) {
        parentStem = "poc:orgs";
      }
      parentStem = GrouperUtil.stripSuffix(parentStem, ":");
      StringBuilder result = new StringBuilder(parentStem);
      for (int i=0; i<hierarchy.size(); i++) {
        result.append(":").append(hierarchy.get(i).getOrgName());
      }
      result.append(":").append(this.getOrgName());
      
      return result.toString();
    }
    
    /**
     * 
     * @param allNodes
     * @return the group name
     */
    public String groupName(Map<String, OrgHierarchyNode> allNodes) {
      
      return this.stemName(allNodes) + ":" + this.getOrgName() + "_group";
      
    }
  
    /**
     * 
     * @param allNodes
     * @return the group name
     */
    public String allName(Map<String, OrgHierarchyNode> allNodes) {
      
      return this.stemName(allNodes) + ":" + this.getOrgName() + "_all";
      
    }
  
    /**
     * 
     * @return the group name
     */
    public String getAllDisplayName() {
      String displayName = this.getOrgDisplayName();
            
      String includeExcludeDisplaySuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.displayExtension.suffix");
      includeExcludeDisplaySuffix = StringUtils.defaultIfEmpty(includeExcludeDisplaySuffix, "${space}system of record");
      includeExcludeDisplaySuffix = StringUtils.replace(includeExcludeDisplaySuffix, "${space}", " ");
  
      displayName = StringUtils.removeEnd(displayName, includeExcludeDisplaySuffix);
      
      return displayName + " All";
      
    }
  
    /**
     * 
     * @param allNodes 
     * @return the group name
     */
    public String allSorName(Map<String, OrgHierarchyNode> allNodes) {
      
      
      String sorSuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.extension.suffix");
      sorSuffix = StringUtils.defaultIfEmpty(sorSuffix, "_systemOfRecord");
  
      return this.allName(allNodes) + sorSuffix;
      
    }
  
    /**
     * @return the group name
     */
    public String getAllSorDisplayName() {
      
      String includeExcludeDisplaySuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.displayExtension.suffix");
      includeExcludeDisplaySuffix = StringUtils.defaultIfEmpty(includeExcludeDisplaySuffix, "${space}system of record");
      includeExcludeDisplaySuffix = StringUtils.replace(includeExcludeDisplaySuffix, "${space}", " ");
  
      return this.getAllDisplayName() + includeExcludeDisplaySuffix;
      
    }
  
    /**
     * @return the group name
     */
    public String getAllSorDescription() {
      
      return "Members of " + this.getOrgName() + " and all groups underneath the hierarchy";
      
    }
  
  
    /**
     * org display name
     * @return org display name
     */
    public String getOrgDisplayName() {
      return this.orgDisplayName;
    }
  
    /**
     * org display name
     * @param orgDisplayName1
     */
    public void setOrgDisplayName(String orgDisplayName1) {
      this.orgDisplayName = orgDisplayName1;
    }
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.LoaderHooks#loaderPreRun(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksLoaderBean)
   */
  @Override
  public void loaderPreRun(HooksContext hooksContext, HooksLoaderBean preRunBean) {
    //we want this hook to kick in if the group name is the org group name:
    String orgsGroupConfigName = GrouperConfig.retrieveConfig().propertyValueString("orgs.configGroupName");
    if (StringUtils.isBlank(orgsGroupConfigName)) {
      throw new RuntimeException("Why is the orgs config name not configured in grouper properties? orgs.configGroupName");
    }
    if (StringUtils.equals(orgsGroupConfigName, preRunBean.getLoaderJobBean().getGroupNameOverall())) {
      
      //we are about to run the orgs group config loader job, we should sync up the table
      syncUpHierarchicalOrgTable();
      
    }
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    syncUpHierarchicalOrgTable();
  }

  /**
   * get all existing nodes from the db
   * @param grouperLoaderDb 
   * @return the map of all nodes, id to name
   */
  public static Map<String, String> retrieveAllExistingNodes(GrouperLoaderDb grouperLoaderDb) {
    
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    String query = "select org_id, org_hierarchical_name from grouperorgs_hierarchical";
    try {
      connection = grouperLoaderDb.connection();
      try {
        // create and execute a SELECT
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);
        
        Map<String, String> allExistingNodes = new LinkedHashMap<String, String>();
        
        while(resultSet.next()) {
          allExistingNodes.put(resultSet.getString(1), resultSet.getString(2));
        }
        return allExistingNodes;
      } finally {
        //this is important so no one sneaks some delete statement in there
        GrouperUtil.rollbackQuietly(connection);
      }
    } catch (SQLException se) {
      throw new RuntimeException("Problem with query: " + query + ",  on db: " + grouperLoaderDb, se);
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
    }
  }

  /**
   * get all nodes from the db in map of id to OrgHierarchyNode object
   * @param grouperLoaderDb 
   * @param orgsTableName 
   * @param orgsIdCol 
   * @param orgsNameCol 
   * @param orgsParentIdCol 
   * @param orgsDisplayNameCol 
   * @return the map of all nodes
   */
  public static Map<String, OrgHierarchyNode> retrieveAllNodes(GrouperLoaderDb grouperLoaderDb, String orgsTableName,
      String orgsIdCol, String orgsNameCol, String orgsParentIdCol, String orgsDisplayNameCol) {
    
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    String query = "select " + orgsIdCol + ", " + orgsNameCol + ", " + orgsParentIdCol + ", " + orgsDisplayNameCol
      + " from " + orgsTableName;
    
    String includeExcludeDisplaySuffix = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.systemOfRecord.displayExtension.suffix");
    includeExcludeDisplaySuffix = StringUtils.defaultIfEmpty(includeExcludeDisplaySuffix, "${space}system of record");
    includeExcludeDisplaySuffix = StringUtils.replace(includeExcludeDisplaySuffix, "${space}", " ");
        
    try {
      connection = grouperLoaderDb.connection();
      try {
        // create and execute a SELECT
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);
        
        Map<String, OrgHierarchyNode> allNodes = new LinkedHashMap<String, OrgHierarchyNode>();
        
        while(resultSet.next()) {
          //assume everything is a string
          OrgHierarchyNode orgHierarchyNode = new OrgHierarchyNode();
          orgHierarchyNode.setOrgId(resultSet.getString(1));
          orgHierarchyNode.setOrgName(resultSet.getString(2));
          orgHierarchyNode.setOrgParentId(resultSet.getString(3));
  
          String displayName = resultSet.getString(4);
          displayName += includeExcludeDisplaySuffix;
          orgHierarchyNode.setOrgDisplayName(displayName);
  
          allNodes.put(orgHierarchyNode.getOrgId(), orgHierarchyNode);
        }
        return allNodes;
      } finally {
        //this is important so no one sneaks some delete statement in there
        GrouperUtil.rollbackQuietly(connection);
      }
    } catch (SQLException se) {
      throw new RuntimeException("Problem with query: " + query + ",  on db: " + grouperLoaderDb, se);
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
    }
  }

  /**
   * make sure the org hierarchical table is in sync
   */
  public static void syncUpHierarchicalOrgTable() {
    String orgsDatabaseName = GrouperConfig.retrieveConfig().propertyValueString("orgs.databaseName");
    GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(orgsDatabaseName);
  
  
    String orgsTableName = GrouperConfig.retrieveConfig().propertyValueString("orgs.orgTableName");
    
    String orgsIdCol = GrouperConfig.retrieveConfig().propertyValueString("orgs.orgIdCol");
    String orgsNameCol = GrouperConfig.retrieveConfig().propertyValueString("orgs.orgNameCol");
    String orgsDisplayNameCol = GrouperConfig.retrieveConfig().propertyValueString("orgs.orgDisplayNameCol");
    
    String orgsParentIdCol = GrouperConfig.retrieveConfig().propertyValueString("orgs.orgParentIdCol");
    
    Map<String, OrgHierarchyNode> allNodes = retrieveAllNodes(grouperLoaderDb, 
        orgsTableName, orgsIdCol, orgsNameCol, orgsParentIdCol, orgsDisplayNameCol);
    
    Map<String, String> existingNodes = retrieveAllExistingNodes(grouperLoaderDb);
    
    syncUpTables(grouperLoaderDb, allNodes, existingNodes);
    
    //for (OrgHierarchyNode orgHierarchyNode : allNodes.values()) {
    //  System.out.println(orgHierarchyNode.toString(allNodes));
    //}
    
  }

  /**
   * get all existing nodes from the db
   * @param grouperLoaderDb 
   * @param allNodes 
   * @param existingNodes 
   */
  public static void syncUpTables(GrouperLoaderDb grouperLoaderDb, 
      Map<String, OrgHierarchyNode> allNodes, Map<String, String> existingNodes) {
    
    Connection connection = null;
    PreparedStatement statement = null;
    try {
      connection = grouperLoaderDb.connection();
      connection.setAutoCommit(false);
      try {
        
        // delete query
        statement = connection.prepareStatement("delete from grouperorgs_hierarchical where org_id = ?");
  
        try {
          Iterator<String> iterator = existingNodes.keySet().iterator();
          //first lets do delete statements
          while (iterator.hasNext()) {
            String existingId = iterator.next();
            if (!allNodes.containsKey(existingId)) {
              statement.setString(1, existingId);
              statement.executeUpdate();
              iterator.remove();
            }
          }
        } finally {
          GrouperUtil.closeQuietly(statement);
        }
  
        // update query
        statement = connection.prepareStatement("update grouperorgs_hierarchical set org_hierarchical_name = ?, " +
        		"org_hierarchical_sor_name = ?, org_hierarchical_sor_disp_name = ?, org_hierarchical_stem = ?, " +
        		"org_hier_all_name = ?, org_hier_all_display_name = ?, org_hier_all_sor_name = ?, " +
        		"org_hier_all_sor_display_name = ?, org_hier_all_sor_description = ? where org_id = ?");
  
        try {
  
          //then lets do update statements
          for (String existingId : existingNodes.keySet()) {
            OrgHierarchyNode orgHierarchyNode = allNodes.get(existingId);
            String newName = orgHierarchyNode.groupName(allNodes);
            String newStemName = orgHierarchyNode.stemName(allNodes);
            String newSorName = orgHierarchyNode.groupSorName(allNodes);
            String newDisplayName = orgHierarchyNode.getOrgDisplayName();
  
            boolean hasChildren = orgHierarchyNode.hasChildren(allNodes);
  
            String newAllName = hasChildren ? orgHierarchyNode.allName(allNodes) : null;
            String newAllDisplayName = hasChildren ? orgHierarchyNode.getAllDisplayName() : null;
            String newAllSorName = hasChildren ? orgHierarchyNode.allSorName(allNodes) : null;
            String newAllSorDisplayName = hasChildren ? orgHierarchyNode.getAllSorDisplayName() : null;
            String newAllSorDescription = hasChildren ? orgHierarchyNode.getAllSorDescription() : null;
            
            statement.setString(1, newName);
            statement.setString(2, newSorName);
            statement.setString(3, newDisplayName);
            statement.setString(4, newStemName);
            statement.setString(5, newAllName);
            statement.setString(6, newAllDisplayName);
            statement.setString(7, newAllSorName);
            statement.setString(8, newAllSorDisplayName);
            statement.setString(9, newAllSorDescription);
            statement.setString(10, existingId);
            statement.executeUpdate();
          }
        } finally {
          GrouperUtil.closeQuietly(statement);
        }
        
        
        // insert statement
        statement = connection.prepareStatement("insert into grouperorgs_hierarchical (org_id, org_hierarchical_name, " +
        		"org_hierarchical_sor_name, org_hierarchical_sor_disp_name, org_hierarchical_stem, org_hier_all_name, " +
        		"org_hier_all_display_name, org_hier_all_sor_name, " +
            "org_hier_all_sor_display_name, org_hier_all_sor_description) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
  
        try {
  
          //first lets do insert statements
          for (String newId : allNodes.keySet()) {
            
            if (!existingNodes.containsKey(newId)) {
              statement.setString(1, newId);
              OrgHierarchyNode orgHierarchyNode = allNodes.get(newId);
              String groupName = orgHierarchyNode.groupName(allNodes);
              statement.setString(2, groupName);
              String groupSorName = orgHierarchyNode.groupSorName(allNodes);
              statement.setString(3, groupSorName);
              statement.setString(4, orgHierarchyNode.getOrgDisplayName());
              statement.setString(5, orgHierarchyNode.stemName(allNodes));
              boolean hasChildren = orgHierarchyNode.hasChildren(allNodes);
              String newAllName = hasChildren ? orgHierarchyNode.allName(allNodes) : null;
              String newAllDisplayName = hasChildren ? orgHierarchyNode.getAllDisplayName() : null;
              String newAllSorName = hasChildren ? orgHierarchyNode.allSorName(allNodes) : null;
              String newAllSorDisplayName = hasChildren ? orgHierarchyNode.getAllSorDisplayName() : null;
              String newAllSorDescription = hasChildren ? orgHierarchyNode.getAllSorDescription() : null;
              statement.setString(6, newAllName);
              statement.setString(7, newAllDisplayName);
              statement.setString(8, newAllSorName);
              statement.setString(9, newAllSorDisplayName);
              statement.setString(10, newAllSorDescription);
              statement.executeUpdate();
            }
          }
        } finally {
          GrouperUtil.closeQuietly(statement);
        }
  
        //commit
        connection.commit();
        
      } finally {
        //this is important so if there is a problem it wont commit
        GrouperUtil.rollbackQuietly(connection);
      }
    } catch (SQLException se) {
      throw new RuntimeException("Problem with db: " + grouperLoaderDb, se);
    } finally {
      GrouperUtil.closeQuietly(connection);
    }
  }


}
