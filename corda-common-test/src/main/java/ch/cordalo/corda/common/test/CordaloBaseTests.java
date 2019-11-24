/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
    public <T extends ContractState> T startFlowAndState(CordaNodeEnvironment env, FlowLogic<T> flow) throws FlowException {
        CordaFuture<T> future = env.node.startFlow(flow);
        env.network.runNetwork();
        try {
            return future.get();
        } catch (InterruptedException var5) {
            throw new FlowException("InterruptedException while start flow", var5);
        } catch (ExecutionException var6) {
            throw new FlowException("ExecutionException while start flow", var6);
        }
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
