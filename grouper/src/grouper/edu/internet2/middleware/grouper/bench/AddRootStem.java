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
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;

/**
 * Benchmark adding a root-level {@link Stem}.
 * @author  blair christensen.
 * @version $Id: AddRootStem.java,v 1.8 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.1.0
 */
public class AddRootStem extends BaseGrouperBenchmark {

  // PRIVATE INSTANCE VARIABLES
  Stem root;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddRootStem();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddRootStem() {
    super();
  } // protected AddRootStem()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperException 
  {
    try {
      this.root = StemFinder.findRootStem(
        GrouperSession.start( SubjectFinder.findRootSubject() )
      );
    }
    catch (Exception e) {
      throw new GrouperException(e.getMessage());
    }
  } // public void init()

  /**
   * @since 1.1.0
   */
  public void run() 
    throws GrouperException 
  {
    try {
      this.root.addChildStem("example", "example");
    }
    catch (Exception e) {
      throw new GrouperException(e);
    }
  } // public void run()

} // public class AddRootStem

