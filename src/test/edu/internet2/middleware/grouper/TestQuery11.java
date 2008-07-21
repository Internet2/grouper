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
import  junit.framework.*;
import  org.apache.commons.logging.*;

import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.StemNameFilter;
import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * @author  blair christensen.
 * @version $Id: TestQuery11.java,v 1.5 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestQuery11 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestQuery11.class);

  public TestQuery11(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.info("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testStemNameFilterFindSomethingScoped() {
    LOG.info("testStemNameFilterFindSomethingScoped");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemNameFilter("i2:a", r.ns)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 1,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemNameFilterFindSomethingScoped()

}

