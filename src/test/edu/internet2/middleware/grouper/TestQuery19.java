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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestQuery19.java,v 1.1 2006-09-11 18:14:47 blair Exp $
 * @since   1.1.0
 */
public class TestQuery19 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestQuery19.class);

  public TestQuery19(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.info("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testMembershipCreatedBeforeFilter() {
    LOG.info("testMembershipCreatedBeforeFilter");
    try {
      R       r       = R.populateRegistry(2, 1, 2);
      Stem    nsA     = r.getStem("a");
      Group   a       = r.getGroup("a", "a");
      Group   b       = r.getGroup("b", "a");
      Subject subjA   = r.getSubject("a");
      Subject subjB   = r.getSubject("b");

      Date    past    = DateHelper.getPastDate();
      a.addMember(subjA); 
      b.addMember(subjB);
      Date    future  = DateHelper.getFutureDate();

      // Find nothing
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedBeforeFilter(past, StemFinder.findRootStem(r.rs))
      );
      T.amount( "nothing - groups"  , 0 , gq.getGroups().size()       );
      T.amount( "nothing - members" , 0 , gq.getMembers().size()      );
      T.amount( "nothing - mships"  , 0 , gq.getMemberships().size()  );
      T.amount( "nothing - stems"   , 0 , gq.getStems().size()        );

      // Find something
      gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedBeforeFilter(future, StemFinder.findRootStem(r.rs))
      );
      T.amount( "something - groups"  , 0 , gq.getGroups().size()       );
      T.amount( "something - members" , 2 , gq.getMembers().size()      );
      T.amount( "something - mships"  , 2 , gq.getMemberships().size()  );
      T.amount( "something - stems"   , 0 , gq.getStems().size()        );

      // Find something - scoped
      gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedBeforeFilter(future, nsA)
      );
      T.amount( "scoped - groups"   , 0 , gq.getGroups().size()       );
      T.amount( "scoped - members"  , 1 , gq.getMembers().size()      );
      T.amount( "scoped - mships"   , 1 , gq.getMemberships().size()  );
      T.amount( "scoped - stems"    , 0 , gq.getStems().size()        );

      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testMembershipCreatedBeforeFilter()

} // public class TestQuery19

