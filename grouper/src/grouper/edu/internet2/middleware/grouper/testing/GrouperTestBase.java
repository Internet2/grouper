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

package edu.internet2.middleware.grouper.testing;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import junit.framework.TestCase;

/**
 * Grouper-specific JUnit assertions.
 * Do not subclass this, only use GrouperTestInApi or GrouperTest (unit test)
 * Note, this shouldnt do anything in constructor, setup, teardown, etc since can be used from a prod env
 * <p/>
 */
public class GrouperTestBase extends TestCase {

  public GrouperTestBase() {
    super();
  }

  public GrouperTestBase(String name) {
    super(name);
  }

  /**
   * Asserts that two object arrays are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEqualsObjectArrays(Set<Object[]> expected, Set<Object[]> actual) {
    assertEqualsObjectArrays("", expected, actual);
  }

  /**
   * Asserts that two subjects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEqualsObjectArrays(String message, Set<Object[]> expected, Set<Object[]> actual) {
    if (GrouperUtil.length(expected) == 0 && GrouperUtil.length(actual) == 0) {
      return;
    }
    Set<MultiKey> expectedMultikeys = new HashSet<MultiKey>();
    for (Object[] expectedItem : GrouperUtil.nonNull(expected)) {
      expectedMultikeys.add(new MultiKey(expectedItem));
    }
    Set<MultiKey> actualMultikeys = new HashSet<MultiKey>();
    for (Object[] actualItem : GrouperUtil.nonNull(actual)) {
      actualMultikeys.add(new MultiKey(actualItem));
    }
    assertEqualsMultiKey(message, expectedMultikeys, actualMultikeys);
  }

  /**
   * Asserts that two subjects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEqualsMultiKey(Set<MultiKey> expected, Set<MultiKey> actual) {
    assertEqualsMultiKey("", expected, actual);
  }

  /**
   * Asserts that two subjects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEqualsMultiKey(String message, Set<MultiKey> expected, Set<MultiKey> actual) {
    if (GrouperUtil.length(expected) == 0 && GrouperUtil.length(actual) == 0) {
      return;
    }
    for (MultiKey expectedKey : expected) {
      if (!actual.contains(expectedKey)) {
        fail(StringUtils.defaultString(message) + ", expected multiKey: " + GrouperUtil.toStringForLog(expectedKey, 2000) + " (size: " + GrouperUtil.length(expected) + "), but not in actual (size: " + GrouperUtil.length(actual) + ")");
      }
    }
    for (MultiKey actualKey : actual) {
      if (!expected.contains(actualKey)) {
        fail(StringUtils.defaultString(message) + ", actual multiKey: " + GrouperUtil.toStringForLog(actualKey, 2000) + " (size: " + GrouperUtil.length(actual) + "), but not in expected (size: " + GrouperUtil.length(expected) + ")");
      }
    }
    if (GrouperUtil.length(expected) != GrouperUtil.length(actual)) {
      // shouldnt really get here
      fail(StringUtils.defaultString(message) + ", expected multiKeys: " + GrouperUtil.toStringForLog(expected, 2000) + " (size: " + GrouperUtil.length(expected) + "), but actual was: " + GrouperUtil.toStringForLog(actual, 2000) + " (size: " + GrouperUtil.length(actual) + ")");
    }
  }

  /**
   * Asserts that two subjects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(Subject expected, Subject actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two subjects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, Subject expected, Subject actual) {
    if (expected == null && actual == null)
      return;
    if (expected != null && SubjectHelper.eq(expected,actual))
      return;
    failNotEquals(message, expected, actual);
  }


  private static final Log    LOG = GrouperUtil.getLog(GrouperTestBase.class);
  // PRIVATE CLASS CONSTANTS //
  protected static final String G   = "group";
  protected static final String NS  = "stem";


  /**
   * 
   * @param message
   * @param outer
   * @param inner
   */
  public void assertContains(String message, String outer, String inner) {
    if (!outer.contains(inner)) {
      fail(StringUtils.defaultString(message) + ", expected string '" + outer + "' to contain '" + inner + "'");
    }
  }
  
  /**
   * @param message
   * @param groups
   * @param group
   */
  public void assertContainsGroup(Collection<Group> groups, Group group, String message) {
    
    if (groups == null || groups.size() == 0) {
      fail("Empty set does not contain group: " + group.getName());
    }

    for (Group current : groups) {
      if (StringUtils.equals(current.getId(), group.getId())) {
        return;
      }
      
    }
    
    fail(StringUtils.defaultString(message) + ", expected groups to contain group '" + group.getName() + "' but contains: " + groupsString(groups));

  }

