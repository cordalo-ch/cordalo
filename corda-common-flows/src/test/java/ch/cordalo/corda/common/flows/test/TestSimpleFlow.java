package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.test.TestSimpleContract;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.contracts.test.TestState;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import ch.cordalo.corda.common.flows.SimpleBaseFlow;
import ch.cordalo.corda.common.flows.SimpleFlow;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;

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
}