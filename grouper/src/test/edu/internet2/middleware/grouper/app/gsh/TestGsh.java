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

package edu.internet2.middleware.grouper.app.gsh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Test {@link ChildGroupFilter}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: TestGsh.java,v 1.12 2009-11-05 17:59:40 mchyzer Exp $
 * @since   1.2.1
 */
public class TestGsh extends GrouperTest {

  /**
   * 
   */
  public TestGsh() {
    super();
  }
  
  /**
   * @param name
   */
  public TestGsh(String name) {
    super(name);
  }


  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "true");

  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new TestGsh("testGshGroups"));

    //TestRunner.run(TestGsh.class);
  }

  /**
   * run the gsh test from script
   * @throws GrouperShellException 
   */
  public void testGshTest() throws GrouperShellException {
    runGshScriptHelper("test.gsh");
  }

  /**
   * run the gsh test from script
   * @throws GrouperShellException 
   */
  public void testGshStems() throws GrouperShellException {
    runGshScriptHelper("stems.gsh");
  }

  /**
   * run the gsh test from script
   * @throws GrouperShellException 
   */
  public void testGshGroups() throws GrouperShellException {
    Properties  properties = GrouperHibernateConfig.retrieveConfig().properties();
    if (!((String)properties.get("hibernate.connection.url")).contains(":sqlserver:")) {
      runGshScriptHelper("groups.gsh");
    }
  }

  /**
   * run the gsh test from script
   * @throws GrouperShellException 
   */
  public void testGshPrivs() throws GrouperShellException {
    runGshScriptHelper("privs.gsh");
  }

  /**
   * run the gsh test from script
   * @throws GrouperShellException 
   */
  public void testGshComposites() throws GrouperShellException {
    runGshScriptHelper("composites.gsh");
  }

  /**
   * run the gsh test from script
   * @throws GrouperShellException 
   */
  public void testGshGroupTypes() throws GrouperShellException {
    runGshScriptHelper("group_types.gsh");
  }

  /**
   * run the gsh test from script
   * @throws GrouperShellException 
   */
  public void testGshXml() throws GrouperShellException {
    runGshScriptHelper("xml.gsh");
  }

  /**
   * @param gshScript
   * @throws GrouperShellException
   */
  private void runGshScriptHelper(String gshScript) throws GrouperShellException {
    
    System.err.println("Running GSH test: " + gshScript);
    
    URL url = GrouperUtil.computeUrl("edu/internet2/middleware/grouper/app/gsh/" + gshScript, true);
    
    
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    
    ByteArrayOutputStream baosOutErr = new ByteArrayOutputStream();
    
    PrintStream outErrStream = new PrintStream(baosOutErr);
    System.setOut(outErrStream);
    System.setErr(outErrStream);
    
    try {
    
      GrouperShell.grouperShellHelper(null, url.openStream());
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with file: " + gshScript);
    } finally {
      System.out.flush();
      System.err.flush();
      System.setOut(originalOut);
      System.setErr(originalErr);
    }
    
    String outErr = baosOutErr.toString();
    
    assertFalse("Script: " + gshScript + " stdout/stderr cant contain 'error': " + outErr, outErr.toLowerCase().contains("error"));
    
    System.err.println(outErr);
  } 

}

