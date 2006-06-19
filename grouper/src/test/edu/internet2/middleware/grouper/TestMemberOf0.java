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

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestMemberOf0.java,v 1.2 2006-06-19 02:06:45 blair Exp $
 */
public class TestMemberOf0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMemberOf0.class);

  public TestMemberOf0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFullLoop() {
    LOG.info("testFullLoop");
    try {
      R       r     = R.populateRegistry(1, 2, 2);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");

      // Add subjA to gA
      gA.addMember(subjA);
      T.getMemberships(gA, 1);
      T.getImmediateMemberships(gA, 1);
      T.getEffectiveMemberships(gA, 0);

      // Add subjB to gB
      gB.addMember(subjB);
      T.getMemberships(gB, 1);
      T.getImmediateMemberships(gB, 1);
      T.getEffectiveMemberships(gB, 0);

      // Add gB to gA
      gA.addMember(gB.toSubject());
      T.getMemberships(gA, 3);
      T.getMemberships(gB, 1);
      T.getImmediateMemberships(gA, 2);
      T.getImmediateMemberships(gB, 1);
      T.getEffectiveMemberships(gA, 1);
      T.getEffectiveMemberships(gB, 0);

      // Add gA to gB

      gB.addMember(gA.toSubject());
      // gA -> gB
      //
      // * subjA -> gA
      // * gB -> gA
      // * subjB -> gB -> gA
      // + gA -> gB -> gA
      // + subjA -> gA -> gB -> gA
      // + gB -> gA -> gB -> gA
      // + subjB -> gB -> gA -> gB -> gA
      //
      // * subjB -> gB
      // + gA -> gB
      // + subjA -> gA -> gB
      // + gB -> gA -> gB
      // + subjB -> gB -> gA -> gB
      T.getMemberships(gA, 7);
      T.getMemberships(gB, 5);
      T.getImmediateMemberships(gA, 2);
      T.getImmediateMemberships(gB, 2);
      T.getEffectiveMemberships(gA, 5);
      T.getEffectiveMemberships(gB, 3);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullLoop()

}

