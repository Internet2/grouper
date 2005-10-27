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

/**
 * Test {@link InternalSourceAdapter} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestInternalSourceAdapter.java,v 1.1.2.3 2005-10-27 18:00:38 blair Exp $
 */
public class TestInternalSourceAdapter extends TestCase {

  private Source sa;

  public TestInternalSourceAdapter(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
    sa = new InternalSourceAdapter("isa", "isa");
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests
 
  public void testAdapter() { 
    Assert.assertNotNull("sa !null", sa);
    Assert.assertTrue("sa.id == isa", sa.getId().equals("isa"));
    Assert.assertTrue("sa.name == isa", sa.getName().equals("isa"));
  } // public void testAdapter()

  public void testAdapterTypes() {
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
    String id = "i do not exist";
    try { 
      Subject subj = sa.getSubjectByIdentifier(id);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testAdapterBadSubjectByIdentifer() 

  public void testAdapterGrouperSystemSubject() {
    String id = "GrouperSystem";
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
    String id = "GrouperSystem";
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

}

