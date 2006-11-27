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
import  java.util.Iterator;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupTypeFilter.java,v 1.1 2006-11-27 19:43:06 blair Exp $
 * @since   1.2.0
 */
public class TestGroupTypeFilter extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestGroupTypeFilter.class);

  public TestGroupTypeFilter(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.info("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.info("tearDown");
  }

  public void testQueryByGroupTypeFilterNothing() {
    LOG.info("testQueryByGroupTypeFilterNothing");
    try {
      R             r   = R.populateRegistry(0, 0, 0);
      GrouperQuery  gq  = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base"), r.root )
      );
      T.amount( "groups"      , 0, gq.getGroups().size()      );
      T.amount( "members"     , 0, gq.getMembers().size()     );
      T.amount( "memberships" , 0, gq.getMemberships().size() );
      T.amount( "stems"       , 0, gq.getStems().size()       );
      r.rs.stop();
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testQueryByGroupTypeFilterNothing()

  public void testQueryByGroupTypeFilterSomething() {
    LOG.info("testQueryByGroupTypeFilterSomething");
    try {
      R             r   = R.populateRegistry(2, 2, 0);
      GrouperQuery  gq  = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base"), r.root )
      );
      T.amount( "groups"      , 4, gq.getGroups().size()      );
      T.amount( "members"     , 0, gq.getMembers().size()     );
      T.amount( "memberships" , 0, gq.getMemberships().size() );
      T.amount( "stems"       , 0, gq.getStems().size()       );
      r.rs.stop();
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testQueryByGroupTypeFilterSomething()

  public void testQueryByGroupTypeFilterSomethingCustomType() {
    LOG.info("testQueryByGroupTypeFilterSomethingCustomType");
    try {
      R             r     = R.populateRegistry(2, 2, 0);
      GroupType     type  = GroupType.createType(r.rs, "custom type");
      // Add custom type to all existing groups
      GrouperQuery  gq0   = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base"), r.root )
      );
      Group         g;
      Iterator      it    = gq0.getGroups().iterator();
      while (it.hasNext()) {
        g = (Group) it.next();
        g.addType(type);
      }
      // Now test
      GrouperQuery  gq1   = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter(type, r.root)
      );
      T.amount( "groups"      , 4, gq1.getGroups().size()      );
      T.amount( "members"     , 0, gq1.getMembers().size()     );
      T.amount( "memberships" , 0, gq1.getMemberships().size() );
      T.amount( "stems"       , 0, gq1.getStems().size()       );
      r.rs.stop();
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testQueryByGroupTypeFilterSomethingCustomType()

  public void testQueryByGroupTypeFilterSomethingScoped() {
    LOG.info("testQueryByGroupTypeFilterSomethingScoped");
    try {
      R             r   = R.populateRegistry(2, 2, 0);
      GrouperQuery  gq  = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base"), r.getStem("a") )
      );
      T.amount( "groups"      , 2, gq.getGroups().size()      );
      T.amount( "members"     , 0, gq.getMembers().size()     );
      T.amount( "memberships" , 0, gq.getMemberships().size() );
      T.amount( "stems"       , 0, gq.getStems().size()       );
      r.rs.stop();
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testQueryByGroupTypeFilterSomethingScoped()

  public void testQueryByGroupTypeFilterSomethingScopedCustomType() {
    LOG.info("testQueryByGroupTypeFilterSomethingScopedCustomType");
    try {
      R             r     = R.populateRegistry(2, 2, 0);
      GroupType     type  = GroupType.createType(r.rs, "custom type");
      // Add custom type to all existing groups
      GrouperQuery  gq0   = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base"), r.root )
      );
      Group         g;
      Iterator      it    = gq0.getGroups().iterator();
      while (it.hasNext()) {
        g = (Group) it.next();
        g.addType(type);
      }
      // Now test
      GrouperQuery  gq1   = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( type, r.getStem("a") )
      );
      T.amount( "groups"      , 2, gq1.getGroups().size()      );
      T.amount( "members"     , 0, gq1.getMembers().size()     );
      T.amount( "memberships" , 0, gq1.getMemberships().size() );
      T.amount( "stems"       , 0, gq1.getStems().size()       );
      r.rs.stop();
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testQueryByGroupTypeFilterSomethingScopedCustomType()

} // public class TestGroupTypeFilter extends GrouperTest

