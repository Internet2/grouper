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

package edu.internet2.middleware.ldappc.spml.provider;

import java.util.Set;

import org.openspml.v2.msg.spml.PSOIdentifier;

import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.spml.PSP;
import edu.internet2.middleware.ldappc.spml.definitions.PSODefinition;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;

/**
 * An SPML Provisioning Service Provider which is also a Provisioning Service Target.
 */
public interface SpmlTargetProvider extends SpmlProvider {

  /**
   * Set the target definition.
   * 
   * @param target the target definition
   */
  public void setTargetDefinition(TargetDefinition target);

  /**
   * Get the target definition.
   * 
   * @return the target definition
   */
  public TargetDefinition getTargetDefinition();

  /**
   * Set the provisioning service provider.
   * 
   * @param psp the provisioning service provider
   */
  public void setPSP(PSP psp);

  /**
   * Get the provisioning service provider.
   * 
   * @return the provisioning service provider
   */
  public PSP getPSP();

  /**
   * Returns the {@link PSOIdentifier}s which exist on the target for the given {@link PSODefinition}.
   * 
   * @param psoDefinition the provisioned object definition
   * @return the possibly empty set of identifiers
   * @throws LdappcException if an error occurs
   */
  public Set<PSOIdentifier> getPsoIdentifiers(PSODefinition psoDefinition) throws LdappcException;

  /**
   * Returns a set of {@link PSOIdentifiers}s in order suitable for deletion from the given set.
   * 
   * @param psoIdentifiers a set of identifiers
   * @return a new set of identifiers ordered for deletion
   * @throws LdappcException if an error occurs
   */
  public Set<PSOIdentifier> orderForDeletion(Set<PSOIdentifier> psoIdentifiers) throws LdappcException;
}
