/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;

import  java.util.*;
import  junit.framework.*;


public class TestGroupsConvenience extends TestCase {

  private GrouperSession  s;
  private GrouperQuery    q;

  public TestGroupsConvenience(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    s = Constants.createSession();
    Constants.createGroups(s);
    Constants.addMembers(s);
  }

  protected void tearDown () {
    s.stop();
  }


  /*
   * TESTS
   */

  public void testGroupsConvenience() {
    Assert.assertNotNull("s != null", s);

    // name
    Assert.assertNotNull(
      "name !=null", 
      Constants.gA.getName()
    );
    Assert.assertTrue(
      "name",
      Constants.gA.getName().equals(
        Constants.gA.attribute("name").value()
      )
    );

    // stem
    Assert.assertNotNull(
      "stem !=null", 
      Constants.gA.getStem()
    );
    Assert.assertTrue(
      "stem",
      Constants.gA.getStem().equals(
        Constants.gA.attribute("stem").value()
      )
    );
  
    // extension
    Assert.assertNotNull(
      "extn !=null", 
      Constants.gA.getExtension()
    );
    Assert.assertTrue(
      "extn",
      Constants.gA.getExtension().equals(
        Constants.gA.attribute("extension").value()
      )
    );
    
    // displayName
    Assert.assertNotNull(
      "displayName !=null",
      Constants.gA.getDisplayName()
    );
    Assert.assertTrue(
      "displayName",
      Constants.gA.getDisplayName().equals(
        Constants.gA.attribute("displayName").value()
      )
    );

    // displayExtension
    Assert.assertNotNull(
      "displayExtn !=null",
      Constants.gA.getDisplayExtension()
    );
    Assert.assertTrue(
      "displayExtn",
      Constants.gA.getDisplayExtension().equals(
        Constants.gA.attribute("displayExtension").value()
      )
    );

    // members
    Assert.assertNotNull(
      "members != null",
      Constants.gA.getMembers()
    );
    Assert.assertTrue(
      "members=1",
      Constants.gA.getMembers().size() == 1
    );
    Assert.assertTrue(
      "members=listVals",
      Constants.gA.getMembers().size() ==
        Constants.gA.listVals().size()
    );
    Object m = Constants.gA.getMembers().get(0);
    Assert.assertNotNull("m != null", m);
    Assert.assertTrue(
      "m instanceof GrouperMember",
      m instanceof GrouperMember
    );
    GrouperList lv = (GrouperList) Constants.gA.listVals().get(0);
    Assert.assertTrue(
      "m=lv.member()",
      m.equals(lv.member())
    );

  }

}

