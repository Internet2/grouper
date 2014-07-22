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

package edu.internet2.middleware.grouper.subj;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;

/**
 * Test {@link InternalSourceAdapter} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestInternalSourceAdapter.java,v 1.4 2009-09-02 05:57:26 mchyzer Exp $
 */
public class TestInternalSourceAdapter extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestInternalSourceAdapter("testGrouperAllAttributes"));
  }
  
  // Private Class Constants
  private static final  String  ID    = InternalSourceAdapter.ID;
  private static final  Log     LOG   = GrouperUtil.getLog(TestInternalSourceAdapter.class);
  private static final  String  NAME  = InternalSourceAdapter.NAME;


  // Private Class Variables
  private Source sa;

  public TestInternalSourceAdapter(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    sa = InternalSourceAdapter.instance();
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
      Subject subj = sa.getSubject(id, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public void testAdapterBadSubject()

  public void testAdapterBadSubjectByIdentifier() {
    LOG.info("testAdapterBadSubjectByIdentifier");
    String id = "i do not exist";
    try { 
      Subject subj = sa.getSubjectByIdentifier(id, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
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
    String id = SubjectTestHelper.SUBJ_ALL;
    try { 
      Subject subj = sa.getSubject(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      
      //Remove assertion so that name can be configured
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public void testAdapterGrouperAllSubject()

  public void testAdapterGrouperAllSubjectByIdentifier() {
    LOG.info("testAdapterGrouperAllSubjectByIdentifier");
    String id = SubjectTestHelper.SUBJ_ALL;
    try { 
      Subject subj = sa.getSubjectByIdentifier(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      
      //Remove assertion so that name can be configured
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public void testAdapterGrouperAllSubjectByIdentifier()

  public void testAdapterGrouperAllSubjectBySearch() {
    LOG.info("testAdapterGrouperAllSubjectBySearch");
    String id = SubjectTestHelper.SUBJ_ALL;
    Set results = sa.search(id);
    Assert.assertTrue("found one", results.size() == 1);
  } // public void testAdapterGrouperAllSubjectByIdentifier()

  public void testAdapterGrouperSystemSubject() {
    LOG.info("testAdapterGrouperSystemSubject");
    String id = SubjectTestHelper.SUBJ_ROOT;
    try { 
      Subject subj = sa.getSubject(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      
      //Remove assertion so that name can be configured
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public void testAdapterGrouperSystemSubject()

  public void testAdapterGrouperSystemSubjectByIdentifier() {
    LOG.info("testAdapterGrouperSystemSubjectByIdentifier");
    String id = SubjectTestHelper.SUBJ_ROOT;
    try { 
      Subject subj = sa.getSubjectByIdentifier(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      
      //Remove assertion so that name can be configured
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public void testAdapterGrouperSystemSubjectByIdentifier()

  public void testAdapterGrouperSystemSubjectBySearch() {
    LOG.info("testAdapterGrouperSystemSubjectBySearch");
    String id = SubjectTestHelper.SUBJ_ROOT;
    Set results = sa.search(id);
    Assert.assertTrue("found one", results.size() == 1);
  } // public void testAdapterGrouperSystemSubjectByIdentifier()

  public void testGrouperAllAttributes() {
    LOG.info("testGrouperAllAttributes");
    String id = SubjectTestHelper.SUBJ_ALL;
    try { 
      Subject subj  = sa.getSubject(id, true);
      Map     attrs = subj.getAttributes();
      Assert.assertTrue("zero attrs", attrs.size() == 1);
      String  val   = subj.getAttributeValue("foo");
      Assert.assertTrue(
        "no attr (" + val + ")", val == null
      );
      Set     vals  = subj.getAttributeValues("foo");
      Assert.assertTrue("zero values", GrouperUtil.length(vals) == 0);
      Assert.assertTrue(
        "desc == name", subj.getDescription().equals(subj.getName())
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + e.getMessage());
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public void testGrouperAllAttributes()

}

