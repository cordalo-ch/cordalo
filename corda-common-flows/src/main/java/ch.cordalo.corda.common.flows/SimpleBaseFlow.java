/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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

public abstract class SimpleBaseFlow<S> extends BaseFlow<S> {

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Create(SimpleFlow.Create<T> creator, CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(progress.PREPARATION);
        try {
            ContractState state = creator.create();
            getProgressTracker().setCurrentStep(progress.BUILDING);
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
        getProgressTracker().setCurrentStep(progress.PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        T newState = creator.update(state);
        Parties parties = new Parties(state, newState);
        getProgressTracker().setCurrentStep(progress.BUILDING);
        TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(parties, command);
        creator.updateBuilder(transactionBuilder, stateRef, state, newState);
        return signSyncCollectAndFinalize(parties.getParties(), transactionBuilder);
    }


    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Delete(Class<T> stateClass, UniqueIdentifier id, SimpleFlow.Delete<T> deleter, CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(progress.PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        deleter.validateToDelete(state);
        getProgressTracker().setCurrentStep(progress.BUILDING);
        TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParticipants(state, command);
        transactionBuilder.addInputState(stateRef);
        // no output state - means consume it
        return signSyncCollectAndFinalize(state.getParticipants(), transactionBuilder);
    }

}
