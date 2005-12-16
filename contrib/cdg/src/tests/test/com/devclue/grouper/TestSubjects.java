/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package test.com.devclue.grouper;

import  com.devclue.grouper.registry.*;
import  com.devclue.grouper.subject.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test <i>com.devclue.grouper.subject.*</i> classes.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSubjects.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class TestSubjects extends TestCase {

  public TestSubjects(String name) {
    super(name);
  }

  protected void setUp() {
    GroupsRegistry gr = new GroupsRegistry();
    gr.reset();
  }

  protected void tearDown() {
    // Nothing
  }

  /*
   * TESTS
   */

  /* GENERAL */
  // Test instantiation
  public void testInstantiation() {
    CSV2JDBC      c2j = new CSV2JDBC();
    Assert.assertNotNull("c2j !null", c2j);
    Source        msa = new MockSourceAdapter();
    Assert.assertNotNull("msa !null", msa);
    Subject       ms  = new MockSubject();
    Assert.assertNotNull("ms !null", ms);
    SubjectAdd    sa  = new SubjectAdd();
    Assert.assertNotNull("sa !null", sa);
    SubjectQ      sq  = new SubjectQ();
    Assert.assertNotNull("sq !null", sq);
  }
  /* GENERAL */

  /* MOCKSUBJECT */
  public void testMockSubjectCreateDefault() {
    Subject     ms    = new MockSubject();
    Assert.assertNotNull("ms !null", ms);

    Map         attrs = ms.getAttributes();
    Assert.assertNotNull("ms attrs !null", attrs);
    Assert.assertTrue("ms attrs keys == 0", attrs.size() == 0);

    String      attr  = ms.getAttributeValue("loginid");
    Assert.assertNotNull("ms attr loginid !null", attr);
    Assert.assertTrue("ms attr loginid == ''", attr.equals(""));

    String      sval  = ( (MockSubject) ms).getAttributeSearchValue("loginid");
    Assert.assertNotNull("ms sval loginid !null", sval);
    Assert.assertTrue("ms sval loginid == ''", sval.equals(""));

    Set         vals  = ms.getAttributeValues("loginid");
    Assert.assertNotNull("ms attr vals loginid !null", vals);
    Assert.assertTrue("ms attr loginid vals == 0", vals.size() == 0);

    Set         svals = ( (MockSubject) ms).getAttributeSearchValues("loginid");
    Assert.assertNotNull("ms attr svals loginid !null", vals);
    Assert.assertTrue("ms attr loginid svals == 0", vals.size() == 0);

    String      desc  = ms.getDescription();
    Assert.assertNotNull("ms desc !null", desc);
    Assert.assertTrue("ms desc == ''", desc.equals(""));

    String      id    = ms.getId();
    Assert.assertNotNull("ms id !null", id);
    Assert.assertTrue("ms id == ''", id.equals(""));

    String      name  = ms.getName();
    Assert.assertNotNull("ms name !null", name);
    Assert.assertTrue("ms name == ''", name.equals(""));

    Source      msa   = ms.getSource();
    Assert.assertNotNull("msa !null", msa);

    SubjectType type  = ms.getType();
    Assert.assertNotNull("type !null", type);
    Assert.assertTrue("type == person", type.getName().equals("person"));
  }

  public void testMockSubjectCreateOverride() {
    Subject     ms    = new MockSubject("id", "name", new MockSourceAdapter());
    Assert.assertNotNull("ms !null", ms);

    Map         attrs = ms.getAttributes();
    Assert.assertNotNull("ms attrs !null", attrs);
    Assert.assertTrue("ms attrs keys == 0", attrs.size() == 0);

    String      attr  = ms.getAttributeValue("loginid");
    Assert.assertNotNull("ms attr loginid !null", attr);
    Assert.assertTrue("ms attr loginid == ''", attr.equals(""));
    
    String      sval  = ( (MockSubject) ms).getAttributeSearchValue("loginid");
    Assert.assertNotNull("ms sval loginid !null", sval);
    Assert.assertTrue("ms sval loginid == ''", sval.equals(""));

    Set         vals  = ms.getAttributeValues("loginid");
    Assert.assertNotNull("ms attr vals loginid !null", vals);
    Assert.assertTrue("ms attr loginid vals == 0", vals.size() == 0);
    
    Set         svals = ( (MockSubject) ms).getAttributeSearchValues("loginid");
    Assert.assertNotNull("ms attr svals loginid !null", vals);
    Assert.assertTrue("ms attr loginid svals == 0", vals.size() == 0);

    String      desc  = ms.getDescription();
    Assert.assertNotNull("ms desc !null", desc);
    Assert.assertTrue("ms desc == name", desc.equals("name"));

    String      id    = ms.getId();
    Assert.assertNotNull("ms id !null", id);
    Assert.assertTrue("ms id == id", id.equals("id"));

    String      name  = ms.getName();
    Assert.assertNotNull("ms name !null", name);
    Assert.assertTrue("ms name == name", name.equals("name"));

    Source      msa   = ms.getSource();
    Assert.assertNotNull("msa !null", msa);

    SubjectType type  = ms.getType();
    Assert.assertNotNull("type !null", type);
    Assert.assertTrue("type == person", type.getName().equals("person"));
  }

  public void testMockSubjectSet() {
    Subject     ms    = new MockSubject();
    Assert.assertNotNull("ms !null", ms);

    ( (MockSubject) ms).setAttributeValue("loginid", "uid");
    Map         attrs = ms.getAttributes();
    Assert.assertNotNull("ms attrs !null", attrs);
    Assert.assertTrue("ms attrs keys == 1", attrs.size() == 1);

    String      attr  = ms.getAttributeValue("loginid");
    Assert.assertNotNull("ms attr loginid !null", attr);
    Assert.assertTrue("ms attr loginid == uid", attr.equals("uid"));
    
    String      sval  = ( (MockSubject) ms).getAttributeSearchValue("loginid");
    Assert.assertNotNull("ms sval loginid !null", sval);
    Assert.assertTrue("ms sval loginid == uid", sval.equals("uid"));

    Set         vals  = ms.getAttributeValues("loginid");
    Assert.assertNotNull("ms attr vals loginid !null", vals);
    Assert.assertTrue("ms attr loginid vals == 1", vals.size() == 1);

    Set         svals = ( (MockSubject) ms).getAttributeSearchValues("loginid");
    Assert.assertNotNull("ms attr svals loginid !null", vals);
    Assert.assertTrue("ms attr loginid svals == 1", vals.size() == 1);

    ( (MockSubject) ms).setDescriptionValue("description");
    String      desc  = ms.getDescription();
    Assert.assertNotNull("ms desc !null", desc);
    Assert.assertTrue("ms desc == description", desc.equals("description"));

    ( (MockSubject) ms).setIdValue("id");
    String      id    = ms.getId();
    Assert.assertNotNull("ms id !null", id);
    Assert.assertTrue("ms id == id", id.equals("id"));

    ( (MockSubject) ms).setNameValue("name");
    String      name  = ms.getName();
    Assert.assertNotNull("ms name !null", name);
    Assert.assertTrue("ms name == name", name.equals("name"));

    ( (MockSubject) ms).setSource( new MockSourceAdapter() );
    Source      msa   = ms.getSource();
    Assert.assertNotNull("msa !null", msa);

    ( (MockSubject) ms).setType("person"); 
    SubjectType type  = ms.getType();
    Assert.assertNotNull("type !null", type);
    Assert.assertTrue("type == person", type.getName().equals("person"));
  }
  /* MOCKSUBJECT */

  /* MOCKSOURCE */
  public void testMockSourceGetSubject() {
    Source msa = new MockSourceAdapter();
    Assert.assertNotNull("msa !null", msa);
    try {
      Subject ms  = msa.getSubject("id");
      Assert.assertNotNull("ms !null", ms);
      Assert.assertTrue("ms id == id", ms.getId().equals("id"));
      Assert.assertTrue("ms desc == id", ms.getDescription().equals("id"));
      Assert.assertTrue("ms name == id", ms.getName().equals("id"));
      Assert.assertTrue(
        "ms source == this", ms.getSource() == msa
      );
      Assert.assertTrue(
        "ms type == person", ms.getType().getName().equals("person")
      );
    } catch (SubjectNotFoundException e) {
      Assert.fail("no subject: " + e.getMessage());
    }
  } 

  public void testMockSourceGetSubjectByIdentifier() {
    Source msa = new MockSourceAdapter();
    Assert.assertNotNull("msa !null", msa);
    try {
      Subject ms  = msa.getSubjectByIdentifier("id");
      Assert.assertNotNull("ms !null", ms);
      Assert.assertTrue("ms id == id", ms.getId().equals("id"));
      Assert.assertTrue("ms desc == id", ms.getDescription().equals("id"));
      Assert.assertTrue("ms name == id", ms.getName().equals("id"));
      Assert.assertTrue(
        "ms source == this", ms.getSource() == msa
      );
      Assert.assertTrue(
        "ms type == person", ms.getType().getName().equals("person")
      );
    } catch (SubjectNotFoundException e) {
      Assert.fail("no subject: " + e.getMessage());
    }
  } 

  public void testMockSourceSearch() {
    Source msa = new MockSourceAdapter();
    Assert.assertNotNull("msa !null", msa);
    Set results = msa.search("id");
    Assert.assertTrue("results size == 0", results.size() == 0);
  }
  /* MOCKSOURCE */

  /* SUBJECTADD */
  // Test SubjectAdd subject adding
  public void testSubjectAddSubject() {
    SubjectAdd  sa  = new SubjectAdd();
    MockSubject ms  = new MockSubject("id0","id0", new MockSourceAdapter());
    try {
      sa.addSubject(ms);
      Assert.assertTrue("added subject", true);
    }
    catch (RuntimeException e) {
      Assert.fail("failed to add subject");
    }
  }

  // Test SubjectAdd subject duplicate adding
  public void testSubjectAddSubjectDuplicateSubject() {
    SubjectAdd  sa  = new SubjectAdd();
    MockSubject ms  = new MockSubject("id0","id0", new MockSourceAdapter());
    try {
      sa.addSubject(ms);
      Assert.assertTrue("added subject", true);
    }
    catch (RuntimeException e) {
      Assert.fail("failed to add subject");
    }
    try {
      sa.addSubject(ms);
      Assert.fail("added duplicate subject");
    }
    catch (RuntimeException e) {
      Assert.assertTrue("failed to add duplicate subject", true);
    }
  }
  /* SUBJECTADD */

  /* CSV2JDBC */
  // Test CSV2JDBC parsing
  public void testCSV2JDBCParsingGood() {
    CSV2JDBC c2j = new CSV2JDBC();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(
        TestSubjects.class.getResourceAsStream(
          "/subject.CSV2JDBC.good.csv"
        )
      )
    );
    Assert.assertNotNull("buffered reader", br);
    List subjects = c2j.parseCSVFile(br);
    Assert.assertNotNull("subjects", subjects);
    Assert.assertTrue("subjects==7",subjects.size() == 7);
    // subject 0 - id0,type0
    MockSubject ms = (MockSubject) subjects.get(0);
    Assert.assertNotNull("ms[0] !null", ms);
    Assert.assertTrue("ms[0] id == id0", ms.getId().equals("id0"));
    Assert.assertTrue("ms[0] name == id0", ms.getName().equals("id0"));
    Assert.assertTrue(
      "ms[0] desc v == id0", 
      ms.getAttributeValue("description").equals("id0")
    );
    Assert.assertTrue(
      "ms[0] desc sv == id0", 
      ms.getAttributeSearchValue("description").equals("id0")
    );
    // subject 1 - id1,type1,CN
    ms = (MockSubject) subjects.get(1);
    Assert.assertNotNull("ms[1] !null", ms);
    Assert.assertTrue("ms[1] id == id1", ms.getId().equals("id1"));
    Assert.assertTrue("ms[1] name == CN", ms.getName().equals("CN"));
    Assert.assertTrue(
      "ms[1] desc v == CN", 
      ms.getAttributeValue("description").equals("CN")
    );
    Assert.assertTrue(
      "ms[1] desc sv == cn", 
      ms.getAttributeSearchValue("description").equals("cn")
    );
    // subject 2 - id2,type2,CN,uid2
    ms = (MockSubject) subjects.get(2);
    Assert.assertNotNull("ms[2] !null", ms);
    Assert.assertTrue("ms[2] id == id2", ms.getId().equals("id2"));
    Assert.assertTrue("ms[2] name == CN", ms.getName().equals("CN"));
    Assert.assertTrue(
      "ms[2] desc v == CN", 
      ms.getAttributeValue("description").equals("CN")
    );
    Assert.assertTrue(
      "ms[2] desc sv == cn", 
      ms.getAttributeSearchValue("description").equals("cn")
    );
    // subject 3 - id3,type3,GN SN
    ms = (MockSubject) subjects.get(3);
    Assert.assertNotNull("ms[3] !null", ms);
    Assert.assertTrue("ms[3] id == id3", ms.getId().equals("id3"));
    Assert.assertTrue("ms[3] name == GN SN", ms.getName().equals("GN SN"));
    Assert.assertTrue(
      "ms[3] desc v == SN, GN", 
      ms.getAttributeValue("description").equals("SN, GN")
    );
    Assert.assertTrue(
      "ms[3] desc sv == sn gn", 
      ms.getAttributeSearchValue("description").equals("sn gn")
    );
    // subject 4 - id4,type4,GN SN,uid4
    ms = (MockSubject) subjects.get(4);
    Assert.assertNotNull("ms[4] !null", ms);
    Assert.assertTrue("ms[4] id == id4", ms.getId().equals("id4"));
    Assert.assertTrue("ms[4] name == GN SN", ms.getName().equals("GN SN"));
    Assert.assertTrue(
      "ms[4] desc v == SN, GN", 
      ms.getAttributeValue("description").equals("SN, GN")
    );
    Assert.assertTrue(
      "ms[4] desc sv == sn gn", 
      ms.getAttributeSearchValue("description").equals("sn gn")
    );
    // subject 5 - id5,type5,GN MN SN
    ms = (MockSubject) subjects.get(5);
    Assert.assertNotNull("ms[5] !null", ms);
    Assert.assertTrue("ms[5] id == id5", ms.getId().equals("id5"));
    Assert.assertTrue("ms[5] name == GN MN SN", ms.getName().equals("GN MN SN"));
    Assert.assertTrue(
      "ms[5] desc v == MN SN, GN", 
      ms.getAttributeValue("description").equals("MN SN, GN")
    );
    Assert.assertTrue(
      "ms[5] desc sv == mn sn gn", 
      ms.getAttributeSearchValue("description").equals("mn sn gn")
    );
    // subject 6 - 1id6,type6,GN MN SN,uid6
    ms = (MockSubject) subjects.get(6);
    Assert.assertNotNull("ms[6] !null", ms);
    Assert.assertTrue("ms[6] id == id6", ms.getId().equals("id6"));
    Assert.assertTrue("ms[6] name == GN MN SN", ms.getName().equals("GN MN SN"));
    Assert.assertTrue(
      "ms[6] desc v == MN SN, GN", 
      ms.getAttributeValue("description").equals("MN SN, GN")
    );
    Assert.assertTrue(
      "ms[6] desc sv == mn sn gn", 
      ms.getAttributeSearchValue("description").equals("mn sn gn")
    );
  }
 
  // Test CSV2JDBC parsing
  public void testCSV2JDBCParsingBadShort() {
    CSV2JDBC c2j = new CSV2JDBC();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(
        TestSubjects.class.getResourceAsStream(
          "/subject.CSV2JDBC.bad-short.csv"
        )
      )
    );
    Assert.assertNotNull("buffered reader", br);
    try {
      List subjects = c2j.parseCSVFile(br);
      Assert.fail("Should be RTE; found " + subjects.size());
    } 
    catch (RuntimeException e) {
      Assert.assertTrue("unable to parse file", true);
    }
  }
 
  // Test CSV2JDBC parsing
  public void testCSV2JDBCParsingBadLong() {
    CSV2JDBC c2j = new CSV2JDBC();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(
        TestSubjects.class.getResourceAsStream(
          "/subject.CSV2JDBC.bad-long.csv"
        )
      )
    );
    Assert.assertNotNull("buffered reader", br);
    try {
      List subjects = c2j.parseCSVFile(br);
      Assert.fail("Should be RTE; found " + subjects.size());
    } 
    catch (RuntimeException e) {
      Assert.assertTrue("unable to parse file", true);
    }
  }
 
  // Test CSV2JDBC parsing
  public void testCSV2JDBCParsingBadNoID() {
    CSV2JDBC c2j = new CSV2JDBC();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(
        TestSubjects.class.getResourceAsStream(
          "/subject.CSV2JDBC.bad-noid.csv"
        )
      )
    );
    Assert.assertNotNull("buffered reader", br);
    try {
      List subjects = c2j.parseCSVFile(br);
      Assert.fail("Should be RTE; found " + subjects.size());
    } 
    catch (RuntimeException e) {
      Assert.assertTrue("unable to parse file", true);
    }
  }

  // Test CSV2JDBC parsing
  public void testCSV2JDBCParsingBadNoType() {
    CSV2JDBC c2j = new CSV2JDBC();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(
        TestSubjects.class.getResourceAsStream(
          "/subject.CSV2JDBC.bad-notype.csv"
        )
      )
    );
    Assert.assertNotNull("buffered reader", br);
    try {
      List subjects = c2j.parseCSVFile(br);
      Assert.fail("Should be RTE; found " + subjects.size());
    } 
    catch (RuntimeException e) {
      Assert.assertTrue("unable to parse file", true);
    }
  }

  // Test CSV2JDBC subject adding
  public void testCSV2JDBCAdding() {
    CSV2JDBC c2j = new CSV2JDBC();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(
        TestSubjects.class.getResourceAsStream(
          "/subject.CSV2JDBC.good.csv"
        )
      )
    );
    Assert.assertNotNull("buffered reader", br);
    List subjects = c2j.parseCSVFile(br);
    Assert.assertNotNull("subjects", subjects);
    Assert.assertTrue("subjects==7",subjects.size() == 7);
    try {
      c2j.addSubjects(subjects);
      Assert.assertTrue("added subjects", true);
    } catch (RuntimeException e) {
      Assert.fail("unable to add subjects: " + e.getMessage());
    }
  }
  /* CSV2JDBC */

  /* SUBJECTQ */
  // Test SubjectQ - find GrouperSystem by id
  public void testSubjectQGetSubjectGrouperSystem() {
    SubjectQ  sq = new SubjectQ();
    String    id  = "GrouperSystem";
    try {
      Subject subj = sq.getSubject(id); 
      Assert.assertTrue("subj found", true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj id == " + id, subj.getId().equals(id));
    }
    catch (SubjectNotFoundException e) {
      Assert.fail("subject not found: " + e.getMessage());
    }
  }

  // Test SubjectQ - fail to find invalid subject by id
  public void testSubjectQGetSubjectFailToGetInvalidSubj() {
    SubjectQ  sq  = new SubjectQ();
    String    id  = "NotGrouperSystem";
    try {
      Subject subj = sq.getSubject(id);
      Assert.fail("found subject " + id);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("subj not found", true);
    }
  }

  // Test SubjectQ - find loaded subjects
  public void testSubjectQGetSubjectFindLoadedSubj() {
    CSV2JDBC c2j = new CSV2JDBC();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(
        TestSubjects.class.getResourceAsStream(
          "/subject.CSV2JDBC.good.csv"
        )
      )
    );
    try {
      c2j.addSubjects( c2j.parseCSVFile(br) );
      Assert.assertTrue("added subjects", true);
    } catch (RuntimeException e) {
      Assert.fail("unable to add subjects: " + e.getMessage());
    }

    // Now query
    SubjectQ  sq  = new SubjectQ();
    String    id  = "GrouperSystem";
    try {
      Subject subj = sq.getSubject(id);
      Assert.assertTrue("subj found", true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj id == " + id, subj.getId().equals(id));
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("subj not found " + id + ": " + e.getMessage());
    }
    id = "id0";
    try {
      Subject subj = sq.getSubject(id);
      Assert.assertTrue("subj found", true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj id == " + id, subj.getId().equals(id));
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("subj not found " + id + ": " + e.getMessage());
    }
    id = "cn";
    try {
      Subject subj = sq.getSubject(id);
      Assert.fail("subj " + id + " found");
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("subj not found", true);
    }
    id = "uid2";
    try {
      Subject subj = sq.getSubject(id);
      Assert.assertTrue("subj found", true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj id != " + id, !subj.getId().equals(id));
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("subj " + id + " not found");
    }
  }
  /* SUBJECTQ */

}

