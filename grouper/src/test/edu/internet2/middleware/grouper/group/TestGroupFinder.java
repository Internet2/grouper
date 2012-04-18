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

package edu.internet2.middleware.grouper.group;
import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.filter.TestGroupTypeFilter;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupFinder.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.0
 */
public class TestGroupFinder extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestGroupFinder.class);

  public TestGroupFinder(String name) {
    super(name);
  }

  public void testFailToFindGroupByAttributeNullSession() {
    LOG.info("testFailToFindGroupByAttributeNullSession");
    try {
      R.populateRegistry(0, 0, 0);
      GroupFinder.findByAttribute(null, "description", "i2:a:a", true);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttributeNullSession()

  public void testFailToFindGroupByAttributeNullAttribute() {
    LOG.info("testFailToFindGroupByAttributeNullAttribute");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findByAttribute(r.rs, null, "i2:a:a", true);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttributeNullAttribute()

  public void testFailToFindGroupByAttributeNullAttributeValue() {
    LOG.info("testFailToFindGroupByAttributeNullAttributeValue");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findByAttribute(r.rs, "description", null, true);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttributeNullAttributeValue()

  public void testFailToFindGroupByAttribute() {
    LOG.info("testFailToFindGroupByAttribute");
    try {
      R r = R.populateRegistry(0, 0, 0);
      assertDoNotFindGroupByAttribute(r.rs, "description", "i2:a:a");
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttribute()

  public void testFindGroupByAttribute() {
    LOG.info("testFindGroupByAttribute");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   gA  = r.getGroup("a", "a");
      String  val = "a unique value";
      gA.setDescription(val);
      gA.store();
      gA = assertFindGroupByAttribute(r.rs, "description", val);
      assertTrue( gA.getDescription().equals(val) );
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindGroupByAttribute()

  public void testFailToFindGroupByCustomTypeNotUnique() {
    LOG.info("testFailToFindGroupByCustomTypeNotUnique");
    try {
      R         r     = R.populateRegistry(1, 2, 0);
      Group     gA    = r.getGroup("a", "a");
      Group     gB    = r.getGroup("a", "b");
      GroupType type  = GroupType.createType(r.rs, "custom group type");
      gA.addType(type);
      gB.addType(type);
      assertDoNotFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByCustomTypeNotUnique()

  public void testFailToFindGroupByType() {
    LOG.info("testFailToFindGroupByType");
    try {
      R r = R.populateRegistry(0, 0, 0);
      assertDoNotFindGroupByType( r.rs, GroupTypeFinder.find("base", true) );
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByType()

  public void testFailToFindGroupByTypeInvalidType() {
    LOG.info("testFailToFindGroupByTypeInvalidType");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findAllByType( r.rs, GroupTypeFinder.find("this is an invalid group type", true) );
      fail("failed to throw IllegalArgumentException");
    }
    catch (SchemaException eS) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeInvalidType()

  public void testFailToFindGroupByTypeNotUnique() {
    LOG.info("testFailToFindGroupByTypeNotUnique");
    try {
      R         r     = R.populateRegistry(1, 2, 0);
      GroupType type  = GroupTypeFinder.find("base", true);
      assertDoNotFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeNotUnique()

  public void testFailToFindGroupByTypeNullSession() {
    LOG.info("testFailToFindGroupByTypeNullSession");
    try {
      R.populateRegistry(0, 0, 0);
      GroupFinder.findAllByType( null, GroupTypeFinder.find("base", true) );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeNullSession()

  public void testFailToFindGroupByTypeNullType() {
    LOG.info("testFailToFindGroupByTypeNullType");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findAllByType(r.rs, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeNullType()

  public void testFindGroupByCustomType() {
    LOG.info("testFindGroupByCustomType");
    try {
      R         r     = R.populateRegistry(1, 1, 0);
      Group     gA    = r.getGroup("a", "a");
      GroupType type  = GroupType.createType(r.rs, "custom group type");
      gA.addType(type);
      assertFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindGroupByCustomType()

  public void testFindGroupByType() {
    LOG.info("testFindGroupByType");
    try {
      R         r     = R.populateRegistry(1, 1, 0);
      GroupType type  = GroupTypeFinder.find("base", true);
      assertFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindGroupByType()

  // Tests
  
  public void testFindByName() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      Group found = GroupFinder.findByName(s, i2.getName(), true);
      Assert.assertTrue("found a group", true);
      Assert.assertNotNull("found group !null", found);
      Assert.assertTrue("found instanceof Group", found instanceof Group);
      Assert.assertTrue("i2 equals found", i2.equals(found));
    }
    catch (GroupNotFoundException e) {
      Assert.fail("failed to find group");
    }
  } // public void testFindByName()

  // Tests
  
  public void testFindByUuid() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      Group found = GroupFinder.findByUuid(s, i2.getUuid(), true);
      Assert.assertTrue("found a group", true);
      Assert.assertNotNull("found group !null", found);
      Assert.assertTrue("found instanceof Group", found instanceof Group);
      Assert.assertTrue("i2 equals", i2.equals(found));
    }
    catch (GroupNotFoundException e) {
      Assert.fail("failed to find group");
    }
  } // public void testFindByUuid()

  /**
   * @see GrouperTest#setupConfigs
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    ApiConfig.testConfig.put("groups.wheel.use", "false");
  
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(TestGroupFinder.class);
  }

} // public class TestGroupFinder_FindByAttribute

