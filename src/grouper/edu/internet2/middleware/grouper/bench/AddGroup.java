/*
  Copyright 2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006 The University Of Chicago

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

package edu.internet2.middleware.grouper.bench;
import  edu.internet2.middleware.grouper.*;      

/**
 * Benchmark adding a {@link Group}.
 * @author  blair christensen.
 * @version $Id: AddGroup.java,v 1.2 2006-08-22 19:48:22 blair Exp $
 * @since   1.1.0
 */
public class AddGroup extends BaseGrouperBenchmark {

  // PRIVATE INSTANCE VARIABLES
  Stem parent;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddGroup();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddGroup() {
    super();
  } // protected AddGroup()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    try {
      Stem root = StemFinder.findRootStem(
        GrouperSession.start(
          SubjectFinder.findById("GrouperSystem")
        )
      );
      this.parent = root.addChildStem("example", "example");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e.getMessage());
    }
  } // public void init()

  /**
   * @since 1.1.0
   */
  public void run() 
    throws GrouperRuntimeException 
  {
    try {
      Group g = this.parent.addChildGroup("group", "group");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class AddGroup

