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
 * Benchmark <code>Member.hasRead()</code> when <i>GrouperAll</i> has a large
 * number of memberships.
 * @author  blair christensen.
 * @version $Id: MemberHasRead.java,v 1.5 2007-03-06 17:02:43 blair Exp $
 * @since   1.2.0
 */
public class MemberHasRead extends BaseGrouperBenchmark {

  // PRIVATE CLASS CONSTANTS //
  private static final int CNT = 50; // this seems to blow up easily


  // PRIVATE INSTANCE VARIABLES //
  Member m;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new MemberHasRead();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected MemberHasRead() {
    super();
  } // protected MemberHasRead()

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
      Stem            ns    = root.addChildStem("example", "example");
      for (int i=0; i < CNT; i++) {
        ns.addChildGroup("group " + i, "group " + i);
      }
      String subjectId = "MemberHasRead";
      RegistrySubject.add(s, subjectId, "person", subjectId + " Subject");
      this.m = MemberFinder.findBySubject( s, SubjectFinder.findById(subjectId) );
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
      this.m.hasRead();
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class MemberHasRead

