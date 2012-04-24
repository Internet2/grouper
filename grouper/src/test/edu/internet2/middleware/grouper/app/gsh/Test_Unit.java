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

package edu.internet2.middleware.grouper.app.gsh;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @since   0.1.1
 */
public class Test_Unit extends GrouperTest {

  /** */
  private static final Log LOG = GrouperUtil.getLog(Test_Unit.class);


  /**
   * 
   */
  public void testUnit_SimpleCommandReader_Instantiate() {
    try {
      LOG.info("testUnit_SimpleCommandReader_Instantiate");
      CommandReader r = new SimpleCommandReader();
      assertNotNull("reader not null", r);
      assertTrue("reader instanceof CommandReader", r instanceof CommandReader);
      assertTrue("reader instanceof SimpleCommandReader", r instanceof SimpleCommandReader);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testUnit_SimpleCommandReader_Instantiate

  /**
   * 
   */
  public void testUnit_SimpleCommandReader_AddAndRemove() {
    try {
      LOG.info("testUnit_SimpleCommandReader_AddAndRemove");
      SimpleCommandReader r = new SimpleCommandReader();
      assertNull( "null top of queue", r.getNext() );
      String cmd0 = "first command";
      String cmd1 = "second command";
      r.add(cmd0);
      r.add(cmd1);
      assertEquals( "got first item added to queue", cmd0, r.getNext() );
      assertEquals( "got second item added to queue", cmd1, r.getNext() );
      assertNull( "queue is now empty again", r.getNext() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testUnit_SimpleCommandReader_AddAndRemove()

} // public class Test_Unit extends GrouperTest

