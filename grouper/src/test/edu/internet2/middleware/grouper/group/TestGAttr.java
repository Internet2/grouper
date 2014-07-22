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

package edu.internet2.middleware.grouper.group;

import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGAttr.java,v 1.4 2009-08-12 04:52:21 mchyzer Exp $
 * @since   1.1.0
 */
public class TestGAttr extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGAttr("testFailGetAttributeNotSet"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestGAttr.class);

  public TestGAttr(String name) {
    super(name);
  }
  
  public void testFailGetAttributeNotSet() {
    R       r     = R.populateRegistry(1, 1, 1);
    Group   gA    = r.getGroup("a", "a");
    Subject subjA = r.getSubject("a");
    
    GroupType groupType1 = GroupType.createType(r.rs, "theGroupType1", false); 
    groupType1.addAttribute(r.rs, "theAttribute1", false);
    gA.addType(groupType1, false);
    
    GroupType groupType2 = GroupType.createType(r.rs, "theGroupType2", false); 
    groupType2.addAttribute(r.rs, "theAttribute2", false);
    
    r.rs.stop();  
    GrouperSession.start(subjA);
    try {
      gA.getAttributeValue("theAttribute0", true, false);
      fail("Should throw exception");
    } catch (AttributeNotFoundException e) {
      // good
    }
    
    try {
      gA.getAttributeValue("theAttribute2", true, false);
      fail("Should throw exception");
    } catch (AttributeNotFoundException e) {
      // good
    }
    
    try {
      gA.getAttributeValue("theAttribute1", true, true);
      fail("Should throw exception");
    } catch (AttributeNotFoundException e) {
      // good
    }
    
    assertEquals("", gA.getAttributeValue("theAttribute1", true, false));
  }

  public void testFailGetAttributeNullAttribute() {
    LOG.info("testFailGetAttributeNullAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      try {
        gA.getAttributeValue(null, false, true);
        T.fail("did not throw exception when retrieving null attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("failed to find null attribute");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailGetAttributeNullAttribute()

  public void testFailDeleteAttributeNullAttribute() {
    LOG.info("testFailDeleteAttributeNullAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      try {
        gA.deleteAttribute(null);
        T.fail("deleted null attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("did not delete null attribute");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailDeleteAttributeNullAttribute()

  public void testFailDeleteAttributeBlankAttribute() {
    LOG.info("testFailDeleteAttributeBlankAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      try {
        gA.deleteAttribute("");
        T.fail("deleted blank attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("did not delete blank attribute");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailDeleteAttributeBlankAttribute()

  public void testDeleteAttribute() {
    LOG.info("testDeleteAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      GroupType groupType = GroupType.createType(r.rs, "theGroupType", false); 
      groupType.addAttribute(r.rs, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";
  
  
      gA.setAttribute(theAttribute, theAttribute);

      gA.deleteAttribute(theAttribute);
      T.ok("deleted attribute");
      T.string(
        "fetch deleted attribute",
        GrouperConfig.EMPTY_STRING,
        gA.getAttributeValue(theAttribute, false, false)
      );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteAttribute()

  public void testDeleteAttributeNotRootButAllHasAdmin() {
    LOG.info("testDeleteAttributeNotRootButAllHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
  
      GroupType groupType = GroupType.createType(grouperSession, "theGroupType", false); 
      groupType.addAttribute(grouperSession, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_READ, false);
      groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_UPDATE, false);
      groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_READ, false);
      groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_UPDATE, false);
      
      String theAttribute = "theAttribute1";
      gA.setAttribute(theAttribute, "whatever");
    
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
      grouperSession.stop();
      r.rs.stop();  
      GrouperSession.start(subjA);
      gA.setAttribute(theAttribute, theAttribute);

      gA.deleteAttribute(theAttribute);
      T.string(
        "fetch deleted attribute",
        GrouperConfig.EMPTY_STRING,
        gA.getAttributeValue(theAttribute, false, false)
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDeleteAttributeNotRootButAllHasAdmin()

  public void testDeleteAttributeNotRootButHasAdmin() {
    LOG.info("testDeleteAttributeNotRootButHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
  
      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
  
      GroupType groupType = GroupType.createType(grouperSession, "theGroupType", false); 
      groupType.addAttribute(grouperSession, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_READ, false);
      groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_UPDATE, false);
      groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_READ, false);
      groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_UPDATE, false);
      
      String theAttribute = "theAttribute1";
      gA.setAttribute(theAttribute, "whatever");
    
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
  
      grouperSession.stop();
      
  
      r.rs.stop();  
      GrouperSession.start(subjA);
      gA.setAttribute(theAttribute, theAttribute);

      gA.deleteAttribute(theAttribute);
      T.string(
        "fetch deleted attribute",
        GrouperConfig.EMPTY_STRING,
        gA.getAttributeValue(theAttribute, false, false)
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDeleteAttributeNotRootButHasAdmin()

  public void testFailDeleteAttributeBlankAttributeNotRootButAllHasAdmin() {
    LOG.info("testFailDeleteAttributeBlankAttributeNotRootButAllHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
      r.rs.stop();  
      GrouperSession.start(subjA);
      try {
        gA.deleteAttribute("");
        fail("deleted blank attribute");
      }
      catch (AttributeNotFoundException eANF) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailDeleteAttributeBlankAttributeNotRootButAllHasAdmin()

  public void testFailDeleteAttributeBlankAttributeNotRootButHasAdmin() {
    LOG.info("testFailDeleteAttributeBlankAttributeNotRootButHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      r.rs.stop();  
      GrouperSession.start(subjA);
      try {
        gA.deleteAttribute("");
        fail("deleted blank attribute");
      }
      catch (AttributeNotFoundException eANF) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailDeleteAttributeBlankAttributeNotRootButHasAdmin()

  public void testFailDeleteAttributeNullAttributeNotRootButAllHasAdmin() {
    LOG.info("testFailDeleteAttributeNullAttributeNotRootButAllHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
      r.rs.stop();  
      GrouperSession.start(subjA);
      try {
        gA.deleteAttribute(null);
        fail("deleted null attribute");
      }
      catch (AttributeNotFoundException eANF) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailDeleteAttributeNullAttributeNotRootButAllHasAdmin()

  public void testFailDeleteAttributeNullAttributeNotRootButHasAdmin() {
    LOG.info("testFailDeleteAttributeNullAttributeNotRootButHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      r.rs.stop();  
      GrouperSession.start(subjA);
      try {
        gA.deleteAttribute(null);
        fail("deleted null attribute");
      }
      catch (AttributeNotFoundException eANF) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailDeleteAttributeNullAttributeNotRootButHasAdmin()

  public void testFailDeleteAttributeUnset() {
    LOG.info("testFailDeleteAttributeUnset");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      try {
        gA.deleteAttribute("description");
        T.fail("deleted unset attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("did not delete unset attribute");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailDeleteAttributeUnset()

  public void testFailDeleteAttributeUnsetNotRootButAllHasAdmin() {
    LOG.info("testFailDeleteAttributeUnsetNotRootButAllHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv( SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
      r.rs.stop();  
      GrouperSession.start(subjA);
      try {
        gA.deleteAttribute("description");
        fail("deleted unset attribute");
      }
      catch (AttributeNotFoundException eANF) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailDeleteAttributeUnsetNotRootButAllHasAdmin()

  public void testFailDeleteAttributeUnsetNotRootButHasAdmin() {
    LOG.info("testFailDeleteAttributeUnsetNotRootButHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      r.rs.stop();  
      GrouperSession.start(subjA);
      try {
        gA.deleteAttribute("description");
        fail("deleted unset attribute");
      }
      catch (AttributeNotFoundException eANF) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailDeleteAttributeUnsetNotRootButHasAdmin()

  public void testFailGetAttributeBlankAttribute() {
    LOG.info("testFailGetAttributeBlankAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      try {
        gA.getAttributeValue("", false, true);
        T.fail("did not throw exception when retrieving blank attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("failed to find blank attribute");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailGetAttributeBlankAttribute()

  public void testFailSetAttributeBlankAttribute() {
    LOG.info("testFailSetAttributeBlankAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      try {
        gA.setAttribute("", "foo");

        T.fail("set blank attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("did not set blank attribute");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailSetAttributeBlankAttribute()

  public void testFailSetAttributeBlankAttributeValue() {
    LOG.info("testFailSetAttributeBlankAttributeValue");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      GroupType groupType = GroupType.createType(r.rs, "theGroupType", false); 
      groupType.addAttribute(r.rs, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";
  
      try {
        gA.setAttribute(theAttribute, "");

        T.fail("set blank attribute value");
      }
      catch (GroupModifyException eGM) {
        T.ok("did not set blank attribute value");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailSetAttributeBlankAttributeValue()

  public void testFailSetAttributeNullAttribute() {
    LOG.info("testFailSetAttributeNullAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      try {
        gA.setAttribute(null, "foo");

        T.fail("set null attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("did not set null attribute");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailSetAttributeNullAttribute()

  public void testFailSetAttributeNullValue() {
    LOG.info("testFailSetAttributeNullValue");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      GroupType groupType = GroupType.createType(r.rs, "theGroupType", false); 
      groupType.addAttribute(r.rs, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";
  
      try {
        gA.setAttribute(theAttribute, null);

        T.fail("set null attribute value");
      }
      catch (GroupModifyException eGM) {
        T.ok("did not set null attribute value");
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailSetAttributeNullValue()

  public void testGetAttribute() {
    LOG.info("testGetAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      GroupType groupType = GroupType.createType(r.rs, "theGroupType", false); 
      groupType.addAttribute(r.rs, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";
      gA.setAttribute(theAttribute, "whatever");
      
      T.string(
        "theAttribute",  
        "whatever",
        gA.getAttributeValue(theAttribute, false, true)
      );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetAttribute()

  public void testGetAttributeNotYetSetAttribute() {
    LOG.info("testGetAttributeNotYetSetAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      GroupType groupType = GroupType.createType(r.rs, "theGroupType", false); 
      groupType.addAttribute(r.rs, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";
  
      T.string(
        "unset attribute",  
        GrouperConfig.EMPTY_STRING, 
        gA.getAttributeValue(theAttribute, false, false)
      );
  
      T.string(
          "unset attribute",  
          GrouperConfig.EMPTY_STRING, 
          gA.getDescription()
        );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetAttributeNotYetSetAttribute()

  public void testSetAttribute() {
    LOG.info("testSetAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");
  
      GroupType groupType = GroupType.createType(r.rs, "theGroupType", false); 
      groupType.addAttribute(r.rs, "theAttribute1", 
            false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";
  
      String  v = "foo";
      gA.setAttribute(theAttribute, v);

      T.ok("set attr value");
      T.string(
        "updated attr value",
        v,
        gA.getAttributeValue(theAttribute, false, true)
      );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testSetAttribute()

} // public class TestGAttr0

