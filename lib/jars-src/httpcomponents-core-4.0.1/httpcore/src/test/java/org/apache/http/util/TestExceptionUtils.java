/*
 * $HeadURL: https://svn.apache.org/repos/asf/httpcomponents/httpcore/tags/4.0.1/httpcore/src/test/java/org/apache/http/util/TestExceptionUtils.java $
 * $Revision: 744517 $
 * $Date: 2009-02-14 17:39:33 +0100 (Sat, 14 Feb 2009) $
 * 
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for {@link TestExceptionUtils}.
 *
 */
public class TestExceptionUtils extends TestCase {

    public TestExceptionUtils(String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestExceptionUtils.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        return new TestSuite(TestExceptionUtils.class);
    }
    
    public void testExceptionChaining() throws Exception {
        Exception ex1 = new Exception(); 
        Exception ex2 = new Exception();
        ExceptionUtils.initCause(ex1, ex2);
    }
    
}
