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
package edu.internet2.middleware.grouper.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.cache.SubjectSourceCache;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * load test data (will not error if already there)
 */
public class LoadData {
  
  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main (String args[]) throws Exception {
    
    //select gm.subject_id, count(1) from grouper_memberships_all_v gmav, grouper_fields gf, grouper_members gm 
    //where gmav.member_id = gm.id and gmav.field_id = gf.id and gf.type = 'naming' group by subject_id having count(*) > 5 order by count(*) desc ;
    
    //loadDukeData();
    //loadDukeRandomPrivileges();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = StemFinder.findRootStem(grouperSession);
    GrouperSession.stopQuietly(grouperSession);
    boolean first = true;
    for (String subjectId : new String[] {"2105111", "2104114", "2105119", "2105117", "21051111", "2105112", "2105225", "2105220", "2105221", "21041111", "2105116", "2105110", "2105118", "2105113"}) {
      long startNanos = System.nanoTime();
      grouperSession = GrouperSession.startRootSession();
      Subject subject = SubjectFinder.findByIdAndSource(subjectId, "jdbc", true);
      GrouperSession.stopQuietly(grouperSession);
      grouperSession = GrouperSession.start(subject);

      stem.getChildStems(Scope.ONE, QueryOptions.create("displayExtension", true, 1, 50));

      GrouperSession.stopQuietly(grouperSession);
      if (!first) {
        System.out.println("Find root stems: " + ((System.nanoTime()-startNanos)/1000000) + "ms");
      }
      first = false;
    }

  }
  
  /**
   * 
   */
  public static void loadDukeRandomPrivileges() {

    GrouperSession.startRootSession();

    List<Stem> stems = new ArrayList<Stem>(HibernateSession.byHqlStatic()
        .createQuery("from Stem where nameDb like 'duke%' order by nameDb").listSet(Stem.class));
    
    List<Group> groups = new ArrayList<Group>(HibernateSession.byHqlStatic()
        .createQuery("from Group where nameDb like 'duke%' order by nameDb").listSet(Group.class));

    List<String> subjectIds = new GcDbAccess().sql(
        "select subjectid from subject where subjectid not like 'test%' order by subjectid").selectList(String.class);
    
    List<Privilege> attributeDefPrivileges = new ArrayList<Privilege>(AttributeDefPrivilege.ALL_PRIVILEGES);
    List<Privilege> stemPrivileges = new ArrayList<Privilege>(NamingPrivilege.ALL_PRIVILEGES);
    List<Privilege> groupPrivileges = new ArrayList<Privilege>(AccessPrivilege.ALL_PRIVILEGES);
    
    int groupPrivCount = 0;
    int stemPrivCount = 0;
    int attributeDefPrivCount = 0;
    int assignToGroupCount = 0;
    
    // lets add 5k privileges stem/group, 500 attribute defs with privs
    int loopMax = 20000;
    for (int i=0;i<loopMax;i++) {

      Subject subject = null;
      
      // maybe assign privilege to group instead of subject
      if (Math.random() < 0.5d) {
        int group2Index = Math.min((int)(Math.random()*groups.size()), groups.size()-1);
        Group group2 = groups.get(group2Index);

        subject = group2.toSubject();
        assignToGroupCount++;
      } else {
        int subjectIndex = Math.min((int)(Math.random()*subjectIds.size()), subjectIds.size()-1);
        subject = SubjectFinder.findByIdAndSource(subjectIds.get(subjectIndex), "jdbc", true);
      }
      int stemIndex = Math.min((int)(Math.random()*stems.size()), stems.size()-1);
      Stem stem = stems.get(stemIndex);
      
      Double actionRandom = Math.random();
      if (actionRandom < 0.1d) {
        
        AttributeDef attributeDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
            .assignName(stem.getName() + ":" + "attributeDef_" + i).assignToStem(true).assignAttributeDefType(AttributeDefType.attr)
            .assignMultiAssignable(false).assignMultiValued(false).assignValueType(AttributeDefValueType.string).save();
        
        int attributeDefPrivilegeIndex = Math.min((int)(Math.random()*attributeDefPrivileges.size()), attributeDefPrivileges.size()-1);
        Privilege attributeDefPrivilege = attributeDefPrivileges.get(attributeDefPrivilegeIndex);
        
        attributeDef.getPrivilegeDelegate().grantPriv(subject, attributeDefPrivilege, false);
        attributeDefPrivCount++;
      } else if (actionRandom < 0.55d) {
        int stemPrivilegeIndex = Math.min((int)(Math.random()*stemPrivileges.size()), stemPrivileges.size()-1);
        Privilege stemPrivilege = stemPrivileges.get(stemPrivilegeIndex);
        // make a stem priv to an individual or a group
        stem.grantPriv(subject, stemPrivilege, false);
        stemPrivCount++;
      } else {
        int groupPrivilegeIndex = Math.min((int)(Math.random()*groupPrivileges.size()), groupPrivileges.size()-1);
        Privilege groupPrivilege = groupPrivileges.get(groupPrivilegeIndex);

        // make a group priv
        int groupIndex = Math.min((int)(Math.random()*groups.size()), groups.size()-1);
        Group group = groups.get(groupIndex);
        
        group.grantPriv(subject, groupPrivilege, false);
        groupPrivCount++;
      }
      if ((i+1)%100==0) {
        System.out.println("Granted priv " + (i+1) + " out of " + loopMax + " (group: " + groupPrivCount + ", stem: " + stemPrivCount + ", attributeDef: " + attributeDefPrivCount + "), (assignedToGroup: " + assignToGroupCount + ")");
      }
    }
  }

