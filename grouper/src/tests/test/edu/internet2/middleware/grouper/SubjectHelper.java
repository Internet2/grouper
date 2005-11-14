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

import  com.devclue.grouper.subject.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;

/**
 * {@link Subject} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectHelper.java,v 1.3 2005-11-14 18:35:39 blair Exp $
 */
public class SubjectHelper {

  // Protected Class Constants
  protected static final MockSourceAdapter  MSA   = new MockSourceAdapter(
    "mock source adapter", "msa"
  );
  protected static final Subject            SUBJ0 = new MockSubject(
    "subject 0", "my name is subject 0", MSA
  );
  protected static final Subject            SUBJ1 = new MockSubject(
    "subject 1", "my name is subject 1", MSA
  );


  // Protected Class Methods

  // Don't get a subject by bad id
  protected static void getSubjectByBadId(String id) { 
    try {
      Subject subj = SubjectFinder.findById(id);
      Assert.fail("found bad subject '" + id + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // protected static void getSubjectByBadId(id)

  // Don't get a subject by bad identifier
  protected static void getSubjectByBadIdentifier(String id) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id);
      Assert.fail("found bad subject '" + id + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // protected static void getSubjectByBadIdentifier(id)

  // Don't get a subject by bad id and type
  protected static void getSubjectByBadIdType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findById(id, type);
      Assert.fail("found bad subject '" + id + "'/'" + type + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // protected static void getSubjectByBadIdType(id, type)

  // Don't get a subject by bad identifier and type
  protected static void getSubjectByBadIdentifierType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id, type);
      Assert.fail("found bad subject '" + id + "'/'" + type + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // protected static void getSubjectByBadIdentifierType(id, type)

  // Get a subject by id
  // @return  A {@link Subject}
  protected static Subject getSubjectById(String id) { 
    try {
      Subject subj = SubjectFinder.findById(id);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject '" + id + "': " + e.getMessage());
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static Subject getSubjectById(id)

  // Get a subject by identifier
  // @return  A {@link Subject}
  protected static Subject getSubjectByIdentifier(String id) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj name", subj.getName().equals(id));
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject '" + id + "': " + e.getMessage());
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static Subject getSubjectByIdentifier(id)

  // Get a subject by id and type
  // @return  A {@link Subject}
  protected static Subject getSubjectByIdType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findById(id, type);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      Assert.assertTrue(
        "subj type",subj.getType().getName().equals(type)
      );
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail(
        "failed to find subject '" + id + "'/'" + type + "': " + e.getMessage()
      );
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static Subject getSubjectByIdType(id, type)

  // Get a subject by identifier and type
  // @return  A {@link Subject}
  protected static Subject getSubjectByIdentifierType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id, type);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",subj.getType().getName().equals(type)
      );
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail(
        "failed to find subject '" + id + "'/'" + type + "': " + e.getMessage()
      );
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static Subject getSubjectByIdentifierType(id, type)

}

