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
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.ModificationItem;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * This set of tests validates the
 * {@link edu.internet2.middleware.ldappc.synchronize.AttributeModifier}.
 */
public class AttributeModifierTest extends TestCase {

  /**
   * Class constructor
   * 
   * @param name
   *          Name of the test case.
   */
  public AttributeModifierTest(String name) {
    super(name);
  }

  /**
   * Tear down the fixture.
   */
  protected void tearDown() {
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
    String noValue = "xxxxxxxx";
    boolean caseSensitive = true;
    boolean caseInsensitive = false;

    AttributeModifier am = new AttributeModifier(attrName);
    assertEquals("Attribute names do not match", attrName, am.getAttributeName());
    assertEquals("No value does not match", null, am.getNoValue());
    assertEquals("Case sensitivity does not match", caseInsensitive, am.isCaseSensitive());

    am = new AttributeModifier(attrName, noValue);
    assertEquals("Attribute names do not match", attrName, am.getAttributeName());
    assertEquals("No value does not match", noValue, am.getNoValue());
    assertEquals("Case sensitivity does not match", caseInsensitive, am.isCaseSensitive());

    am = new AttributeModifier(attrName, caseSensitive);
    assertEquals("Attribute names do not match", attrName, am.getAttributeName());
    assertEquals("No value does not match", null, am.getNoValue());
    assertEquals("Case sensitivity does not match", caseSensitive, am.isCaseSensitive());

    am = new AttributeModifier(attrName, noValue, caseSensitive);
    assertEquals("Attribute names do not match", attrName, am.getAttributeName());
    assertEquals("No value does not match", noValue, am.getNoValue());
    assertEquals("Case sensitivity does not match", caseSensitive, am.isCaseSensitive());
  }

  /**
   * Test invalid value type
   */
  public void testInvalidValueType() {

    BasicAttribute attr = new BasicAttribute("someAttribute");
    attr.add(new byte[] {});

    String attrName = "name";
    AttributeModifier am = new AttributeModifier(attrName);
    try {
      am.init(attr);
      fail("Invalid attribute value type allowed");
    } catch (InvalidAttributeValueException iave) {
      // Do nothing this is expected.
    } catch (NamingException ne) {
      fail("Unexpected NamingException encountered");
    }
  }

