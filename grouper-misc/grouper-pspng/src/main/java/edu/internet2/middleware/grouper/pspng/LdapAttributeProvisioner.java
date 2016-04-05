package edu.internet2.middleware.grouper.pspng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchRequest;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.subject.Subject;

/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/** This class implements group-membership changes into an Attribute of 
 * the User objects that are members of the group.
 * 
 * @author bert
 *
 */
public class LdapAttributeProvisioner extends LdapProvisioner {
  protected LdapAttributeProvisionerProperties config;
  
  
  /**
   * What class holds what is necessary for our configuration
   * @return
   */
  public static Class<? extends ProvisionerProperties> getPropertyClass() {
    return LdapAttributeProvisionerProperties.class;
  }
  
  

  public LdapAttributeProvisioner(String provisionerName, LdapAttributeProvisionerProperties config) {
    super(provisionerName, config);
    this.config = config;
  }

  
  /**
   * This adds/removes values from the given user. This is a helper class to package
   * up the modification into the necessary LDAP ModifyRequest.
   * 
   * @param ldapUser
   * @param modType
   * @param valuesToChange
   */
  protected void scheduleUserModification(LdapUser ldapUser, AttributeModificationType modType, Collection<String> valuesToChange) {
    uncacheUser(null, ldapUser); 
    String attributeName = config.getProvisionedAttributeName();
    
    for ( String value : valuesToChange )
      // ADD/REMOVE <value> to/from <attribute> of <user>
      LOG.info("Will change LDAP: {} {} {} {} of {}", 
          modType, value,
          modType == AttributeModificationType.ADD ? "to" : "from",
          attributeName, ldapUser);
      
    scheduleLdapModification(
        new ModifyRequest(
            ldapUser.getLdapObject().getDn(), 
            new AttributeModification(modType, new LdapAttribute(attributeName, valuesToChange.toArray(new String[0])))));
  }
  
  
  @Override
  protected void addMembership(Group group, TargetSystemGroup tsGroup,
      Subject subject, TargetSystemUser tsUser) throws PspException {

    if ( tsUser == null )
      throw new PspException("%s: LdapUser does not exist for subject %s", getName(), subject.getId());
    
    LdapUser ldapUser   = (LdapUser) tsUser;
    
    String attributeValue = evaluateJexlExpression(config.getProvisionedAttributeValueFormat(), subject, group);
    
    scheduleUserModification(ldapUser, AttributeModificationType.ADD, Arrays.asList(attributeValue));
  }

  @Override
  protected void deleteMembership(Group group, TargetSystemGroup tsGroup,
      Subject subject, TargetSystemUser tsUser) throws PspException {

    if ( tsUser == null )
      throw new PspException("%s: LdapUser does not exist for subject %s", getName(), subject.getId());
    
    LdapUser ldapUser   = (LdapUser) tsUser;
    
    String attributeValue = evaluateJexlExpression(config.getProvisionedAttributeValueFormat(), subject, group);

    scheduleUserModification(ldapUser, AttributeModificationType.REMOVE, Arrays.asList(attributeValue));
  }

  @Override
  protected void doFullSync(Group group, TargetSystemGroup tsGroup,
      Set<Subject> correctSubjects, Set<? extends TargetSystemUser> correctTSUsers)
      throws PspException {
    
    String attributeName = config.getProvisionedAttributeName();
    String attributeValue = evaluateJexlExpression(config.getProvisionedAttributeValueFormat(), null, group);
    
    List<LdapObject> currentMatches_ldapObjects = performLdapSearchRequest(
        new SearchRequest(config.getUserSearchBaseDn(),
            String.format("%s=%s", attributeName, attributeValue),
            config.getUserSearchAttributes()));
    
    List<LdapUser> currentMatches = new ArrayList<LdapUser>(currentMatches_ldapObjects.size());
    for ( LdapObject ldapObject : currentMatches_ldapObjects )
      currentMatches.add(new LdapUser(ldapObject));
    
    // EXTRA MATCHES = CURRENT_MATCHES - CORRECT_MATCHES
    Set<LdapUser> extraMatches = new HashSet<LdapUser>(currentMatches);
    extraMatches.removeAll(correctTSUsers);
    
    for (LdapUser extraMatch : extraMatches)
      scheduleUserModification(extraMatch, AttributeModificationType.REMOVE, Arrays.asList(attributeValue));
            
    // MISSING MATCHES = CORRECT_MATCHES - CURRENT_MATCHES
    Set<LdapUser> missingMatches = new HashSet<LdapUser>((Set<LdapUser>)correctTSUsers);
    missingMatches.removeAll(currentMatches);
    
    for (LdapUser missingMatch : missingMatches)
      scheduleUserModification(missingMatch, AttributeModificationType.ADD, Arrays.asList(attributeValue));

    LOG.info("{}: Brief full-sync summary: Correct={}, Current={}, Extra={}, Missing={}", 
        getName(), correctSubjects.size(), currentMatches_ldapObjects.size(),
        extraMatches.size(), missingMatches.size());
    
  }

}
