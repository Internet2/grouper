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
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;

/**
 * Benchmark adding an immediate {@link Membership}.
 * 
 * An immediate member is directly assigned to a group.
 * A composite group has no immediate members.  Note that a 
 * member can have 0 to 1 immediate memberships
 * to a single group, and 0 to many effective memberships to a group.
 * A group can have potentially unlimited effective 
 * memberships
 * 
 * @author  blair christensen.
 * @version $Id: AddImmMember.java,v 1.11 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.1.0
 */
public class AddImmMember extends BaseGrouperBenchmark {

  // PRIVATE INSTANCE VARIABLES
  Group   g;
  Subject subj;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddImmMember();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddImmMember() {
    super();
  } // protected AddImmMember()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperException 
  {
    try {
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Stem            root  = StemFinder.findRootStem(s);
      Stem            ns    = root.addChildStem("example", "example");
      this.g                = ns.addChildGroup("group", "group");
      RegistrySubject.add(s, "subj0", "person", "subject0");
      this.subj             = SubjectFinder.findById("subj0", true);
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
      this.g.addMember(this.subj);
    }
    catch (Exception e) {
      throw new GrouperException(e);
    }
  } // public void run()

} // public class AddImmMember

