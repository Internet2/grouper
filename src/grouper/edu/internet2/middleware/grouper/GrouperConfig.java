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

package edu.internet2.middleware.grouper;


import  java.io.*;
import  java.util.*;
import  org.apache.commons.lang.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper configuration information.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.12 2006-01-18 20:23:29 blair Exp $
 *     
*/
public class GrouperConfig {

  // Protected Class Constants
  protected static final String ALL     = "GrouperAll";
  protected static final String BT      = "true";
  protected static final String GWG     = "groups.wheel.group";
  protected static final String GWU     = "groups.wheel.use";
  protected static final String IST     = "application";
  protected static final String MSLGEA  = "memberships.log.group.effective.add";
  protected static final String MSLGED  = "memberships.log.group.effective.del";
  protected static final String MSLSEA  = "memberships.log.stem.effective.add";
  protected static final String MSLSED  = "memberships.log.stem.effective.del";
  protected static final String PAI     = "privileges.access.interface";
  protected static final String PNI     = "privileges.naming.interface";
  protected static final String ROOT    = "GrouperSystem";

  // Protected Class Constants - Queries
  protected static final String   QCR_FF_FA     = "edu.internet2.middleware.grouper.FieldFinder.FindAll";
  protected static final boolean  QRY_FF_FA     = false;
  protected static final String   QCR_FF_FABT   = "edu.internet2.middleware.grouper.FieldFinder.FindAllByType";
  protected static final boolean  QRY_FF_FABT   = false;
  protected static final String   QCR_GF_FBAA   = "edu.internet2.middleware.grouper.GroupFinder.FindByApproximateAttr";
  protected static final boolean  QRY_GF_FBAA   = false;
  protected static final String   QCR_GF_FBAN   = "edu.internet2.middleware.grouper.GroupFinder.FindByApproximateName";
  protected static final boolean  QRY_GF_FBAN   = false;
  protected static final String   QCR_GF_FBAAA  = "edu.internet2.middleware.grouper.GroupFinder.FindByAnyApproximateAttr";
  protected static final boolean  QRY_GF_FBAAA  = false;
  protected static final String   QCR_GF_FBCA   = "edu.internet2.middleware.grouper.GroupFinder.FindByCreatedAfter";
  protected static final boolean  QRY_GF_FBCA   = false;
  protected static final String   QCR_GF_FBCB   = "edu.internet2.middleware.grouper.GroupFinder.FindByCreatedBefore";
  protected static final boolean  QRY_GF_FBCB   = false;
  protected static final String   QCR_GF_FBN    = "edu.internet2.middleware.grouper.GroupFinder.FindByName";
  protected static final boolean  QRY_GF_FBN    = false;
  protected static final String   QCR_GF_FBU    = "edu.internet2.middleware.grouper.GroupFinder.FindByUuid";
  protected static final boolean  QRY_GF_FBU    = false;
  protected static final String   QCR_GTF_FA    = "edu.internet2.middleware.grouper.GroupTypeFinder.FindAll";
  protected static final boolean  QRY_GTF_FA    = false; 
  protected static final String   QCR_MF_FBS    = "edu.internet2.middleware.grouper.MemberFinder.FindBySubject";
  protected static final boolean  QRY_MF_FBS    = false;
  protected static final String   QCR_MF_FBU    = "edu.internet2.middleware.grouper.MemberFinder.FindByUuid";
  protected static final boolean  QRY_MF_FBU    = false;
  protected static final String   QCR_MSF_FAM    = "edu.internet2.middleware.grouper.MembershipFinder.FindAllMemberships";
  protected static final boolean  QRY_MSF_FAM   = false;
  protected static final String   QCR_MSF_FCM   = "edu.internet2.middleware.grouper.MembershipFinder.FindChildMemberships";
  protected static final boolean  QRY_MSF_FCM   = false;
  protected static final String   QCR_MSF_FEM   = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMemberships";
  protected static final boolean  QRY_MSF_FEM   = false;
  protected static final String   QCR_MSF_FEMG  = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsGroup";
  protected static final boolean  QRY_MSF_FEMG  = false;
  protected static final String   QCR_MSF_FEMM  = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsMember";
  protected static final boolean  QRY_MSF_FEMM  = false;
  protected static final String   QCR_MSF_FEMOM = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsOwnerMember";
  protected static final boolean  QRY_MSF_FEMOM = false;
  protected static final String   QCR_MSF_FIM   = "edu.internet2.middleware.grouper.MembershipFinder.FindImmediateMemberships";
  protected static final boolean  QRY_MSF_FIM   = false;
  protected static final String   QCR_MSF_FIMM  = "edu.internet2.middleware.grouper.MembershipFinder.FindImmediateMembershipsMember";
  protected static final boolean  QRY_MSF_FIMM  = false;
  protected static final String   QCR_MSF_FIMO  = "edu.internet2.middleware.grouper.MembershipFinder.FindImmediateMembershipsOwner";
  protected static final boolean  QRY_MSF_FIMO  = false;
  protected static final String   QCR_MSF_FM    = "edu.internet2.middleware.grouper.MembershipFinder.FindMemberships";
  protected static final boolean  QRY_MSF_FM    = false;
  protected static final String   QCR_MSF_FMO   = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner";
  protected static final boolean  QRY_MSF_FMO   = false;
  protected static final String   QCR_MSF_FMOM  = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwnerMember";
  protected static final boolean  QRY_MSF_FMOM  = false;
  protected static final String   QCR_SF_FBAN   = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateName";
  protected static final boolean  QRY_SF_FBAN   = false;
  protected static final String   QCR_SF_FBCA   = "edu.internet2.middleware.grouper.StemFinder.FindByCreatedAfter";
  protected static final boolean  QRY_SF_FBCA   = false;
  protected static final String   QCR_SF_FBCB   = "edu.internet2.middleware.grouper.StemFinder.FindByCreatedBefore";
  protected static final boolean  QRY_SF_FBCB   = false;
  protected static final String   QCR_SF_FBN    = "edu.internet2.middleware.grouper.StemFinder.FindByName";
  protected static final boolean  QRY_SF_FBN    = false;
  protected static final String   QCR_SF_FBU    = "edu.internet2.middleware.grouper.StemFinder.FindByUuid";
  protected static final boolean  QRY_SF_FBU    = false;


