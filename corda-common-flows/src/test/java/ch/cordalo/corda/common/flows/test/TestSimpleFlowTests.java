package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.CordaloTestEnvironment;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.FindResponderClasses;
import net.corda.core.flows.FlowException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class TestSimpleFlowTests extends CordaloTestEnvironment {

    @Before
    public void setup() {
        this.setup(
            true,
            FindResponderClasses.find(TestSimpleFlow.class));
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


}
