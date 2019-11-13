package ch.cordalo.corda.common.flows.test;

import ch.cordalo.corda.common.contracts.test.TestContract;
import ch.cordalo.corda.common.contracts.test.TestState;
import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

public class TestBaseFlow  {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends BaseFlow {
        private final Party provider;
        private final String value;
        private final Integer intValue;

        public Create(Party provider, String value, Integer intValue) {
            this.provider = provider;
            this.value = value;
            this.intValue = intValue;
        }
        @Override
        public ProgressTracker getProgressTracker () {
            return this.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call () throws FlowException {
            getProgressTracker().setCurrentStep(PREPARATION);
            // We get a reference to our own identity.
            Party me = getOurIdentity();

            /* ============================================================================
             *         Task 1 - Create our object !
             * ===========================================================================*/
            // We create our new Object
            TestState test = new TestState(
                    new UniqueIdentifier(),
                    me,
                    this.provider,
                    this.value,
                    this.intValue);

            /* ============================================================================
             *      Task 2 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParticipants(
                    test,
                    new TestContract.Commands.CreateSingleOperators());
            transactionBuilder.addOutputState(test);

            /* ============================================================================
             *          Task 3 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signSyncCollectAndFinalize(test.getParticipants(), transactionBuilder);
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



    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class UpdateProvider extends BaseFlow {
        private final UniqueIdentifier id;
        private final Party provider;

        public UpdateProvider(UniqueIdentifier id, Party provider) {
            this.id = id;
            this.provider = provider;
        }
        @Override
        public ProgressTracker getProgressTracker () {
            return this.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call () throws FlowException {
            getProgressTracker().setCurrentStep(PREPARATION);
            // We get a reference to our own identity.
            Party me = getOurIdentity();

            /* ============================================================================
             *         Task 1 - Create our object !
             * ===========================================================================*/
            // We create our new Object
            StateAndRef<TestState> testRef = this.getLastStateByLinearId(TestState.class, this.id);
            TestState test = this.getStateByRef(testRef);
            TestState newTest = test.withProvider(this.provider);

            /* ============================================================================
             *      Task 2 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            List<AbstractParty> list = test.getParticipants();
            list.addAll(newTest.getParticipants());
            TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParties(
                    list, new TestContract.Commands.UpdateOneInOut());
            transactionBuilder.addInputState(testRef);
            transactionBuilder.addOutputState(newTest);

            /* ============================================================================
             *          Task 3 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signSyncCollectAndFinalize(list, transactionBuilder);
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



}
