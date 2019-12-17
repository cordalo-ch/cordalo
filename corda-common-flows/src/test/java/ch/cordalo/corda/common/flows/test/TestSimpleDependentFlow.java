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

import ch.cordalo.corda.common.contracts.test.TestSimpleDependentContract;
import ch.cordalo.corda.common.contracts.test.TestSimpleDependentState;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.flows.*;
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;

@CordaloFlowVerifier
public class TestSimpleDependentFlow {


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Create<TestSimpleDependentState> {

        private final UniqueIdentifier simpleId;

        public Create(UniqueIdentifier simpleId) {
            this.simpleId = simpleId;
        }

        @Suspendable
        public TestSimpleDependentState create() throws FlowException {
            FlowHelper<TestSimpleState> flowHelper = new FlowHelper<>(this.getServiceHub());
            StateAndRef<TestSimpleState> stateAndRef = flowHelper.getLastStateByLinearId(TestSimpleState.class, this.simpleId);
            TestSimpleState simpleState = this.getContext().addReferenceState(stateAndRef);
            return new TestSimpleDependentState(new UniqueIdentifier(), simpleState.getLinearId(), simpleState.getOwner(), simpleState.getKey());
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Create(this,
                    new TestSimpleDependentContract.Commands.Create());
        }

    }


    @InitiatedBy(Create.class)
    public static class CreateResponder extends SignedTransactionResponderBaseFlow<TestSimpleDependentState> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            SignedTransaction signedTransaction = this.receiveIdentitiesCounterpartiesNoTxChecking();
            List<ContractState> outputStates = signedTransaction.getTx().getOutputStates();
            return signedTransaction;
        }
    }


}