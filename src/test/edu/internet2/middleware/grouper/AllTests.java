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

package edu.internet2.middleware.grouper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.AllAppTests;
import edu.internet2.middleware.grouper.audit.AllAuditTests;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.AllConfigTests;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.AllDdlTests;
import edu.internet2.middleware.grouper.filter.AllFilterTests;
import edu.internet2.middleware.grouper.group.AllGroupTests;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.AllHooksTests;
import edu.internet2.middleware.grouper.internal.dao.AllInternalDaoTests;
import edu.internet2.middleware.grouper.member.AllMemberTests;
import edu.internet2.middleware.grouper.membership.AllMembershipTests;
import edu.internet2.middleware.grouper.misc.AllMiscTests;
import edu.internet2.middleware.grouper.privs.AllPrivsTests;
import edu.internet2.middleware.grouper.subj.AllSubjectTests;
import edu.internet2.middleware.grouper.util.AllUtilTests;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.AllValidatorTests;
import edu.internet2.middleware.grouper.xml.AllXmlTests;
import edu.internet2.middleware.ldappcTest.AllLdappcJunitTests;

/**
 * Run default tests.
 * @author  blair christensen.
 * @version $Id: AllTests.java,v 1.4 2009-03-23 06:00:00 mchyzer Exp $
 */
public class AllTests extends GrouperTest {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AllTests.class);

  /**
   * run tests from bat or sh file.  Note, dont change this method unless you want the 
   * bat or sh test runer to be changed
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperTest.testing = true;
    
    Test test = null;
    boolean noPrompt = false;

    if (args == null || args.length == 0 || args[0].equalsIgnoreCase("-h")) {
      System.out.println(_getUsage());
      return;
    }
    if (args.length == 2 && args[1].equalsIgnoreCase("-noprompt")) {
      noPrompt = true;
    } else if ((args.length == 2 && !args[1].equalsIgnoreCase("-noprompt"))
        || args.length > 2) {
      System.out.println("Invalid argumants. Please check usage:");
      System.out.println(_getUsage());
      return;
    }

    if (!args[0].equalsIgnoreCase("-all")) {
      String testName = null;
      if (args[0].startsWith("edu.internet2.middleware.")) {
        testName = args[0];
      } else {
        testName = "edu.internet2.middleware.grouper." + args[0];
      }
      Class claz = null;
      Exception ex = null;

      try {
        claz = Class.forName(testName);
        Method method = null;
        try {
          method = claz.getMethod("suite");
          test = (Test) method.invoke(null);
        } catch (NoSuchMethodException e) {
          TestSuite testSuite = new TestSuite();
          testSuite.addTestSuite(claz);
          test = testSuite;
        }

      } catch (ClassNotFoundException e) {
        LOG.error("Error finding test class: " + testName, e);
        ex = e;
      } catch (ClassCastException e) {
        LOG.error("Test class is not of type Test: " + testName, e);
        ex = e;
      } catch (InvocationTargetException e) {
        LOG.error("Error invoking suite(): " + testName, e);
        ex = e;
      } catch (IllegalAccessException e) {
        LOG.error("Error invoking suite(): " + testName, e);
        ex = e;
      }
      if (ex != null) {
        throw new RuntimeException(ex);
      }
    }

    if (test == null) {
      test = AllTests.suite();
    }
    if (noPrompt) {
      System.setProperty("grouper.allow.db.changes", "true");
    }
    try {
      TestRunner.run(test);
    } catch (RuntimeException re) {
      LOG.error("Error in testing", re);
      throw re;
    }
  }

  /**
   * 
   * @return the suite
   */
  public static Test suite() {

    GrouperTest.testing = true;
    
    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.JUNIT, false, true);

    GrouperTest.setupTests();

    TestSuite suite = new TestSuite();

    //$JUnit-BEGIN$
    suite.addTestSuite(TestGroupTypeIncludeExclude.class);
    suite.addTestSuite(GrouperVersionTest.class);
    suite.addTestSuite(TestCompositeU.class);
    suite.addTestSuite(TestStemIntegration.class);
    suite.addTestSuite(TestStemApi.class);
    suite.addTestSuite(TestSession.class);
    suite.addTestSuite(TestGrouperSession.class);
    suite.addTestSuite(TestStemFinder.class);
    suite.addTestSuite(TestStem.class);
    suite.addTestSuite(TestCompositeModel.class);
    suite.addTestSuite(TestGroupType.class);
    suite.addTestSuite(TestCompositeI.class);
    suite.addTestSuite(TestField.class);
    suite.addTestSuite(TestComposite.class);
    //$JUnit-END$

    //////////////////////////////////////////
    // All manual suites from packages in alphabetical order

    suite.addTest(AllAppTests.suite());
    suite.addTest(AllAuditTests.suite());
    suite.addTest(AllConfigTests.suite());

    if (GrouperConfig.getPropertyBoolean("junit.test.ddl", true)) {
      //do this first so all tests are done on new ddl
      suite.addTest(AllDdlTests.suite());
    }

    suite.addTest(AllFilterTests.suite());
    suite.addTest(AllGroupTests.suite());
    suite.addTest(AllHooksTests.suite());
    suite.addTest(AllInternalDaoTests.suite());
    suite.addTest(AllMemberTests.suite());
    suite.addTest(AllMembershipTests.suite());
    suite.addTest(AllMiscTests.suite());
    suite.addTest(AllPrivsTests.suite());
    suite.addTest(AllSubjectTests.suite());
    suite.addTest(AllUtilTests.suite());
    suite.addTest(AllValidatorTests.suite());
    suite.addTest(AllXmlTests.suite());

    if (GrouperConfig.getPropertyBoolean("junit.test.ldappc", false)) {
      suite.addTest(AllLdappcJunitTests.suite());
    }

    return suite;
  }

  /**
   * 
   * @return the string
   */
  private static String _getUsage() {
    return "Usage:" + GrouperConfig.NL + "args: -h,            Prints this message"
        + GrouperConfig.NL + "args: (-all | testName [-noprompt]" + GrouperConfig.NL

        + "  -all,              Run all JUnit tests" + GrouperConfig.NL
        + "  testName,          Run specific test - omit package name" + GrouperConfig.NL
        + "                     Grouper data e.g. root stem and fields"
        + GrouperConfig.NL + "  -noprompt,         Do not prompt user about data loss"
        + GrouperConfig.NL

    ;
  } // private static String _getUsage()

}
