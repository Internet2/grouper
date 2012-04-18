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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.synchronize;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * This set of tests validates the
 * {@link edu.internet2.middleware.ldappc.synchronize.DnAttributeModifier}.
 */
public class DnAttributeModifierTest extends TestCase {

  /**
   * Class constructor
   * 
   * @param name
   *          Name of the test case.
   */
  public DnAttributeModifierTest(String name) {
    super(name);
  }

  /**
   * The main method for running the test.
   */
  public static void main(String args[]) {
    TestRunner.run(AttributeModifierTest.class);
  }

  /**
   * Test all of the constructors
   */
  public void testConstructors() {

    String attrName = "name";
    String noValue = "";

    DnAttributeModifier am = new DnAttributeModifier(attrName);
    assertEquals("Attribute names do not match", attrName, am.getAttributeName());
    assertEquals("No value does not match", null, am.getNoValue());

    am = new DnAttributeModifier(attrName, noValue);
    assertEquals("Attribute names do not match", attrName, am.getAttributeName());
    assertEquals("No value does not match", noValue, am.getNoValue());
  }

  /**
   * Test initialize and store
   */
  public void testInitAndStore() {

    BasicAttribute attr = new BasicAttribute("someAttribute");
    String attrName = "name";
    String noValue = "";
    String[] dnSet = { "cn=abc", "ou=value1", "ou=value2", "cn=1234+ou=xyz,dc=com" };

    DnAttributeModifier am = new DnAttributeModifier(attrName, noValue);

    try {
      //
      // Test storing values in both upper and lower case to ensure
      // duplicates not added
      //
      am.init();
      attr.clear();
      for (int i = 0; i < dnSet.length; i++) {
        am.store(dnSet[i]);
      }

      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 1, mods.length);
      assertEquals("Wrong modification operation", DirContext.ADD_ATTRIBUTE, mods[0]
          .getModificationOp());
      Attribute attribute = mods[0].getAttribute();
      assertEquals("To many add values", dnSet.length, attribute.size());
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all values retained
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < dnSet.length; i++) {
        attr.add(dnSet[i]);
      }
      am.init(attr);
      for (int i = 0; i < dnSet.length; i++) {
        am.store(dnSet[i].toUpperCase());
      }
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all values retained
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < dnSet.length; i++) {
        attr.add(dnSet[i]);
      }
      am.init(attr);
      am.retainAll();
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 0, mods.length);

      for (int i = 0; i < dnSet.length; i++) {
        am.store(dnSet[i].toUpperCase());
      }
      mods = am.getModifications();
      assertEquals("To many modification items", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all part of orig values deleted and all new values
      // added
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < dnSet.length; i++) {
        attr.add(dnSet[i]);
      }
      am.init(attr);

      String[] newValues = { "cn=rrr", "ou=bbbb", "o=c1", "dc=22" };
      for (int i = 0; i < newValues.length; i++) {
        am.store(newValues[i]);
      }
      int increment = 2;
      for (int i = 0; i < dnSet.length; i += increment) {
        am.store(dnSet[i].toUpperCase());
      }
      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 2, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test multi valued RDNs compared correctly
      //
      String rdn1 = "ou=xxx+cn=yyy,cn=aaa";
      String rdn2 = "cn=yyy+ou=xxx,cn=aaa";

      am.setNoValue(null);
      attr.clear();
      attr.add(rdn1);
      am.init(attr);
      am.store(rdn2);
      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }
  }
  
  public void testComparisonString() {
    String dn = "CN=foo, DC=tEst,  dc=edu";
    String normalized = "cn=foo,dc=test,dc=edu";
    DnAttributeModifier am = new DnAttributeModifier(dn);
    assertEquals(normalized, am.makeComparisonString(dn));
  }

  public void testComparisonStringCustomRdn() {
    String dn = "custom=foo, DC=tEst,  dc=edu";
    String normalized = "custom=foo,dc=test,dc=edu";
    DnAttributeModifier am = new DnAttributeModifier(dn);
    assertEquals(normalized, am.makeComparisonString(dn));
  }
}
