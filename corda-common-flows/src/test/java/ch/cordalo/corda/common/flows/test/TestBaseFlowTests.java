/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.CordaloTestEnvironment;
import ch.cordalo.corda.common.contracts.test.TestState;
import ch.cordalo.corda.common.test.FindResponderClasses;
import net.corda.core.flows.FlowException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBaseFlowTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setup(
            true,
            FindResponderClasses.find(TestBaseFlow.class));
    }
    @After
    public void tearDown() { super.tearDown(); }

    @Test
    public void testCreate()  {
        try {
            TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
            TestState testState = this.startFlowAndResult(testNode1, flow, TestState.class);
            Assert.assertNotNull("test object found", testState);
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testUpdate() {
        try {
            TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
            TestState test = this.startFlowAndResult(testNode1, flow, TestState.class);

            TestBaseFlow.UpdateProvider flowU = new TestBaseFlow.UpdateProvider(test.getLinearId(), testNode3.party);
            TestState test2 = this.startFlowAndResult(testNode1, flowU, TestState.class);

        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
            TestState test = this.startFlowAndResult(testNode1, flow, TestState.class);

            TestBaseFlow.Delete flowU = new TestBaseFlow.Delete(test.getLinearId());
            TestState test2 = this.startFlowAndResult(testNode1, flowU, TestState.class);

        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testShare() {
        try {
            TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
            TestState test = this.startFlowAndResult(testNode1, flow, TestState.class);

            TestBaseFlow.Delete flowU = new TestBaseFlow.Delete(test.getLinearId());
            TestState test2 = this.startFlowAndResult(testNode1, flowU, TestState.class);

        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
