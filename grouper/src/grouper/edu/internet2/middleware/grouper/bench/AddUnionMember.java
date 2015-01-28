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
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.subject.Subject;

/**
 * Benchmark adding a union {@link Composite} {@link Membership}.
 * @author  blair christensen.
 * @version $Id: AddUnionMember.java,v 1.9 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.1.0
 */
public class AddUnionMember extends BaseGrouperBenchmark {

  // PRIVATE INSTANCE VARIABLES
  Group   g0, g1, g2;
  Subject subj0, subj1;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddUnionMember();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddUnionMember() {
    super();
  } // protected AddUnionMember()

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
      this.g0               = ns.addChildGroup("group 0", "group 0");
      this.g1               = ns.addChildGroup("group 1", "group 1");
      this.g2               = ns.addChildGroup("group 2", "group 2");
      RegistrySubject.add(s, "subj0", "person", "subject 0");
      RegistrySubject.add(s, "subj1", "person", "subject 1");
      this.subj0            = SubjectFinder.findById("subj0", true);
      this.subj1            = SubjectFinder.findById("subj1", true);
      this.g0.addMember(this.subj0);
      this.g1.addMember(this.subj1);
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
      this.g2.addCompositeMember(CompositeType.UNION, this.g0, this.g1);
    }
    catch (Exception e) {
      throw new GrouperException(e);
    }
  } // public void run()

} // public class AddUnionMember

