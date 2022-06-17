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
 * @author mchyzer
 * $Id: AllLoaderDbTests.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.provisioning;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * test suite
 */
public class AllProvisioningTests {

  /**
   * test
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.loader.db");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperProvisioningObjectMetadataTest.class);
    suite.addTestSuite(GrouperProvisioningServiceTest.class);
    suite.addTestSuite(ProvisionerConfigurationTest.class);
    suite.addTestSuite(PspngToNewProvisioningAttributeConversionTest.class);
    suite.addTestSuite(GrouperProvisioningAttributePropagationTest.class);
    suite.addTestSuite(ProvisionableStemSaveTest.class);
    suite.addTestSuite(ProvisionableGroupSaveTest.class);
    suite.addTestSuite(ProvisionableStemFinderTest.class);
    suite.addTestSuite(ProvisionableGroupFinderTest.class);
    suite.addTestSuite(ProvisioningGroupTest.class);
    //$JUnit-END$
    return suite;
  }

}
