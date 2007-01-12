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

/**
 * This interface defines the options utilized by the Grouper provisioner.
 */
public interface GrouperProvisionerOptions extends ProvisionerOptions
{
    /**
     * This returns a boolean indicating whether or not groups are provisioned.
     * 
     * @return <code>true</code> if groups are to be provisioned and
     *         <code>false</code> otherwise.
     */
    public boolean getDoGroups();

    /**
     * This returns a boolean indicating whether or not memberships are
     * provisioned.
     * 
     * @return <code>true</code> if memberships are to be provisioned and
     *         <code>false</code> otherwise.
     */
    public boolean getDoMemberships();

    /**
     * Get the location of the alternative configuration manager. 
     * @return URI of the alternative configuration XML file.
     */
    public String getConfigManagerLocation();
}
