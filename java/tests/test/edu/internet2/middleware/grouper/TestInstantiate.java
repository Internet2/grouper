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
import  junit.framework.*;


public class TestInstantiate extends TestCase {

  public TestInstantiate(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
  }

  protected void tearDown () {
    // Nothing -- Yet
  }


  /*
   * TESTS
   */
  

  // Instantiate a GrouperAccessImpl instance 
  public void testGrouperAccessImplInstantiate() {
    GrouperAccess obj = new GrouperAccessImpl();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GAI.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperAttribute instance 
  public void testGrouperAttributeInstantiate() {
    GrouperAttribute obj = new GrouperAttribute();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GA.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperField instance 
  public void testGrouperException() {
    GrouperException obj = new GrouperException();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GE.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperField instance 
  public void testGrouperFieldInstantiate() {
    GrouperField obj = new GrouperField();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GF.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperGroup instance 
  public void testGrouperGroupInstantiate() {
    GrouperGroup obj = new GrouperGroup();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GG.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperList instance 
  public void testGrouperListInstantiate() {
    GrouperList obj = new GrouperList();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperMember instance 
  public void testGrouperMemberInstantiate() {
    GrouperMember obj = new GrouperMember();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GM.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperNamingImpl instance 
  public void testGrouperNamingImplInstantiate() {
    GrouperNaming obj = new GrouperNamingImpl();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GNI.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSchema instance 
  public void testGrouperSchemaInstantiate() {
    GrouperSchema obj = new GrouperSchema();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GSC.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSession instance 
  public void testGrouperSessionInstantiate() {
    GrouperSession obj = new GrouperSession();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GS.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperStem instance 
  public void testGrouperStemInstantiate() {
    GrouperStem obj = new GrouperStem();
    Assert.assertNotNull("obj !null", obj);
    Assert.assertTrue(
      "obj right class",  
      Constants.KLASS_GST.equals( obj.getClass().getName() ) 
    );
  }

  // Instantiate a GrouperType instance 
  public void testGrouperTypeInstantiate() {
    GrouperType obj = new GrouperType();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GT.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperTypeDef instance 
  public void testGrouperTypeDefInstantiate() {
    GrouperTypeDef obj = new GrouperTypeDef();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_GTD.equals( obj.getClass().getName() ) );
  }

  // Instantiate a MemberVia instance 
  public void testMemberViaInstantiate() {
    MemberVia obj = new MemberVia();
    Assert.assertNotNull("obj !null", obj);
    Assert.assertTrue(
                      "correct class",
                      Constants.KLASS_MV.equals( obj.getClass().getName() ) 
                     );
  }

  // Instantiate a SubjectImpl instance 
  public void testGrouperSubjectImplInstantiate() {
    SubjectImpl obj = new SubjectImpl();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_SI.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeAdapterGroupImpl instance 
  public void testSubjectTypeAdapterGroupImplInstantiate() {
    SubjectTypeAdapterGroupImpl obj = new SubjectTypeAdapterGroupImpl();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_STAGI.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeAdapterPersonImpl instance 
  public void testSubjectTypeAdapterPersonImplInstantiate() {
    SubjectTypeAdapterPersonImpl obj = new SubjectTypeAdapterPersonImpl();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_STAPI.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeImpl instance 
  public void testSubjectTypeImplInstantiate() {
    SubjectTypeImpl obj = new SubjectTypeImpl();
    Assert.assertNotNull(obj);
    Assert.assertTrue( Constants.KLASS_STI.equals( obj.getClass().getName() ) );
  }

}

