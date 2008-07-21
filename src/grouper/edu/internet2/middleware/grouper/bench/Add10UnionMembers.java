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
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import  edu.internet2.middleware.subject.*;      

/**
 * Benchmark adding 10 union {@link Composite} {@link Membership}s.
 * @author  blair christensen.
 * @version $Id: Add10UnionMembers.java,v 1.5 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.1.0
 */
public class Add10UnionMembers extends BaseGrouperBenchmark {

  // PRIVATE CLASS CONSTANTS //
  private static final int CNT = 10;


  // PRIVATE INSTANCE VARIABLES
  Group     g0, g1, g2;
  Subject[] subjects    = new Subject[CNT];


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new Add10UnionMembers();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected Add10UnionMembers() {
    super();
  } // protected Add10UnionMembers()

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
      this.g0               = ns.addChildGroup("group 0", "group 0");
      this.g1               = ns.addChildGroup("group 1", "group 1");
      this.g2               = ns.addChildGroup("group 2", "group 2");
      String          type  = "person";
      for (int i=0; i < CNT; i++) {
        String id = "subj" + i;
        RegistrySubject.add(s, id, type, "subject " + i);
        subjects[i] = SubjectFinder.findById(id);
        // add half to each group
        if (i % 2 == 0) {
          this.g0.addMember( subjects[i] );
        }
        else {
          this.g1.addMember( subjects[i] );
        }
      }
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
      this.g2.addCompositeMember(CompositeType.UNION, this.g0, this.g1);
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class Add10UnionMembers

