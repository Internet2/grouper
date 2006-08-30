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
 * Benchmark adding a {@link HibernateSubject}.
 * @author  blair christensen.
 * @version $Id: AddHibernateSubject.java,v 1.3 2006-08-30 18:35:38 blair Exp $
 * @since   1.1.0
 */
public class AddHibernateSubject extends BaseGrouperBenchmark {

  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddHibernateSubject();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddHibernateSubject() {
    super();
  } // protected AddHibernateSubject()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void run() 
    throws GrouperRuntimeException 
  {
    try {
      HibernateSubject.add("subj0", "person", "subject 0");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class AddHibernateSubject

