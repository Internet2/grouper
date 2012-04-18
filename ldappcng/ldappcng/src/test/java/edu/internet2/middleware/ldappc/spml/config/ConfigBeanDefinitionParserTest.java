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
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.ldappc.spml.config;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.internet2.middleware.ldappc.BaseProvisioningTest;

public class ConfigBeanDefinitionParserTest extends XMLTestCase {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigBeanDefinitionParserTest.class);

  /** Base resource location. */
  public static final String TEST_PATH = BaseProvisioningTest.TEST_PATH + "/spml/config/";
  
  private BasicParserPool xmlParser;

  public void setUp() {
    try {
      DefaultBootstrap.bootstrap();

      xmlParser = new BasicParserPool();
      xmlParser.setNamespaceAware(true);

      XMLUnit.setIgnoreWhitespace(true);

    } catch (ConfigurationException e) {
      fail("An error occurred : " + e.getMessage());
    }
  }

  public void testConfig() {

    String before = TEST_PATH + "ConfigBeanParserTest.before.xml";
    String after = TEST_PATH + "ConfigBeanParserTest.after.xml";

    testRewrite(before, after);
  }

  public void testConfigMacro() {

    String before = TEST_PATH + "ConfigBeanParserTest.macro.before.xml";
    String after = TEST_PATH +  "ConfigBeanParserTest.macro.after.xml";

    testRewrite(before, after);
  }

  private void testRewrite(String before, String after) {

    try {

      File beforeFile = new File(getClass().getResource(before).toURI());
      File afterFile = new File(getClass().getResource(after).toURI());

      Document beforeDoc = xmlParser.parse(new FileReader(beforeFile));
      Element beforeElement = beforeDoc.getDocumentElement();
      String beforeXML = XMLHelper.prettyPrintXML(beforeElement);

      Document afterDoc = xmlParser.parse(new FileReader(afterFile));
      Element afterElement = afterDoc.getDocumentElement();
      String afterXML = XMLHelper.prettyPrintXML(afterElement);

      ConfigBeanDefinitionParser parser = new ConfigBeanDefinitionParser();
      Element rewrite = parser.rewriteConfig(beforeElement);
      String rewriteXML = XMLHelper.prettyPrintXML(rewrite);

      LOG.debug("before XML:\n" + beforeXML);
      LOG.debug("after XML:\n" + afterXML);
      LOG.debug("rewritten XML:\n" + rewriteXML);

      DetailedDiff myDiff = new DetailedDiff(new Diff(afterXML, rewriteXML));

      List allDifferences = myDiff.getAllDifferences();
      LOG.debug("differences '{}'", allDifferences);
      LOG.debug("diff '{}'", myDiff.toString());
      assertTrue(allDifferences.isEmpty());

      assertXMLEqual(afterXML, rewriteXML);

    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e.getMessage());
    }
  }
}
