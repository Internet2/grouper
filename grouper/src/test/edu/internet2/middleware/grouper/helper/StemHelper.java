/**
 * Copyright 2014 Internet2
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

package edu.internet2.middleware.grouper.helper;
import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * {@link Stem} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemHelper.java,v 1.3 2009-03-21 19:48:50 mchyzer Exp $
 */
public class StemHelper {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(StemHelper.class);


  // public Class Methods

  // Add and test a child group
  // @return  Created {@link Group}
  public static Group addChildGroup(Stem ns, String extn, String displayExtn) {
    try {
      LOG.debug("addChildGroup.0 " + extn);
      
      Group child = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ns.getName() + ":" + extn, false);

      if (child == null) {
        child = ns.addChildGroup(extn, displayExtn);
      }
      
      LOG.debug("addChildGroup.1 " + extn);
      Assert.assertNotNull("child !null", child);
      LOG.debug("addChildGroup.2 " + extn);
      Assert.assertTrue("added child group", true);
      LOG.debug("addChildGroup.3 " + extn);
      Assert.assertTrue(
        "child group instanceof Group", 
        child instanceof Group
      );
      LOG.debug("addChildGroup.4 " + extn);
      Assert.assertNotNull("child uuid !null", child.getUuid());
      LOG.debug("addChildGroup.5 " + extn);
      Assert.assertTrue("child has uuid", !child.getUuid().equals(""));
      LOG.debug("addChildGroup.6 " + extn);
      Assert.assertTrue(
        "parent stem", child.getParentStem().equals(ns)
      );
      LOG.debug("addChildGroup.7 " + extn);
      Assert.assertTrue(
        "group extension", child.getExtension().equals(extn)
      );
      LOG.debug("addChildGroup.8 " + extn);
      Assert.assertTrue(
        "group name", child.getName().equals(ns.getName() + ":" + extn)
      );
      LOG.debug("addChildGroup.9 " + extn);
      Assert.assertTrue(
        "group displayExtension", 
        child.getDisplayExtension().equals(displayExtn)
      );
      LOG.debug("addChildGroup.10 " + extn);
      Assert.assertTrue(
        "group displayName", 
        child.getDisplayName().equals(ns.getDisplayName() + ":" + displayExtn)
      );
      LOG.debug("addChildGroup.11 " + extn);
      return child;
    } catch (HookVeto hookVeto) {
      throw hookVeto;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  } // public static Group addChildGroup(ns, extn, displayExtn)

  public static void addChildGroupFail(Stem ns, String extn, String displayExtn) {
    try {
      Group child = ns.addChildGroup(extn, displayExtn);
      Assert.fail("created child group: " + child.getName());
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("failed to add group (priv)", true);
    }
    catch (GroupAddException eAG) {
      Assert.assertTrue("failed to add group (groupadd)", true);
    }
  } // public static void addChildGroupFail(ns, extn, displayExtn)

  // Add and test a child stem
  // @return  Created {@link Stem}
  public static Stem addChildStem(Stem ns, String extn, String displayExtn) {
    String parentPrefix = ns.isRootStem() ? "" : (ns.getName() + ":");
    
    Stem child = StemFinder.findByName(GrouperSession.staticGrouperSession(), 
        parentPrefix + extn, false);
    
    if (child == null) {
      child = ns.addChildStem(extn, displayExtn);
    }
    
    Assert.assertNotNull("child !null", child);
    Assert.assertTrue("added child stem", true);
    Assert.assertTrue(
      "child stem instanceof Stem", 
      child instanceof Stem
    );
    Assert.assertNotNull("child uuid !null", child.getUuid());
    Assert.assertTrue("child has uuid", !child.getUuid().equals(""));
    Assert.assertTrue(
      "parent stem", child.getParentStem().equals(ns)
    );
    return child;
  } // public static Stem addChildStem(ns, extn, displayExtn)

  public static void addChildStemFail(Stem ns, String extn, String displayExtn) {
    try {
      Stem child = ns.addChildStem(extn, displayExtn);
      Assert.fail("created child stem: " + child.getName());
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("failed to add stem", true);
    }
    catch (StemAddException eSA) {
      Assert.assertTrue("failed to add stem", true);
    }
  } // public static void addChildStemFail(ns, extn, displayExtn)

  public static Stem findByName(GrouperSession s, String name) {
    Stem ns = null;
    try {
      ns = StemFinder.findByName(s, name, true);
      Assert.assertNotNull("!null", ns);
      Assert.assertTrue("instance of Stem", ns instanceof Stem);
      Assert.assertTrue("name", ns.getName().equals(name));
    }
    catch (StemNotFoundException eSNF) {
      Assert.fail("did not find stem " + name);
    }
    return ns;
  } // public static Stem findByName(s, name)

  // Get the root stem
  // @return  The root {@link Stem}
  public static Stem findRootStem(GrouperSession s) {
    Stem root = StemFinder.findRootStem(s);
    Assert.assertNotNull("root !null", root);
    Assert.assertTrue("found root stem", true);
    Assert.assertTrue(
      "root stem instanceof Stem", 
      root instanceof Stem
    );
    Assert.assertNotNull("root uuid !null", root.getUuid());
    Assert.assertTrue("root has uuid",      !root.getUuid().equals(""));
    String val = "";
    Assert.assertTrue(
      "root extn (" + root.getExtension() + ")", 
      root.getExtension().equals(val)
    );
    Assert.assertEquals(
      "root displayExtn (" + root.getDisplayExtension() + ")", 
      val, root.getDisplayExtension());
    Assert.assertTrue(
      "root name (" + root.getName() + ")", 
      root.getName().equals(val)
    );
    Assert.assertTrue(
      "root displayName (" + root.getDisplayName() + ")", 
      root.getDisplayName().equals(val)
    );
    return root;
  } // public static Stem findRootStem(s)

  public static void setAttr(Stem ns, String attr, String val) {
    try {
      if      (attr.equals("description")) {
        String orig = ns.getDescription();
        ns.setDescription(val);
        ns.store();
        Assert.assertTrue("set description", true);
        Assert.assertTrue(
          "description", ns.getDescription().equals(val)
        );
        ns.setDescription(orig);
        ns.store();
        Assert.assertTrue(
          "description reset", ns.getDescription().equals(orig)
        );
      } 
      else if (attr.equals("displayExtension")) {
        String orig = ns.getDisplayExtension();
        ns.setDisplayExtension(val);
        ns.store();
        Assert.assertTrue("set displayExtension", true);
        Assert.assertTrue(
          "displayExtension", ns.getDisplayExtension().equals(val)
        );
        ns.setDisplayExtension(orig);
        ns.store();
        Assert.assertTrue(
          "displayExtension reset", ns.getDisplayExtension().equals(orig)
        );
      }
      else {
        Assert.fail("invalid stem attr: " + attr);
      }
    }
    catch (Exception e) {
      Assert.fail("failed to modify " + attr + ": " + e.getMessage());
    }
  } // public static void setAttr(ns, attr, val)

  public static void setAttrFail(Stem ns, String attr, String val) {
    try {
      if      (attr.equals("description")) {
        ns.setDescription(val);
        ns.store();
        Assert.fail("set description");
      } 
      else if (attr.equals("displayExtension")) {
        ns.setDisplayExtension(val);
        ns.store();
        Assert.fail("set displayExtension");
      }
      else {
        Assert.fail("invalid stem attr: " + attr);
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("failed to set " + attr, true);
    }
    catch (Exception e) {
      Assert.fail("failed to modify " + attr + ": " + e.getMessage());
    }
  } // public static void setAttr(ns, attr, val)

}

