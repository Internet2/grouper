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


public class Util {

  /*
   * PUBLIC CLASS CONSTANTS
   */
  public static final String klassGG = "edu.internet2.middleware.grouper.GrouperGroup";
  public static final String klassSI = "edu.internet2.middleware.grouper.SubjectImpl";
  public static final String KLASS_GF = "edu.internet2.middleware.grouper.GrouperField";
  public static final String KLASS_MV = "edu.internet2.middleware.grouper.MemberVia";
  public static final String KLASS_VP = "edu.internet2.middleware.grouper.ViaPath";


  /*
   * PUBLIC MEMBER CONSTANTS
   */
  public static final String rooti  = Grouper.config("member.system");
  public static final String roott  = Grouper.DEF_SUBJ_TYPE;
  public static final String m0i    = "blair";
  public static final String m0t    = Grouper.DEF_SUBJ_TYPE;
  public static final String m1i    = "notblair";
  public static final String m1t    = Grouper.DEF_SUBJ_TYPE;

  /*
   * PUBLIC NS CONSTANTS
   */
  public static final String ns0s   = Grouper.NS_ROOT;
  public static final String ns0e   = "stem.0";
  public static final String ns00s  = "stem.0";
  public static final String ns00e  = "stem.0.0";
  public static final String ns1s   = Grouper.NS_ROOT;
  public static final String ns1e   = "stem.1";
  public static final String ns2s   = Grouper.NS_ROOT;
  public static final String ns2e   = "stem.2";


  /*
   * PUBLIC GROUP CONSTANTS
   */
  public static final String stem0  = "stem.0";
  public static final String extn0  = "extn.0";
  public static final String stem1  = "stem.1";
  public static final String extn1  = "extn.1";
  public static final String stem2  = "stem.2";
  public static final String extn2  = "extn.2";
  public static final String stem3  = "stem.0";
  public static final String extn3  = "extn.3";
  public static final String stem4  = "stem.0";
  public static final String extn4  = "extn.4";
  public static final String stem5  = "stem.0";
  public static final String extn5  = "ext:n.5";
  public static final String stem6  = "stem.0";
  public static final String extn6  = "extn.6";
  public static final String stem7  = "stem.0";
  public static final String extn7  = "extn.7";
  public static final String stem8  = "stem.0";
  public static final String extn8  = "extn.8";
  public static final String stem9  = "stem.1";
  public static final String extn9  = "extn.9";
  public static final String stem10 = "stem.1";
  public static final String extn10 = "extn.10";
  public static final String stem11 = "stem.1";
  public static final String extn11 = "extn.11";
  public static final String stem12 = "stem.1";
  public static final String extn12 = "extn.12";
  public static final String stem13 = "stem.1";
  public static final String extn13 = "extn.13";

}

