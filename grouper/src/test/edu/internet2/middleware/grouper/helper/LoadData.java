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
import java.util.Date;

import org.hibernate.Session;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
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
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.permissions.role.Role;

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
    loadDukeData();
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

    Stem eduStem = Stem.saveStem(session, "edu", null, "edu", "edu", "", 
        null, false);

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

        @SuppressWarnings("deprecation")
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
          Session session = hibernateSession.getSession();
          Connection connection = session.connection();
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
      
    }
    return SubjectFinder.findById(subjectId, true);
  }
  
}
