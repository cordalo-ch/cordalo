package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.test.TestContract;
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

public class TestBaseFlow  {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends SimpleBaseFlow implements SimpleFlow.Create<TestState> {
        private final Party provider;
        private final String value;
        private final Integer intValue;

        public Create(Party provider, String value, Integer intValue) {
            this.provider = provider;
            this.value = value;
            this.intValue = intValue;
        }
        @Override
        @Suspendable
        public TestState create() {
            return new TestState(
                    new UniqueIdentifier(),
                    getOurIdentity(),
                    this.provider,
                    this.value,
                    this.intValue);
        }

        @Suspendable
        @Override
        public SignedTransaction call () throws FlowException {
            return this.simpleFlow_Create(
                    this,
                new TestContract.Commands.CreateSingleOperators());
       }

    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class UpdateProvider extends SimpleBaseFlow implements SimpleFlow.Update<TestState> {
        private final UniqueIdentifier id;
        private final Party provider;

        public UpdateProvider(UniqueIdentifier id, Party provider) {
            this.id = id;
            this.provider = provider;
        }

        @Override
        @Suspendable
        public TestState update(TestState state) {
            return state.withProvider(this.provider);
        }

        @Suspendable
        @Override
        public SignedTransaction call () throws FlowException {
            return this.simpleFlow_Update(
                    TestState.class,
                    this.id,
                    this,
                    new TestContract.Commands.UpdateOneInOut()
            );
        }

    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Delete extends SimpleBaseFlow {
        private final UniqueIdentifier id;
        public Delete(UniqueIdentifier id) {
            this.id = id;
        }
        @Suspendable
        @Override
        public SignedTransaction call () throws FlowException {
            return this.simpleFlow_Delete(
                    TestState.class,
                    this.id,
                    new TestContract.Commands.Delete()
            );
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


    @InitiatedBy(UpdateProvider.class)
    public static class UpdateProviderResponder extends ResponderBaseFlow<TestState> {

        public UpdateProviderResponder(FlowSession otherFlow) {
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
