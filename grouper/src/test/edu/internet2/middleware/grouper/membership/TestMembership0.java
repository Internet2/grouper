/**
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
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestMembership0.java,v 1.2 2009-08-18 23:11:39 shilen Exp $
 */
public class TestMembership0 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembership0.class);

  public TestMembership0(String name) {
    super(name);
  }

  public void testEffectiveUuidDoesNotChange() {
    LOG.info("testEffectiveUuidDoesNotChange");
    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      r.getSubject("b");
      r.getSubject("c");
      gA.addMember(subjA);
      gB.addMember(gA.toSubject());
      T.amount("gB eff mships[0]", 1, gB.getEffectiveMemberships().size());
      Iterator iter = gB.getEffectiveMemberships().iterator();
      Membership  expMS   = null;
      String      expUUID = null;
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        expMS   = ms;
        expUUID = ms.getUuid();
      }
      Membership ms0 = GrouperDAOFactory.getFactory().getMembership().findByUuid(expUUID, true, true);
      Assert.assertEquals("ms0", expMS, ms0);

      gA.deleteMember(subjA);
      T.amount("gB eff mships[D]", 0, gB.getEffectiveMemberships().size());

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testEffectiveUuidDoesNotChange()

}

