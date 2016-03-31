/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package tests.security.cert;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorSpi;

import org.junit.Test;

/**
 * Tests for <code>CertPathValidatorSpi</code> class constructors and methods.
 *
 */
public class CertPathValidatorSpiTest {
    @Test
    public void testCertPathValidatorSpi_engineValidate() throws Exception {
        CertPathValidatorSpi cpvs = mock(CertPathValidatorSpi.class);
        when(cpvs.engineValidate(null, null)).thenReturn(null);
        assertNull(cpvs.engineValidate(null, null));
        verify(cpvs, times(1)).engineValidate(null, null);
    }

    @Test(expected = CertPathValidatorException.class)
    public void engineValidate_throws_CertPathValidatorException() throws Exception {
        CertPathValidatorSpi cpvs = mock(CertPathValidatorSpi.class);
        doThrow(new CertPathValidatorException()).when(cpvs).engineValidate(null, null);
        cpvs.engineValidate(null, null);
    }

    @Test(expected = InvalidAlgorithmParameterException.class)
    public void engineValidate_throws_InvalidAlgorithmParameterException() throws Exception {
        CertPathValidatorSpi cpvs = mock(CertPathValidatorSpi.class);
        doThrow(new InvalidAlgorithmParameterException()).when(cpvs).engineValidate(null, null);
        cpvs.engineValidate(null, null);
    }

    @Test
    public void engineGetRevocationChecker() throws Exception {
        CertPathValidatorSpi cpvs = mock(CertPathValidatorSpi.class);
        when(cpvs.engineGetRevocationChecker()).thenReturn(null);
        assertNull(cpvs.engineGetRevocationChecker());
        verify(cpvs, times(1)).engineGetRevocationChecker();
    }
}
