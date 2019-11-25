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
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class TestSimpleFlowTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setup(
            true,
            TestSimpleFlow.class);
    }
    @After
    public void tearDown() { super.tearDown(); }

    private TestSimpleState newSimple(CordaNodeEnvironment env, String key, String value) throws FlowException {
        return this.startFlowAndResult(
                env,
                new TestSimpleFlow.Create(key, value, new ArrayList<>()),
                TestSimpleState.class);
    }

    private TestSimpleState newUpdate(CordaNodeEnvironment env, TestSimpleState state, String key, String value) throws FlowException {
        return this.startFlowAndResult(
                env,
                new TestSimpleFlow.Update(state.getLinearId(), key, value),
                TestSimpleState.class);
    }
    private TestSimpleState newShare(CordaNodeEnvironment env, TestSimpleState state, CordaNodeEnvironment partner) throws FlowException {
        return this.startFlowAndResult(
                env,
                new TestSimpleFlow.Share(state.getLinearId(), partner.party),
                TestSimpleState.class);
    }
    private StateVerifier newDelete(CordaNodeEnvironment env, TestSimpleState state) throws FlowException {
        return this.startFlow(
                env,
                new TestSimpleFlow.Delete(state.getLinearId()));
    }
    private TestSimpleState newSearch(CordaNodeEnvironment env, UniqueIdentifier linearId, CordaNodeEnvironment partner) throws FlowException {
        return this.startFlowAndState(
                env,
                new TestSimpleFlow.Search(linearId, partner.party));
    }

    @Test
    public void testCreate()  {
        try {
            TestSimpleState test = newSimple(this.testNode1, "Test1", "Value");
            Assert.assertNotNull("test object found", test);
            Assert.assertEquals("key is there", "Test1", test.getKey());
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testUpdate()  {
        try {
            TestSimpleState test = newSimple(this.testNode1, "Test1", "Value1");
            TestSimpleState update = newUpdate(this.testNode1, test, "Test2", "Value2");
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testShare()  {
        try {
            TestSimpleState test = newSimple(this.testNode1, "Test1", "Value");
            TestSimpleState shared = newShare(this.testNode1, test, this.testNode2);
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSearch()  {
        try {
            /* save state in Node 1 */
            String randomKey = "RandomKey-"+new Random().nextInt();
            TestSimpleState test = newSimple(this.testNode1, randomKey, "Value");

            /* search state from Node 2 via Node 1 using linear id */
            TestSimpleState receivedState = newSearch(this.testNode2, test.getLinearId(), this.testNode1);
            Assert.assertEquals("received key is = to random key from other node", randomKey, receivedState.getKey());

        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testDelete()  {
        try {
            TestSimpleState test = newSimple(this.testNode1, "Test1", "Value");
            StateVerifier stateVerifier = newDelete(this.testNode1, test);
            stateVerifier.output().empty();
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testDelete_aftershare()  {
        try {
            TestSimpleState test = newSimple(this.testNode1, "Test1", "Value");
            TestSimpleState shared = newShare(this.testNode1, test, this.testNode2);
            StateVerifier stateVerifier = newDelete(this.testNode1, shared);
            stateVerifier.output().empty();
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


}