  /**
   * <pre>
   * load test duke data (written by Shilen, adapted by Chris
   * 
   * I've attached the script I used to load test data in Grouper.  
   * I have the following settings set to true.
   * 
   * groups.create.grant.all.read
   * groups.create.grant.all.view
   * 
   * I mentioning that because it will cause there to be more rows in the grouper_memberships table.  After running the script, I ended up with the following rows in the tables.
   * 
   * grouper_memberships = 1,067,103
   * grouper_stems = 105,704
   * grouper_groups = 125,400
   * grouper_members = 128,329
   * 
   * </pre>
   * @throws Exception
   */
  public static void loadDukeData() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();

    Stem eduStem = Stem.saveStem(session, "edu", null, "edu", "edu", "", null, false);

    AttributeDef attributeDef = new AttributeDefSave(session).assignName("edu:attributeDef").assignAttributeDefType(AttributeDefType.perm).assignToGroup(true).save();

    Stem dukeStem = Stem.saveStem(session, "duke", null, "duke", "Duke University", "", 
        null, false);
    Stem sissStem = Stem.saveStem(session, dukeStem.getName() + ":siss",null,  dukeStem.getName() + ":siss",  "siss","",
        null, false);
    Stem coursesStem = Stem.saveStem(session, sissStem.getName() + ":courses",  null,sissStem.getName() + ":courses", "courses","",  
        null, false);

    int stemsCreated = 3;
    int groupsCreated = 0;
    int membersAdded = 0;

