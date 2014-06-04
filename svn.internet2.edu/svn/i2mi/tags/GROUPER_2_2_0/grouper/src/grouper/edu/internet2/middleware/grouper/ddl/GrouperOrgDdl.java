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
/*
 * @author mchyzer
 * $Id: GrouperOrgDdl.java,v 1.4 2009-10-27 15:15:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;
import java.util.List;
import java.util.Random;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * ddl config file for the org hook / poc
 */
public enum GrouperOrgDdl implements DdlVersionable {

  /** first version of org test grouper, this is all dynamic */
  V1 {
    /**
     * @see GrouperOrgDdl#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, DdlVersionBean ddlVersionBean) {

      {
        //see if the grouperorgs_hierarchical table is there
        Table grouperOrgHierarchicalTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouperorgs_hierarchical");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_id",
            Types.VARCHAR, GrouperDdl.ID_SIZE, true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hierarchical_stem", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hierarchical_name", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hierarchical_sor_name", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hierarchical_sor_disp_name", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hier_all_name", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hier_all_display_name", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hier_all_sor_name", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hier_all_sor_display_name", 
            Types.VARCHAR, "4000", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgHierarchicalTable, "org_hier_all_sor_description", 
            Types.VARCHAR, "4000", false, false);


      }

      {
        boolean hasPocOrgsTable = database.findTable("grouperorgs_poc_orgs") != null;
        
        //table for org definitions
        //see if the grouperorgs_orgs table is there
        Table grouperOrgTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouperorgs_poc_orgs");

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgTable, "id",
            Types.VARCHAR, GrouperDdl.ID_SIZE, true, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgTable, "org_name", 
            Types.VARCHAR, "128", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgTable, "org_display_name", 
            Types.VARCHAR, "128", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgTable, "org_description", 
            Types.VARCHAR, "500", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgTable, "parent_id", 
            Types.VARCHAR, GrouperDdl.ID_SIZE, false, false);

        if (!hasPocOrgsTable) {
          
          //if it wasnt there, lets give some insert statements
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
          		"(id, org_name, org_display_name, org_description, parent_id) values ('1', 'org_1', " +
          		  "'Org 1 - Arts and Sciences', 'Org 1 - Arts and Sciences School description', null);\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('2', 'org_2', " +
                "'Org 2 - Engineering', 'Org 2 - Engineering School description', null);\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('3', 'org_3', " +
                "'Org 3 - Executive VP', 'Org 3 - Executive VP department description', null);\n");

          //second level
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('11', 'org_11', " +
                "'Org 11 - Math', 'Org 11 - Math Department description', '1');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('12', 'org_12', " +
                "'Org 12 - History', 'Org 12 - History Department description', '1');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('13', 'org_13', " +
                "'Org 13 - Chemistry', 'Org 13 - Chemistry Department description', '1');\n");
          
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('21', 'org_21', " +
                "'Org 21 - Mechanical Engineering', 'Org 21 - Mechanical Engineering description', '2');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('22', 'org_22', " +
                "'Org 22 - Electrical Engineering', 'Org 22 - Electrical Engineering description', '2');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('23', 'org_23', " +
                "'Org 23 - Computer Engineering', 'Org 23 - Computer Engineering description', '2');\n");

          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('31', 'org_31', " +
                "'Org 31 - Information Technology', 'Org 31 - Information Technology description', '3');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('32', 'org_32', " +
                "'Org 32 - Facilities', 'Org 32 - Facilities description', '3');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('33', 'org_33', " +
                "'Org 33 - Security', 'Org 33 - Security description', '3');\n");

          //third level
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('111', 'org_111', " +
                "'Org 111 - Math Professors', 'Org 111 - Math Professors description', '11');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('112', 'org_112', " +
                "'Org 112 - Math Admins', 'Org 112 - Math Admins description', '11');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('113', 'org_113', " +
                "'Org 113 - Math Researchers', 'Org 113 - Math Researchers description', '11');\n");

          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('121', 'org_121', " +
                "'Org 121 - History Professors', 'Org 121 - History Professors description', '12');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('122', 'org_122', " +
                "'Org 122 - History Admins', 'Org 122 - History Admins description', '12');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('123', 'org_123', " +
                "'Org 123 - History Researchers', 'Org 123 - History Researchers description', '12');\n");
          
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('131', 'org_131', " +
                "'Org 131 - Chemistry Professors', 'Org 131 - Chemistry Professors description', '13');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('132', 'org_132', " +
                "'Org 132 - Chemistry Admins', 'Org 132 - Chemistry Admins description', '13');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('133', 'org_133', " +
                "'Org 133 - Chemistry Researchers', 'Org 133 - Chemistry Researchers description', '13');\n");

          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('211', 'org_211', " +
                "'Org 211 - Mechanical Engineering Professors', 'Org 211 - Mechanical Engineering Professors description', '21');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('212', 'org_212', " +
                "'Org 212 - Mechanical Engineering Admins', 'Org 212 - Mechanical Engineering Admins description', '21');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('213', 'org_213', " +
                "'Org 213 - Mechanical Engineering Researchers', 'Org 213 - Mechanical Engineering Researchers description', '21');\n");

          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('221', 'org_221', " +
                "'Org 221 - Electrical Engineering Professors', 'Org 221 - Electrical Engineering Professors description', '22');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('222', 'org_222', " +
                "'Org 222 - Electrical Engineering Admins', 'Org 222 - Electrical Engineering Admins description', '22');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('223', 'org_223', " +
                "'Org 223 - Electrical Engineering Researchers', 'Org 223 - Electrical Engineering Researchers description', '22');\n");

          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('231', 'org_231', " +
                "'Org 231 - Computer Engineering Professors', 'Org 231 - Computer Engineering Professors description', '23');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('232', 'org_232', " +
                "'Org 232 - Computer Engineering Admins', 'Org 232 - Computer Engineering Admins description', '23');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('233', 'org_233', " +
                "'Org 233 - Computer Engineering Researchers', 'Org 233 - Computer Engineering Researchers description', '23');\n");
          
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('311', 'org_311', " +
                "'Org 311 - IT Applications', 'Org 311 - IT Applications description', '31');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('312', 'org_312', " +
                "'Org 312 - IT Networking', 'Org 312 - IT Networking description', '31');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('313', 'org_313', " +
                "'Org 313 - IT Security', 'Org 313 - IT Security description', '31');\n");

          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('321', 'org_321', " +
                "'Org 321 - Facilities Plumbing', 'Org 321 - Facilities Plumbing description', '32');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('322', 'org_322', " +
                "'Org 322 - Facilities Groundkeeping', 'Org 322 - Facilities Groundkeeping description', '32');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('323', 'org_323', " +
                "'Org 323 - Facilities Parking', 'Org 323 - Facilities Parking description', '32');\n");

          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('331', 'org_331', " +
                "'Org 331 - Security Police', 'Org 331 - Security Police description', '33');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('332', 'org_332', " +
                "'Org 332 - Security Guards', 'Org 332 - Security Guards description', '33');\n");
          ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_orgs " +
              "(id, org_name, org_display_name, org_description, parent_id) values ('333', 'org_333', " +
                "'Org 333 - Security Admin', 'Org 333 - Security Admin description', '33');\n");
        }
        
      }
      
      {
        boolean hasPocOrgAssignTable = database.findTable("grouperorgs_poc_org_assign") != null;

        //table for org definitions
        //see if the grouperorgs_poc_org_assign table is there
        Table grouperOrgAssignTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouperorgs_poc_org_assign");

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgAssignTable, "id",
            Types.VARCHAR, GrouperDdl.ID_SIZE, true, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgAssignTable, "org_id", 
            Types.VARCHAR, GrouperDdl.ID_SIZE, false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperOrgAssignTable, "subject_id", 
            Types.VARCHAR, "128", false, false);

        if (!hasPocOrgAssignTable) {
          
          //if it wasnt there, lets give some insert statements
          //ddlVersionBean.appendAdditionalScriptUnique("insert into grouperorgs_poc_org_assign " +
          //    "(id, org_id, subject_id) values ('1', '1', '1');\n");
          generateOrgAssignment(ddlVersionBean);          
        }
      }
      
    }
  };

  /**
   * create the assignments
   * @param ddlVersionBean
   */
  public static void generateOrgAssignment(DdlVersionBean ddlVersionBean) {
    
    List<String> orgIds = GrouperUtil.toList("1", "2", "3", "11", "12", "13", "21", "22", "23", "31", "32", "33",
        "111", "112", "113", "121", "122", "123", "131", "132", "133", "211", "212", "213", "221", "222", "223", 
        "231", "232", "233", "311", "312", "313", "321", "322", "323", "331", "332", "333");
    
    //make this deterministic
    Random random = new Random(1);

    int id = 0;
    
    for (String orgId : orgIds) {

      //pick a random number of people in here, from 0 to 9
      int memberCount = (int)(random.nextDouble() * 10);
      
      for (int i=0;i<memberCount;i++) {
        int subjectIndex = (int) (random.nextDouble() * subjectIds.size());
        String subjectId = subjectIds.get(subjectIndex);
        ddlVersionBean.appendAdditionalScriptUnique("insert " +
        		"into grouperorgs_poc_org_assign (id, org_id, subject_id) " +
        		"values ('" + id++ + "', '" + orgId + "', '" + subjectId + "');\n");        
      }
      
    }
    
  }
  
  /**
   * test subject ids
   */
  private static final List<String> subjectIds = GrouperUtil.toList("babe", "babl", "babr", 
      "babu", "baco", "bado", "baed", "bama", "bapo", "bata", "bato", "bawi", "elbe", "elbl", 
      "elbr", "elbu", "elco", "eldo", "eled", "elma", "elpo", "elta", "elto", "elwi", "fibe", 
      "fibl", "fibr", "fibu", "fico", "fido", "fied", "fima", "fipo", "fita", "fito", "fiwi", 
      "habe", "habl", "habr", "habu", "haco", "hado", "haed", "hama", "hapo", "hata", "hato", 
      "hawi", "iabe", "iabl", "iabr", "iabu", "iaco", "iado", "iaed", "iama", "iapo", "iata", 
      "iato", "iawi", "jabe", "jabl", "jabr", "jabu", "jaco", "jado", "jaed", "jama", "japo", 
      "jata", "jato", "jawi", "jobe", "jobl", "jobr", "jobu", "joco", "jodo", "joed", "joma", 
      "jopo", "jota", "joto", "jowi", "kebe", "kebl", "kebr", "kebu", "keco", "kedo", "keed", 
      "kema", "kepo", "keta", "keto", "kewi", "mabe", "mabl", "mabr", "mabu", "maco", "mado", 
      "maed", "mama", "mapo", "mata", "mato", "mawi", "mobe", "mobl", "mobr", "mobu", "moco", 
      "modo", "moed", "moma", "mopo", "mota", "moto", "mowi", "pebe", "pebl", "pebr", "pebu", 
      "peco", "pedo", "peed", "pema", "pepo", "peta", "peto", "pewi", "stbe", "stbl", "stbr", 
      "stbu", "stco", "stdo", "sted", "stma", "stpo", "stta", "stto", "stwi");
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getVersion()
   */
  public int getVersion() {
    return GrouperDdlUtils.versionIntFromEnum(this);
  }

  /**
   * cache this
   */
  private static int currentVersion = -1;
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    if (currentVersion == -1) {
      int max = -1;
      for (GrouperOrgDdl grouperDdl : GrouperOrgDdl.values()) {
        String number = grouperDdl.name().substring(1);
        int theInt = Integer.parseInt(number);
        max = Math.max(max, theInt);
      }
      currentVersion = max;
    }
    return currentVersion;
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getObjectName()
   */
  public String getObjectName() {
    return GrouperDdlUtils.objectName(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getDefaultTablePattern()
   */
  public String getDefaultTablePattern() {
    return "GROUPERORGS_%";
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
   */
  public abstract void updateVersionFromPrevious(Database database, 
      DdlVersionBean ddlVersionBean);  
  
  /**
   * add all foreign keys
   * @param ddlVersionBean 
   */
  public void addAllForeignKeysViewsEtc(DdlVersionBean ddlVersionBean) {
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouperorgs_all_members_v",
        "Groups which are members of all groups.  Three cases, the org itself, children which " +
        "have children (all group), and children which dont have children)",
        GrouperUtil.toSet("GROUP_NAME", 
            "MEMBER_GROUP_NAME"),
         GrouperUtil.toSet("Group name is full id path, e.g. school:stem1:groupId of the parent all group",
             "member group name is the group name of the member group in the all group"),
             "select gh.org_hier_all_sor_name as group_name, gh.org_hierarchical_name as member_group_name "
             + "from grouperorgs_hierarchical gh "
             + "where gh.org_hier_all_name is not null "
             + "union "
             + "select gh.org_hier_all_sor_name as group_name, gh_member.org_hier_all_name as subject_identifier "
             + "from grouperorgs_hierarchical gh, grouperorgs_hierarchical gh_member, grouperorgs_poc_orgs gpo "
             + "where gh.org_hier_all_name is not null and gpo.parent_id = gh.org_id and gpo.id = gh_member.org_id "
             + "and gh_member.org_hier_all_name is not null "
             + "union "
             + "select gh.org_hier_all_sor_name as group_name, gh_member.org_hierarchical_name as subject_identifier "
             + "from grouperorgs_hierarchical gh, grouperorgs_hierarchical gh_member, grouperorgs_poc_orgs gpo "
             + "where gh.org_hier_all_name is not null and gpo.parent_id = gh.org_id and gpo.id = gh_member.org_id "
             + "and gh_member.org_hier_all_name is null ");

    
  }
  
  /**
   * drop all views
   * @param ddlVersionBean 
   */
  public void dropAllViews(DdlVersionBean ddlVersionBean) {

    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouperorgs_all_members_v", false);

  }
  
  /**
   * an example table name so we can hone in on the exact metadata
   * @return the table name
   */
  public String[] getSampleTablenames() {
    return new String[]{"grouperorgs_hierarchical"};
  }

}
