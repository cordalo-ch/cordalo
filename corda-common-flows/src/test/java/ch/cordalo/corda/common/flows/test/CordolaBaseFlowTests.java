package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaloBaseTests;
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;

import java.util.concurrent.ExecutionException;

public abstract class CordolaBaseFlowTests extends CordaloBaseTests {

    @Suspendable
    public <T extends ContractState> T startFlow(
            CordaNodeEnvironment env,
            FlowLogic<SignedTransaction> flow,
            Class<T> stateClass) throws FlowException {
        CordaFuture<SignedTransaction> future = env.node.startFlow(flow);
        env.network.runNetwork();
        try {
            SignedTransaction tx = future.get();
            StateVerifier verifier = StateVerifier.fromTransaction(tx, env.ledgerServices);
            return verifier
                    .output()
                    .one()
                    .one(stateClass)
                    .object();
        } catch (InterruptedException e) {
            throw new FlowException("InterruptedException while start flow", e);
        } catch (ExecutionException e) {
            throw new FlowException("ExecutionException while start flow", e);
        }
    }


}
