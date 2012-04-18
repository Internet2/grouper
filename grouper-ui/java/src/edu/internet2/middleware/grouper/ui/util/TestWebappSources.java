/*******************************************************************************
 * Copyright 2012 Internet2
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
/*
Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2006 The University Of Bristol

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
package edu.internet2.middleware.grouper.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Called from ant 'test' target. Attempts to find GrouperSystem and the root stem
 * If it fails, exceptions should be displayed
  */


public class TestWebappSources {
	public static void main(String[] args) throws Exception{
		Logger subjectLogger = Logger.getLogger(Subject.class);
		subjectLogger.setLevel(Level.DEBUG);
		SourceManager sm = SourceManager.getInstance();
		BaseSourceAdapter isa = InternalSourceAdapter.instance();
		      sm.loadSource(isa);
		Collection sources = new ArrayList(sm.getSources());
		Iterator it = sources.iterator();
		Source source;
		Subject subj=null;
		while(it.hasNext()) {
			source = (Source)it.next();
			System.out.println("Attempting to load GrouperSystem from " + source.getId());
			try {
				subj = source.getSubject("GrouperSystem", true);
				System.out.println("...found GrouperSystem");
			}catch(Exception e) {
				System.out.println("..." +e.getClass().getName() + ":" + e.getMessage());
			}
			System.out.println();
		}
		System.out.println("Finished checking sources");
		if(subj!=null) {
			GrouperSession s = null;
			try {
				s=GrouperSession.start(subj);
			}catch(Exception e) {
				e.printStackTrace();
				return;
			}
			System.out.println("Attempting to load root stem");
			Stem stem = StemFinder.findRootStem(s);
			System.out.println("Root stem found with id=" + stem.getUuid());
			System.out.println("PASSED");
		}else{
			System.out.println("Failed: could not find GrouperSystem");
		}
	}
}
