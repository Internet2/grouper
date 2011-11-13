/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.attributeDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.openspml.v2.msg.spml.PSOIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.AttributeDefinition;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.BaseAttributeDefinition;

/**
 * An {@link AttributeDefinition} which returns a {@link PSOIdentifier} whose ID is an LDAP DN computed from
 * dependencies.
 */
public class LdapDnPSOIdentifierAttributeDefinition extends BaseAttributeDefinition {

  /** The logger. */
  private static Logger LOG = LoggerFactory.getLogger(LdapDnPSOIdentifierAttributeDefinition.class);

  /** The RDN attribute name used to build LDAP DNs. */
  public static final String STEM_RDN_ATTRIBUTE = "ou";

  /** The LDAP DN base. */
  private String base;

  /** The LDAP RDN attribute name. */
  private String rdnAttributeName;

  /** The Grouper DN structure. */
  private GroupDNStructure structure;

  /**
   * Get the LDAP DN base.
   * 
   * @return the base DN
   */
  public String getBase() {
    return base;
  }

  /**
   * Set the LDAP DN base.
   * 
   * @param base the base DN
   */
  public void setBase(String base) {
    this.base = base;
  }

  /**
   * Get the LDAP RDN attribute name.
   * 
   * @return the RDN attribute name
   */
  public String getRdnAttributeName() {
    return rdnAttributeName;
  }

  /**
   * Set the LDAP RDN attribute name.
   * 
   * @param rdnAttributeName the RDN attribute name
   */
  public void setRdnAttributeName(String rdnAttributeName) {
    this.rdnAttributeName = rdnAttributeName;
  }

  /**
   * Get the Grouper DN structure.
   * 
   * @return the DN structure
   */
  public GroupDNStructure getStructure() {
    return structure;
  }

  /**
   * Set the Grouper DN structure.
   * 
   * @param structure the DN structure
   */
  public void setStructure(GroupDNStructure structure) {
    this.structure = structure;
  }

  /**
   * Given a string of the form a:b:c, convert this to LDAP RDNs.
   * 
   * @param stemName the string of the form a:b:c
   * @return the list of LDAP RDNs
   * @throws AttributeResolutionException
   */
  public List<Rdn> getRdnsFromStemName(String stemName) throws AttributeResolutionException {

    ArrayList<Rdn> rdns = new ArrayList<Rdn>();

    StringTokenizer stemTokens = new StringTokenizer(stemName, Stem.DELIM);

    while (stemTokens.hasMoreTokens()) {
      try {
        rdns.add(new Rdn(STEM_RDN_ATTRIBUTE, stemTokens.nextToken()));
      } catch (InvalidNameException e) {
        LOG.error("An error occurred creating an rdn.", e);
        throw new AttributeResolutionException("An error occurred creating an rdn.", e);
      }
    }

    return rdns;
  }

  /** {@inheritDoc} */
  protected BaseAttribute<PSOIdentifier> doResolve(ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();

    String msg = "Ldap Dn PSOIdentifier attribute definition '" + getId() + "' - Resolve principal '" + principalName
        + "'";
    LOG.debug("{}", msg);

    BasicAttribute<PSOIdentifier> attribute = new BasicAttribute<PSOIdentifier>(getId());

    Collection<?> values = getValuesFromAllDependencies(resolutionContext, getSourceAttributeID());

    LOG.debug("{} Dependency '{}'", msg, values);

    if (values.isEmpty()) {
      LOG.debug("{} No dependencies.", msg);
      return attribute;
    }

    for (Object value : values) {

      // the rdn attribute value
      String rdnAttributeValue = value.toString();
      
      // build RDNs
      List<Rdn> rdns = new ArrayList<Rdn>();
      try {
        // base
        LdapName baseDn = new LdapName(base);
        rdns.addAll(baseDn.getRdns());

        if (getStructure().equals(GroupDNStructure.bushy)) {
          String parentStemName = GrouperUtil.parentStemNameFromName(rdnAttributeValue, true);
          String extension = GrouperUtil.extensionFromName(rdnAttributeValue);
          if (parentStemName != null) {
            rdns.addAll(getRdnsFromStemName(parentStemName));
          }
          rdns.add(new Rdn(rdnAttributeName, extension));
        } else {
          rdns.add(new Rdn(rdnAttributeName, rdnAttributeValue));
        }

      } catch (InvalidNameException e) {
        LOG.error("{} Unable to resolve identifier, an error occurred {}", msg, e.getMessage());
        throw new AttributeResolutionException("Unable to resolve identifier", e);
      }

      // dn
      LdapName dn = new LdapName(rdns);

      // pso id
      PSOIdentifier psoIdentifier = new PSOIdentifier();

      // TODO container id
      // PSOIdentifier containerID = new PSOIdentifier();
      // containerID.setID(base);
      // psoIdentifier.setContainerID(containerID);

      // canonicalize
      try {
        psoIdentifier.setID(LdapUtil.canonicalizeDn(dn.toString()));
      } catch (InvalidNameException e) {
        LOG.error("{} Unable to canonicalize identifier, an error occurred {}", msg, e.getMessage());
        throw new AttributeResolutionException("Unable to canonicalize identifier", e);
      }

      attribute.getValues().add(psoIdentifier);     
    }

    if (LOG.isTraceEnabled()) {
      for (Object value : attribute.getValues()) {
        LOG.trace("{} value '{}'", msg, PSPUtil.getString(value));
      }
    }

    return attribute;
  }

  /** {@inheritDoc} */
  public void validate() throws AttributeResolutionException {

  }
}