  /**
   * @param <T> 
   * @param strings
   * @param string
   */
  public void assertContainsString(Collection<String> strings, String string) {
    assertContainsString(strings, string, null);
  }

  /**
   * @param message
   * @param strings
   * @param string
   */
  public void assertContainsString(Collection<String> strings, String string, String message) {
    
    if (strings == null || strings.size() == 0) {
      fail("Empty set does not contain object: " + string);
    }

    for (String current : strings) {
      if (StringUtils.equals(current, string)) {
        return;
      }
      
    }
    
    fail(StringUtils.defaultString(message) + ", expected strings to contain string '" + string + "' but contains: " + GrouperUtil.collectionToString(strings));

  }

  /** 
   * @param groups
   * @param group
   */
  public void assertContainsGroups(Collection<Group> expectedGroups, Collection<Group> actualGroups, String message) {
    
    if (GrouperUtil.length(expectedGroups) != GrouperUtil.length(actualGroups)) {
      fail(StringUtils.defaultString(message) + ", expected " + GrouperUtil.length(expectedGroups) 
        + " groups but had " + GrouperUtil.length(actualGroups) + ",\nexpected: " + groupsString(expectedGroups)
        + "\nactual: " + groupsString(actualGroups));
    }

    //we good
    if (GrouperUtil.length(expectedGroups) == 0) {
      return;
    }
    
    for (Group current : actualGroups) {
      assertContainsGroup(expectedGroups, current, message);
    }
    

  }

  /**
   * @param message
   * @param groups
   * @param group
   */
  public void assertContainsStems(Collection<Stem> expectedStems, Collection<Stem> actualStems, String message) {
    
    if (GrouperUtil.length(expectedStems) != GrouperUtil.length(actualStems)) {
      fail(StringUtils.defaultString(message) + ", expected " + GrouperUtil.length(expectedStems) 
        + " stems but had " + GrouperUtil.length(actualStems) + ",\nexpected: " + stemsString(expectedStems)
        + "\nactual: " + stemsString(actualStems));
    }

    //we good
    if (GrouperUtil.length(expectedStems) == 0) {
      return;
    }
    
    for (Stem current : actualStems) {
      assertContainsStem(expectedStems, current, message);
    }
    

  }

  /**
   * 
   * @param groups
   * @return the groups string
   */
  public static String groupsString(Collection<Group> groups) {
    
    StringBuilder groupsString = new StringBuilder(GrouperUtil.length(groups) + " groups: " );

    int i=0;
    
    if (GrouperUtil.length(groups) == 0) {
      groupsString.append(" <none>");
      return groupsString.toString();
    }
    
    for (Group current : groups) {

      if (i != 0) {
        groupsString.append(", ");
      }
      
      groupsString.append(current.getName());

      if (i>100) {
        groupsString.append(", and " + (GrouperUtil.length(groups) - 100) + " more groups...");
        break;
      }
      
      i++;
    }

    return groupsString.toString();
    
  }
  
  /**
   * 
   * @param stems
   * @return the stems string
   */
  public static String stemsString(Collection<Stem> stems) {
    
    StringBuilder stemsString = new StringBuilder(GrouperUtil.length(stems) + " stems: " );

    int i=0;
    
    if (GrouperUtil.length(stems) == 0) {
      stemsString.append(" <none>");
      return stemsString.toString();
    }
    
    for (Stem current : stems) {

      if (i != 0) {
        stemsString.append(", ");
      }
      
      stemsString.append(current.getName());

      if (i>100) {
        stemsString.append(", and " + (GrouperUtil.length(stems) - 100) + " more stems...");
        break;
      }
      
      i++;
    }

    return stemsString.toString();
    
  }
  
  /**
   * @param message
   * @param stems
   * @param stem
   */
  public void assertContainsStem(Collection<Stem> stems, Stem stem, String message) {
    
    if (stems == null || stems.size() == 0) {
      fail("Empty set does not contain stem: " + stem.getName());
    }

    StringBuilder stemsString = new StringBuilder();
    
    for (Stem current : stems) {
      if (StringUtils.equals(current.getId(), stem.getId())) {
        return;
      }
      
      stemsString.append(current.getName()).append(", ");
      
    }
    
    if (stems.size() > 100) {
      fail(StringUtils.defaultString(message) + ", expected stems to contain stem '" + stem.getName() + "' but doesnt");
    }

    fail(StringUtils.defaultString(message) + ", expected stems to contain stem '" + stem.getName() + "' but contains: " + stemsString);

  }
  
