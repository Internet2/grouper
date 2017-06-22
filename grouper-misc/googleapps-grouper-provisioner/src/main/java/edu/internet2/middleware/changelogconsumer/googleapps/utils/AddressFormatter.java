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
package edu.internet2.middleware.changelogconsumer.googleapps.utils;

import edu.internet2.middleware.subject.Subject;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.UnifiedJEXL;

/**
 * Formats user and group addresses. Supports JEXL manipulations/evaluations
 *
 * @author John Gasper, Unicon
 */
public class AddressFormatter {
    private final JexlEngine jexl = new JexlEngine();
    private final UnifiedJEXL ujexl = new UnifiedJEXL(jexl);
    private UnifiedJEXL.Expression groupIdentifierExp = null;
    private UnifiedJEXL.Expression subjectIdentifierExp = null;
    private String domain;

    public String qualifySubjectAddress(final Subject subject) {
        final JexlContext context = new MapContext();
        context.set("subject", subject);
        context.set("subjectId", subject.getId());

        String address = subjectIdentifierExp.evaluate(context).toString();

        if (!address.contains("@")) {
            address = String.format("%s@%s", address, this.domain);
        }

        return address.replace(":", "-");
    }

    public String qualifyGroupAddress(String group) {
        final JexlContext context = new MapContext();
        context.set("groupPath", group);

        final String mailbox = groupIdentifierExp.evaluate(context).toString();

        return String.format("%s@%s", mailbox.replace(":", "-").toLowerCase(), this.domain);
    }

    public AddressFormatter setGroupIdentifierExpression(String groupIdentifierExpression){
        this.groupIdentifierExp = ujexl.parse(groupIdentifierExpression);

        return this;
    }

    public AddressFormatter setSubjectIdentifierExpression(String subjectIdentifierExpression){
        this.subjectIdentifierExp = ujexl.parse(subjectIdentifierExpression);

        return this;
    }


    public AddressFormatter setDomain(String domain) {
        this.domain = domain;

        return this;
    }
}