  /**
   * Test addition of "no value"
   */
  public void testNoValue() {

    BasicAttribute attr = new BasicAttribute("someAttribute");
    String attrName = "name";
    String noValue = "xxxxxxxx";
    boolean caseInsensitive = false;
    String[] lowerCaseSet = { "abc", "value1", "value2", "1234", "@#$abc" };

    AttributeModifier am = new AttributeModifier(attrName, noValue, caseInsensitive);

    try {
      //
      // Test that all values deleted and "no value" not added
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 1, mods.length);
      assertEquals("Wrong modification operation", mods[0].getModificationOp(),
          DirContext.REMOVE_ATTRIBUTE);
      Attribute attribute = mods[0].getAttribute();
      assertEquals("To many add values", lowerCaseSet.length, attribute.size());

      am.clear();
      mods = am.getModifications();
      assertEquals("Shouldn't have any mods", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all values retained and "no value" not added
      //
      am.setNoValue(noValue);
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);
      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i].toUpperCase());
      }
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that "no value" added correctly and state of object remains
      // unchanged
      //
      am.setNoValue(noValue);
      attr.clear();
      am.init();
      am.store(noValue);
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 1, mods.length);
      assertEquals("Wrong modification operation", DirContext.ADD_ATTRIBUTE, mods[0]
          .getModificationOp());
      Attribute attribute = mods[0].getAttribute();
      assertEquals("To many add values", 1, attribute.size());
      attribute.remove(noValue);
      assertEquals("'No value' still remains", 0, attribute.size());

      // 20090423 tz omit dynamically changing noValue state
      // am.setNoValue(null);
      // Attribute additions = am.getAdditions();
      // assertEquals("Wrong number of additions. It should be 0", 0,
      // additions.size());

    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all orig values deleted and "no value" added
      //
      am.setNoValue(noValue);
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);
      am.store(noValue);
      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 2, mods.length);
      assertEquals("Wrong modification operation", DirContext.ADD_ATTRIBUTE, mods[0]
          .getModificationOp());
      assertEquals("Wrong modification operation", DirContext.REMOVE_ATTRIBUTE, mods[1]
          .getModificationOp());
      Attribute attribute = mods[0].getAttribute();
      assertEquals("To many add values", 1, mods[0].getAttribute().size());
      assertEquals("To many add values", lowerCaseSet.length, mods[1].getAttribute()
          .size());
      attribute.remove(noValue);
      assertEquals("'No value' still remains", 0, attribute.size());
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that no modifications are returned
      //
      am.setNoValue(null);
      am.init();
      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that no modifications are returned when init'd with just no
      // value
      //
      attr.clear();
      am.setNoValue(noValue);
      attr.add(noValue);
      am.init(attr);
      am.store(noValue);
      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }
  }

  /**
   * Test initialize and store with case insensitivity
   */
  public void testCaseInsensitiveInitAndStore() {

    BasicAttribute attr = new BasicAttribute("someAttribute");
    String attrName = "name";
    String noValue = "xxxxxxxx";
    boolean caseInsensitive = false;
    String[] lowerCaseSet = { "abc", "value1", "value2", "1234", "@#$abc" };

    AttributeModifier am = new AttributeModifier(attrName, noValue, caseInsensitive);

    try {
      //
      // Test storing values in both upper and lower case to ensure
      // duplicates not added
      //
      am.init();
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i]);
        am.store(lowerCaseSet[i].toUpperCase());
        am.store(lowerCaseSet[i]);
      }

      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 1, mods.length);
      assertEquals("Wrong modification operation", DirContext.ADD_ATTRIBUTE, mods[0]
          .getModificationOp());
      Attribute attribute = mods[0].getAttribute();
      assertEquals("To many add values", lowerCaseSet.length, attribute.size());
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attribute.remove(lowerCaseSet[i]);
      }
      assertEquals("Additions still contains values", 0, attribute.size());
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all values retained
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);
      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i].toUpperCase());
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
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);
      am.retainAll();
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 0, mods.length);

      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i].toUpperCase());
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
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);

      String[] newValues = { "aaaa", "bbbb", "c1", "22", "@@" };
      for (int i = 0; i < newValues.length; i++) {
        am.store(newValues[i]);
      }
      int increment = 2;
      for (int i = 0; i < lowerCaseSet.length; i += increment) {
        am.store(lowerCaseSet[i].toUpperCase());
      }
      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 2, mods.length);

      for (int j = 0; j < mods.length; j++) {
        if (DirContext.ADD_ATTRIBUTE == mods[j].getModificationOp()) {
          Attribute attribute = mods[j].getAttribute();
          assertEquals("Wrong number of values", newValues.length, attribute.size());
          for (int i = 0; i < newValues.length; i++) {
            attribute.remove(newValues[i]);
          }
          assertEquals("Additions still contains values", 0, attribute.size());
        } else if (DirContext.REMOVE_ATTRIBUTE == mods[j].getModificationOp()) {
          Attribute attribute = mods[j].getAttribute();
          for (int i = 1; i < lowerCaseSet.length; i += increment) {
            attribute.remove(lowerCaseSet[i]);
          }
          assertEquals("Additions still contains values", 0, attribute.size());
        } else {
          fail("Unexpected modification operation");
        }
      }
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }
  }

  /**
   * Test initialize and store with case insensitivity
   */
  public void testCaseSensitiveInitAndStore() {

    BasicAttribute attr = new BasicAttribute("someAttribute");
    String attrName = "name";
    String noValue = "xxxxxxxx";
    boolean caseSensitive = true;
    String[] lowerCaseSet = { "abc", "xyz", "efg" };

    AttributeModifier am = new AttributeModifier(attrName, noValue, caseSensitive);

    try {
      //
      // Test storing values
      //
      am.init();
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i]);
        am.store(lowerCaseSet[i]);
        am.store(lowerCaseSet[i].toUpperCase());
        am.store(lowerCaseSet[i].toUpperCase());
      }

      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 1, mods.length);
      assertEquals("Wrong modification operation", DirContext.ADD_ATTRIBUTE, mods[0]
          .getModificationOp());
      Attribute attribute = mods[0].getAttribute();
      assertEquals("Wrong number of add values", 2 * lowerCaseSet.length, attribute
          .size());
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attribute.remove(lowerCaseSet[i]);
        attribute.remove(lowerCaseSet[i].toUpperCase());
      }
      assertEquals("Additions still contains values", 0, attribute.size());
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all values retained and "no value" not added
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);
      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i]);
      }
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 0, mods.length);
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all values retained and "no value" not added
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);
      am.retainAll();
      ModificationItem[] mods = am.getModifications();
      assertEquals("To many modification items", 0, mods.length);

      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i]);
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
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);

      String[] newValues = { "123", "456", "c1", "22", "@@" };
      for (int i = 0; i < newValues.length; i++) {
        am.store(newValues[i]);
        am.store(newValues[i]);
      }
      int increment = 2;
      for (int i = 0; i < lowerCaseSet.length; i += increment) {
        am.store(lowerCaseSet[i]);
      }
      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 2, mods.length);

      for (int j = 0; j < mods.length; j++) {
        if (DirContext.ADD_ATTRIBUTE == mods[j].getModificationOp()) {
          Attribute attribute = mods[j].getAttribute();
          assertEquals("Wrong number of values", newValues.length, attribute.size());
          for (int i = 0; i < newValues.length; i++) {
            attribute.remove(newValues[i]);
          }
          assertEquals("Additions still contains values", 0, attribute.size());
        } else if (DirContext.REMOVE_ATTRIBUTE == mods[j].getModificationOp()) {
          Attribute attribute = mods[j].getAttribute();
          for (int i = 1; i < lowerCaseSet.length; i += increment) {
            attribute.remove(lowerCaseSet[i]);
          }
          assertEquals("Additions still contains values", 0, attribute.size());
        } else {
          fail("Unexpected modification operation");
        }
      }
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }

    try {
      //
      // Test that all of orig values deleted and all upper case
      // versions are
      // added
      //
      am.setNoValue(null);
      attr.clear();
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attr.add(lowerCaseSet[i]);
      }
      am.init(attr);

      for (int i = 0; i < lowerCaseSet.length; i++) {
        am.store(lowerCaseSet[i].toUpperCase());
      }

      ModificationItem[] mods = am.getModifications();
      assertEquals("Wrong number of modification items", 2, mods.length);
      assertEquals("Wrong modification operation", DirContext.ADD_ATTRIBUTE, mods[0]
          .getModificationOp());
      assertEquals("Wrong modification operation", DirContext.REMOVE_ATTRIBUTE, mods[1]
          .getModificationOp());
      Attribute attribute = mods[0].getAttribute();
      assertEquals("Wrong number of values", lowerCaseSet.length, mods[0].getAttribute()
          .size());
      assertEquals("Wrong number of values", lowerCaseSet.length, mods[1].getAttribute()
          .size());
      for (int i = 0; i < lowerCaseSet.length; i++) {
        attribute.remove(lowerCaseSet[i].toUpperCase());
      }
      assertEquals("Additions still contains values", 0, attribute.size());
    } catch (NamingException ne) {
      fail("Naming exception thrown unexpectedly");
    }
  }
}
