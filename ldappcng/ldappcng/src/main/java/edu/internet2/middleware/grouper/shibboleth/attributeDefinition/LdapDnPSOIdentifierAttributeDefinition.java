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
import java.util.Arrays;
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
import edu.internet2.middleware.grouper.shibboleth.dataConnector.GroupDataConnector;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.BaseAttributeDefinition;

public class LdapDnPSOIdentifierAttributeDefinition extends BaseAttributeDefinition {

  private static Logger LOG = LoggerFactory.getLogger(LdapDnPSOIdentifierAttributeDefinition.class);

  public static final String STEM_RDN_ATTRIBUTE = "ou";

  private String base;

  private String rdnAttributeName;

  private GroupDNStructure structure;

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public String getRdnAttributeName() {
    return rdnAttributeName;
  }

  public void setRdnAttributeName(String rdnAttributeName) {
    this.rdnAttributeName = rdnAttributeName;
  }

  public GroupDNStructure getStructure() {
    return structure;
  }

  public void setStructure(GroupDNStructure structure) {
    this.structure = structure;
  }

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

  protected BaseAttribute<PSOIdentifier> doResolve(ShibbolethResolutionContext resolutionContext)
      throws AttributeResolutionException {

    String principalName = resolutionContext.getAttributeRequestContext().getPrincipalName();
    String msg = "resolve '" + principalName + "' ad '" + this.getId() + "'";
    LOG.debug("{}", msg);

    BasicAttribute<PSOIdentifier> attribute = new BasicAttribute<PSOIdentifier>(this.getId());

    Collection<?> rdnValues = this.getValuesFromAllDependencies(resolutionContext, this.getSourceAttributeID());
    LOG.debug("{} dependency '{}' : {}", new Object[] { msg, this.getSourceAttributeID(), rdnValues });

    Collection<?> stemValues = this.getValuesFromAllDependencies(resolutionContext,
        GroupDataConnector.PARENT_STEM_NAME_ATTR);
    LOG.debug("{} dependency '{}' : {}", new Object[] { msg, GroupDataConnector.PARENT_STEM_NAME_ATTR, stemValues });

    if (rdnValues.isEmpty()) {
      LOG.debug("{} no dependency values", msg);
      return attribute;
    }

    if (rdnValues.size() > 1) {
      LOG.warn("{} Unable to resolve identifier, dependency '{}' has more than one value", msg, this
          .getSourceAttributeID());
      throw new AttributeResolutionException("Unable to resolve identifier, dependency has more than one value");
    }
    if (stemValues.size() > 1) {
      LOG.warn("{} Unable to resolve identifier, dependency '{}' has more than one value", msg,
          GroupDataConnector.PARENT_STEM_NAME_ATTR);
      throw new AttributeResolutionException("Unable to resolve identifier, dependency has more than one value");
    }

    List<Rdn> rdns = new ArrayList<Rdn>();
    try {
      // base
      LdapName baseDn = new LdapName(this.base);
      rdns.addAll(baseDn.getRdns());

      // stem if bushy and parent is not root
      if (this.getStructure().equals(GroupDNStructure.bushy) && stemValues.size() == 1) {
        String stemValue = stemValues.iterator().next().toString();
        rdns.addAll(this.getRdnsFromStemName(stemValue));
      }

      // rdn
      String rdnAttributeValue = rdnValues.iterator().next().toString();
      rdns.add(new Rdn(rdnAttributeName, rdnAttributeValue));

    } catch (InvalidNameException e) {
      LOG.error("{} Unable to resolve identifier, an error occurred {}", msg, e.getMessage());
      throw new AttributeResolutionException("Unable to resolve identifier", e);
    }

    // pso id
    PSOIdentifier psoIdentifier = new PSOIdentifier();

    // TODO container id
    // PSOIdentifier containerID = new PSOIdentifier();
    // containerID.setID(base);
    // psoIdentifier.setContainerID(containerID);

    // dn
    LdapName dn = new LdapName(rdns);
    // canonicalize ?
    try {
      psoIdentifier.setID(LdapUtil.canonicalizeDn(dn.toString()));
    } catch (InvalidNameException e) {
      LOG.error("{} Unable to canonicalize identifier, an error occurred {}", msg, e.getMessage());
      throw new AttributeResolutionException("Unable to canonicalize identifier", e);
    }

    attribute.setValues(Arrays.asList(new PSOIdentifier[] { psoIdentifier }));

    if (LOG.isDebugEnabled()) {
      for (Object value : attribute.getValues()) {
        LOG.debug("{} value '{}'", msg, PSPUtil.getString(value));
      }
    }

    return attribute;
  }

  public void validate() throws AttributeResolutionException {

  }
}
