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
            this.startFlow(testNode1, flow, TestState.class);
        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testUpdate() {
        try {
            TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
            TestState test = this.startFlow(testNode1, flow, TestState.class);

            TestBaseFlow.UpdateProvider flowU = new TestBaseFlow.UpdateProvider(test.getLinearId(), testNode3.party);
            TestState test2 = this.startFlow(testNode1, flowU, TestState.class);

        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
            TestState test = this.startFlow(testNode1, flow, TestState.class);

            TestBaseFlow.Delete flowU = new TestBaseFlow.Delete(test.getLinearId());
            TestState test2 = this.startFlow(testNode1, flowU, TestState.class);

        } catch (FlowException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
