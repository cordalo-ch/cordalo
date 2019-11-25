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
import ch.cordalo.corda.common.contracts.test.TestState;
import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.FlowHelper;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import ch.cordalo.corda.common.flows.SimpleBaseFlow;
import ch.cordalo.corda.common.test.FlowTestSupporter;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class FlowHelperTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setup(
                true,
                TestBaseFlow.class
        );
    }
    @After
    public void tearDown() { super.tearDown(); }

    @Test
    public void test_getLastStatesByCriteria() throws ExecutionException, InterruptedException {
        TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string42", 42);
        SignedTransaction signedTransaction = testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string43", 43);
        testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string44", 44);
        testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string45", 45);
        testNode1.startFlow(flow);

        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                Vault.StateStatus.ALL,
                null);
        FlowHelper<TestState> helper  = new FlowHelper(flow.getServiceHub());
        List<StateAndRef<TestState>> lastStatesByCriteria = helper.getLastStatesByCriteria(TestState.class, queryCriteria, 2);
        Assert.assertEquals("shall be 2", 2, lastStatesByCriteria.size());
        Assert.assertEquals("last element has 45", new Integer(45), lastStatesByCriteria.get(1).getState().getData().getIntValue());
        Assert.assertEquals("2nd last element has 44", new Integer(44), lastStatesByCriteria.get(0).getState().getData().getIntValue());
    }


    @Test
    public void test_getLastStatesByCriteria_not_full_page() throws ExecutionException, InterruptedException {
        TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string42", 42);
        SignedTransaction signedTransaction = testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string43", 43);
        testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string44", 44);
        testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string45", 45);
        testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string46", 46);
        testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string47", 47);
        testNode1.startFlow(flow);
        flow = new TestBaseFlow.Create(testNode2.party, "my string48", 48);
        testNode1.startFlow(flow);

        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                Vault.StateStatus.ALL,
                null);
        FlowHelper<TestState> helper  = new FlowHelper(flow.getServiceHub());
        List<StateAndRef<TestState>> lastStatesByCriteria = helper.getLastStatesByCriteria(TestState.class, queryCriteria, 3);
        Assert.assertEquals("shall be 3", 3, lastStatesByCriteria.size());
        Assert.assertEquals("last element has 48", new Integer(48), lastStatesByCriteria.get(2).getState().getData().getIntValue());
    }


    @Test
    public void validateToHaveAllMethods_Suspendable() {
        Class[] flowClasses = {
                BaseFlow.class,
                FlowHelper.class,
                SimpleBaseFlow.class,
                ResponderBaseFlow.class
        };
        for (Class clazz: flowClasses) {
            FlowTestSupporter.validateAllMethodsMustHaveSuspendable(clazz);
        }
    }

}
