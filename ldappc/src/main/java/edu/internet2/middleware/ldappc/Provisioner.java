/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
 
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

package edu.internet2.middleware.ldappc;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;


import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * This abstract parent class for provisioners.
 */
public abstract class Provisioner
{
    /**
     * Delimiter used in messages
     */
    final private String   MSG_DELIMITER = " :: ";

    protected SubjectCache subjectCache;

    public Provisioner(SubjectCache subjectCache)
    {
        this.subjectCache = subjectCache;
    }

    /**
     * Utility method for logging a <code>Throwable</code> error.
     * 
     * @param throwable
     *            Throwable to log
     */
    protected void logThrowableError(Throwable throwable)
    {
        logThrowableError(throwable, null);
    }

    /**
     * Utility method for logging a <code>Throwable</code> error.
     * 
     * @param throwable
     *            Throwable to log
     * @param errorData
     *            Additional data for helping to debug
     */
    protected void logThrowableError(Throwable throwable, String errorData)
    {
        ErrorLog.error(getClass(), buildThrowableMsg(throwable, errorData));
    }

    /**
     * Utility method for logging a <code>Throwable</code> warning.
     * 
     * @param throwable
     *            Throwable to log
     */
    protected void logThrowableWarning(Throwable throwable)
    {
        logThrowableWarning(throwable, null);
    }

    /**
     * Utility method for logging a <code>Throwable</code> warning.
     * 
     * @param throwable
     *            Throwable to log
     * @param errorData
     *            Additional data for helping to debug
     */
    protected void logThrowableWarning(Throwable throwable, String errorData)
    {
        ErrorLog.warn(getClass(), buildThrowableMsg(throwable, errorData));
    }

    /**
     * Builds the message for logging throwables
     * 
     * @param throwable
     *            Throwable to log
     * @param errorData
     *            Additional data for helping to debug
     * @return message string
     */
    protected String buildThrowableMsg(Throwable throwable, String errorData)
    {
        return throwable.getClass().getName() + MSG_DELIMITER
                + throwable.getMessage()
                + (errorData == null ? "" : MSG_DELIMITER + errorData);
    }
}
