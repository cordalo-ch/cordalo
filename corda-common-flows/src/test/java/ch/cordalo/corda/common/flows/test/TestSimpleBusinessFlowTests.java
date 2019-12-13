/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.CordaloTestEnvironment;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import net.corda.core.flows.FlowException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class TestSimpleBusinessFlowTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setupWithFlows(
                true,
                TestSimpleFlow.class,
                TestSimpleBusinessFlow.class);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }


    private TestSimplePojo newSimpleBusiness(CordaNodeEnvironment env, String key, String value) throws FlowException {
        return this.startFlowAndObject(
                env,
                new TestSimpleBusinessFlow.Create(key, value, new ArrayList<>()));
    }


    @Test
    public void testBusinessFlowCreate() {
        try {
            TestSimplePojo testSimplePojo = newSimpleBusiness(this.testNode1, "Test1", "Value");
            TestSimpleState test = testSimplePojo.getSimpleState();
            Assert.assertNotNull("test object found", test);
            Assert.assertEquals("key is there", "Test1", test.getKey());
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


}