    for (int subjCount = 1; subjCount <= 100; subjCount++) {
      String subjStemName = coursesStem.getName() + ":SUBJECT" + subjCount;
      Stem subjStem = Stem.saveStem(session, subjStemName, null, subjStemName, "SUBJECT" + subjCount, "", 
           null, false);
      stemsCreated++;
      //for (int catelogCount = 100; catelogCount <= 110; catelogCount++) {
      for (int catelogCount = 100; catelogCount <= 110; catelogCount++) {
        String catelogStemName = subjStem.getName() + ":" + catelogCount;
        Stem catelogStem = Stem.saveStem(session, catelogStemName, null,catelogStemName,""+catelogCount, "", 
               null, false);
         stemsCreated++;
         //for (int secCount = 10; secCount <= 28; secCount++) {
         for (int secCount = 10; secCount <= 28; secCount++) {
           String secStemName = catelogStem.getName() + ":" + secCount;
          Stem secStem = Stem.saveStem(session, secStemName, null,secStemName, "" + secCount,  "", 
               null, false);
           stemsCreated++;
           for (int classCount = 1000; classCount <= 1001; classCount++) {
             Stem classStem = null;
             try {
               classStem = secStem.addChildStem("" + classCount, "" + classCount); 
               stemsCreated++;
             } catch (StemAddException sae) {
               //hopefully it is because the group already exists
               classStem = StemFinder.findByName(session, secStem.getName() + ":" + classCount, true);
             }
             for (int termCount = 2000; termCount <= 2000; termCount++) {
               Stem termStem = null;
               try {
                 termStem = classStem.addChildStem("" + termCount, "" + termCount);
                 stemsCreated++;
               } catch (StemAddException sae) {
                 //hopefully it is because the group already exists
                 termStem = StemFinder.findByName(session, classStem.getName() + ":" + termCount, true);
               }
               
               Role instructors = null;
               try {
                 instructors = termStem.addChildRole("instructors", "instructors");
                 groupsCreated++;
               } catch (GroupAddException gae) {
                 //hopefully it is because the group already exists
                 instructors = GroupFinder.findByName(session, termStem.getName() + ":instructors", true);
               }
               
               Role ta = null;
               try {
                 ta = termStem.addChildRole("TAs", "TAs");
                 groupsCreated++;
               } catch (GroupAddException gae) {
                 //hopefully it is because the group already exists
                 ta = GroupFinder.findByName(session, termStem.getName() + ":TAs", true);
               }
                 
               Role students = null;
               try {
                 students = termStem.addChildRole("students", "students");
                 groupsCreated++;
               } catch (GroupAddException gae) {
                 //hopefully it is because the group already exists
                 students = GroupFinder.findByName(session, termStem.getName() + ":students", true);
               }

               AttributeDefName attributeDefName = new AttributeDefNameSave(session, attributeDef).assignName(termStem.getName() + ":testAttribute").save();
               instructors.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
               students.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
               ta.getPermissionRoleDelegate().assignRolePermission(attributeDefName);

               for (int memberCount = 0; memberCount <= 0; memberCount++) { 
                 Subject subject = subjectFindCreateById("0" + catelogCount + secCount + memberCount);
                 try {
                   instructors.addMember(subject, false);
                   membersAdded++;
                 } catch (MemberAddException mae) {
                   //hopefully it is because the member is already a member
                 }
               }

               for (int memberCount = 0; memberCount <= 0; memberCount++) { 
                 Subject subject = subjectFindCreateById("1" + catelogCount + secCount + memberCount);
                 try {
                   ta.addMember(subject, false);
                   membersAdded++;
                 } catch (MemberAddException mae) {
                   //hopefully it is because the member is already a member
                 }
                   
               }

               for (int memberCount = 0; memberCount <= 11; memberCount++) { 
                 Subject subject = subjectFindCreateById("2" + catelogCount + secCount + memberCount);
                 try {
                   students.addMember(subject, false);
                   membersAdded++;
                 } catch (MemberAddException mae) {
                   //hopefully it is because the member is already a member
                 }
               }
             }
           }

           System.out.println(new Date() + ", stem: " + secStem.getName() + ", stems: " + stemsCreated + ", Groups: " + groupsCreated + ", Memberships: " + membersAdded);
         }
      }
    }
    System.out.println("Stems created: " + stemsCreated);
    System.out.println("Groups created: " + groupsCreated);
    System.out.println("Memberships added: " + membersAdded);

  }
  
  /**
   * find or create subject in the default subject api tables
   * @param subjectId
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  private static Subject subjectFindCreateById(final String subjectId) 
      throws SubjectNotFoundException, SubjectNotUniqueException {

    try {
      return SubjectFinder.findById(subjectId, true);
    } catch (SubjectNotFoundException snfe) {
      //create;
      
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
          Session session = hibernateSession.getSession();
          Connection connection = ((SessionImpl)session).connection();
          PreparedStatement preparedStatement = null;
          try {

            //INSERT INTO SUBJECT ( SUBJECTID, SUBJECTTYPEID, NAME ) VALUES ('10000000', 'person', '10000000') 
            String query = "INSERT INTO SUBJECT ( SUBJECTID, SUBJECTTYPEID, NAME ) VALUES (?, 'person', ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, subjectId);
            preparedStatement.setString(2, subjectId);
            preparedStatement.execute();
            
            //INSERT INTO SUBJECTATTRIBUTE ( SUBJECTID, NAME, VALUE, SEARCHVALUE ) VALUES ( 
            //'10000000', 'name', 'Nancy Eggers', 'nancy eggers')
            //INSERT INTO SUBJECTATTRIBUTE ( SUBJECTID, NAME, VALUE, SEARCHVALUE ) VALUES ( 
            //'10000000', 'loginid', 'Nancy Eggers', 'nancy eggers')
            //INSERT INTO SUBJECTATTRIBUTE ( SUBJECTID, NAME, VALUE, SEARCHVALUE ) VALUES ( 
            //'10000000', 'description', 'Nancy Eggers', 'nancy eggers'); 
            for (String name : new String[]{"name", "loginid", "description"}) {
              
              query = "INSERT INTO SUBJECTATTRIBUTE ( SUBJECTID, NAME, VALUE, SEARCHVALUE ) VALUES ( " +
                  "?, ?, ?, ?)";
              preparedStatement = connection.prepareStatement(query);
              preparedStatement.setString(1, subjectId);
              preparedStatement.setString(2, name);
              preparedStatement.setString(3, subjectId);
              preparedStatement.setString(4, subjectId);
              preparedStatement.execute();
            }
            
            connection.commit();
          } catch (Exception e) {
            throw new RuntimeException(e);
          } finally {
            HibUtils.closeQuietly(preparedStatement);
          }
          return null;
        }
        
      });
      SubjectSourceCache.clearCache();
    }
    return SubjectFinder.findById(subjectId, true);
  }
  
}
