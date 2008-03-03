package edu.internet2.middleware.grouper.data;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.hibernate.Session;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InternalSourceAdapter;
import edu.internet2.middleware.grouper.SaveMode;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
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
    GrouperSession session = GrouperSession.start(SubjectFinder.findById("GrouperSystem", 
        "application", InternalSourceAdapter.ID));

    Stem dukeStem = Stem.saveStem(session, "", "Duke University", "duke", null, 
        null, false);
    Stem sissStem = Stem.saveStem(session, "", "siss", dukeStem.getName() + ":siss", null, 
        null, false);
    Stem coursesStem = Stem.saveStem(session, "", "courses", sissStem.getName() + ":courses", null, 
        null, false);

    int stemsCreated = 3;
    int groupsCreated = 0;
    int membersAdded = 0;

    //for (int subjCount = 1; subjCount <= 100; subjCount++) {
    for (int subjCount = 48; subjCount <= 48; subjCount++) {
      Stem subjStem = Stem.saveStem(session, "", "SUBJECT" + subjCount, 
          coursesStem.getName() + ":SUBJECT" + subjCount, null, null, false);
      stemsCreated++;
      //for (int catelogCount = 100; catelogCount <= 110; catelogCount++) {
      for (int catelogCount = 110; catelogCount <= 110; catelogCount++) {
         Stem catelogStem = Stem.saveStem(session, "", ""+catelogCount, 
             subjStem.getName() + ":" + catelogCount, null, null, false);
         stemsCreated++;
         //for (int secCount = 10; secCount <= 28; secCount++) {
         for (int secCount = 28; secCount <= 28; secCount++) {
           Stem secStem = Stem.saveStem(session, "", "" + secCount, 
               catelogStem.getName() + ":" + secCount, null, null, false);
           stemsCreated++;
           for (int classCount = 1000; classCount <= 1001; classCount++) {
             Stem classStem = Stem.saveStem(session, "", "" + classCount, 
                 secStem.getName() + ":" + classCount, null, null, false);
             stemsCreated++;
             for (int termCount = 2000; termCount <= 2000; termCount++) {
               Stem termStem = Stem.saveStem(session, "", "" + termCount, 
                   classStem.getName() + ":" + termCount, null, null, false);
               stemsCreated++;

               Group instructors  = Group.saveGroup(session, null, "instructors", termStem.getName() + ":instructors",
                   null, SaveMode.INSERT_OR_UPDATE, false);
               Group ta = Group.saveGroup(session, null, "TAs", termStem.getName() + ":TAs",
                   null, SaveMode.INSERT_OR_UPDATE, false);
               Group students = Group.saveGroup(session, null, "students", termStem.getName() + ":students",
                   null, SaveMode.INSERT_OR_UPDATE, false);
               groupsCreated+=3;

               for (int memberCount = 0; memberCount <= 0; memberCount++) { 
                 Subject subject = subjectFindCreateById("0" + catelogCount + secCount + memberCount);
                 instructors.addMember(subject, false);
                 membersAdded++;
               }

               for (int memberCount = 0; memberCount <= 0; memberCount++) { 
                 Subject subject = subjectFindCreateById("1" + catelogCount + secCount + memberCount);
                 ta.addMember(subject, false);
                 membersAdded++;
               }

               for (int memberCount = 0; memberCount <= 11; memberCount++) { 
                 Subject subject = subjectFindCreateById("2" + catelogCount + secCount + memberCount);
                 students.addMember(subject, false);
                 membersAdded++;
               }
             }
           }

           System.out.println("Stems: " + stemsCreated + ", Groups: " + groupsCreated + ", Memberships: " + membersAdded);
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
      return SubjectFinder.findById(subjectId);
    } catch (SubjectNotFoundException snfe) {
      //create;
      
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, new HibernateHandler() {

        @SuppressWarnings("deprecation")
        public Object callback(HibernateSession hibernateSession)
            throws GrouperDAOException {
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
    return SubjectFinder.findById(subjectId);
  }
  
}