  /**
   * @param message
   * @param stems
   * @param stem
   */
  public void assertNotContainsStem(Collection<Stem> stems, Stem stem, String message) {
    
    if (stems == null || stems.size() == 0) {
      return;
    }

    for (Stem current : stems) {
      if (StringUtils.equals(current.getId(), stem.getId())) {
        fail(StringUtils.defaultString(message) + ", expected stems to not contain stem '" + stem.getName() + "' but does");
      }
      
    }
    
  }
  
  /**
   * @param message
   * @param groups
   * @param group
   */
  public void assertNotContainsGroup(Collection<Group> groups, Group group, String message) {
    
    if (groups == null || groups.size() == 0) {
      return;
    }

    for (Group current : groups) {
      if (StringUtils.equals(current.getId(), group.getId())) {
        fail(StringUtils.defaultString(message) + ", expected groups to not contain group '" + group.getName() + "' but does");
      }
    }
  }

  /**
   * @param message
   * @param attributeDefs
   * @param attributeDef
   */
  public void assertNotContainsAttributeDef(Collection<AttributeDef> attributeDefs, AttributeDef attributeDef, String message) {
    
    if (attributeDefs == null || attributeDefs.size() == 0) {
      return;
    }

    for (AttributeDef current : attributeDefs) {
      if (StringUtils.equals(current.getId(), attributeDef.getId())) {
        fail(StringUtils.defaultString(message) + ", expected attributeDefs to not contain attributeDef '" + attributeDef.getName() + "' but does");
      }
    }
  }
  

  /**
   * @param message
   * @param attributeDefNames
   * @param attributeDefName
   */
  public void assertNotContainsAttributeDefName(Collection<AttributeDefName> attributeDefNames, AttributeDefName attributeDefName, String message) {
    
    if (attributeDefNames == null || attributeDefNames.size() == 0) {
      return;
    }

    for (AttributeDefName current : attributeDefNames) {
      if (StringUtils.equals(current.getId(), attributeDefName.getId())) {
        fail(StringUtils.defaultString(message) + ", expected attributeDefNames to not contain attributeDef '" + attributeDefName.getName() + "' but does");
      }
    }
  }

  /**
   * @param message
   * @param attributeDefs
   * @param attributeDef
   */
  public void assertContainsAttributeDef(Collection<AttributeDef> attributeDefs, AttributeDef attributeDef, String message) {
    
    if (attributeDefs == null || attributeDefs.size() == 0) {
      fail("Empty set does not contain attributeDef: " + attributeDef.getName());
    }

    StringBuilder attributeDefsString = new StringBuilder();
    
    for (AttributeDef current : attributeDefs) {
      if (StringUtils.equals(current.getId(), attributeDef.getId())) {
        return;
      }
      
      attributeDefsString.append(current.getName()).append(", ");
      
    }
    
    if (attributeDefs.size() > 100) {
      fail(StringUtils.defaultString(message) + ", expected attributeDefs to contain attributeDef '" + attributeDef.getName() + "' but doesnt");
    }

    fail(StringUtils.defaultString(message) + ", expected attributeDefs to contain attributeDef '" + attributeDef.getName() + "' but contains: " + attributeDefsString);

  }
  
  /**
   * @param message
   * @param attributeDefNames
   * @param attributeDefName
   */
  public void assertContainsAttributeDefName(Collection<AttributeDefName> attributeDefNames, AttributeDefName attributeDefName, String message) {
    
    if (attributeDefNames == null || attributeDefNames.size() == 0) {
      fail("Empty set does not contain attributeDefName: " + attributeDefName.getName());
    }

    StringBuilder attributeDefNamesString = new StringBuilder();
    
    for (AttributeDefName current : attributeDefNames) {
      if (StringUtils.equals(current.getId(), attributeDefName.getId())) {
        return;
      }
      
      attributeDefNamesString.append(current.getName()).append(", ");
      
    }
    
    if (attributeDefNames.size() > 100) {
      fail(StringUtils.defaultString(message) + ", expected attributeDefNames to contain attributeDefName '" + attributeDefName.getName() + "' but doesnt");
    }

    fail(StringUtils.defaultString(message) + ", expected attributeDefNames to contain attributeDefName '" + attributeDefName.getName() + "' but contains: " + attributeDefNamesString);

  }
  
  /**
   * 
   * @param message
   * @param outer
   * @param inner
   */
  public void assertContains(String outer, String inner) {
    assertContains(null, outer, inner);
  }
  
  /**
   * @since   1.1.0
   */
  public void assertDoNotFindGroupByAttribute(GrouperSession s, String attr, String val) {
    try {
      GroupFinder.findByAttribute(s, attr, val, true);
      fail("unexpected found group by attribute(" + attr + ")=value(" + val + ")");
    }
    catch (GroupNotFoundException eGNF) {
      assertTrue(true);
    }
  } // public void assertDoNotFindGroupByAttribute(s, attr, val)

