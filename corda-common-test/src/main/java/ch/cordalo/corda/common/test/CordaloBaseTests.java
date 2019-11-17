package ch.cordalo.corda.common.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Lists;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;
import java.util.concurrent.ExecutionException;

abstract public class CordaloBaseTests {

    public abstract CordaTestNetwork setup(boolean withNodes, List<Class<? extends FlowLogic>> responderClasses);
    public abstract CordaTestNetwork getNetwork();

    public CordaTestNetwork setup(boolean withNodes, Class responderMainClasses) {
        return setup(withNodes, FindResponderClasses.find(responderMainClasses));
    }
    public CordaTestNetwork setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses) {
        return setup(withNodes, Lists.newArrayList(responderClasses));
    }
    public void tearDown() {
        if (this.getNetwork() != null) this.getNetwork().stopNodes();
    };

    @Suspendable
    public <T extends ContractState> T startFlowAndResult(
            CordaNodeEnvironment env,
            FlowLogic<SignedTransaction> flow,
            Class<T> stateClass) throws FlowException {
        StateVerifier verifier = startFlow(env, flow);
        List<T> objects = verifier.output().objects();
        if (objects.size() > 0) {
            return verifier
                    .output()
                    .one()
                    .one(stateClass)
                    .object();
        } else {
            return null;
        }
    }
    @Suspendable
    public <T extends ContractState> List<T> startFlowAndResults(
            CordaNodeEnvironment env,
            FlowLogic<SignedTransaction> flow,
            Class<T> stateClass) throws FlowException {
        StateVerifier verifier = startFlow(env, flow);
        return verifier
                .output()
                .filter(stateClass)
                .objects();
    }



    @Suspendable
    public StateVerifier startFlow(
            CordaNodeEnvironment env,
            FlowLogic<SignedTransaction> flow) throws FlowException {
        CordaFuture<SignedTransaction> future = env.node.startFlow(flow);
        env.network.runNetwork();
        try {
            SignedTransaction tx = future.get();
            return StateVerifier.fromTransaction(tx, env.ledgerServices);
        } catch (InterruptedException e) {
            throw new FlowException("InterruptedException while start flow", e);
        } catch (ExecutionException e) {
            throw new FlowException("ExecutionException while start flow", e);
        }
    }
}
