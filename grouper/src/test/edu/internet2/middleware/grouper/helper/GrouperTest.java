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

package edu.internet2.middleware.grouper.helper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.registry.RegistryInitializeSchema;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * Grouper-specific JUnit assertions.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperTest.java,v 1.3 2009-12-10 08:54:15 mchyzer Exp $
 * @since   1.1.0
 */
public class GrouperTest extends TestCase {

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


  // PRIVATE CLASS CONSTANTS //
  private static final String G   = "group";
  private static final Log    LOG = GrouperUtil.getLog(GrouperTest.class);
  private static final String NS  = "stem";


  /**
   * make sure enough memory to run tests
   */
  public static void assertEnoughMemory() {
    if (Runtime.getRuntime().maxMemory() < 400000000) {
      throw new RuntimeException("Not enough memory, you should have at least 500 megs, e.g. -XX:MaxPermSize=300m -Xms80m -Xmx640m, but this much was detected: " + Runtime.getRuntime().maxMemory());
    }
  }
  
  /**
   * @since   1.2.0
   */
  public GrouperTest() {
    super();
    
    testing = true;
    
    //I believe this needs to be here before Grouper starts up
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.autoCreateSource", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubject.sourceId", "grouperExternal");

    //let the database release...
    GrouperStartup.startup();
  } // public GrouperTest()

  /** 
   * @since   1.1.0
   */
  public GrouperTest(String name) {
    super(name);
    testing = true;

    //let the database release...
    GrouperStartup.startup();
  } // public GrouperTest()

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
   * Return consistent test initialization error message.
   * @since   1.2.0
   */
  protected void errorInitializingTest(Exception e) {
    fail( "ERROR INITIALIZING TEST: " + e.getMessage() );
  }

  /**
   * override this method to configure the configs...
   */
  protected void setupConfigs() {
    
  }
  
  // @since   1.2.0
  protected void setUp () {
    LOG.debug("setUp");
    
    GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
    GrouperSession.clearGrouperSessions();
    
    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.JUNIT, false, true);
    
    //remove any settings in testconfig
    GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().clear();
    SubjectFinder.internalClearSubjectCustomizerCache();

    for (int i=0;i<20;i++) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name." + i, null);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.description." + i, null);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.subjects." + i, null);
    }

    //set grouper.example.properties stuff...
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.admin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.optin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.optout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.update", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.create.grant.all.create", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.create.grant.all.stem", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("entities.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", "etc:sysadmingroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subject.internal.grouperall.name", "EveryEntity");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subject.internal.groupersystem.name", "GrouperSysAdmin");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute0.el", "${subject.name},${subject.id}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute0.el", "${subject.name}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.grouperLoader.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.grouperGroupMembershipSettings.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToMoveStem", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToRenameStem", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.stem.groupAllowedToCopyStem", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.search.defaultIndexOrder", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.sort.defaultIndexOrder", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.allPage.useThreadForkJoin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.idOrIdentifier.useThreadForkJoin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.group.useCreatorAndModifierAsSubjectAttributes", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.customizer.className", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.startRootSessionIfOneIsntStarted", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attribute.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeDef.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeDefName.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeAssign.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.attributeAssignValue.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.group.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.lifecycle.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.membership.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.member.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.stem.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.composite.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.field.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.grouperSession.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.groupType.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.groupTypeTuple.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.loader.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.externalSubject.class", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.act.as.group", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.accessToApiInEl.group", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.act.as.cache.minutes", "30");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.customElClasses", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.allowActAsGrouperSystemForInheritedStemPrivileges", null); 
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.emailTemplatesFolder", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.attribute.validator.attributeName.0", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.attribute.validator.regex.0", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.attribute.validator.vetoMessage.0", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.api.readonly", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("membershipUpdateLiteTypeAutoCreate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.attribute.rootStem", "etc:attribute");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.attribute.loader.autoconfigure", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.builtin.createAs.public", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ddlutils.use.nestedTransactions", "true");
    
    setupConfigs();
    
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("default.subject.source.id", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.system.groups", "false");
    
    //dont send emails
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", "testing");
    
    GrouperEmail.testingEmails().clear();

  }

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
  
  /**
   * init groups and attributes after reset
   */
  public static void initGroupsAndAttributes() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedRootSession = false;
    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession();
      startedRootSession = false;
    }
    try {
      if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
        grouperSession = grouperSession.internal_getRootSession();
      }
    } catch (Exception e) {
      //might throw an exception if wheel isnt created yet
      grouperSession = grouperSession.internal_getRootSession();
    }
    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
  
          GrouperCheckConfig.checkGroups();
          GrouperCheckConfig.checkAttributes();
          return null;
        }
      });
    } finally {
      if (startedRootSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  } 

  // @since   1.2.0
  protected void tearDown () {
    LOG.debug("tearDown");
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
   */
  public void setupTestConfigForIncludeExclude() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "true");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.type.name", "addIncludeExclude");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.type.name", "requireInGroups");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.tooltip", "Select this type to auto-create other groups which facilitate having include and exclude list, and setting up group math so that other groups can be required (e.g. activeEmployee)");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.attributeName", "requireAlsoInGroups");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.tooltip", "Enter in comma separated group path(s).  An entity must be in these groups for it to be in the overall group.  e.g. stem1:stem2:group1, stem1:stem3:group2");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecord.extension.suffix", "_systemOfRecord");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.include.extension.suffix", "_includes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.exclude.extension.suffix", "_excludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecordAndIncludes.extension.suffix", "_systemOfRecordAndIncludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.includesMinusExcludes.extension.suffix", "_includesMinusExcludes");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.extension.suffix", "_requireGroups${i}");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecord.displayExtension.suffix", "${space}system of record");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.include.displayExtension.suffix", "${space}includes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.exclude.displayExtension.suffix", "${space}excludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecordAndIncludes.displayExtension.suffix", "${space}system of record and includes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.includesMinusExcludes.displayExtension.suffix", "${space}includes minus excludes");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.displayExtension.suffix", "${space}includes minus exludes minus andGroup${i}");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.overall.description", "Group containing list of ${displayExtension} after adding the includes and subtracting the excludes");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecord.description", "Group containing list of ${displayExtension} (generally straight from the system of record) without yet considering manual include or exclude lists");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.include.description", "Group containing manual list of includes for group ${displayExtension} which will be added to the system of record list (unless the subject is also in the excludes group)");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.exclude.description", "Group containing manual list of excludes for group ${displayExtension} which will not be in the overall group");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.systemOfRecordAndIncludes.description", "Internal utility group for group ${displayExtension} which facilitates the group math for the include and exclude lists");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.includesMinusExclude.description", "Internal utility group for group ${displayExtension} which facilitates includes, excludes, and required groups (e.g. activeEmployee)");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.description", "Internal utility group for group ${displayExtension} which facilitates includes, excludes, and required groups (e.g. activeEmployee)");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.name.0", "requireActiveEmployee");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.attributeOrType.0", "attribute");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.group.0", "aStem:activeEmployee");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.description.0", "If value is true, members of the overall group must be an active employee (in the aStem:activeEmployee group).  Otherwise, leave this value not filled in.");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.name.1", "requireActiveStudent");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.attributeOrType.1", "type");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.group.1", "aStem:activeStudent");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroup.description.1", "If value is true, members of the overall group must be an active student (in the aStem:activeStudent group).  Otherwise, leave this value not filled in.");
  }

  /** see if we are testing */
  public static boolean testing = false;

  /**
   * 
   */
  public static void setupTests() {
    //dont keep prompting user about DB
    GrouperUtil.stopPromptingUser = true;
    GrouperDdlUtils.internal_printDdlUpdateMessage = false;
    RegistryInitializeSchema.initializeSchemaForTests();
    GrouperDdlUtils.internal_printDdlUpdateMessage = true;
  }

  /**
   * concat to stem name full
   * @param names
   * @param length
   * @return stem name based on array and length
   */
  public static String stemName(String[] names, int length) {
    StringBuilder result = new StringBuilder();
    for (int i=0;i<length;i++) {
      result.append(names[i]);
      if (i<length-1) {
        result.append(":");
      }
    }
    return result.toString();
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
   * helper method to delete stems if exist
   * @param grouperSession
   * @param name
   * @throws Exception 
   */
  public static void deleteAllStemsIfExists(GrouperSession grouperSession, String name) throws Exception {
    //this isnt good, it exists
    String[] stems = StringUtils.split(name, ':');
    Stem currentStem = null;
    for (int i=stems.length-1;i>-0;i--) {
      String currentName = GrouperTest.stemName(stems, i+1);
      try {
        currentStem = StemFinder.findByName(grouperSession, currentName, true);
      } catch (StemNotFoundException snfe1) {
        continue;
      }
      currentStem.delete();
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
} // public class GrouperTest