  /**  
   * @since   1.1.0
   */
  public void assertDoNotFindGroupByName(GrouperSession s, String name) {
    assertDoNotFindGroupByName(s, name, GrouperConfig.EMPTY_STRING);
  } // public void assertDoNotFindGroupByName(s, name)

  /**  
   * @since   1.1.0
   */
  public void assertDoNotFindGroupByName(GrouperSession s, String name, String msg) {
    try {
      GroupFinder.findByName(s, name, true);
      fail(Quote.parens(msg) + "unexpectedly found group by name: " + name);
    }
    catch (GroupNotFoundException eGNF) {
      assertTrue(msg, true);
    }
  } // public void assertDoNotFindGroupByName(s, name, msg)

  /**  
   * @since   1.2.0
   */
  public void assertDoNotFindGroupByType(GrouperSession s, GroupType type) {
    assertDoNotFindGroupByType(s, type, GrouperConfig.EMPTY_STRING);
  } // public void assertDoNotFindGroupByType(s, type)

  /**  
   * @since   1.2.0
   */
  public void assertDoNotFindGroupByType(GrouperSession s, GroupType type, String msg) {
    try {
      Set<Group> groups = GroupFinder.findAllByType(s, type);
      if (groups.size() == 1) {
        String errorInfo = "size is " + groups.size();
        if (groups.size() != 0) {
          for (Group group : groups) {
            errorInfo += ", group: " + group.getName() + " ";
          }
        }
        fail(Quote.parens(msg) + "unexpectedly found one group by type: " + type + ", " + errorInfo);
      }
    }
    catch (GroupNotFoundException eGNF) {
      assertTrue(msg, true);
    }
  } // public void assertDoNotFindGroupByName(s, name, msg)

  /**  
   * @since   1.1.0
   */
  public void assertDoNotFindStemByName(GrouperSession s, String name) {
    assertDoNotFindStemByName(s, name, GrouperConfig.EMPTY_STRING);
  } // public void assertDoNotFindStemByName(s, name)

  /**
   * @since   1.1.0
   */
  public void assertDoNotFindStemByName(GrouperSession s, String name, String msg) {
    try {
      StemFinder.findByName(s, name, true);
      fail(Quote.parens(msg) + "unexpectedly found stem by name: " + name);
    }
    catch (StemNotFoundException eNSNF) {
      assertTrue(msg, true);
    }
  } // public void assertDoNotFindStemByName(s, name, msg)

  /** 
   * @since   1.1.0
   */
  public Field assertFindField(String name) {
    Field f = null;
    try {
      f = FieldFinder.find(name, true);
      assertTrue(true);
    }
    catch (SchemaException eS) {
      fail("field=(" + name + "): " + eS.getMessage());
    } 
    return f;
  } // public Field assertFindField(name)

  /**
   * @return  Retrieved {@link Group}.
   * @since   1.1.0
   */
  public Group assertFindGroupByAttribute(GrouperSession s, String attr, String val) {
    Group g = null;
    try {
      g = GroupFinder.findByAttribute(s, attr, val, true);
      assertTrue(true);
    }
    catch (GroupNotFoundException eGNF) {
      fail("did not find group by attribute(" + attr + ")=value(" + val + ")");
    }
    return g;
  } // public Group assertFindGroupByAttribute(s, attr, val)

  /**  
   * @return  Retrieved {@link Group}.
   * @since   1.1.0
   */
  public Group assertFindGroupByName(GrouperSession s, String name) {
    return assertFindGroupByName(s, name, GrouperConfig.EMPTY_STRING);
  } // public Group assertFindGroupByName(s, name)

  /**  
   * @return  Retrieved {@link Group}.
   * @since   1.1.0
   */
  public Group assertFindGroupByName(GrouperSession s, String name, String msg) {
    Group g = null;
    try {
      g = GroupFinder.findByName(s, name, true);
      assertTrue(msg, true);
    }
    catch (GroupNotFoundException eGNF) {
      fail(Quote.parens(msg) + "did not find group (" + name + ") by name: " + eGNF.getMessage());
    }
    return g;
  } // public Group assertFindGroupByName(s, name, msg)

  /**  
   * @since   1.2.0
   */
  public Group assertFindGroupByType(GrouperSession s, GroupType type) {
    return assertFindGroupByType(s, type, GrouperConfig.EMPTY_STRING);
  } // public Group assertFindGroupByType(s, type)

