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
    // Nothing -- Yet
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
    String klass = "edu.internet2.middleware.grouper.GrouperAccessImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperAttribute instance 
  public void testGrouperAttributeInstantiate() {
    GrouperAttribute obj = new GrouperAttribute();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperAttribute";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperField instance 
  public void testGrouperException() {
    GrouperException obj = new GrouperException();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperException";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperField instance 
  public void testGrouperFieldInstantiate() {
    GrouperField obj = new GrouperField();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperField";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperGroup instance 
  public void testGrouperGroupInstantiate() {
    GrouperGroup obj = new GrouperGroup();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperGroup";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperList instance 
  public void testGrouperListInstantiate() {
    GrouperList obj = new GrouperList();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperList";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperMember instance 
  public void testGrouperMemberInstantiate() {
    GrouperMember obj = new GrouperMember();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperNamingImpl instance 
  public void testGrouperNamingImplInstantiate() {
    GrouperNaming obj = new GrouperNamingImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperNamingImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSchema instance 
  public void testGrouperSchemaInstantiate() {
    GrouperSchema obj = new GrouperSchema();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperSchema";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSession instance 
  public void testGrouperSessionInstantiate() {
    GrouperSession obj = new GrouperSession();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperSession";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSubjectAttribute instance 
  public void testGrouperSubjectAttributeInstantiate() {
    GrouperSubjectAttribute obj = new GrouperSubjectAttribute();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperSubjectAttribute";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperType instance 
  public void testGrouperTypeInstantiate() {
    GrouperType obj = new GrouperType();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperType";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperTypeDef instance 
  public void testGrouperTypeDefInstantiate() {
    GrouperTypeDef obj = new GrouperTypeDef();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperTypeDef";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectImpl instance 
  public void testGrouperSubjectImplInstantiate() {
    SubjectImpl obj = new SubjectImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeAdapterGroupImpl instance 
  public void testSubjectTypeAdapterGroupImplInstantiate() {
    SubjectTypeAdapterGroupImpl obj = new SubjectTypeAdapterGroupImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectTypeAdapterGroupImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeAdapterPersonImpl instance 
  public void testSubjectTypeAdapterPersonImplInstantiate() {
    SubjectTypeAdapterPersonImpl obj = new SubjectTypeAdapterPersonImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectTypeAdapterPersonImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeImpl instance 
  public void testSubjectTypeImplInstantiate() {
    SubjectTypeImpl obj = new SubjectTypeImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectTypeImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a ViaElement instance 
  public void testViaElementInstantiate() {
    ViaElement obj = new ViaElement();
    Assert.assertNotNull("obj !null", obj);
    Assert.assertTrue(
                      "correct class",
                      Util.KLASS_VE.equals( obj.getClass().getName() ) 
                     );
  }

}

