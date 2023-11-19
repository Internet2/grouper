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
 * $Header: /home/hagleyj/i2mi/grouper-misc/grouperClient/src/ext/edu/internet2/middleware/grouperClientExt/org/apache/commons/httpclient/params/HttpParams.java,v 1.1 2008-11-30 10:57:20 mchyzer Exp $
 * $Revision: 1.1 $
 * $Date: 2008-11-30 10:57:20 $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.params;

/**
 * This interface represents a collection of HTTP protocol parameters. Protocol parameters
 * may be linked together to form a hierarchy. If a particular parameter value has not been
 * explicitly defined in the collection itself, its value will be drawn from the parent 
 * collection of parameters.
 *   
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 1.1 $
 *
 * @since 3.0
 */
public interface HttpParams {

    /** 
     * Returns the parent collection that this collection will defer to
     * for a default value if a particular parameter is not explicitly 
     * set in the collection itself
     * 
     * @return the parent collection to defer to, if a particular parameter
     * is not explictly set in the collection itself.
     * 
     * @see #setDefaults(HttpParams)
     */
    public HttpParams getDefaults();

    /** 
     * Assigns the parent collection that this collection will defer to
     * for a default value if a particular parameter is not explicitly 
     * set in the collection itself
     * 
     * @param params the parent collection to defer to, if a particular 
     * parameter is not explictly set in the collection itself.
     * 
     * @see #getDefaults()
     */
    public void setDefaults(final HttpParams params);
    
    /** 
     * Returns a parameter value with the given name. If the parameter is
     * not explicitly defined in this collection, its value will be drawn 
     * from a higer level collection at which this parameter is defined.
     * If the parameter is not explicitly set anywhere up the hierarchy,
     * <tt>null</tt> value is returned.  
     * 
     * @param name the parent name.
     * 
     * @return an object that represents the value of the parameter.
     * 
     * @see #setParameter(String, Object)
     */
    public Object getParameter(final String name);

    /**
     * Assigns the value to the parameter with the given name
     * 
     * @param name parameter name
     * @param value parameter value
     */ 
    public void setParameter(final String name, final Object value);
    
    /** 
     * Returns a {@link Long} parameter value with the given name. 
     * If the parameter is not explicitly defined in this collection, its 
     * value will be drawn from a higer level collection at which this parameter 
     * is defined. If the parameter is not explicitly set anywhere up the hierarchy,
     * the default value is returned.  
     * 
     * @param name the parent name.
     * @param defaultValue the default value.
     * 
     * @return a {@link Long} that represents the value of the parameter.
     * 
     * @see #setLongParameter(String, long)
     */
    public long getLongParameter(final String name, long defaultValue); 
    
    /**
     * Assigns a {@link Long} to the parameter with the given name
     * 
     * @param name parameter name
     * @param value parameter value
     */ 
    public void setLongParameter(final String name, long value);

    /** 
     * Returns an {@link Integer} parameter value with the given name. 
     * If the parameter is not explicitly defined in this collection, its 
     * value will be drawn from a higer level collection at which this parameter 
     * is defined. If the parameter is not explicitly set anywhere up the hierarchy,
     * the default value is returned.  
     * 
     * @param name the parent name.
     * @param defaultValue the default value.
     * 
     * @return a {@link Integer} that represents the value of the parameter.
     * 
     * @see #setIntParameter(String, int)
     */
    public int getIntParameter(final String name, int defaultValue); 
    
    /**
     * Assigns an {@link Integer} to the parameter with the given name
     * 
     * @param name parameter name
     * @param value parameter value
     */ 
    public void setIntParameter(final String name, int value);

    /** 
     * Returns a {@link Double} parameter value with the given name. 
     * If the parameter is not explicitly defined in this collection, its 
     * value will be drawn from a higer level collection at which this parameter 
     * is defined. If the parameter is not explicitly set anywhere up the hierarchy,
     * the default value is returned.  
     * 
     * @param name the parent name.
     * @param defaultValue the default value.
     * 
     * @return a {@link Double} that represents the value of the parameter.
     * 
     * @see #setDoubleParameter(String, double)
     */
    public double getDoubleParameter(final String name, double defaultValue); 
    
    /**
     * Assigns a {@link Double} to the parameter with the given name
     * 
     * @param name parameter name
     * @param value parameter value
     */ 
    public void setDoubleParameter(final String name, double value);

    /** 
     * Returns a {@link Boolean} parameter value with the given name. 
     * If the parameter is not explicitly defined in this collection, its 
     * value will be drawn from a higer level collection at which this parameter 
     * is defined. If the parameter is not explicitly set anywhere up the hierarchy,
     * the default value is returned.  
     * 
     * @param name the parent name.
     * @param defaultValue the default value.
     * 
     * @return a {@link Boolean} that represents the value of the parameter.
     * 
     * @see #setBooleanParameter(String, boolean)
     */
    public boolean getBooleanParameter(final String name, boolean defaultValue); 
    
    /**
     * Assigns a {@link Boolean} to the parameter with the given name
     * 
     * @param name parameter name
     * @param value parameter value
     */ 
    public void setBooleanParameter(final String name, boolean value);

    /**
     * Returns <tt>true</tt> if the parameter is set at any level, <tt>false</tt> otherwise.
     * 
     * @param name parameter name
     * 
     * @return <tt>true</tt> if the parameter is set at any level, <tt>false</tt>
     * otherwise.
     */
    public boolean isParameterSet(final String name);
        
    /**
     * Returns <tt>true</tt> if the parameter is set locally, <tt>false</tt> otherwise.
     * 
     * @param name parameter name
     * 
     * @return <tt>true</tt> if the parameter is set locally, <tt>false</tt>
     * otherwise.
     */
    public boolean isParameterSetLocally(final String name);
        
    /**
     * Returns <tt>true</tt> if the parameter is set and is <tt>true</tt>, <tt>false</tt>
     * otherwise.
     * 
     * @param name parameter name
     * 
     * @return <tt>true</tt> if the parameter is set and is <tt>true</tt>, <tt>false</tt>
     * otherwise.
     */
    public boolean isParameterTrue(final String name);
        
    /**
     * Returns <tt>true</tt> if the parameter is either not set or is <tt>false</tt>, 
     * <tt>false</tt> otherwise.
     * 
     * @param name parameter name
     * 
     * @return <tt>true</tt> if the parameter is either not set or is <tt>false</tt>, 
     * <tt>false</tt> otherwise.
     */
    public boolean isParameterFalse(final String name);

}
