/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

/** 
 * Grouper configuration information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.23 2006-06-15 04:45:58 blair Exp $
 */
public class GrouperConfig {

  // PUBLIC CLASS CONSTANTS //
  
  /**
   * Grouper configuration file.
   */
  public static final String GROUPER_CF   = "/grouper.properties";
  /**
   * Hibernate configuration file.
   */
  public static final String HIBERNATE_CF = "/grouper.hibernate.properties";


  // PROTECTED CLASS CONSTANTS //
  protected static final String ALL           = "GrouperAll";
  protected static final String BT            = "true";
  protected static final String EMPTY_STRING  = "";
  protected static final String GWG           = "groups.wheel.group";
  protected static final String GWU           = "groups.wheel.use";
  protected static final String IST           = "application";
  protected static final String LIST          = "members";
  protected static final String MSLGEA        = "memberships.log.group.effective.add";
  protected static final String MSLGED        = "memberships.log.group.effective.del";
  protected static final String MSLSEA        = "memberships.log.stem.effective.add";
  protected static final String MSLSED        = "memberships.log.stem.effective.del";
  protected static final String PAI           = "privileges.access.interface";
  protected static final String PNI           = "privileges.naming.interface";
  protected static final String ROOT          = "GrouperSystem";

  // PROTECTED CLASS CONSTANTS -- QUERIES //
  // FIXME I really hate all of these
  protected static final String   QCR_CF_IF     = "edu.internet2.middleware.grouper.CompositeFinder.IsFactor";
  protected static final boolean  QRY_CF_IF     = true;
  protected static final String   QCR_CF_IO     = "edu.internet2.middleware.grouper.CompositeFinder.IsOwner";
  protected static final boolean  QRY_CF_IO     = true;
  protected static final String   QCR_FF_FA     = "edu.internet2.middleware.grouper.FieldFinder.FindAll";
  protected static final boolean  QRY_FF_FA     = true;
  protected static final String   QCR_FF_FABT   = "edu.internet2.middleware.grouper.FieldFinder.FindAllByType";
  protected static final boolean  QRY_FF_FABT   = true;
  protected static final String   QCR_GF_FBAA   = "edu.internet2.middleware.grouper.GroupFinder.FindByApproximateAttr";
  protected static final boolean  QRY_GF_FBAA   = true;
  protected static final String   QCR_GF_FBAN   = "edu.internet2.middleware.grouper.GroupFinder.FindByApproximateName";
  protected static final boolean  QRY_GF_FBAN   = true;
  protected static final String   QCR_GF_FBAAA  = "edu.internet2.middleware.grouper.GroupFinder.FindByAnyApproximateAttr";
  protected static final boolean  QRY_GF_FBAAA  = true;
  protected static final String   QCR_GF_FBCA   = "edu.internet2.middleware.grouper.GroupFinder.FindByCreatedAfter";
  protected static final boolean  QRY_GF_FBCA   = true;
  protected static final String   QCR_GF_FBCB   = "edu.internet2.middleware.grouper.GroupFinder.FindByCreatedBefore";
  protected static final boolean  QRY_GF_FBCB   = true;
  protected static final String   QCR_GF_FBN    = "edu.internet2.middleware.grouper.GroupFinder.FindByName";
  protected static final boolean  QRY_GF_FBN    = true;
  protected static final String   QCR_GF_FBU    = "edu.internet2.middleware.grouper.GroupFinder.FindByUuid";
  protected static final boolean  QRY_GF_FBU    = true;
  protected static final String   QCR_GTF_FA    = "edu.internet2.middleware.grouper.GroupTypeFinder.FindAll";
  protected static final boolean  QRY_GTF_FA    = false; 
  protected static final String   QCR_MF_FBS    = "edu.internet2.middleware.grouper.MemberFinder.FindBySubject";
  protected static final boolean  QRY_MF_FBS    = true;
  protected static final String   QCR_MF_FBU    = "edu.internet2.middleware.grouper.MemberFinder.FindByUuid";
  protected static final boolean  QRY_MF_FBU    = true;
  protected static final String   QCR_MSF_FAM    = "edu.internet2.middleware.grouper.MembershipFinder.FindAllMemberships";
  protected static final boolean  QRY_MSF_FAM   = true;
  protected static final String   QCR_MSF_FBU   = "edu.internet2.middleware.grouper.MembershipFinder.FindByUuid";
  protected static final boolean  QRY_MSF_FBU   = true;
  protected static final String   QCR_MSF_FCM   = "edu.internet2.middleware.grouper.MembershipFinder.FindChildMemberships";
  protected static final boolean  QRY_MSF_FCM   = true;
  protected static final String   QCR_MSF_FEM   = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMemberships";
  protected static final boolean  QRY_MSF_FEM   = true;
  protected static final String   QCR_MSF_FEMM  = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsMember";
  protected static final boolean  QRY_MSF_FEMM  = true;
  protected static final String   QCR_MSF_FEMO  = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsOwner";
  protected static final boolean  QRY_MSF_FEMO  = true;
  protected static final String   QCR_MSF_FEMOM = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsOwnerMember";
  protected static final boolean  QRY_MSF_FEMOM = true;
  protected static final String   QCR_MSF_FIM   = "edu.internet2.middleware.grouper.MembershipFinder.FindImmediateMemberships";
  protected static final boolean  QRY_MSF_FIM   = true;
  protected static final String   QCR_MSF_FIMM  = "edu.internet2.middleware.grouper.MembershipFinder.FindImmediateMembershipsMember";
  protected static final boolean  QRY_MSF_FIMM  = true;
  protected static final String   QCR_MSF_FIMO  = "edu.internet2.middleware.grouper.MembershipFinder.FindImmediateMembershipsOwner";
  protected static final boolean  QRY_MSF_FIMO  = true;
  protected static final String   QCR_MSF_FM    = "edu.internet2.middleware.grouper.MembershipFinder.FindMemberships";
  protected static final boolean  QRY_MSF_FM    = true;
  protected static final String   QCR_MSF_FMO   = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner";
  protected static final boolean  QRY_MSF_FMO   = true;
  protected static final String   QCR_MSF_FMOM  = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwnerMember";
  protected static final boolean  QRY_MSF_FMOM  = true;
  protected static final String   QCR_SF_FBADE  = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateDisplayExtension";
  protected static final boolean  QRY_SF_FBADE  = true;
  protected static final String   QCR_SF_FBADN  = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateDisplayName";
  protected static final boolean  QRY_SF_FBADN  = true;
  protected static final String   QCR_SF_FBAE   = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateExtension";
  protected static final boolean  QRY_SF_FBAE   = true;
  protected static final String   QCR_SF_FBAN   = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateName";
  protected static final boolean  QRY_SF_FBAN   = true;
  protected static final String   QCR_SF_FBANA  = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateNameAny";
  protected static final boolean  QRY_SF_FBANA  = true;
  protected static final String   QCR_SF_FBCA   = "edu.internet2.middleware.grouper.StemFinder.FindByCreatedAfter";
  protected static final boolean  QRY_SF_FBCA   = true;
  protected static final String   QCR_SF_FBCB   = "edu.internet2.middleware.grouper.StemFinder.FindByCreatedBefore";
  protected static final boolean  QRY_SF_FBCB   = true;
  protected static final String   QCR_SF_FBN    = "edu.internet2.middleware.grouper.StemFinder.FindByName";
  protected static final boolean  QRY_SF_FBN    = true;
  protected static final String   QCR_SF_FBU    = "edu.internet2.middleware.grouper.StemFinder.FindByUuid";
  protected static final boolean  QRY_SF_FBU    = true;


