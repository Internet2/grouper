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
 * Test {@link SubjectFinder} class with {@link InternalSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSubjectFinderInternal.java,v 1.1.2.2 2005-10-27 18:00:38 blair Exp $
 */
public class TestSubjectFinderInternal extends TestCase {

  private Source sa;

  public TestSubjectFinderInternal(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

/* 
  public void testFinder() { 
    Assert.assertNotNull("sa !null", sa);
    Assert.assertTrue("sa.id == isa", sa.getId().equals("isa"));
    Assert.assertTrue("sa.name == isa", sa.getName().equals("isa"));
  } // public void testFinder()

  public void testFinderTypes() {
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
  } // public void testFinderTypes()
*/

  public void testFinderBadSubject() {
    String id = "i do not exist";
    try { 
      Subject subj = SubjectFinder.findById(id);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testFinderBadSubject()

  public void testFinderBadSubjectWithBadType() {
    String  id    = "i do not exist";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findById(id, type);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testFinderBadSubjectWithBadType()

  public void testFinderBadSubjectWithGoodType() {
    String  id    = "i do not exist";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findById(id, type);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testFinderBadSubjectWithGoodType()

  public void testFinderBadSubjectByIdentifier() {
    String id = "i do not exist";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
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
  } // public void testFinderGrouperSystemSubjectByIdentifierWithGoodType()

}