  /**  
   * @since   1.2.0
   */
  public Group assertFindGroupByType(GrouperSession s, GroupType type, String msg) {
    Group g = null;
    try {
      g = GroupFinder.findAllByType(s, type).iterator().next();
      assertTrue(msg, true);
      assertGroupHasType(g, type, true);
    }
    catch (Exception eGNF) {
      fail(Quote.parens(msg) + "did not find group (" + type + ") by type: " + eGNF.getMessage());
    }
    return g;
  } // public Group assertFindGroupByType(s, type, msg)

  /** 
   * @since   1.1.0
   */
  public GroupType assertFindGroupType(String name) {
    GroupType type = null;
    try {
      type = GroupTypeFinder.find(name, true);
      assertTrue(true);
    }
    catch (SchemaException eS) {
      fail("type=(" + name + "): " + eS.getMessage());
    } 
    return type;
  } // public GroupType assertFindGroupType(name)

  /**  
   * @return  Retrieved {@link Stem}.
   * @since   1.1.0
   */
  public Stem assertFindStemByName(GrouperSession s, String name) {
    return assertFindStemByName(s, name, GrouperConfig.EMPTY_STRING);
  } // public Stem assertFindStemByName(s, name)

  /**
   * @since   1.1.0
   */
  public Stem assertFindStemByName(GrouperSession s, String name, String msg) {
    Stem ns = null;
    try {
      ns = StemFinder.findByName(s, name, true);
      assertTrue(msg, true);
    }
    catch (StemNotFoundException eNSNF) {
      fail(Quote.parens(msg) + "did not find stem (" + name + ") by name: " + eNSNF.getMessage());
    }
    return ns;
  } // public Stem assertFindStemByName(s, name, msg)

  /**
   * @since   1.1.0
   */
  public void assertGroupAttribute(Group g, String attr, String exp) {
    String name = g.getName();
    try {
      _assertString(G, name, attr, exp, g.getAttributeValue(attr, false, true));
    }
    catch (AttributeNotFoundException eANF) {
      fail("group=(" + name + ") attr=(" + attr + "): " + eANF.getMessage());
    }
  } // public void assertGroupDescription(g, val)

  /** 
   * @since   1.1.0
   */
  public void assertGroupCreateSubject(Group g, Subject subj) {
    try {
      _assertSubject(G, g.getName(), "createSubject", g.getCreateSubject(), subj);
    }
    catch (SubjectNotFoundException eSNF) {
      fail("group (" + g.getName() + "): " + eSNF.getMessage());
    }
  } // public void assertStemCreateSubject(ns, subj)

  /** 
   * @since   1.1.0
   */
  public void assertGroupCreateTime(Group g, Date d) {
    _assertDate(G, g.getName(), "createTime", d, g.getCreateTime());
  } // public void assertStemCreateTime(ns, d)

