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

package edu.internet2.middleware.grouper;
import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.filter.GroupModifiedBeforeFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestQuery17.java,v 1.7 2008-09-29 03:38:27 mchyzer Exp $
 * @since   1.1.0
 */
public class TestQuery17 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestQuery17.class);

  public TestQuery17(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.info("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGroupModifiedBeforeFilterFindSomethingScoped() {
    LOG.info("testGroupModifiedBeforeFilterFindSomethingScoped");
    try {
      R     r = R.populateRegistry(2, 1, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("b", "a");
      a.setDescription("modified");
      a.store();
      b.setDescription("modified");
      b.store();
      Date  d = new Date( new Date().getTime() + T.DATE_OFFSET );
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedBeforeFilter(d, r.getStem("a"))
      );
      T.amount( "groups"  , 1,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedBeforeFilterFindSomethingScoped()

} // public class TestQuery17

