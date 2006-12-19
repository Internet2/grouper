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
import  edu.internet2.middleware.subject.*;      

/**
 * Add a number of groups beneath a single stem.
 * @author  blair christensen.
 * @version $Id: AddLotsOfGroupsToStem.java,v 1.2 2006-12-19 13:32:35 blair Exp $
 * @since   1.2.0
 */
public class AddLotsOfGroupsToStem extends BaseGrouperBenchmark {

  // PRIVATE CLASS CONSTANTS //
  private static final int CNT = 10; // i think this blows up quickly just like `MemberHasRead`


  // PRIVATE INSTANCE VARIABLES //
  Stem ns;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddLotsOfGroupsToStem();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddLotsOfGroupsToStem() {
    super();
  } // protected AddLotsOfGroupsToStem()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    try {
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Stem            root  = StemFinder.findRootStem(s);
      this.ns               = root.addChildStem("example", "example");
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
      for (int i=0; i < CNT; i++) {
        this.ns.addChildGroup("group " + i, "group " + i);
      }
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class AddLotsOfGroupsToStem

