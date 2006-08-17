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
import  edu.internet2.middleware.grouper.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSetting2.java,v 1.1 2006-08-17 16:28:18 blair Exp $
 * @since   1.1.0
 */
public class TestSetting2 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestSetting2.class);


  public TestSetting2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {  
    LOG.debug("tearDown");
  }

  public void testGrouperProps() {
    LOG.info("testGrouperProps");

    String k = "privileges.access.interface";
    String v = "edu.internet2.middleware.grouper.GrouperAccessAdapter";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "privileges.naming.interface";
    v = "edu.internet2.middleware.grouper.GrouperNamingAdapter";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.create.grant.all.admin";
    v = "false";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.create.grant.all.optin";
    v = "false";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.create.grant.all.optout";
    v = "false";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.create.grant.all.read";
    v = "true";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.create.grant.all.update";
    v = "false";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.create.grant.all.view";
    v = "true";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "stems.create.grant.all.create";
    v = "false";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "stems.create.grant.all.stem";
    v = "false";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "memberships.log.group.effective.add";
    v = "true";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "memberships.log.group.effective.del";
    v = "true";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "memberships.log.stem.effective.add";
    v = "true";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "memberships.log.stem.effective.del";
    v = "true";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.wheel.use";
    v = "false";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
    k = "groups.wheel.group";
    v = "your:wheel:group";
    Assert.assertTrue(k, GrouperConfig.getProperty(k).equals(v));
  } // public void testGrouperProps()

} // public class TestSetting2

