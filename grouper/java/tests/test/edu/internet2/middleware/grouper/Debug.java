/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;


/**
 * A class for spewing.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Debug.java,v 1.1 2005-12-02 17:17:01 blair Exp $
 */
class Debug {

  protected static void spewMembers(Group g) {
    try {
      Set imm = g.getImmediateMemberships();
      Set eff = g.getEffectiveMemberships();
      System.err.println(g.getName() + " imm: " + imm.size());
      System.err.println(g.getName() + " eff: " + eff.size());
      Iterator immIter = imm.iterator();
      while (immIter.hasNext()) {
        Membership ms = (Membership) immIter.next();
        System.err.println(
          "imm.g="+ms.getGroup().getUuid()+"/"+ms.getGroup().getName()
        );
        System.err.println(
          "imm.s="+ms.getMember().getUuid()+"/"+ms.getMember().getSubject().getName()
        );
        System.err.println("imm.f="+ms.getList().getName());
        System.err.println("imm.d="+ms.getDepth());    
      }
      Iterator effIter = eff.iterator();
      while (effIter.hasNext()) {
        Membership ms = (Membership) effIter.next();
        System.err.println(
          "eff.g="+ms.getGroup().getUuid()+"/"+ms.getGroup().getName()
        );
        System.err.println(
          "eff.s="+ms.getMember().getUuid()+"/"+ms.getMember().getSubject().getName()
        );
        System.err.println("eff.f="+ms.getList().getName());
        System.err.println("eff.d="+ms.getDepth());    
        System.err.println(
          "eff.v="+ms.getViaGroup().getUuid()+"/"+ms.getViaGroup().getName()
        );
      }
    }
    catch (Exception e) {
      throw new RuntimeException("error spewing members: " + e.getMessage());
    }
  }

}

