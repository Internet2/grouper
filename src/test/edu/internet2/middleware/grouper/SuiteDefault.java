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
import  junit.framework.*;

/**
 * Run default tests.
 * @author  blair christensen.
 * @version $Id: SuiteDefault.java,v 1.26 2007-08-02 19:25:15 blair Exp $
 */
public class SuiteDefault extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite( Test_api_ChildGroupFilter.class );
    suite.addTestSuite( Test_api_ChildStemFilter.class );
    suite.addTestSuite( Test_api_GrouperConfig.class );
    suite.addTestSuite( Test_api_GrouperDAOFactory.class );
    suite.addTestSuite( Test_api_GrouperSession.class );
    suite.addTestSuite( Test_api_Stem.class );

    suite.addTestSuite( Test_cfg_ApiConfig.class );
    suite.addTestSuite( Test_cfg_BuildConfig.class );
    suite.addTestSuite( Test_cfg_ConfigurationHelper.class );
    suite.addTestSuite( Test_cfg_PropertiesConfiguration.class );

    suite.addTestSuite( Test_dao_hibernate_HibernateDaoConfig.class );

    suite.addTestSuite( Test_uc_WheelGroup.class );


    suite.addTestSuite( Test_UnresolvedBugs.class );
    suite.addTest( Suite_Unit.suite() );
    suite.addTest( Suite_Integration.suite() );

    suite.addTest( SuiteSessions.suite()         );
    suite.addTest( SuiteStems.suite()            );
    suite.addTest( SuiteGroupTypes.suite()       );
    suite.addTest( SuiteGroups.suite()           );
    suite.addTest( SuiteGrouperQuery.suite()     );
    suite.addTest( SuiteGroupFinder.suite()      );
    suite.addTest( SuiteComposites.suite()       );
    suite.addTest( SuiteSubjects.suite()         );
    suite.addTest( SuiteMembers.suite()          );
    suite.addTest( SuiteMemberFinder.suite()     );
    suite.addTest( SuiteMemberships.suite()      );
    suite.addTest( SuiteMemberOf.suite()         );
    suite.addTest( SuiteAccessPrivs.suite()      );
    suite.addTest( SuiteXml.suite()              );
    suite.addTest( SuiteWheelGroup.suite()       );
    suite.addTest( SuitePrivCache.suite()        );
    return suite;
  }

} 

