package ch.cordalo.corda.common.flows;

import ch.cordalo.corda.ext.Participants;
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

public abstract class SimpleBaseFlow extends BaseFlow {

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Create(SimpleFlow.Create<T> creator, CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(PREPARATION);
        try {
            ContractState state = creator.create();
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParticipants(
                    state,
                    command);
            transactionBuilder.addOutputState(state);
            return signSyncCollectAndFinalize(state.getParticipants(), transactionBuilder);
        } catch (FlowException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowException("Error in creator", e);
        }
    }

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Update(Class<T> stateClass, UniqueIdentifier id, SimpleFlow.Update<T> creator, CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        T newState = creator.update(state);
        Participants participants = new Participants(state, newState);
        getProgressTracker().setCurrentStep(BUILDING);
        TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(participants, command);
        transactionBuilder.addInputState(stateRef);
        transactionBuilder.addOutputState(newState);
        return signSyncCollectAndFinalize(participants.getParties(), transactionBuilder);
    }

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Delete(Class<T> stateClass, UniqueIdentifier id, CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        getProgressTracker().setCurrentStep(BUILDING);
        TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParticipants(state, command);
        transactionBuilder.addInputState(stateRef);
        // no output state - means consume it
        return signSyncCollectAndFinalize(state.getParticipants(), transactionBuilder);
    }

}
