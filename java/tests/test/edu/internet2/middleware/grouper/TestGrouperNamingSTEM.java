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
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link GrouperNamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperNamingSTEM.java,v 1.10 2005-11-28 17:53:06 blair Exp $
 */
public class TestGrouperNamingSTEM extends TestCase {

  // Private Class Constants
  private static final Privilege PRIV = NamingPrivilege.STEM;


  // Private Class Variables
  Stem            edu;
  Stem            root;
  GrouperSession  s;
  Set             stems = new HashSet();
  Set             subjs = new HashSet();


  public TestGrouperNamingSTEM(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
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
    PrivHelper.getPrivs(s, edu, s.getSubject(),       0, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0,  0, false, false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1,  0, false, false);
    // TODO PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
    // TODO PrivHelper.subjInStems(s, s.getSubject(), stems, PRIV);
  } // public void testDefaultPrivs()

  public void testGrantPrivs() {
    PrivHelper.grantPriv( s, edu, s.getSubject()      , PRIV);      
    PrivHelper.grantPriv( s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(  s, edu, s.getSubject()      , 1, true,  true);
    PrivHelper.getPrivs(  s, edu, SubjectHelper.SUBJ0 , 1, false, true);
    PrivHelper.getPrivs(  s, edu, SubjectHelper.SUBJ1 , 0, false, false);
/* TODO 
    subjs.add(s.getSubject());
    subjs.add(SubjectHelper.SUBJ0);
    PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
*/
    stems.add(edu);
    // TODO PrivHelper.subjInStems(s, s.getSubject(), stems, PRIV);
  } // public void testGrantPrivs()

  // TODO Genericize?
/* TODO
  public void testGetPrivs() {
    Subject subj = SubjectHelper.SUBJ0;
    PrivHelper.grantPriv( s, edu, subj , PRIV);      
    List  creators  = new ArrayList( edu.getCreators() );
    List  stemmers  = new ArrayList( edu.getStemmers() );
    Assert.assertTrue("creators: 0", creators.size() == 0);
    Assert.assertTrue("stemmers: 1", stemmers.size() == 1);
    NamingPrivilege np = (NamingPrivilege) stemmers.get(0);
    Assert.assertNotNull("np !null", np);
    Assert.assertTrue(
      "np instanceof NamingPrivilege", np instanceof NamingPrivilege
    );
    Assert.assertTrue(
      "np implementation name",
      np.getImplementationName().equals(
        "edu.internet2.middleware.grouper.GrouperNamingAdapter"
      )
    );  
    Assert.assertTrue("np revokable", np.isRevokable());
    Assert.assertTrue("np name", np.getName().equals(PRIV.toString()));
    Assert.assertTrue(
      "np object instanceof Stem", np.getObject() instanceof Stem
    );
    Assert.assertTrue(
      "np object == edu", 
      ( (Stem) np.getObject() ).equals(edu)
    );
    try {
      Assert.assertTrue(
        "np owner == subj", np.getOwner().equals(subj)
      );
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail("np has no owner");
    }
    Assert.assertTrue(
      "np subject", np.getSubject().equals(subj)
    );
  } // public void testGrantPrivs()
*/

  public void testRevokePrivs() {
    PrivHelper.grantPriv(s, edu, s.getSubject()      , PRIV);      
    PrivHelper.grantPriv(s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 1, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 1, false, true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    PrivHelper.revokePriv(s, edu, s.getSubject()      , PRIV);      
    PrivHelper.revokePriv(s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 0, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 0, false, false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    // TODO PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
    // TODO PrivHelper.subjInStems(s, s.getSubject(), stems, PRIV);
  } // public void testRevokePrivs()

}

