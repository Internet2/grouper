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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import junit.framework.Test;
import edu.internet2.middleware.ldappc.apachedsSchema.EduMemberSchema;
import edu.internet2.middleware.ldappc.apachedsSchema.EduPermissionSchema;
import edu.internet2.middleware.ldappc.apachedsSchema.EduPersonSchema;

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
        // Set the port we'll use.
        port = 10389;

        // Add the "edu" object classes and attributes.
        Set schemas = configuration.getBootstrapSchemas();
        Set newset = new HashSet();
        newset.addAll(schemas);
        newset.add(new EduMemberSchema());
        newset.add(new EduPermissionSchema());
        newset.add(new EduPersonSchema());
        configuration.setBootstrapSchemas(newset);

        // Actually start the LDAP server.
        super.setUp();
    }

    /**
     * Shut down the LDAP server.
     */
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
