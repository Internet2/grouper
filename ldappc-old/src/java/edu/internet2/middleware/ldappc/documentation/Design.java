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

package edu.internet2.middleware.ldappc.documentation;

/**
 *
 * <p>
 * This document describes the design of the Ldappc program.
 * </p>
 *
 * <h2>Introduction</h2>
 *
 * <p>
 *
 * </p>
 *
 * <h2>Top Level Code Structure</h2>
 *
 * <h3>Inheritance Hierachy</h3>
 *
 * Key: i = interface, c = class
 * <pre>
 *                                ProvisionerOptions (i)
 *                                       ^^ 
 *                                      /  \
 *                                     /    \
 *                                    /      \
 *                                   /        \
 *                                  /          \
 *                                 /            \
 *                                /              \
 *          GrouperProvisionerOptions (i)   SignetProvisionerOptions (i)
 *                               ^                ^
 *                                \              /
 *                                 \            /
 *                                  \          /
 *                                   \        /
 *                                    \      /
 *                                     \    /
 *                                InputOptions (c)
 *
 *
 *
 *                             ProvisionerConfiguration (i)
 *                                       ^^ 
 *                                      /  \
 *                                     /    \
 *                                    /      \
 *                                   /        \
 *                                  /          \
 *                                 /            \
 *                                /              \
 *      GrouperProvisionerConfiguration (i)   SignetProvisionerConfiguration (i)
 *                               ^                ^
 *                                \              /
 *                                 \            /
 *                                  \          /
 *                                   \        /
 *                                    \      /
 *                                     \    /
 *                             ConfigurationManager (c)
 * </pre>
 *
 * <h3>Inheritance Hierachy Discussion</h3>
 * <p>
 * The ProvisioningConfiguration interface defines methods common to Grouper and
 * Signet.  These include obtaining the LDAP context parameters and subject and source
 * related data.  The SignetProvisionerConfiguration interface defines methods related
 * to obtaining permission information.  The GrouperProvisionerConfiguration interface 
 * defines methods related to obtaining group and membership information.
 * </p>
 * <p>
 * The ConfigurationManager class uses the Apache Digester package to parse a
 * configuration file, ldappc.xml, to obtain information about what Grouper
 * and/or Signet data is to be used to populate the LDAP directory.  It stores 
 * this data in memory and makes it available to the code. 
 * </p>
 *
 * <h2>Sequence Diagrams</h2>
 *
 * <p>
 * This describes the high level sequence diagram for the program. 
 * </p>
 *
 * <h3>About the Notation</h3> 
 * <p>
 * Note that due to the limitation of doing sequence diagrams in ASCII text,
 * there are several adaptions made to the normal diagrams.
 * Only messages of particular interest are displayed.
 * The object names are used to represent objects without the usual
 * underlining of object names, and the class name is inferred from the object
 * name.  Object construction is shown using equal signs instead of a closed
 * arrow because the keyboard lacks a closed arrow and because it 
 * it easier to identify the constructors.
 * </p>
 * <p>
 * On occasion, pseudocode is substituted for formal sequence diagrams to avoid 
 * creating new columns for objects that are not essential to understanding
 * the diagram and whose presence might obscure more important features.
 * For example, the following notation would be used to avoid creating a column
 * for the LdapUtil class because it just uses a static method 
 * to create a local variable. 
 * </p>
 * <p>
 *     ldapContext:LdapContext:=LDAPUtil.getLdapContext(Hashtable)
 * </p>
 *
 * <h3>Top Level Sequence Diagram</h3>
 *
 * <p> 
 * The sequence diagram below shows the cascade of calls that occur when the
 * user starts the program down to where the provisioning takes place
 * </p> 
 * <p> 
 * Note that sequence diagrams are instance related.  The diagram here
 * shows a particular top level scenario for the case where the
 * GrouperProvisionerOptions and the SignetProvisionerOptions are represented 
 * by an instance of the InputOptions class that implements those
 * interfaces. 
 * </p> 
 * <p> 
 * Similarly, the SignetProvisionerConfiguration and GrouperProvisionerConfiguration 
 * are represented by an instance of the ConfigManager class that implements
 * those interfaces. 
 * </p> 
 *
 <pre>
 * <b>Ldappc</b>
 *      |(cmd line args)
 *      |===================>
 *      |              <b>options:InputOptions</b>
 *      |                    
 *      |===================>
 *      |              <b>:LdappcProvisionControl</b>
 *      |provision(options)
 *      |------------------>|
 *      |                   |(options)
 *      |                   |===================>
 *      |                   |              <b>:LdappcGrouperProvisioner</b>
 *      |                   |                   |
 *      |                   |ldapContext:LdapContext:=LdapUtil.getLdapContext(Hashtable)
 *      |                   |configuration:ConfigManager:=ConfigManager.getInstance()
 *      |                   |                   |
 *      |                   |provisionGroups(options)
 *      |                   |------------------->
 *      |                   |                   |(configuration, options, ldapContext);
 *      |                   |                   |===================>
 *      |                   |                   |              <b>:GrouperProvisioner</b>
 *      |                   |                   |                   | // Performs various settings.
 *      |                   |                   |provision()        |
 *      |                   |                   |------------------->
 *      |                   |<- - - - - - - - - | 
 *      |                   |                   
 *      |                   |(options) 
 *      |                   |===================>
 *      |                   |              <b>:LdappcSignetProvisioner</b>
 *      |                   |provisionPermissions(options)
 *      |                   |------------------->
 *      |                   |                   |(configuration, options, ldapContext);
 *      |                   |                   |===================>
 *      |                   |                   |              <b>:SignetProvisioner</b>
 *      |                   |                   |                   | // Performs various settings.
 *      |                   |                   |provision()        |
 *      |                   |                   |------------------->
 *      |                   |<- - - - - - - - - | 
 *      |<- - - - - - - - - | 
 */

public class Design
{
}
