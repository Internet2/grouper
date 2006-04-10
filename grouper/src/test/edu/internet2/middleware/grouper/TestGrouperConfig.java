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
 * Test {@link GrouperConfig}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperConfig.java,v 1.1.2.1 2006-04-10 19:07:20 blair Exp $
 */
public class TestGrouperConfig extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGrouperConfig.class);


  public TestGrouperConfig(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {  
    LOG.debug("tearDown");
  }

  // Tests

  public void testConfig() {
    LOG.info("testConfig");
    GrouperConfig cfg = GrouperConfig.getInstance();
    Assert.assertNotNull("cfg !null", cfg);
    Assert.assertTrue("cfg instanceof GrouperConfig", cfg instanceof GrouperConfig);

    // Now test configuration properties
    String k = "privileges.access.interface";
    String v = "edu.internet2.middleware.grouper.GrouperAccessAdapter";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "privileges.naming.interface";
    v = "edu.internet2.middleware.grouper.GrouperNamingAdapter";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.create.grant.all.admin";
    v = "false";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.create.grant.all.optin";
    v = "false";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.create.grant.all.optout";
    v = "false";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.create.grant.all.read";
    v = "true";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.create.grant.all.update";
    v = "false";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.create.grant.all.view";
    v = "true";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "stems.create.grant.all.create";
    v = "false";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "stems.create.grant.all.stem";
    v = "false";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "memberships.log.group.effective.add";
    v = "true";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "memberships.log.group.effective.del";
    v = "true";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "memberships.log.stem.effective.add";
    v = "true";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "memberships.log.stem.effective.del";
    v = "true";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.wheel.use";
    v = "false";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "groups.wheel.group";
    v = "your:wheel:group";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
  } // public void testConfig()

  public void testConfigBadProps() {
    LOG.info("testConfigBadProps");
    GrouperConfig cfg = GrouperConfig.getInstance();
    Assert.assertNotNull("cfg !null", cfg);
    Assert.assertTrue("cfg instanceof GrouperConfig", cfg instanceof GrouperConfig);

    // Now test bad configuration properties
    String k = "i.do.not.exist";
    String v = new String();
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = "";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = " ";
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
    k = null;
    Assert.assertTrue(k, cfg.getProperty(k).equals(v));
  } // public void testConfigBadProps()

}

