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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;

/**
 * Test {@link SubjectFinder} class with {@link InternalSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSubjectFinderInternal.java,v 1.7 2007-01-08 16:43:56 blair Exp $
 */
public class TestSubjectFinderInternal extends TestCase {

  public TestSubjectFinderInternal(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFinderBadSubject() {
    SubjectTestHelper.getSubjectByBadId(SubjectHelper.BAD_SUBJ_ID);
    Assert.assertTrue("failed to get bad subject", true);
  } // public void testFinderBadSubject()

  public void testFinderBadSubjectWithType() {
    SubjectTestHelper.getSubjectByBadIdType(SubjectHelper.BAD_SUBJ_ID, "person");
    Assert.assertTrue("failed to get bad subject", true);
  } // public void testFinderBadSubjectWithType()

  public void testFinderBadSubjectByIdentifier() {
    String id = "i do not exist";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifier()

  public void testFinderBadSubjectByIdentifierWithBadType() {
    String  id    = "i do not exist";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifierWithBadType()

  public void testFinderBadSubjectByIdentifierWithGoodType() {
    String  id    = "i do not exist";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifierWithGoodType()

  public void testFinderGrouperSystemSubject() {
    String id = "GrouperSystem";
    try { 
      Subject subj = SubjectFinder.findById(id);
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
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubject()

  public void testFinderGrouperSystemSubjectWithBadType() {
    String  id    = "GrouperSystem";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findById(id, type);
      Assert.fail("found good subject with bad type: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find good subject with bad type", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectWithBadType()

  public void testFinderGrouperSystemSubjectWithGoodType() {
    String  id    = "GrouperSystem";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findById(id, type);
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
        subj.getType().getName().equals(type)
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectWithGoodType()

  public void testFinderGrouperSystemSubjectByIdentifier() {
    String id = "GrouperSystem";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id);
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
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifier()

  public void testFinderGrouperSystemSubjectByIdentifierWithBadType() {
    String  id    = "GrouperSystem";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type);
      Assert.fail("found good subject with bad type: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find good subject with bad type", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifierWithBadType()

  public void testFinderGrouperSystemSubjectByIdentifierWithGoodType() {
    String  id    = "GrouperSystem";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type);
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
        subj.getType().getName().equals(type)
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifierWithGoodType()

}

