/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
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

package edu.internet2.middleware.ldappc.synchronize;

import java.util.Iterator;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.Provisioner;
import edu.internet2.middleware.ldappc.ProvisionerConfiguration;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This defines the common functionality needed by all synchronizers.
 */
public abstract class Synchronizer {

  protected Provisioner provisioner;

  protected ProvisionerConfiguration configuration;

  protected LdapContext ldapCtx;

  protected Name rootDn;

  /**
   * Cache for subjects that have already been retrieved.
   */
  protected SubjectCache subjectCache;
  
  public Synchronizer(Provisioner provisioner) {
    
    this.provisioner = provisioner;

    this.ldapCtx = provisioner.getContext();
    
    this.configuration = provisioner.getConfiguration();
    
    this.subjectCache = provisioner.getSubjectCache();
    
  }
}
