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
 * @version $Id: GrouperConfig.java,v 1.30 2006-09-11 18:53:11 blair Exp $
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
  protected static final String GCGAA         = "groups.create.grant.all.admin";
  protected static final String GCGAOI        = "groups.create.grant.all.optin";
  protected static final String GCGAOO        = "groups.create.grant.all.optout";
  protected static final String GCGAR         = "groups.create.grant.all.read";
  protected static final String GCGAU         = "groups.create.grant.all.update";
  protected static final String GCGAV         = "groups.create.grant.all.view";
  protected static final String GWG           = "groups.wheel.group";
  protected static final String GWU           = "groups.wheel.use";
  protected static final String IST           = "application";
  protected static final String LIST          = "members";
  protected static final String MSLGEA        = "memberships.log.group.effective.add";
  protected static final String MSLGED        = "memberships.log.group.effective.del";
  protected static final String MSLSEA        = "memberships.log.stem.effective.add";
  protected static final String MSLSED        = "memberships.log.stem.effective.del";
  protected static final String PACI          = "privileges.access.cache.interface";
  protected static final String PAI           = "privileges.access.interface";
  protected static final String PNCI          = "privileges.naming.cache.interface";
  protected static final String PNI           = "privileges.naming.interface";
  protected static final String ROOT          = "GrouperSystem";
  protected static final String SCGAC         = "stems.create.grant.all.create";
  protected static final String SCGAS         = "stems.create.grant.all.stem";

  // PROTECTED CLASS CONSTANTS -- QUERIES //
  // FIXME I really hate all of these
  protected static final String   QCR_CF_IF     = "edu.internet2.middleware.grouper.CompositeFinder.IsFactor";
  protected static final String   QCR_CF_IO     = "edu.internet2.middleware.grouper.CompositeFinder.IsOwner";
  protected static final String   QCR_FF_FA     = "edu.internet2.middleware.grouper.FieldFinder.FindAll";
  protected static final String   QCR_FF_FABT   = "edu.internet2.middleware.grouper.FieldFinder.FindAllByType";
  protected static final String   QCR_GF_FBAA   = "edu.internet2.middleware.grouper.GroupFinder.FindByApproximateAttr";
  protected static final String   QCR_GF_FBAN   = "edu.internet2.middleware.grouper.GroupFinder.FindByApproximateName";
  protected static final String   QCR_GF_FBAAA  = "edu.internet2.middleware.grouper.GroupFinder.FindByAnyApproximateAttr";
  protected static final String   QCR_GF_FBCA   = "edu.internet2.middleware.grouper.GroupFinder.FindByCreatedAfter";
  protected static final String   QCR_GF_FBCB   = "edu.internet2.middleware.grouper.GroupFinder.FindByCreatedBefore";
  protected static final String   QCR_GF_FBMA   = "edu.internet2.middleware.grouper.GroupFinder.FindByModifiedAfter";
  protected static final String   QCR_GF_FBMB   = "edu.internet2.middleware.grouper.GroupFinder.FindByModifiedBefore";
  protected static final String   QCR_GF_FBN    = "edu.internet2.middleware.grouper.GroupFinder.FindByName";
  protected static final String   QCR_GF_FBU    = "edu.internet2.middleware.grouper.GroupFinder.FindByUuid";
  protected static final String   QCR_GTF_FA    = "edu.internet2.middleware.grouper.GroupTypeFinder.FindAll";
  protected static final boolean  QRY_GTF_FA    = false; 
  protected static final String   QCR_MF_FBS    = "edu.internet2.middleware.grouper.MemberFinder.FindBySubject";
  protected static final String   QCR_MF_FBU    = "edu.internet2.middleware.grouper.MemberFinder.FindByUuid";
  protected static final String   QCR_MSF_FAM    = "edu.internet2.middleware.grouper.MembershipFinder.FindAllMemberships";
  protected static final String   QCR_MSF_FBU   = "edu.internet2.middleware.grouper.MembershipFinder.FindByUuid";
  protected static final String   QCR_MSF_FCM   = "edu.internet2.middleware.grouper.MembershipFinder.FindChildMemberships";
  protected static final String   QCR_MSF_FEM   = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMemberships";
  protected static final String   QCR_MSF_FEMM  = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsMember";
  protected static final String   QCR_MSF_FEMOM = "edu.internet2.middleware.grouper.MembershipFinder.FindEffectiveMembershipsOwnerMember";
  protected static final String   QCR_MSF_FIMM  = "edu.internet2.middleware.grouper.MembershipFinder.FindImmediateMembershipsMember";
  protected static final String   QCR_MSF_FM    = "edu.internet2.middleware.grouper.MembershipFinder.FindMemberships";
  protected static final String   QCR_MSF_FMO   = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner";
  protected static final String   QCR_MSF_FMOM  = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwnerMember";
  protected static final String   QCR_MSF_FMSBT   = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipByType";
  protected static final String   QCR_MSF_FMSBT_C = "edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsByType";
  protected static final String   QCR_SF_FBADE  = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateDisplayExtension";
  protected static final String   QCR_SF_FBADN  = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateDisplayName";
  protected static final String   QCR_SF_FBAE   = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateExtension";
  protected static final String   QCR_SF_FBAN   = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateName";
  protected static final String   QCR_SF_FBANA  = "edu.internet2.middleware.grouper.StemFinder.FindByApproximateNameAny";
  protected static final String   QCR_SF_FBCA   = "edu.internet2.middleware.grouper.StemFinder.FindByCreatedAfter";
  protected static final String   QCR_SF_FBCB   = "edu.internet2.middleware.grouper.StemFinder.FindByCreatedBefore";
  protected static final String   QCR_SF_FBN    = "edu.internet2.middleware.grouper.StemFinder.FindByName";
  protected static final String   QCR_SF_FBU    = "edu.internet2.middleware.grouper.StemFinder.FindByUuid";


  // PRIVATE CLASS VARIABLES //
  private static  Properties  grouper_props = new Properties();
  private static  Properties  hib_props     = new Properties();


  // STATIC //
  static {
    // Load Grouper properties
    try {
      InputStream in = GrouperConfig.class.getResourceAsStream(GROUPER_CF);
      grouper_props.load(in);
    }
    catch (IOException eIO) {
      String msg = E.CONFIG_READ + eIO.getMessage();
      ErrorLog.fatal(GrouperConfig.class, msg);
      throw new GrouperRuntimeException(msg, eIO);
    }
    // Load Hibernate properties
    try {
      InputStream in = GrouperConfig.class.getResourceAsStream(HIBERNATE_CF);
      hib_props.load(in);
    }
    catch (IOException eIO) {
      String msg = E.CONFIG_READ_HIBERNATE + eIO.getMessage();
      ErrorLog.fatal(GrouperConfig.class, msg);
      throw new GrouperRuntimeException(msg, eIO);
    }
  } // static



  // CONSTRUCTORS //
  private GrouperConfig() {
    super();
  } // private GrouperConfig()


  // PUBLIC CLASS METHODS //

  /**
   * Get a Hibernate configuration parameter.
   * <pre class="eg">
   * String dialect = GrouperConfig.getHibernateProperty("hibernate.dialect");
   * </pre>
   * @return  Value of configuration parameter or an empty string if
   *   parameter is invalid.
   * @since   1.1.0
   */
  public static String getHibernateProperty(String property) {
    return _getProperty(hib_props, property);
  } // public static String getHibernateProperty(property)

  /**
   * Get a Grouper configuration parameter.
   * <pre class="eg">
   * String wheel = GrouperConfig.getProperty("groups.wheel.group");
   * </pre>
   * @return  Value of configuration parameter or an empty string if
   *   parameter is invalid.
   * @since   1.1.0
   */
  public static String getProperty(String property) {
    return _getProperty(grouper_props, property);
  } // public static String getProperty(property)


  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static void setProperty(String property, String value) {
    grouper_props.setProperty(property, value);
  } // protected static void setProperty(property, value):w


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static String _getProperty(Properties props, String property) {
    String value = GrouperConfig.EMPTY_STRING;
    if ( (property != null) && (props.containsKey(property)) ) {
      value = StringUtils.strip( props.getProperty(property) );
    }
    return value;
  } // private static String _getProperty(props, property)

} // public class GrouperConfig

