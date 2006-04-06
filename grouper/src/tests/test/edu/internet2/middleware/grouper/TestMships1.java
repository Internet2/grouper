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
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: TestMships1.java,v 1.1 2006-04-06 16:53:38 blair Exp $
 */
public class TestMships1 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestMships1.class);

  public TestMships1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testGetMembers() {
    LOG.info("testGetMembers");
    try {
      R r = R.createOneStemAndTwoGroups();
      try {
        // Base
        T.getMembers(         r.i2, 0);
        T.getImmediateMembers(r.i2, 0);
        T.getEffectiveMembers(r.i2, 0);
        T.getMembers(         r.uc, 0);
        T.getImmediateMembers(r.uc, 0);
        T.getEffectiveMembers(r.uc, 0);

        // Add members
        r.i2.addMember(r.subj0);
        r.uc.addMember(r.subj1);
        r.i2.addMember(r.uc.toSubject());
        r.rs.waitForTx();
        T.getMembers(         r.i2, 3);
        T.getImmediateMembers(r.i2, 2);
        T.getEffectiveMembers(r.i2, 1);
        T.getMembers(         r.uc, 1);
        T.getImmediateMembers(r.uc, 1);
        T.getEffectiveMembers(r.uc, 0);

        // Now do things the hard way
        T.getChildStems(  r.root, 1);
        T.getChildGroups( r.root, 0);
        Iterator nsIter = r.root.getChildStems().iterator();
        while (nsIter.hasNext()) {
          Stem ns = (Stem) nsIter.next();
          if (ns.equals(r.edu)) {
            T.getChildStems(  ns, 0 );
            T.getChildGroups( ns, 2 );
            Iterator gIter = ns.getChildGroups().iterator();
            while (gIter.hasNext()) {
              Group g = (Group) gIter.next();
              if      (g.equals(r.i2)) {
                T.getMembers(         g, 3);   
                T.getImmediateMembers(g, 2);   
                T.getEffectiveMembers(g, 1);   
              }
              else if (g.equals(r.uc)) {
                T.getMembers(         g, 1);   
                T.getImmediateMembers(g, 1);   
                T.getEffectiveMembers(g, 0);   
              }
              else {
                Assert.fail("unknown child group: " + g);
              }
            }
          }
          else {
            Assert.fail("unknown child stem: " + ns);
          }          
        }
      }
      catch (Exception e) {
        Assert.fail(e.getMessage());
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetMembers()

}

