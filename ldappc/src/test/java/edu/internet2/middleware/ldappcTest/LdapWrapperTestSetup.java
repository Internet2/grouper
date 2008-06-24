/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package edu.internet2.middleware.ldappcTest;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import junit.framework.Test;

import org.apache.directory.server.core.configuration.MutablePartitionConfiguration;
import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
import org.apache.directory.shared.ldap.ldif.Entry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;

import edu.internet2.middleware.ldappc.apachedsSchema.EduMemberSchema;
import edu.internet2.middleware.ldappc.apachedsSchema.EduPermissionSchema;
import edu.internet2.middleware.ldappc.apachedsSchema.EduPersonSchema;
import edu.internet2.middleware.ldappc.apachedsSchema.KitnEduPersonSchema;

/**
 * Wrap a suite of tests with an embedded instance of Apache Directory Server.
 */
public class LdapWrapperTestSetup extends AbstractServerTestSetup {
    private DirContext ctx = null;

    public LdapWrapperTestSetup(Test suite) {
        super(suite);
    }

    /**
     * Make sure embedded instance uses our schema.
     */
    public void setUp() throws Exception {
        System.out.println("Setting up embedded directory");

        // Set the port we'll use.
        port = 10389;

        // Add the "edu" object classes and attributes.
        Set schemas = configuration.getBootstrapSchemas();
        Set newset = new HashSet();
        newset.addAll(schemas);
        newset.add(new EduMemberSchema());
        newset.add(new EduPermissionSchema());
        newset.add(new EduPersonSchema());
        newset.add(new KitnEduPersonSchema());
        configuration.setBootstrapSchemas(newset);

        // Add partition 'example'
        MutablePartitionConfiguration pcfg = new MutablePartitionConfiguration();
        pcfg.setName("example");
        pcfg.setSuffix("dc=example,dc=edu");

        // Create some indices
        Set<String> indexedAttrs = new HashSet<String>();
        indexedAttrs.add("objectClass");
        indexedAttrs.add("o");
        pcfg.setIndexedAttributes(indexedAttrs);

        // Create a first entry associated to the partition
        Attributes attrs = new BasicAttributes(true);

        // First, the objectClass attribute
        Attribute attr = new BasicAttribute("objectClass");
        attr.add("top");
        attr.add("dcObject");
        attr.add("organization");
        attrs.put(attr);

        // Next, the 'Organization' attribute
        attr = new BasicAttribute("o");
        attr.add("Example University");
        attrs.put(attr);

        // Next, the 'dc' attribute
        attr = new BasicAttribute("dc");
        attr.add("example");
        attrs.put(attr);

        // Associate this entry to the partition
        pcfg.setContextEntry(attrs);

        // As we can create more than one partition, we must store
        // each created partition in a Set before initialization
        Set<MutablePartitionConfiguration> pcfgs = new HashSet<MutablePartitionConfiguration>();
        pcfgs.add(pcfg);

        configuration.setContextPartitionConfigurations(pcfgs);

        // Create a working directory
        File workingDirectory = new File("target/server-work");
        configuration.setWorkingDirectory(workingDirectory);

        // Actually start the LDAP server.
        super.setUp();

        // Import the test data into the server.
        System.out.println("Importing data into directory");
        importLdif(this.getClass().getResourceAsStream("/ldappcSubject.ldif"));
        System.out.println("Embedded directory setup complete");
    }

    /**
     * Shut down the LDAP server.
     */
    public void tearDown() throws Exception {
        System.out.println("Shutting down embedded directory");
        super.tearDown();
        System.out.println("Embedded directory shutdown complete");
    }
}