  /**
   * @since   1.1.0
   */
  public void assertGroupDescription(Group g, String val) {
    _assertString(G, g.getName(), "description", val, g.getDescription());
  } // public void assertGroupDescription(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupDisplayExtension(Group g, String val) {
    _assertString(G, g.getName(), "displayExtension", val, g.getDisplayExtension());
  } // public void assertGroupDisplayExtension(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupDisplayName(Group g, String val) {
    _assertString(G, g.getName(), "displayName", val, g.getDisplayName());
  } // public void assertGroupDisplayName(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupExtension(Group g, String val) {
    _assertString(G, g.getName(), "extension", val, g.getExtension());
  } // public void assertGroupDisplayExtension(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasAdmin(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "ADMIN", exp, g.hasAdmin(subj));
  } // public void assertGroupHasAdmin(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasMember(Group g, Subject subj, boolean exp) {
    assertGroupHasMember(g, subj, Group.getDefaultList(), exp);
  } // public void assertGroupHasMember(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasMember(Group g, Subject subj, Field f, boolean exp) {
    String name = g.getName();
    try {
      boolean got = g.hasMember(subj, f);
      if (got == exp) {
        assertTrue(true);
      }
      else {
        _fail(
          G, name, SubjectHelper.getPretty(subj)  + " is member/" + f.getName(),
          Boolean.toString(exp), Boolean.toString(got)
        );
      }
    }
    catch (SchemaException eS) {
      fail("group=(" + name + "): " + eS.getMessage());
    }
  } // public void assertGroupHasMember(g, subj, f, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasOptin(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "OPTIN", exp, g.hasOptin(subj));
  } // public void assertGroupHasOptin(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasOptout(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "OPTOUT", exp, g.hasOptout(subj));
  } // public void assertGroupHasOptout(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasRead(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "READ", exp, g.hasRead(subj));
  } // public void assertGroupHasRead(g, subj, exp)

  /**
   * @param g
   * @param subj
   * @param exp
   */
  public void assertGroupHasGroupAttrRead(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "GROUP_ATTR_READ", exp, g.hasGroupAttrRead(subj));
  }

  /**
   * @param g
   * @param subj
   * @param exp
   */
  public void assertGroupHasGroupAttrUpdate(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "GROUP_ATTR_UPDATE", exp, g.hasGroupAttrUpdate(subj));
  }
  
  /**
   * @since   1.1.0
   */
  public void assertGroupHasType(Group g, GroupType type, boolean exp) {
    boolean got = g.hasType(type);
    if (got == exp) {
      assertTrue(true);
    }
    else {
      _fail(
        G, g.getName(), type.getName(), Boolean.toString(exp), Boolean.toString(got)
      );
    }
  } // public void assertGroupHasType(g, type, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasUpdate(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "UPDATE", exp, g.hasUpdate(subj));
  } // public void assertGroupHasUpdate(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupHasView(Group g, Subject subj, boolean exp) {
    _assertPriv(G, g.getName(), subj, "VIEW", exp, g.hasView(subj));
  } // public void assertGroupHasView(g, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertGroupName(Group g, String val) {
    _assertString(G, g.getName(), "name", val, g.getName());
  } // public void assertGroupName(g, val)

  /**
   * @since   1.1.0
   */
  public void assertGroupUuid(Group g, String val) {
    _assertString(G, g.getName(), "uuid", val, g.getUuid());
  } // public void assertGroupUuid(g, val)

  /** 
   * @since   1.1.0
   */
  public void assertStemCreateSubject(Stem ns, Subject subj) {
    try {
      _assertSubject(NS, ns.getName(), "createSubject", subj, ns.getCreateSubject());
    }
    catch (SubjectNotFoundException eSNF) {
      fail("stem (" + ns.getName() + "): " + eSNF.getMessage());
    }
  } // public void assertStemCreateSubject(ns, subj)

  /** 
   * @since   1.1.0
   */
  public void assertStemCreateTime(Stem ns, Date d) {
    _assertDate(NS, ns.getName(), "createTime", d, ns.getCreateTime());
  } // public void assertStemCreateTime(ns, d)

  /**
   * @since   1.1.0
   */
  public void assertStemDescription(Stem ns, String val) {
    _assertString(NS, ns.getName(), "description", val, ns.getDescription());
  } // public void assertStemDescription(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemDisplayExtension(Stem ns, String val) {
    _assertString(NS, ns.getName(), "displayExtension", val, ns.getDisplayExtension());
  } // public void assertStemDisplayExtension(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemDisplayName(Stem ns, String val) {
    _assertString(NS, ns.getName(), "displayName", val, ns.getDisplayName());
  } // public void assertStemDisplayName(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemExtension(Stem ns, String val) {
    _assertString(NS, ns.getName(), "extension", val, ns.getExtension());
  } // public void assertStemExtension(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemHasCreate(Stem ns, Subject subj, boolean exp) {
    _assertPriv(NS, ns.getName(), subj, "CREATE", exp, ns.hasCreate(subj));
  } // public void assertStemHasCreate(ns, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertStemHasStem(Stem ns, Subject subj, boolean exp) {
    _assertPriv(NS, ns.getName(), subj, "STEM", exp, ns.hasStem(subj));
    _assertPriv(NS, ns.getName(), subj, "STEM_ADMIN", exp, ns.hasStemAdmin(subj));
  } // public void assertStemHasStem(ns, subj, exp)

  /**
   * @since   1.1.0
   */
  public void assertStemName(Stem ns, String val) {
    _assertString(NS, ns.getName(), "name", val, ns.getName());
  } // public void assertStemName(ns, val)

  /**
   * @since   1.1.0
   */
  public void assertStemUuid(Stem ns, String val) {
    _assertString(NS, ns.getName(), "uuid", val, ns.getUuid());
  } // public void assertStemDescription(ns, val)

  /**
   * @since   1.2.0
   */
  public void unexpectedException(Exception e) {
    e.printStackTrace();
    LOG.error("Error in test", e);
    fail( "UNEXPECTED EXCEPTION: " + ExceptionUtils.getFullStackTrace(e) );
  } // public void unexpectedException(e)


  // PROTECTED INSTANCE METHODS //

  /**
   * make sure a result set has a privilege from a user and stem
   * @param result set of object arrays of membership, stem, member
   * @param stemA 
   * @param subject
   * @param privilege
   */
  public void assertHasPrivilege(Set<Object[]> results, Stem stem, Subject subject, Privilege privilege) {
    for (Object[] result : results) {
      Membership resultMembership = (Membership)result[0];
      if (!(result[1] instanceof Stem)) {
        continue;
      }
      Stem resultStem = (Stem)result[1];
      Member resultMember = (Member)result[2];
      
      if (!StringUtils.equals(resultStem.getId(), stem.getId())) {
        continue;
      }
      
      if (!SubjectHelper.eq(resultMember.getSubject(), subject)) {
        continue;
      }

      if (!StringUtils.equals(resultMembership.getListName(), privilege.getListName())) {
        continue;
      }
      
      if (!resultMembership.isEnabled()) {
        continue;
      }
      //should be good
      return;
    }
    
    printMemberships(results);

    //couldnt find it
    fail("Couldnt find privilege: " + stem.getName() + ", " + subject.getId() + ", " + privilege.getListName());
  }
  
  /**
   * make sure a result set has a privilege from a user and attribute def
   * @param result set of object arrays of membership, attribute def, member
   * @param attributeDef 
   * @param subject
   * @param privilege
   */
  public void assertHasPrivilege(Set<Object[]> results, AttributeDef attributeDef, Subject subject, Privilege privilege) {
    for (Object[] result : results) {
      Membership resultMembership = (Membership)result[0];
      if (!(result[1] instanceof AttributeDef)) {
        continue;
      }
      AttributeDef resultAttributeDef = (AttributeDef)result[1];
      Member resultMember = (Member)result[2];
      
      if (!StringUtils.equals(resultAttributeDef.getId(), attributeDef.getId())) {
        continue;
      }
      
      if (!SubjectHelper.eq(resultMember.getSubject(), subject)) {
        continue;
      }

      if (!StringUtils.equals(resultMembership.getListName(), privilege.getListName())) {
        continue;
      }
      
      if (!resultMembership.isEnabled()) {
        continue;
      }
      //should be good
      return;
    }
    
    printMemberships(results);

    //couldnt find it
    fail("Couldnt find privilege: " + attributeDef.getName() + ", " + subject.getId() + ", " + privilege.getListName());
  }
  
  /**
   * make sure a result set has a privilege from a user and stem
   * @param result set of object arrays of membership, stem, member
   * @param stemA 
   * @param subject
   * @param privilege
   */
  public void assertHasPrivilege(Set<Object[]> results, Group group, Subject subject, Privilege privilege) {
    for (Object[] result : results) {
      Membership resultMembership = (Membership)result[0];
      if (!(result[1] instanceof Group)) {
        continue;
      }
      Group resultGroup = (Group)result[1];
      Member resultMember = (Member)result[2];
      
      if (!StringUtils.equals(resultGroup.getId(), group.getId())) {
        continue;
      }
      
      if (!SubjectHelper.eq(resultMember.getSubject(), subject)) {
        continue;
      }

      if (!StringUtils.equals(resultMembership.getListName(), privilege.getListName())) {
        continue;
      }
      
      if (!resultMembership.isEnabled()) {
        continue;
      }
      //should be good
      return;
    }

    printMemberships(results);
    
    //couldnt find it
    fail("Couldnt find privilege: " + group.getName() + ", " + subject.getId() + ", " + privilege.getListName());
  }

  /**
   * print out memberships to help with debugging
   * @param memberships
   */
  public void printMemberships(Set<Object[]> results) {
    // are we in unit test mode
    if (!(this instanceof GrouperTestInApi)) {
      return;
    }
    //print out memberships
    for (Object[] result : results) {
      Membership resultMembership = (Membership)result[0];
      if (!(result[1] instanceof Group)) {
        System.out.println("Type: " + ((result[1] == null ? null : result[1].getClass().getName())));
      }
      String ownerName = null;
      if (result[1] instanceof Group) {
        ownerName = ((Group)result[1]).getName();
      }
      if (result[1] instanceof Stem) {
        ownerName = ((Stem)result[1]).getName();
      }
      if (result[1] instanceof AttributeDef) {
        ownerName = ((AttributeDef)result[1]).getName();
      }
        
      Member resultMember = (Member)result[2];
      
      String memberString = null;
      
      if (StringUtils.equals(resultMember.getSubjectSourceId(), GrouperSourceAdapter.groupSourceId())) {
        Group theGroup = GroupFinder.findByUuid(
            GrouperSession.staticGrouperSession().internal_getRootSession(), resultMember.getSubjectId(), false);
        memberString = theGroup == null ? "null group?" : theGroup.getName();
      } else {
        memberString = resultMember.getSubjectId();
      }
      
      System.out.println("Group: " + ownerName + ", Member: " + memberString + ", Field: " + resultMembership.getListName());
      
    }    
    
  }

  /**
   * make sure a set of groups is similar to another by group name including order
   * @param set1 expected set
   * @param set2 received set
   */
  public void assertGroupSetsAndOrder(Set<Group> set1, Set<Group> set2) {

    if (set1 == set2) {
      return;
    }

    set2 = filterOutBuiltInGroups(set2);
    
    int set1length = GrouperUtil.length(set1);
    int set2length = GrouperUtil.length(set2);
    if (set1length != set2length) {
      fail("Expecting groups of size: " + set1length + " but received size: " + set2length + ", expecting: "
          + GrouperUtil.toStringForLog(set1, 200) + ", but received: " + GrouperUtil.toStringForLog(set2, 200));
    }
    
    if (set1length == 0) {
      return;
    }
    
    List<Group> list2 = new ArrayList<Group>(set2);
    int i=0;
    for (Group group : set1) {
      if (!StringUtils.equals(group.getName(), list2.get(i).getName())) {
        fail("Expecting index of set: " + i + " to be: " + group.getName() + ", but received: "
            + list2.get(i).getName() + ", expecting: " 
            + GrouperUtil.toStringForLog(set1, 200) + ", but received: " + GrouperUtil.toStringForLog(set2, 200));
      }
      i++;
    }
  }
  
   


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private void _assertDate(String type, String who, String what, Date exp, Date got) {
    if (exp.equals(got)) {
      assertTrue(true);
    }
    else {
      _fail(type, who, what, exp + "/" + exp.getTime(), got + "/" + got.getTime());
    }
  } // private void _assertDate(type, who, what, exp, got)

  // @since   1.1.0
  private void _assertPriv(String type, String who, Subject subj, String what, boolean exp, boolean got) {
    if (exp == got) {
      assertTrue(true);
    }
    else {
      _fail(
        type, who, SubjectHelper.getPretty(subj) + " has " + what, 
        Boolean.toString(exp), Boolean.toString(got)
      );
    }
  } // private void _assertPriv(type, who, subj, what, exp, got)

  /**
   * 
   * @param type
   * @param who
   * @param what
   * @param exp
   * @param got
   */
  private void _assertString(String type, String who, String what, String exp, String got) {
    if (StringUtils.equals(exp, got)) {
      assertTrue(true);
    }
    else {
      _fail(type, who, what, exp, got);
    }
  } // private void _assertString(who, what, exp, got)

  // @since   1.1.0
  private void _assertSubject(String type, String who, String what, Subject exp, Subject got) {
    if (SubjectHelper.eq(exp, got)) {
      assertTrue(true);
    }
    else {
      _fail(type, who, what, SubjectHelper.getPretty(exp), SubjectHelper.getPretty(got));
    }
  } // private void _assertSubject(type, who, what, exp, got)

  // @since   1.1.0
  private void _fail(String type, String who, String what, String exp, String got) {
    fail(type + "=(" + who + "): testing=(" + what + ") expected=(" + exp + ") got=(" + got + ")");
  } // private void _fail(type, who, what, exp, got)

  /**
   * 
   * @param groups
   * @return the filtered groups
   */
  public static Set<Group> filterOutBuiltInGroups(Set<Group> groups) {
    String builtinStem = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc");
    if (GrouperUtil.length(groups) > 0) {
      //dont change calling set
      groups = new LinkedHashSet<Group>(groups);
  
      //remove anything from etc
      Iterator<Group> iterator = groups.iterator();
      while (iterator.hasNext()) {
        Group current = iterator.next();
        if (current.getName().startsWith(builtinStem)) {
          iterator.remove();
        }
      }
    }  
    return groups;
  }

  /**
   * helper method to delete group if exist
   * @param grouperSession
   * @param name
   * @throws Exception 
   */
  public static void deleteGroupIfExists(GrouperSession grouperSession, String name) throws Exception {
    
    try {
      Group group = GroupFinder.findByName(grouperSession, name, true);
      //hopefully this will succeed
      group.delete();
    } catch (GroupNotFoundException gnfe) {
      //this is good
    }
    
  }

  /**
   * helper method to delete stem if exist
   * @param grouperSession
   * @param name
   * @throws Exception 
   */
  public static void deleteStemIfExists(GrouperSession grouperSession, String name) throws Exception {
    try {
      Stem stem = StemFinder.findByName(grouperSession, name, true);
      //hopefully this will succeed
      stem.delete();
    } catch (StemNotFoundException snfe) {
      //this is good
    }
    
  }
  

} 

