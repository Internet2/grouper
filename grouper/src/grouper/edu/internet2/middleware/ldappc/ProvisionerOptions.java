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

import java.util.Date;

/**
 * This interface defines the common options utilized by all provisioners (e.g.,
 * Grouper, Signet).
 */
public interface ProvisionerOptions
{
    /**
     * Returns the Subject ID for the provisioners to use to select data for
     * provisioning.
     * 
     * @return Subject ID.
     */
    public String getSubjectId();

    /**
     * This returns the last modify time.
     * @return Last modify time or <code>null</code> if not defined.
     */
    public Date getLastModifyTime();
}