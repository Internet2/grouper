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
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 06. May 2004 by Joe Walnes
 */
package edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.converters.basic;

import java.text.ParseException;
import java.util.Date;

/**
 * Wrapper around java.text.SimpleDateFormat that can
 * be called by multiple threads concurrently.
 *
 * @author Joe Walnes
 * @deprecated since 1.2.1, moved to com.thoughtworks.xstream.core.util. 
 * It is not part of public API, use on your own risk.
 */
public class ThreadSafeSimpleDateFormat extends edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.core.util.ThreadSafeSimpleDateFormat {

    public ThreadSafeSimpleDateFormat(String format, int initialPoolSize, int maxPoolSize) {
        super(format, initialPoolSize, maxPoolSize, true);
    }

    public String format(Date date) {
        return super.format(date);
    }

    public Date parse(String date) throws ParseException {
        return super.parse(date);
    }
}
