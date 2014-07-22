/**
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

package edu.internet2.middleware.grouper.xml;
import java.util.regex.Pattern;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GrouperTest;

/**
 * @author  blair christensen.
 * @version $Id: Test_U_API_XmlExporter_internal_groupToXML.java,v 1.2 2009-12-07 07:31:09 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_U_API_XmlExporter_internal_groupToXML extends GrouperTest {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(Test_U_API_XmlExporter_internal_groupToXML.class);
    TestRunner.run(new Test_U_API_XmlExporter_internal_groupToXML("test_internal_groupToXML_escapeDisplayName"));
  }
  
  /**
   * 
   */
  public Test_U_API_XmlExporter_internal_groupToXML() {
    super();
  }

  /**
   * @param name
   */
  public Test_U_API_XmlExporter_internal_groupToXML(String name) {
    super(name);
  }

  // PRIVATE CLASS VARIABLES //
  private Group           child;
  private GrouperSession  s;
  private Stem            parent;
  private XmlExporter     export;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      child   = parent.addChildGroup("parent > child", "parent > child");
      export  = new XmlExporter( s, new java.util.Properties() );
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  public void tearDown() {
    try {
      s.stop();
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
    super.tearDown();
  }


  // TESTS //

  /**
   * Verify <i>name</i> is escaped in output.
   * @since   1.2.0
   */
  public void test_internal_groupToXML_escapeName() {
    String xml = export.internal_groupToXML(child, false);
    String pat = "^(?s).*\\sname='parent:parent &gt; child'.*$";
    assertTrue( "name escaped", Pattern.matches(pat, xml) );
  }
    
  /**
   * Verify <i>displayName</i> is escaped in output.
   * @since   1.2.0
   */
  public void test_internal_groupToXML_escapeDisplayName() {
    String xml = export.internal_groupToXML(child, false);
    String pat = "^(?s).*\\sdisplayName='parent:parent &gt; child'.*$";
    assertTrue( "displayName escaped: '" + xml + "'", Pattern.matches(pat, xml) );
  }

} 

