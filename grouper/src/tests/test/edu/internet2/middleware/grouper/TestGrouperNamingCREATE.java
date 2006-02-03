/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link GrouperNamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperNamingCREATE.java,v 1.11 2006-02-03 19:38:53 blair Exp $
 */
public class TestGrouperNamingCREATE extends TestCase {

  // Private Class Constants
  private static final Privilege PRIV = NamingPrivilege.CREATE;


  // Private Class Variables
  Stem            edu;
  Stem            root;
  GrouperSession  s;
  Set             stems = new HashSet();
  Set             subjs = new HashSet();


  public TestGrouperNamingCREATE(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    stems = new HashSet();
    subjs = new HashSet();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testDefaultPrivs() {
    PrivHelper.getPrivs(s, edu, s.getSubject(),       1, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0,  0, false, false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1,  0, false, false);
    PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
    PrivHelper.subjInStems(s, s.getSubject(), stems, PRIV);
  } // public void testDefaultPrivs()

  public void testGrantPrivs() {
    PrivHelper.grantPriv( s, edu, s.getSubject()      , PRIV);      
    PrivHelper.grantPriv( s, edu, SubjectHelper.SUBJ0 , PRIV);    
    PrivHelper.getPrivs(  s, edu, s.getSubject()      , 2, true,  true);
    PrivHelper.getPrivs(  s, edu, SubjectHelper.SUBJ0 , 1, true,  false);
    PrivHelper.getPrivs(  s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    subjs.add(s.getSubject());
    subjs.add(SubjectHelper.SUBJ0);
    PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
    stems.add(edu);
    PrivHelper.subjInStems(s, s.getSubject(), stems, PRIV);
    PrivHelper.subjInStems(s, SubjectHelper.SUBJ0, stems, PRIV);
  } // public void testGrantPrivs()

  public void testGrantPrivsAll() {
    PrivHelper.grantPriv( s, edu, SubjectFinder.findAllSubject(), PRIV);
    PrivHelper.hasPriv(s, edu, s.getSubject(),       PRIV, true);
    PrivHelper.hasPriv(s, edu, SubjectHelper.SUBJ0,  PRIV, true);
    PrivHelper.hasPriv(s, edu, SubjectHelper.SUBJ1,  PRIV, true);
    subjs.add(SubjectFinder.findAllSubject());
    PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
    stems.add(edu);
    PrivHelper.subjInStems(s, SubjectFinder.findAllSubject(), stems, PRIV);
  } // public void testGrantPrivs()

  public void testRevokePrivs() {
    PrivHelper.grantPriv(s, edu, s.getSubject()      , PRIV);      
    PrivHelper.grantPriv(s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 2, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 1, true,  false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    PrivHelper.revokePriv(s, edu, s.getSubject()      , PRIV);      
    PrivHelper.revokePriv(s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 1, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 0, false, false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
    PrivHelper.subjInStems(s, s.getSubject(), stems, PRIV);
  } // public void testRevokePrivs()

  public void testRevokePrivsAll() {
    PrivHelper.grantPriv( s, edu, SubjectFinder.findAllSubject(), PRIV);
    PrivHelper.hasPriv(s, edu, s.getSubject(),       PRIV, true);
    PrivHelper.hasPriv(s, edu, SubjectHelper.SUBJ0,  PRIV, true);
    PrivHelper.hasPriv(s, edu, SubjectHelper.SUBJ1,  PRIV, true);
    PrivHelper.revokePriv(s, edu, SubjectFinder.findAllSubject(), PRIV);
    PrivHelper.hasPriv(s, edu, s.getSubject(),       PRIV, true);
    PrivHelper.hasPriv(s, edu, SubjectHelper.SUBJ0,  PRIV, false);
    PrivHelper.hasPriv(s, edu, SubjectHelper.SUBJ1,  PRIV, false);
  } // public void testRevokePrivsAll()

  public void testRevokeAllPrivs() {
    PrivHelper.grantPriv(s, edu, s.getSubject()      , PRIV);      
    PrivHelper.grantPriv(s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 2, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 1, true,  false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    PrivHelper.revokePriv(s, edu, PRIV);
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 1, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 0, false, false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
    PrivHelper.subjInStems(s, s.getSubject(), stems, PRIV);
  } // public void testRevokeAllPrivs()

}

