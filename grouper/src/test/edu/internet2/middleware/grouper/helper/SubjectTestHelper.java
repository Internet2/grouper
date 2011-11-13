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

package edu.internet2.middleware.grouper.helper;
import junit.framework.Assert;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * {@link Subject} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectTestHelper.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
 public class SubjectTestHelper {

  // public Class Constants
  public static final Subject  SUBJA;
  public static final Subject  SUBJR;
  public static final Subject  SUBJ0;
  public static final Subject  SUBJ1;
  public static final Subject  SUBJ2;
  public static final Subject  SUBJ3;
  public static final Subject  SUBJ4;
  public static final Subject  SUBJ5;
  public static final Subject  SUBJ6;
  public static final Subject  SUBJ7;
  public static final Subject  SUBJ8;
  public static final Subject  SUBJ9;
  public static final String   SUBJ0_ID    = "test.subject.0";
  public static final String   SUBJ0_IDENTIFIER    = "id.test.subject.0";
  public static final String   SUBJ0_NAME  = "my name is test.subject.0";
  public static final String   SUBJ0_TYPE  = "person";
  public static final String   SUBJ1_ID    = "test.subject.1";
  public static final String   SUBJ1_IDENTIFIER    = "id.test.subject.1";
  public static final String   SUBJ1_NAME  = "my name is test.subject.1";
  public static final String   SUBJ1_TYPE  = "person";
  public static final String   SUBJ2_ID    = "test.subject.2";
  public static final String   SUBJ2_IDENTIFIER    = "id.test.subject.2";
  public static final String   SUBJ2_NAME  = "my name is test.subject.2";
  public static final String   SUBJ2_TYPE  = "person";
  public static final String   SUBJ3_ID    = "test.subject.3";
  public static final String   SUBJ3_IDENTIFIER    = "id.test.subject.3";
  public static final String   SUBJ3_NAME  = "my name is test.subject.3";
  public static final String   SUBJ3_TYPE  = "person";
  public static final String   SUBJ4_ID    = "test.subject.4";
  public static final String   SUBJ4_IDENTIFIER    = "id.test.subject.4";
  public static final String   SUBJ4_NAME  = "my name is test.subject.4";
  public static final String   SUBJ4_TYPE  = "person";
  public static final String   SUBJ5_ID    = "test.subject.5";
  public static final String   SUBJ5_IDENTIFIER    = "id.test.subject.5";
  public static final String   SUBJ5_NAME  = "my name is test.subject.5";
  public static final String   SUBJ5_TYPE  = "person";
  public static final String   SUBJ6_ID    = "test.subject.6";
  public static final String   SUBJ6_IDENTIFIER    = "id.test.subject.6";
  public static final String   SUBJ6_NAME  = "my name is test.subject.6";
  public static final String   SUBJ6_TYPE  = "person";
  public static final String   SUBJ7_ID    = "test.subject.7";
  public static final String   SUBJ7_IDENTIFIER    = "id.test.subject.7";
  public static final String   SUBJ7_NAME  = "my name is test.subject.7";
  public static final String   SUBJ7_TYPE  = "person";
  public static final String   SUBJ8_ID    = "test.subject.8";
  public static final String   SUBJ8_IDENTIFIER    = "id.test.subject.8";
  public static final String   SUBJ8_NAME  = "my name is test.subject.8";
  public static final String   SUBJ8_TYPE  = "person";
  public static final String   SUBJ9_ID    = "test.subject.9";
  public static final String   SUBJ9_IDENTIFIER    = "id.test.subject.9";
  public static final String   SUBJ9_NAME  = "my name is test.subject.9";
  public static final String   SUBJ9_TYPE  = "person";
  public static final String   SUBJ_ALL    = "GrouperAll";
  public static final String   SUBJ_ROOT   = "GrouperSystem";

  static {
    try {
      SUBJ0 = SubjectFinder.findById(SUBJ0_ID, true);
      SUBJ1 = SubjectFinder.findById(SUBJ1_ID, true);
      SUBJ2 = SubjectFinder.findById(SUBJ2_ID, true);
      SUBJ3 = SubjectFinder.findById(SUBJ3_ID, true);
      SUBJ4 = SubjectFinder.findById(SUBJ4_ID, true);
      SUBJ5 = SubjectFinder.findById(SUBJ5_ID, true);
      SUBJ6 = SubjectFinder.findById(SUBJ6_ID, true);
      SUBJ7 = SubjectFinder.findById(SUBJ7_ID, true);
      SUBJ8 = SubjectFinder.findById(SUBJ8_ID, true);
      SUBJ9 = SubjectFinder.findById(SUBJ9_ID, true);
      SUBJA = SubjectFinder.findById(SUBJ_ALL, true);
      SUBJR = SubjectFinder.findById(SUBJ_ROOT, true);
    }
    catch (Exception e) {
      throw new RuntimeException(
        "unable to run tests without subjects: " + e.getMessage(), e
      );
    }
  } // static


  // public Class Methods //

  // Don't get a subject by bad id
  public static void getSubjectByBadId(String id) { 
    try {
      Subject subj = SubjectFinder.findById(id, true);
      Assert.fail("found bad subject '" + id + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public static void getSubjectByBadId(id)

  // Don't get a subject by bad identifier and type
  public static void getSubjectByBadIdentifierType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.fail("found bad subject '" + id + "'/'" + type + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // public static void getSubjectByBadIdentifierType(id, type)

  // Get a subject by id
  // @return  A {@link Subject}
  public static Subject getSubjectById(String id) { 
    try {
      Subject subj = SubjectFinder.findById(id, true);
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
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperException();
  } // public static Subject getSubjectById(id)

  // Get a subject by identifier
  // @return  A {@link Subject}
  public static Subject getSubjectByIdentifier(String id) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
    //This is only likely to be true for InternalSubject  but
      //we are now allowing the name to be configured, so do not 
      //do a name check
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject '" + id + "': " + e.getMessage());
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperException();
  } // public static Subject getSubjectByIdentifier(id)

  // Get a subject by id and type
  // @return  A {@link Subject}
  public static Subject getSubjectByIdType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findById(id, type, true);
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
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperException();
  } // public static Subject getSubjectByIdType(id, type)

  // Get a subject by identifier and type
  // @return  A {@link Subject}
  public static Subject getSubjectByIdentifierType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      
      //This is only likely to be true for InternalSubject  but
      //we are now allowing the name to be configured, so do not 
      //do a name check
      //Assert.assertTrue("subj name", subj.getName().equals(id));
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
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperException();
  } // public static Subject getSubjectByIdentifierType(id, type)

} // class SubjectTestHelper

