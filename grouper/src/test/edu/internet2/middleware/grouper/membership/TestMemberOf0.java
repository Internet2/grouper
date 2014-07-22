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

package edu.internet2.middleware.grouper.membership;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestMemberOf0.java,v 1.2 2009-08-12 12:44:45 shilen Exp $
 */
public class TestMemberOf0 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMemberOf0.class);

  public TestMemberOf0(String name) {
    super(name);
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
      T.getMemberships(gA, 4);
      T.getMemberships(gB, 4);
      T.getImmediateMemberships(gA, 2);
      T.getImmediateMemberships(gB, 2);
      T.getEffectiveMemberships(gA, 2);
      T.getEffectiveMemberships(gB, 2);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFullLoop()

}