  // Private Class Constants
  private static final String CF      = "/grouper.properties";
  private static final String ERR_GC  = "unable to read grouper configuration file: ";
  private static final Log    LOG     = LogFactory.getLog(GrouperConfig.class);


  // Private Class Variables
  private static GrouperConfig cfg;


  // Private Instance Variables
  private Properties properties = new Properties();


  // Constructors
  private GrouperConfig() {
    // nothing
  } // private GrouperConfig()


  // Public Class Methods

  /**
   * Get Grouper configuration instance.
   * @return  {@link GrouperConfig} singleton.
   */
  public static GrouperConfig getInstance() {
    if (cfg == null) {
      cfg = _getConfiguration();
    }
    return cfg;
  } // public static GrouperConfig getInstance()


  // Public Instance Methods

  /**
   * Get a Grouper configuration parameter.
   * @return  Value of configuration parameter or an empty string if
   *   parameter is invalid.
   */
  public String getProperty(String property) {
    String value = new String();
    if ( (property != null) && (this.properties.containsKey(property)) ) {
      value = StringUtils.strip( this.properties.getProperty(property) );
    }
    return value;
  } // public String getProperty(property)


  // Private Class Methods
  private static GrouperConfig _getConfiguration() {
    InputStream in = GrouperConfig.class.getResourceAsStream(CF);
    try {
      cfg = new GrouperConfig();
      cfg.properties.load(in);
    }
    catch (IOException eIOE) {
      String err = ERR_GC + eIOE.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
    return cfg;     
  } // private static GrouperConfig _getConfiguration()

}

