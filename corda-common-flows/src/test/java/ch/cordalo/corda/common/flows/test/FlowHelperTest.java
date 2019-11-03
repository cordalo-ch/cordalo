package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.CordaloTestEnvironment;
import ch.cordalo.corda.common.contracts.test.TestState;
import ch.cordalo.corda.common.flows.FlowHelper;
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

public class FlowHelperTest extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setup(
                true,
                TestBaseFlow.CreateResponder.class,
                TestBaseFlow.UpdateProviderResponder.class
        );
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void start3flow_getLastStatesByCriteria_expectCorrectValues() throws ExecutionException, InterruptedException {
        // Arrange + Act
        TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string42", 42);
        testNode1.startFlow(flow);

        flow = new TestBaseFlow.Create(testNode2.party, "my string43", 43);
        testNode1.startFlow(flow);

        flow = new TestBaseFlow.Create(testNode2.party, "my string44", 44);
        testNode1.startFlow(flow);

        flow = new TestBaseFlow.Create(testNode2.party, "my string45", 45);
        testNode1.startFlow(flow);

        // Assert
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                Vault.StateStatus.ALL,
                null);
        FlowHelper<TestState> helper = new FlowHelper(flow.getServiceHub());
        List<StateAndRef<TestState>> lastStatesByCriteria = helper.getLastStatesByCriteria(TestState.class, queryCriteria, 2);
        Assert.assertEquals("shall be 2", 2, lastStatesByCriteria.size());
        Assert.assertEquals("last element has 45", new Integer(45), lastStatesByCriteria.get(1).getState().getData().getIntValue());
        Assert.assertEquals("2nd last element has 44", new Integer(44), lastStatesByCriteria.get(0).getState().getData().getIntValue());
    }


    @Test
    public void start7flow_getLastStatesByCriteria_not_full_page() throws ExecutionException, InterruptedException {
        // Arrange + Act
        TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string42", 42);
        testNode1.startFlow(flow);
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

        // Assert
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                Vault.StateStatus.ALL,
                null);
        FlowHelper<TestState> helper = new FlowHelper(flow.getServiceHub());
        List<StateAndRef<TestState>> lastStatesByCriteria = helper.getLastStatesByCriteria(TestState.class, queryCriteria, 3);
        Assert.assertEquals("shall be 3", 3, lastStatesByCriteria.size());
        Assert.assertEquals("last element has 48", new Integer(48), lastStatesByCriteria.get(2).getState().getData().getIntValue());
    }


}