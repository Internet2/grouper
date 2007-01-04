/*
  Copyright (C) 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2006-2007 The University Of Chicago

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
 * Benchmark finding the root {@link Stem}.
 * @author  blair christensen.
 * @version $Id: FindRootStem.java,v 1.5 2007-01-04 17:17:45 blair Exp $
 * @since   1.1.0
 */
public class FindRootStem extends BaseGrouperBenchmark {

  // PRIVATE INSTANCE VARIABLES
  GrouperSession s;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new FindRootStem();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected FindRootStem() {
    super();
  } // protected FindRootStem()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    try {
      this.s = GrouperSession.start( SubjectFinder.findRootSubject() ); 
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
      StemFinder.findRootStem(this.s);
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class FindRootStem

