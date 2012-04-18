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

package edu.internet2.middleware.grouper.filter;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * Test {@link ChildGroupFilter}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_ChildGroupFilter.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_api_ChildGroupFilter extends GrouperTest {


  private GrouperSession  s;
  private Stem            child, root, top;

  /**
   * 
   */
  public Test_api_ChildGroupFilter() {
    super();
  }
  
  /**
   * @param name
   */
  public Test_api_ChildGroupFilter(String name) {
    super(name);
  }


  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new TestGroup0("testStaticSaveGroupTransactions"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
    TestRunner.run(new Test_api_ChildGroupFilter("test_getResults_fromChild"));

    //TestRunner.run(new Test_api_ChildGroupFilter("test_Constructor_nullStem"));
  }

  /** keep original size before addition */
  private int originalRootSize = -1;
  
  public void setUp() {
    super.setUp();
    try {
      this.s            = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.root         = StemFinder.findRootStem(this.s);
      
      this.originalRootSize = new ChildGroupFilter(this.root).getResults(this.s).size();
      
      this.top          = this.root.addChildStem("top", "top");
      this.top.addChildGroup("top group", "top group");
      this.child        = this.top.addChildStem("child", "child");
      this.child.addChildGroup("child group", "child group");
    }
    catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }


  public void test_Constructor_nullStem() {
    try {
      new ChildGroupFilter(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_getResults_nullSession() 
    throws  QueryException
  {
    try {
      new ChildGroupFilter(this.root).getResults(null);
      fail("failed to throw IllegalStateException");
    }
    catch (IllegalStateException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getResults_fromRoot() 
    throws  QueryException
  {
    assertEquals( this.originalRootSize + 2, new ChildGroupFilter(this.root).getResults(this.s).size() );
  }

  public void test_getResults_fromTop() 
    throws  QueryException
  {
    assertEquals( 2, new ChildGroupFilter(this.top).getResults(this.s).size() );
  }

  public void test_getResults_fromChild() 
    throws  QueryException
  {
    assertEquals( 1, new ChildGroupFilter(this.child).getResults(this.s).size() );
  }

}

