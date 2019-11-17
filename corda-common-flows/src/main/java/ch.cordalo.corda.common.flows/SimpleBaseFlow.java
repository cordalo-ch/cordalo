package ch.cordalo.corda.common.flows;

import ch.cordalo.corda.common.states.Parties;
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
        return this.simpleFlow_UpdateBuilder(stateClass, id, new SimpleFlow.UpdateBuilder<T>() {
            @Override
            @Suspendable
            public void updateBuilder(TransactionBuilder transactionBuilder, StateAndRef<T> stateRef, T state, T newState) throws FlowException {
                transactionBuilder.addInputState(stateRef);
                transactionBuilder.addOutputState(newState);
            }

            @Override
            @Suspendable
            public T update(T state) throws FlowException {
                return creator.update(state);
            }
        }, command);
    }


    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_UpdateBuilder(Class<T> stateClass, UniqueIdentifier id, SimpleFlow.UpdateBuilder<T> creator, CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        T newState = creator.update(state);
        Parties parties = new Parties(state, newState);
        getProgressTracker().setCurrentStep(BUILDING);
        TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(parties, command);
        creator.updateBuilder(transactionBuilder, stateRef, state, newState);
        return signSyncCollectAndFinalize(parties.getParties(), transactionBuilder);
    }


    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Delete(Class<T> stateClass, UniqueIdentifier id, SimpleFlow.Delete<T> deleter, CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        deleter.validateToDelete(state);
        getProgressTracker().setCurrentStep(BUILDING);
        TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParticipants(state, command);
        transactionBuilder.addInputState(stateRef);
        // no output state - means consume it
        return signSyncCollectAndFinalize(state.getParticipants(), transactionBuilder);
    }

}
