package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.CordaloTestEnvironment;
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.corda.common.contracts.test.TestState;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class TestBaseFlowTests extends CordaloTestEnvironment {


    @Before
    public void setup() {
        this.setup(
                true,
                TestBaseFlow.CreateResponder.class,
                TestBaseFlow.UpdateProviderResponder.class

        );
    }
    @After
    public void tearDown() { super.tearDown(); }

    @Test
    public void testCreate() throws ExecutionException, InterruptedException {
        TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
        SignedTransaction signedTransaction = testNode1.startFlow(flow);
        StateVerifier stateVerifier = StateVerifier.fromTransaction(signedTransaction, flow.getServiceHub());
        stateVerifier.output().one();

    }


    @Test
    public void testUpdate() throws ExecutionException, InterruptedException {
        TestBaseFlow.Create flow = new TestBaseFlow.Create(testNode2.party, "my string", 42);
        SignedTransaction signedTransaction = testNode1.startFlow(flow);
        StateVerifier stateVerifier = StateVerifier.fromTransaction(signedTransaction, flow.getServiceHub());
        TestState test = stateVerifier.output().one().object();

        TestBaseFlow.UpdateProvider flowU = new TestBaseFlow.UpdateProvider(test.getLinearId(), testNode3.party);
        SignedTransaction signedTransaction2 = testNode1.startFlow(flowU);
        StateVerifier stateVerifier2 = StateVerifier.fromTransaction(signedTransaction2, flow.getServiceHub());
        TestState test2 = stateVerifier2.output().one().object();


    }


}
