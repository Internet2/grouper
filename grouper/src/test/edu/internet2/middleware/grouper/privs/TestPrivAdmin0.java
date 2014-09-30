/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.privs;
import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestPrivAdmin0.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.1.0
 */
public class TestPrivAdmin0 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestPrivAdmin0.class);

  public TestPrivAdmin0(String name) {
    super(name);
  }

  public void testShouldStillHaveViewAfterHavingPrivRevokedBecauseOfGrouperAll() {
    LOG.info("testShouldStillHaveViewAfterHavingPrivRevokedBecauseOfGrouperAll");
    try {
      R       r     = R.populateRegistry(1, 1, 2);
      Group   g     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject all   = SubjectFinder.findAllSubject();
      g.grantPriv(subjA, AccessPrivilege.VIEW);
      g.revokePriv(all, AccessPrivilege.VIEW);
      g.revokePriv(all, AccessPrivilege.READ);
      g.grantPriv(subjA,  AccessPrivilege.ADMIN);
      g.grantPriv(all, AccessPrivilege.VIEW);
      String  name  = g.getName();
      r.rs.stop();

      GrouperSession  nrs = GrouperSession.start(subjB);
      g = GroupFinder.findByName(r.getSession(), name, true);
      g.revokePriv(subjA, AccessPrivilege.VIEW);
      // Should still have VIEW through ALL
      Assert.assertTrue("subjA VIEW", g.hasView(subjA));
      nrs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }

  } // public void testShouldStillHaveViewAfterHavingPrivRevokedBecauseOfGrouperAll()

} // public class TestPrivAdmin0

