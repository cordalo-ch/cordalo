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

import ch.cordalo.corda.common.contracts.test.TestSimpleContract;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.contracts.test.TestState;
import ch.cordalo.corda.common.flows.*;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CordaloFlowVerifier
public class TestSimpleFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Create<TestSimpleState> {

        private String key;
        private String value;
        private List<Party> partners;

        public Create(String key, String value, List<Party> partners) {
            this.key = key;
            this.value = value;
            this.partners = partners;
        }

        @Override
        @Suspendable
        public TestSimpleState create() throws FlowException {
            return new TestSimpleState(new UniqueIdentifier(), this.getOurIdentity(), this.key, this.value, this.partners);
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Create(this,
                    new TestSimpleContract.Commands.Create());
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Update extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Update<TestSimpleState> {

        private final UniqueIdentifier id;
        private final String key;
        private final String value;

        public Update(UniqueIdentifier id, String key, String value) {
            this.id = id;
            this.key = key;
            this.value = value;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(TestSimpleState.class, this.id, this,
                    new TestSimpleContract.Commands.Update() );
        }

        @Override
        @Suspendable
        public TestSimpleState update(TestSimpleState state) throws FlowException {
            return state.update(this.key, this.value);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Share extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Update<TestSimpleState> {

        private UniqueIdentifier id;
        private Party partner;

        public Share(UniqueIdentifier id, Party partner) {
            this.id = id;
            this.partner = partner;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(TestSimpleState.class, this.id, this,
                    new TestSimpleContract.Commands.Share() );
        }

        @Override
        @Suspendable
        public TestSimpleState update(TestSimpleState state) throws FlowException {
            return state.share(this.partner);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Delete extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Delete<TestSimpleState> {

        private UniqueIdentifier id;

        public Delete(UniqueIdentifier id) {
            this.id = id;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Delete(
                    TestSimpleState.class,
                    this.id,
                    this,
                    new TestSimpleContract.Commands.Delete());
        }

        @Override
        @Suspendable
        public void validateToDelete(TestSimpleState state) throws FlowException {

        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Search extends SimpleBaseFlow<TestSimpleState> {

        @NotNull
        private final UniqueIdentifier id;
        @NotNull
        private final Party owner;

        public Search(@NotNull UniqueIdentifier id, @NotNull Party owner) {
            this.id = id;
            this.owner = owner;
        }

        @Suspendable
        @Override
        public TestSimpleState call() throws FlowException {

            FlowHelper<TestSimpleState> flowHelper = new FlowHelper<>(this.getServiceHub());

            /* search on local vault if already shared */
            StateAndRef<TestSimpleState> localStateByLinearId =
                    flowHelper.getLastStateByLinearId(TestSimpleState.class, this.id);
            if (localStateByLinearId != null) {
                return localStateByLinearId.getState().getData();
            }

            /* initiate flow at counterparty to get LinearId from vaul after successful sharing within responder */
            FlowSession flowSession = this.initiateFlow(this.owner);
            UniqueIdentifier receivedLinearId = flowSession.sendAndReceive(UniqueIdentifier.class, this.id).unwrap(id -> {
                return id;
            });

            /* linear id not found at counter party */
            if (receivedLinearId == null) {
                return null;
            }
            /* state found and synched with linear Id */
            StateAndRef<TestSimpleState> receivedStateByLinearId = flowHelper
                    .getLastStateByLinearId(TestSimpleState.class, receivedLinearId);
            if (receivedStateByLinearId == null) {
                throw new FlowException("state not found in vault after search & share id="+receivedLinearId);
            }
            return receivedStateByLinearId.getState().getData();
        }

    }

    @InitiatedBy(Create.class)
    public static class CreateResponder extends ResponderBaseFlow<TestState> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(Update.class)
    public static class UpdateResponder extends ResponderBaseFlow<TestState> {

        public UpdateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(Share.class)
    public static class ShareResponder extends ResponderBaseFlow<TestState> {

        public ShareResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(Delete.class)
    public static class DeleteResponder extends ResponderBaseFlow<TestState> {

        public DeleteResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }



    /* running in counter party node */
    @InitiatedBy(Search.class)
    public static class SearchResponder extends ResponderBaseFlow<TestSimpleState> {

        public SearchResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        private static UniqueIdentifier unwrapper(UniqueIdentifier data) { return data; };

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            FlowHelper<TestSimpleState> flowHelper = new FlowHelper<>(this.getServiceHub());

            /* receive the requested StammNr from sender */
            UniqueIdentifier searchLinearId = this.otherFlow.receive(UniqueIdentifier.class).unwrap(SearchResponder::unwrapper);

            /* search unconsumed state by linear id */
            StateAndRef<TestSimpleState> localStateByLinearId =
                    flowHelper.getLastStateByLinearId(TestSimpleState.class, searchLinearId);
            if (localStateByLinearId == null) {
                /* send no result back to sender */
                this.otherFlow.send(null);
                return null;
            }

            TestSimpleState state = localStateByLinearId.getState().getData();

            /* try to share officially with corda the state and send back. Using this principle.
            Updates will be shared in the future --> sigle point of truth and not copy */
            Share shareFlow = new Share(state.getLinearId(), this.otherFlow.getCounterparty());
            subFlow(shareFlow);

            /* send back state linear id to counter party */
            this.otherFlow.send(state.getLinearId());
            return null;
        }
    }

}