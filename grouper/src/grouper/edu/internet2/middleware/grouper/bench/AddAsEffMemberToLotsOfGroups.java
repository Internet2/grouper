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
import  edu.internet2.middleware.subject.*;      

/**
 * Benchmarking adding a member to a group that is privileged on a lot of other
 * groups.
 * @author  blair christensen.
 * @version $Id: AddAsEffMemberToLotsOfGroups.java,v 1.4 2007-01-04 17:17:45 blair Exp $
 * @since   1.2.0
 */
public class AddAsEffMemberToLotsOfGroups extends BaseGrouperBenchmark {

  // PRIVATE CLASS CONSTANTS //
  private static final int CNT = 15; // i think this blows up quickly just like `MemberHasRead`


  // PRIVATE INSTANCE VARIABLES //
  Group   g;
  Subject subj;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddAsEffMemberToLotsOfGroups();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddAsEffMemberToLotsOfGroups() {
    super();
  } // protected AddAsEffMemberToLotsOfGroups()

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
      this.g                = ns.addChildGroup("target group", "target group");
      Subject         tmpS  = this.g.toSubject();
      Group           tmpG;
      for (int i=0; i < CNT; i++) {
        tmpG = ns.addChildGroup("group " + i, "group " + i);
        tmpG.grantPriv(tmpS, AccessPrivilege.ADMIN);
      }
      String subjectId = "AddAsEffMemberToLotsOfGroups";
      HibernateSubject.add(s, subjectId, "person", subjectId + " Subject");
      this.subj = SubjectFinder.findById(subjectId);
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
      this.g.addMember(this.subj);
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class AddAsEffMemberToLotsOfGroups