  // PRIVATE CLASS VARIABLES //
  private static GrouperConfig cfg;


  // PRIVATE INSTANCE VARIABLES //
  private Properties properties = new Properties();


  // CONSTRUCTORS //
  private GrouperConfig() {
    // nothing
  } // private GrouperConfig()


  // PUBLIC CLASS METHODS //

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


  // PUBLIC INSTANCE METHODS //

  /**
   * Get a Grouper configuration parameter.
   * @return  Value of configuration parameter or an empty string if
   *   parameter is invalid.
   */
  public String getProperty(String property) {
    String value = GrouperConfig.EMPTY_STRING;
    if ( (property != null) && (this.properties.containsKey(property)) ) {
      value = StringUtils.strip( this.properties.getProperty(property) );
    }
    return value;
  } // public String getProperty(property)


  // PRIVATE CLASS METHODS //
  private static GrouperConfig _getConfiguration() {
    InputStream in = GrouperConfig.class.getResourceAsStream(GROUPER_CF);
    try {
      cfg = new GrouperConfig();
      cfg.properties.load(in);
    }
    catch (IOException eIO) {
      String msg = E.CONFIG_READ + eIO.getMessage();
      ErrorLog.fatal(GrouperConfig.class, msg);
      throw new RuntimeException(msg, eIO);
    }
    return cfg;     
  } // private static GrouperConfig _getConfiguration()

}

