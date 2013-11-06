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

package edu.internet2.middleware.grouper;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem.java,v 1.34 2009-12-07 07:31:09 mchyzer Exp $
 */
public class TestGroupTypeTuple extends GrouperTest {

  /** */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestGroupTypeTuple.class);

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupTypeTuple("testXmlDifferentUpdateProperties"));
    //TestRunner.run(TestGroupTypeTuple.class);
  }

  /**
   * 
   * @param name
   */
  public TestGroupTypeTuple(String name) {
    super(name);
  }
  
  /**
   * make an example group type tuple for testing
   * @return an example group type tuple
   */
  public static GroupTypeTuple exampleGroupTypeTuple() {
    GroupTypeTuple groupTypeTuple = new GroupTypeTuple();
    groupTypeTuple.setContextId("contextId");
    groupTypeTuple.setGroupUuid("groupId");
    groupTypeTuple.setHibernateVersionNumber(3L);
    groupTypeTuple.setTypeUuid("typeId");
    groupTypeTuple.setId("uuid");
    return groupTypeTuple;
  }
  
}

