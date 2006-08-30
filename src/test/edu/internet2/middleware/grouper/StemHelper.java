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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * {@link Stem} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemHelper.java,v 1.3 2006-08-30 18:35:38 blair Exp $
 */
public class StemHelper {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(StemHelper.class);


  // Protected Class Methods

  // Add and test a child group
  // @return  Created {@link Group}
  protected static Group addChildGroup(Stem ns, String extn, String displayExtn) {
    try {
      LOG.debug("addChildGroup.0 " + extn);
      Group child = ns.addChildGroup(extn, displayExtn);
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
    }
    catch (Exception e) {
      LOG.debug("addChildGroup.12 " + extn);
      Assert.fail("failed to add group '" + extn + "': " + e.getMessage());
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static Group addChildGroup(ns, extn, displayExtn)

  protected static void addChildGroupFail(Stem ns, String extn, String displayExtn) {
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
  } // protected static void addChildGroupFail(ns, extn, displayExtn)

  // Add and test a child stem
  // @return  Created {@link Stem}
  protected static Stem addChildStem(Stem ns, String extn, String displayExtn) {
    try {
      Stem child = ns.addChildStem(extn, displayExtn);
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
    }
    catch (Exception e) {
      Assert.fail("failed to add stem '" + extn + "': " + e.getMessage());
    }
    throw new RuntimeException(Helper.ERROR);
  } // protected static Stem addChildStem(ns, extn, displayExtn)

  protected static void addChildStemFail(Stem ns, String extn, String displayExtn) {
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
  } // protected static void addChildStemFail(ns, extn, displayExtn)

  protected static Stem findByName(GrouperSession s, String name) {
    Stem ns = null;
    try {
      ns = StemFinder.findByName(s, name);
      Assert.assertNotNull("!null", ns);
      Assert.assertTrue("instance of Stem", ns instanceof Stem);
      Assert.assertTrue("name", ns.getName().equals(name));
    }
    catch (StemNotFoundException eSNF) {
      Assert.fail("did not find stem " + name);
    }
    return ns;
  } // protected static Stem findByName(s, name)

  // Get the root stem
  // @return  The root {@link Stem}
  protected static Stem findRootStem(GrouperSession s) {
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
    Assert.assertTrue(
      "root displayExtn (" + root.getDisplayExtension() + ")", 
      root.getDisplayExtension().equals(val)
    );
    Assert.assertTrue(
      "root name (" + root.getName() + ")", 
      root.getName().equals(val)
    );
    Assert.assertTrue(
      "root displayName (" + root.getDisplayName() + ")", 
      root.getDisplayName().equals(val)
    );
    return root;
  } // protected static Stem findRootStem(s)

  protected static void setAttr(Stem ns, String attr, String val) {
    try {
      if      (attr.equals("description")) {
        String orig = ns.getDescription();
        ns.setDescription(val);
        Assert.assertTrue("set description", true);
        Assert.assertTrue(
          "description", ns.getDescription().equals(val)
        );
        ns.setDescription(orig);
        Assert.assertTrue(
          "description reset", ns.getDescription().equals(orig)
        );
      } 
      else if (attr.equals("displayExtension")) {
        String orig = ns.getDisplayExtension();
        ns.setDisplayExtension(val);
        Assert.assertTrue("set displayExtension", true);
        Assert.assertTrue(
          "displayExtension", ns.getDisplayExtension().equals(val)
        );
        ns.setDisplayExtension(orig);
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
  } // protected static void setAttr(ns, attr, val)

  protected static void setAttrFail(Stem ns, String attr, String val) {
    try {
      if      (attr.equals("description")) {
        ns.setDescription(val);
        Assert.fail("set description");
      } 
      else if (attr.equals("displayExtension")) {
        ns.setDisplayExtension(val);
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
  } // protected static void setAttr(ns, attr, val)

}

