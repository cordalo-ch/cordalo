/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.flows;

import ch.cordalo.corda.common.states.Parties;
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.*;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowSession;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.serialization.CordaSerializable;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SimpleBaseFlow<S> extends BaseFlow<S> {

    private static final String SEARCH_RESPONDER_NO_STATE_FOUND = "$$-Search-Responder-No-State-Found$$";
    public static final UniqueIdentifier EMPTY_SEARCH_RESULT_LINEAR_ID = new UniqueIdentifier(
            SEARCH_RESPONDER_NO_STATE_FOUND, UUID.fromString("8f247ffb-54f0-49a9-adcd-cb46ceba7fec"));

    private Context context;

    public SimpleBaseFlow() {
        super();
    }


    @CordaSerializable
    public static class Context {
        private final List<ReferencedStateAndRef> referenceStates = new ArrayList<>();
        private final List<StateAndRef> inputStates = new ArrayList<>();
        private final List<ContractState> outputStates = new ArrayList<>();
        private final ServiceHub serviceHub;

        public Context(ServiceHub serviceHub) {
            this.serviceHub = serviceHub;
        }

        @Suspendable
        public <T extends ContractState> T addReferenceState(ReferencedStateAndRef<T> reference) throws FlowException {
            if (reference == null) throw new FlowException("reference must be provided");
            referenceStates.add(reference);
            return reference.getStateAndRef().getState().getData();
        }

        @Suspendable
        public <T extends LinearState> T addReferenceState(Class<T> clazz, UniqueIdentifier id) throws FlowException {
            return this.addReferenceState(new FlowHelper<T>(this.serviceHub).getLastStateByLinearId(clazz, id));
        }

        @Suspendable
        public <T extends ContractState> T addReferenceState(StateAndRef<T> reference) throws FlowException {
            if (reference == null) throw new FlowException("reference must be provided");
            referenceStates.add(reference.referenced());
            return reference.getState().getData();
        }

        @Suspendable
        public <T extends ContractState> T addInputState(StateAndRef<T> input) throws FlowException {
            if (input == null) throw new FlowException("input must be provided");
            inputStates.add(input);
            return input.getState().getData();
        }

        @Suspendable
        public <T extends LinearState> T addInputState(Class<T> clazz, UniqueIdentifier id) throws FlowException {
            return this.addInputState(new FlowHelper<T>(this.serviceHub).getLastStateByLinearId(clazz, id));
        }

        @Suspendable
        public <T extends ContractState> T addOutputState(String contractId, T output) throws FlowException {
            if (output == null) throw new FlowException("output must be provided");
            outputStates.add(output);
            return output;
        }

        @Suspendable
        public boolean hasAnyContext() {
            return !referenceStates.isEmpty() || !inputStates.isEmpty() || !outputStates.isEmpty();
        }

        @Suspendable
        private void addToTransactionBuilder(TransactionBuilder transactionBuilder) {
            if (this.hasAnyContext()) {
                for (ReferencedStateAndRef ref : referenceStates) {
                    transactionBuilder.addReferenceState(ref);
                }
                for (StateAndRef ref : inputStates) {
                    transactionBuilder.addInputState(ref);
                }
                for (ContractState state : outputStates) {
                    transactionBuilder.addOutputState(state);
                }
            }
        }
    }

    @Suspendable
    public Context getContext() {
        if (this.context == null) {
            this.context = new Context(this.getServiceHub());
        }
        return this.context;
    }

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Create(SimpleFlow.Create<T> creator,
                                                                         CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(progress.PREPARATION);
        try {
            ContractState state = creator.create();
            getProgressTracker().setCurrentStep(progress.BUILDING);
            TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParticipants(
                    state,
                    command);
            this.getContext().addToTransactionBuilder(transactionBuilder);
            transactionBuilder.addOutputState(state);
            return signSyncCollectAndFinalize(state.getParticipants(), transactionBuilder);
        } catch (FlowException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowException("Error in creator", e);
        }
    }

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Update(Class<T> stateClass, UniqueIdentifier id,
                                                                         SimpleFlow.Update<T> creator,
                                                                         CommandData command) throws FlowException {
        return this.simpleFlow_UpdateBuilder(stateClass, id, new SimpleFlow_UpdateBuilder<T>(creator, command));
    }

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_UpdateBuilder(Class<T> stateClass, UniqueIdentifier id, SimpleFlow.UpdateBuilder<T> creator) throws FlowException {
        getProgressTracker().setCurrentStep(progress.PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        T newState = creator.update(state);
        Parties parties = new Parties(state, newState);
        getProgressTracker().setCurrentStep(progress.BUILDING);

        CommandData command = creator.getCommand(stateRef, state, newState);
        TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(parties, command);
        this.getContext().addToTransactionBuilder(transactionBuilder);
        creator.updateBuilder(transactionBuilder, stateRef, state, newState);

        return signSyncCollectAndFinalize(parties.getParties(), transactionBuilder);
    }

    @Suspendable
    public <T extends ContractState> SignedTransaction simpleFlow_Delete(Class<T> stateClass,
                                                                         UniqueIdentifier id,
                                                                         SimpleFlow.Delete<T> deleter,
                                                                         CommandData command) throws FlowException {
        getProgressTracker().setCurrentStep(progress.PREPARATION);
        StateAndRef<T> stateRef = this.getLastStateByLinearId(stateClass, id);
        T state = this.getStateByRef(stateRef);
        deleter.validateToDelete(state);
        getProgressTracker().setCurrentStep(progress.BUILDING);
        TransactionBuilder transactionBuilder = this.getTransactionBuilderSignedByParticipants(state, command);
        this.getContext().addToTransactionBuilder(transactionBuilder);
        transactionBuilder.addInputState(stateRef);
        // no output state - means consume it
        return signSyncCollectAndFinalize(state.getParticipants(), transactionBuilder);
    }

    private boolean isNullPayload(UniqueIdentifier id) {
        return EMPTY_SEARCH_RESULT_LINEAR_ID.equals(id) && (SEARCH_RESPONDER_NO_STATE_FOUND.equals(id.getExternalId()));
    }

    @Suspendable
    protected <T extends LinearState, V extends Object> T simpleFlow_Search(Class<T> stateClass,
                                                                            SimpleFlow.Search<T, V> searcher,
                                                                            Party counterParty) throws FlowException {
        FlowHelper<T> flowHelper = new FlowHelper<>(this.getServiceHub());

        V valueToSearch = searcher.getValueToSearch();
        /* search on local vault if already shared */
        T localState = searcher.search(flowHelper, searcher.getValueToSearch());
        if (localState != null) {
            return localState;
        }

        /* initiate flow at counter-party to get LinearId from vault after successful sharing within responder */
        FlowSession flowSession = this.initiateFlow(counterParty);
        UniqueIdentifier receivedLinearId = flowSession.sendAndReceive(UniqueIdentifier.class, valueToSearch).unwrap(itsId -> {
            return itsId;
        });

        /* linear id not found at counter party */
        if (receivedLinearId == null || isNullPayload(receivedLinearId)) {
            return null;
        }

        /* state found and synchronized with linear Id */
        StateAndRef<T> receivedStateByLinearId = flowHelper
                .getLastStateByLinearId(stateClass, receivedLinearId);
        if (receivedStateByLinearId == null) {
            throw new FlowException(MessageFormat.format("state not found in vault after search & share id={0}", receivedLinearId));
        }
        return receivedStateByLinearId.getState().getData();
    }



    @Suspendable
    protected <T extends LinearState, V extends Object> T simpleFlow_SearchById(Class<T> stateClass,
                                                                              UniqueIdentifier id,
                                                                              Party counterParty) throws FlowException {
        return this.simpleFlow_Search(stateClass,
                new SimpleFlow_SearchByUniqueIdentifier<>(stateClass, id),
                counterParty);
    }





    private static class SimpleFlow_UpdateBuilder<X extends ContractState> implements SimpleFlow.UpdateBuilder<X> {
        private final SimpleFlow.Update<X> creator;
        private final CommandData command;

        public SimpleFlow_UpdateBuilder(SimpleFlow.Update<X> creator, CommandData command) {
            this.creator = creator;
            this.command = command;
        }

        @Override
        @Suspendable
        public X update(X state) throws FlowException {
            return this.creator.update(state);
        }

        @Override
        @Suspendable
        public CommandData getCommand(StateAndRef<X> stateRef, X state, X newState) throws FlowException {
            return this.command;
        }

        @Override
        @Suspendable
        public void updateBuilder(TransactionBuilder transactionBuilder,
                                  StateAndRef<X> stateRef,
                                  X state,
                                  X newState) throws FlowException {
            transactionBuilder.addInputState(stateRef);
            transactionBuilder.addOutputState(newState);
        }
    }

    private static class SimpleFlow_SearchByUniqueIdentifier<T extends LinearState, V extends UniqueIdentifier> implements SimpleFlow.Search<T, UniqueIdentifier> {

        private final Class<T> stateClass;
        private final UniqueIdentifier id;

        public SimpleFlow_SearchByUniqueIdentifier(Class<T> stateClass, UniqueIdentifier id) {
            this.stateClass = stateClass;
            this.id = id;
        }

        @Override
        public T search(FlowHelper<T> flowHelper, UniqueIdentifier valueToSearch) throws FlowException {
            StateAndRef<T> lastStateByLinearId = flowHelper.getLastStateByLinearId(this.stateClass, valueToSearch);
            return (lastStateByLinearId == null) ? null : lastStateByLinearId.getState().getData();
        }

        @Override
        @Suspendable
        public UniqueIdentifier getValueToSearch() {
            return this.id;
        }
    }

}
