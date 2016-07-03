package edu.internet2.middleware.grouper.pspng;

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

/**
 * Provisioner subclasses often need User and/or Group information from their
 * target systems. The top-level Provisioner class, of course, does not know about
 * any of the details that these systems might have. Therefore, the Provisioner
 * uses the TargetSystemGroup and TargetSystemUser interfaces to help subclasses
 * fetch, cache and use Target-System specific information.
 * 
 * Note: Provisioners can define needsTargetSystemUsers and needsTargetSystemGroups
 * to specify what information they require.
 * 
 * @author bert
 *
 */
public interface TargetSystemGroup {

  Object getJexlMap();

}
