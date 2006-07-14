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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestBug0.java,v 1.3 2006-07-14 17:10:54 blair Exp $
 * @since   1.0
 */
public class TestBug0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestBug0.class);

  public TestBug0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testChildrenOfViaInMofDeletion() {
    LOG.info("testChildrenOfViaInMofDeletion");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   gA    = r.getGroup("a", "a");   
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");

      gB.addMember( gA.toSubject() );
      // gA -> gB

      gC.addMember( gB.toSubject() );
      // gA -> gB
      // gB -> gC
      // gA -> gB -> gC

      gA.addMember(subjA);
      // gA -> gB
      // gB -> gC
      // gA -> gB -> gC
      // sA -> gA
      // sA -> gA -> gB
      // sA -> gA -> gB -> gC

      try {
        gB.deleteMember( gA.toSubject() );
        // gB -> gC
        // sA -> gA
        Assert.assertTrue("no exception thrown", true);
        T.getMemberships( gA, 1 );
        T.getMemberships( gB, 0 );
        T.getMemberships( gC, 1 );
      }
      catch (GrouperRuntimeException eGRT) {
        Assert.fail("runtime exception thrown: " + eGRT.getMessage());
      }

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testChildrenOfViaInMofDeletion()

} // public class TestBug0

