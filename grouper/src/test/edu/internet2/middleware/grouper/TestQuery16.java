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
 * @version $Id: TestQuery16.java,v 1.1 2006-08-16 20:22:11 blair Exp $
 * @since   1.1.0
 */
public class TestQuery16 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestQuery16.class);

  public TestQuery16(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.info("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGroupModifiedBeforeFilterFindSomething() {
    LOG.info("testGroupModifiedBeforeFilterFindSomething");
    try {
      R     r = R.populateRegistry(2, 1, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("b", "a");
      a.setDescription("modified");
      b.setDescription("modified");
      Date  d = new Date( new Date().getTime() + 1 );
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedBeforeFilter(d, r.root)
      );
      T.amount( "groups"  , 2,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedBeforeFilterFindSomething()

} // public class TestQuery16

