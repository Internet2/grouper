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



import  edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;

import  edu.internet2.middleware.subject.Subject;

import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Class for finding subjects.
 * @author Gil Singer
 */
public class GrouperSubjectRetriever 
{

    public GrouperSubjectRetriever()
    {
    }

    /**
     * Find a subject by Id
     * @param subjectId the subject id to search for.
     */
    public Subject findSubjectById(String subjectId) 
    {
        Subject subject = null;
        try 
        {
          subject = SubjectFinder.findById(subjectId);
          DebugLog.debug(this.getClass(), "Found " + subjectId); // e.g. GrouperSystem
        }
        catch (SubjectNotFoundException snfe) 
        {
          ErrorLog.error(this.getClass(), snfe.getMessage());
        }
        catch (SubjectNotUniqueException snue) 
        {
          ErrorLog.error(this.getClass(), snue.getMessage());
        }
        return subject;
    } 

} 

