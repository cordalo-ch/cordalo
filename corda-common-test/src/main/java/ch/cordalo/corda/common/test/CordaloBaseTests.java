package ch.cordalo.corda.common.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;
import java.util.concurrent.ExecutionException;

abstract public class CordaloBaseTests {

    public abstract CordaTestNetwork setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses);
    public CordaTestNetwork setup(boolean withNodes, List<Class<? extends FlowLogic>> responderClasses) {
        Class<? extends FlowLogic>[] list = new Class[responderClasses.size()];
        responderClasses.toArray(list);
        return setup(withNodes, list);
    }
    public abstract CordaTestNetwork getNetwork();
    public void tearDown() {
        if (this.getNetwork() != null) this.getNetwork().stopNodes();
    };

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
            List<ContractState> objects = verifier.output().objects();
            if (objects.size() > 0) {
                return verifier
                        .output()
                        .one()
                        .one(stateClass)
                        .object();
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            throw new FlowException("InterruptedException while start flow", e);
        } catch (ExecutionException e) {
            throw new FlowException("ExecutionException while start flow", e);
        }
    }

}
