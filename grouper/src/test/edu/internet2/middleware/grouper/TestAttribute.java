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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestComposite.java,v 1.2 2009-03-20 19:56:40 mchyzer Exp $
 */
public class TestAttribute extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestAttribute.class);
    TestRunner.run(new TestAttribute("testXmlDifferentUpdateProperties"));
  }
  
  /** */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestAttribute.class);

  /**
   * 
   * @param name
   */
  public TestAttribute(String name) {
    super(name);
  }

  /**
   * make an example attribute for testing
   * @return an example attribute
   */
  public static Attribute exampleAttribute() {
    Attribute attribute = new Attribute();
    attribute.setContextId("contextId");
    attribute.setGroupUuid("groupId");
    attribute.setHibernateVersionNumber(3L);
    attribute.setId("uuid");
    attribute.setValue("value");
    
    return attribute;
  }
  
  
  
}

