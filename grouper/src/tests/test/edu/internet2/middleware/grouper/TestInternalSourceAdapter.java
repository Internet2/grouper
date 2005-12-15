/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link InternalSourceAdapter} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestInternalSourceAdapter.java,v 1.7 2005-12-15 16:22:42 blair Exp $
 */
public class TestInternalSourceAdapter extends TestCase {

  // Private Class Constants
  private static final  String  ID    = InternalSourceAdapter.ID;
  private static final  Log     LOG   = LogFactory.getLog(TestInternalSourceAdapter.class);
  private static final  String  NAME  = InternalSourceAdapter.NAME;


  // Private Class Variables
  private Source sa;

  public TestInternalSourceAdapter(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    sa = new InternalSourceAdapter(ID, NAME);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests
 
  public void testAdapter() { 
    LOG.info("testAdapter");
    Assert.assertNotNull("sa !null", sa);
    Assert.assertTrue("sa.id", sa.getId().equals(ID));
    Assert.assertTrue("sa.name", sa.getName().equals(NAME));
  } // public void testAdapter()

  public void testAdapterTypes() {
    LOG.info("testAdapterTypes");
    Object[]    types = sa.getSubjectTypes().toArray();
    Assert.assertTrue("1 type", types.length == 1);
    SubjectType type  = (SubjectType) types[0];
    Assert.assertNotNull("type !null", type);
    Assert.assertTrue(
      "type instanceof SubjectType",
      type instanceof SubjectType
    );
    Assert.assertTrue(
      "type == application", type.getName().equals("application")
    );
  } // public void testAdapterTypes()

  public void testAdapterBadSubject() {
    LOG.info("testAdapterBadSubject");
    String id = "i do not exist";
    try { 
      Subject subj = sa.getSubject(id);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testAdapterBadSubject()

  public void testAdapterBadSubjectByIdentifier() {
    LOG.info("testAdapterBadSubjectByIdentifier");
    String id = "i do not exist";
    try { 
      Subject subj = sa.getSubjectByIdentifier(id);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testAdapterBadSubjectByIdentifer() 

  public void testAdapterBadSubjectBySearch() {
    LOG.info("testAdapterBadSubjectBySearch");
    String id = "i do not exist";
    Set results = sa.search(id);
    Assert.assertTrue("found none", results.size() == 0);
  } // public void testAdapterBadSubjectBySearch()

  public void testAdapterGrouperAllSubject() {
    LOG.info("testAdapterGrouperAllSubject");
    String id = SubjectHelper.SUBJ_ALL;
    try { 
      Subject subj = sa.getSubject(id);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
  } // public void testAdapterGrouperAllSubject()

  public void testAdapterGrouperAllSubjectByIdentifier() {
    LOG.info("testAdapterGrouperAllSubjectByIdentifier");
    String id = SubjectHelper.SUBJ_ALL;
    try { 
      Subject subj = sa.getSubjectByIdentifier(id);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
  } // public void testAdapterGrouperAllSubjectByIdentifier()

  public void testAdapterGrouperAllSubjectBySearch() {
    LOG.info("testAdapterGrouperAllSubjectBySearch");
    String id = SubjectHelper.SUBJ_ALL;
    Set results = sa.search(id);
    Assert.assertTrue("found one", results.size() == 1);
  } // public void testAdapterGrouperAllSubjectByIdentifier()

  public void testAdapterGrouperSystemSubject() {
    LOG.info("testAdapterGrouperSystemSubject");
    String id = SubjectHelper.SUBJ_ROOT;
    try { 
      Subject subj = sa.getSubject(id);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
  } // public void testAdapterGrouperSystemSubject()

  public void testAdapterGrouperSystemSubjectByIdentifier() {
    LOG.info("testAdapterGrouperSystemSubjectByIdentifier");
    String id = SubjectHelper.SUBJ_ROOT;
    try { 
      Subject subj = sa.getSubjectByIdentifier(id);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
  } // public void testAdapterGrouperSystemSubjectByIdentifier()

  public void testAdapterGrouperSystemSubjectBySearch() {
    LOG.info("testAdapterGrouperSystemSubjectBySearch");
    String id = SubjectHelper.SUBJ_ROOT;
    Set results = sa.search(id);
    Assert.assertTrue("found one", results.size() == 1);
  } // public void testAdapterGrouperSystemSubjectByIdentifier()

  public void testGrouperAllAttributes() {
    LOG.info("testGrouperAllAttributes");
    String id = SubjectHelper.SUBJ_ALL;
    try { 
      Subject subj  = sa.getSubject(id);
      Map     attrs = subj.getAttributes();
      Assert.assertTrue("zero attrs", attrs.size() == 0);
      String  val   = subj.getAttributeValue("foo");
      Assert.assertTrue(
        "no attr (" + val + ")", val.equals("")
      );
      Set     vals  = subj.getAttributeValues("foo");
      Assert.assertTrue("zero values", vals.size() == 0);
      Assert.assertTrue(
        "desc == name", subj.getDescription().equals(subj.getName())
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + e.getMessage());
    }
  } // public void testGrouperAllAttributes()

}

