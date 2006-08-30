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
 * Benchmark adding 100 effective {@link Membership}s.
 * @author  blair christensen.
 * @version $Id: Add100EffMembers.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 * @since   1.1.0
 */
public class Add100EffMembers extends BaseGrouperBenchmark {

  // PRIVATE CLASS CONSTANTS //
  private static final int CNT = 100;


  // PRIVATE INSTANCE VARIABLES //
  Group     g0, g1;
  Subject   g_subj;
  Subject[] subjects  = new Subject[CNT];


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new Add100EffMembers();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected Add100EffMembers() {
    super();
  } // protected Add100EffMembers()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    try {
      Stem root   = StemFinder.findRootStem(
        GrouperSession.start( SubjectFinder.findRootSubject() )
      );
      Stem    ns    = root.addChildStem("example", "example");
      this.g0       = ns.addChildGroup("group 0", "group 0");
      this.g1       = ns.addChildGroup("group 1", "group 1");
      String  type  = "person";
      for (int i=0; i < CNT; i++) {
        String id = "subj" + i;
        HibernateSubject.add(id, type, "subject " + i);
        subjects[i] = SubjectFinder.findById(id);
        this.g0.addMember( subjects[i] );
      }
      this.g_subj   = this.g0.toSubject();
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
      this.g1.addMember(this.g_subj);
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class Add100EffMembers

