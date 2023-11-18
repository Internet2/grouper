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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
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
  public static Subject  SUBJA;
  public static Subject  SUBJR;
  public static Subject  SUBJ0;
  public static Subject  SUBJ1;
  public static Subject  SUBJ2;
  public static Subject  SUBJ3;
  public static Subject  SUBJ4;
  public static Subject  SUBJ5;
  public static Subject  SUBJ6;
  public static Subject  SUBJ7;
  public static Subject  SUBJ8;
  public static Subject  SUBJ9;
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
      GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
      boolean started = false;
      if (grouperSession == null) {
        grouperSession = GrouperSession.startRootSession();
        started = true;
      } else {
        grouperSession = grouperSession.internal_getRootSession();
      }
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          SUBJ0 = SubjectFinder.findByIdAndSource(SUBJ0_ID, "jdbc", true);
          SUBJ1 = SubjectFinder.findByIdAndSource(SUBJ1_ID, "jdbc", true);
          SUBJ2 = SubjectFinder.findByIdAndSource(SUBJ2_ID, "jdbc", true);
          SUBJ3 = SubjectFinder.findByIdAndSource(SUBJ3_ID, "jdbc", true);
          SUBJ4 = SubjectFinder.findByIdAndSource(SUBJ4_ID, "jdbc", true);
          SUBJ5 = SubjectFinder.findByIdAndSource(SUBJ5_ID, "jdbc", true);
          SUBJ6 = SubjectFinder.findByIdAndSource(SUBJ6_ID, "jdbc", true);
          SUBJ7 = SubjectFinder.findByIdAndSource(SUBJ7_ID, "jdbc", true);
          SUBJ8 = SubjectFinder.findByIdAndSource(SUBJ8_ID, "jdbc", true);
          SUBJ9 = SubjectFinder.findByIdAndSource(SUBJ9_ID, "jdbc", true);
          SUBJA = SubjectFinder.findAllSubject();
          SUBJR = SubjectFinder.findRootSubject();
          return null;
        }
      });
      if (started) {
        GrouperSession.stopQuietly(grouperSession);
      }
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

