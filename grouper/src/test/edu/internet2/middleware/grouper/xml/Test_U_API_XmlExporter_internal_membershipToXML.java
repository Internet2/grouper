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

package edu.internet2.middleware.grouper.xml;
import java.util.regex.Pattern;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * @author  blair christensen.
 * @version $Id: Test_U_API_XmlExporter_internal_membershipToXML.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_U_API_XmlExporter_internal_membershipToXML extends GrouperTest {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(Test_U_API_XmlExporter_internal_membershipToXML.class);
    //TestRunner.run(new Test_U_API_XmlExporter_internal_membershipToXML("test_internal_groupToXML_escapeDisplayName"));
  }

  // PRIVATE CLASS VARIABLES //
  private Subject         all;
  private Group           child;
  private Field           cList;
  private GroupType       cType;
  private XmlExporter     export;
  private Membership      ms;
  private Stem            parent;
  private GrouperSession  s;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {    
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      child   = parent.addChildGroup("parent > child", "parent > child");
      cType   = GroupType.createType(s, "custom type");
      child.addType(cType);
      cList   = cType.addList( s, "custom type > custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN ); 
      all     = SubjectFinder.findAllSubject();
      child.addMember(all, cList);
      ms      = MembershipFinder.findImmediateMembership( s, child, all, cList, true );
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
   * Verify list <i>name</i> is escaped in output.
   * @since   1.2.0
   */
  public void test_internal_membershipToXML_escapeName() {
    try {
      String xml = export.internal_membershipToXML(ms);
      String pat = "^(?s).*<listName>custom type &gt; custom list</listName>.*$";
      assertTrue( "list name escaped", Pattern.matches(pat, xml) );
    }
    catch (GroupNotFoundException eGNF) {
      fail( eGNF.getMessage() );
    }
    catch (MemberNotFoundException eMNF) {
      fail( eMNF.getMessage() );
    }
    catch (SubjectNotFoundException eSNF) {
      fail( eSNF.getMessage() );
    }
  }
    
} 

