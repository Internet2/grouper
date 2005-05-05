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


public class Constants {

  /*
   * PUBLIC CLASS CONSTANTS
   */
  // TODO Move to within Grouper itself?
  public static final String KLASS_GAI    = "edu.internet2.middleware.grouper.GrouperAccessImpl";
  public static final String KLASS_GA     = "edu.internet2.middleware.grouper.GrouperAttribute";
  public static final String KLASS_GE     = "edu.internet2.middleware.grouper.GrouperException";
  public static final String KLASS_GF     = "edu.internet2.middleware.grouper.GrouperField";
  public static final String KLASS_GG     = "edu.internet2.middleware.grouper.GrouperGroup";
  public static final String KLASS_GL     = "edu.internet2.middleware.grouper.GrouperList";
  public static final String KLASS_GM     = "edu.internet2.middleware.grouper.GrouperMember";
  public static final String KLASS_GNI    = "edu.internet2.middleware.grouper.GrouperNamingImpl";
  public static final String KLASS_GQ     = "edu.internet2.middleware.grouper.GrouperQuery";
  public static final String KLASS_GSC    = "edu.internet2.middleware.grouper.GrouperSchema";
  public static final String KLASS_GS     = "edu.internet2.middleware.grouper.GrouperSession";
  public static final String KLASS_GST    = "edu.internet2.middleware.grouper.GrouperStem";
  public static final String KLASS_GT     = "edu.internet2.middleware.grouper.GrouperType";
  public static final String KLASS_GTD    = "edu.internet2.middleware.grouper.GrouperTypeDef";
  public static final String KLASS_MV     = "edu.internet2.middleware.grouper.MemberVia";
  public static final String KLASS_NGA    = "edu.internet2.middleware.grouper.NullGrouperAttribute";
  public static final String KLASS_SI     = "edu.internet2.middleware.grouper.SubjectImpl";
  public static final String KLASS_STAGI  = "edu.internet2.middleware.grouper.SubjectTypeAdapterGroupImpl";
  public static final String KLASS_STAPI  = "edu.internet2.middleware.grouper.SubjectTypeAdapterPersonImpl";
  public static final String KLASS_STI    = "edu.internet2.middleware.grouper.SubjectTypeImpl";


  /*
   * PUBLIC MEMBER CONSTANTS
   */
  public static final String rootI  = Grouper.config("member.system");
  public static final String rootT  = Grouper.DEF_SUBJ_TYPE;
  public static final String mem0I  = "member 0";
  public static final String mem0T  = Grouper.DEF_SUBJ_TYPE;
  public static final String mem1I  = "member 1";
  public static final String mem1T  = Grouper.DEF_SUBJ_TYPE;


  /*
   * PUBLIC NS CONSTANTS
   */
  public static final String  ns0s  = Grouper.NS_ROOT;
  public static final String  ns0e  = "root";
  public static final String  ns1s  = GrouperGroup.groupName(ns0s, ns0e);
  public static final String  ns1e  = "a stem";
  public static final String  ns2s  = GrouperGroup.groupName(ns1s, ns1e);
  public static final String  ns2e  = "another stem";

  /*
   * PUBLIC GROUP CONSTANTS
   */
  public static final String  g0s = GrouperGroup.groupName(ns0s, ns0e);
  public static final String  g0e = "root group";
  public static final String  g1s = GrouperGroup.groupName(ns1s, ns1e);
  public static final String  g1e = "a group";
  public static final String  g2s = GrouperGroup.groupName(ns2s, ns2e);
  public static final String  g2e = "another group";
  public static final String  gAs = GrouperGroup.groupName(ns0s, ns0e);
  public static final String  gAe = "group a";
  public static final String  gBs = GrouperGroup.groupName(ns0s, ns0e);
  public static final String  gBe = "group b";
  public static final String  gCs = GrouperGroup.groupName(ns0s, ns0e);
  public static final String  gCe = "group c";
  public static final String  gDs = GrouperGroup.groupName(ns0s, ns0e);
  public static final String  gDe = "group d";

}

