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

package edu.internet2.middleware.grouper.filter;
import java.util.Iterator;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupTypeFilter.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.0
 */
public class TestGroupTypeFilter extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestGroupTypeFilter.class);

  public TestGroupTypeFilter(String name) {
    super(name);
  }

  /**
   * @see GrouperTest#setupConfigs
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    ApiConfig.testConfig.put("groups.wheel.use", "false");

  }


  public void testQueryByGroupTypeFilterNothing() {
    LOG.info("testQueryByGroupTypeFilterNothing");
    try {
      R             r   = R.populateRegistry(0, 0, 0);
      GrouperQuery  gq  = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base", true), r.root )
      );
      T.amount( "groups"      , 0, gq.getGroups().size()      );
      T.amount( "members"     , 0, gq.getMembers().size()     );
      T.amount( "memberships" , 0, gq.getMemberships().size() );
      T.amount( "stems"       , 0, gq.getStems().size()       );
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testQueryByGroupTypeFilterNothing()

  public void testQueryByGroupTypeFilterSomething() {
    LOG.info("testQueryByGroupTypeFilterSomething");
    try {
      R             r   = R.populateRegistry(2, 2, 0);
      GrouperQuery  gq  = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base", true), r.root )
      );
      T.amount( "groups"      , 4, gq.getGroups().size()      );
      T.amount( "members"     , 0, gq.getMembers().size()     );
      T.amount( "memberships" , 0, gq.getMemberships().size() );
      T.amount( "stems"       , 0, gq.getStems().size()       );
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testQueryByGroupTypeFilterSomething()

  public void testQueryByGroupTypeFilterSomethingCustomType() {
    LOG.info("testQueryByGroupTypeFilterSomethingCustomType");
    try {
      R             r     = R.populateRegistry(2, 2, 0);
      GroupType     type  = GroupType.createType(r.rs, "custom type");
      // Add custom type to all existing groups
      GrouperQuery  gq0   = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base", true), r.root )
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
      unexpectedException(e);
    }
  } // public void testQueryByGroupTypeFilterSomethingCustomType()

  public void testQueryByGroupTypeFilterSomethingScoped() {
    LOG.info("testQueryByGroupTypeFilterSomethingScoped");
    try {
      R             r   = R.populateRegistry(2, 2, 0);
      GrouperQuery  gq  = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base", true), r.getStem("a") )
      );
      T.amount( "groups"      , 2, gq.getGroups().size()      );
      T.amount( "members"     , 0, gq.getMembers().size()     );
      T.amount( "memberships" , 0, gq.getMemberships().size() );
      T.amount( "stems"       , 0, gq.getStems().size()       );

      GroupType     type  = GroupType.createType(r.rs, "custom type");
      gq  = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter(type, r.getStem("a"))
      );            
      T.amount( "groups"      , 0, gq.getGroups().size()      );
      T.amount( "members"     , 0, gq.getMembers().size()     ); 
      T.amount( "memberships" , 0, gq.getMemberships().size() );
      T.amount( "stems"       , 0, gq.getStems().size()       );

      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testQueryByGroupTypeFilterSomethingScoped()

  public void testQueryByGroupTypeFilterSomethingScopedCustomType() {
    LOG.info("testQueryByGroupTypeFilterSomethingScopedCustomType");
    try {
      R             r     = R.populateRegistry(2, 2, 0);
      GroupType     type  = GroupType.createType(r.rs, "custom type");
      // Add custom type to all existing groups
      GrouperQuery  gq0   = GrouperQuery.createQuery(
        r.rs, new GroupTypeFilter( GroupTypeFinder.find("base", true), r.root )
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
      unexpectedException(e);
    }
  } // public void testQueryByGroupTypeFilterSomethingScopedCustomType()

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(TestGroupTypeFilter.class);
  }

} // public class TestGroupTypeFilter extends GrouperTest

