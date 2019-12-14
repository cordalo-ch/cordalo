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
import ch.cordalo.corda.common.contracts.test.TestSimpleSchemaV1;
import ch.cordalo.corda.common.contracts.test.TestSimpleState;
import ch.cordalo.corda.common.flows.*;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.FieldInfo;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.corda.core.node.services.vault.QueryCriteriaUtils.getField;

@CordaloFlowVerifier
public class TestSimpleFlow {


    @NotNull
    @Suspendable
    protected static QueryCriteria getKeyCriteria(String key) throws FlowException {
        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        FieldInfo keyField = null;
        try {
            keyField = getField("key", TestSimpleSchemaV1.PersistentTestSimple.class);
        } catch (NoSuchFieldException e) {
            throw new FlowException("error while getting key fields", e);
        }
        CriteriaExpression keyCriteriaExpression = Builder.equal(keyField, key);
        QueryCriteria keyCriteria = new QueryCriteria.VaultCustomQueryCriteria(keyCriteriaExpression);

        return generalCriteria.and(keyCriteria);
    }

    @NotNull
    @Suspendable
    protected static StateAndRef<TestSimpleState> getSimpleByKey(FlowHelper<TestSimpleState> flowHelper, String key) throws FlowException {
        QueryCriteria query = getKeyCriteria(key);
        return flowHelper.getLastStateByCriteria(TestSimpleState.class, query);
    }


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
                    new TestSimpleContract.Commands.Update());
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
                    new TestSimpleContract.Commands.Share());
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
            return this.simpleFlow_SearchById(
                    TestSimpleState.class, this.id, this.owner);
        }
    }



    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class SearchByKey extends SimpleBaseFlow<TestSimpleState> implements SimpleFlow.Search<TestSimpleState, String> {

        @NotNull
        private final String key;
        @NotNull
        private final Party owner;

        public SearchByKey(@NotNull String key, @NotNull Party owner) {
            this.key = key;
            this.owner = owner;
        }

        @Suspendable
        @Override
        public TestSimpleState call() throws FlowException {
            return this.simpleFlow_Search(
                    TestSimpleState.class, this, this.owner);
        }

        @Override
        @Suspendable
        public TestSimpleState search(FlowHelper<TestSimpleState> flowHelper, String valueToSearch) throws FlowException{
            StateAndRef<TestSimpleState> simpleByKey = getSimpleByKey(flowHelper, this.key);
            return (simpleByKey == null) ? null : simpleByKey.getState().getData();
        }

        @Override
        @Suspendable
        public String getValueToSearch() {
            return this.key;
        }
    }

    @InitiatedBy(Create.class)
    public static class CreateResponder extends ResponderBaseFlow<TestSimpleState> {

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
    public static class UpdateResponder extends ResponderBaseFlow<TestSimpleState> {

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
    public static class ShareResponder extends ResponderBaseFlow<TestSimpleState> {

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
    public static class DeleteResponder extends ResponderBaseFlow<TestSimpleState> {

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
    public static class SearchResponder extends SearchResponderBaseFlow implements SimpleFlow.SearchResponder<TestSimpleState, UniqueIdentifier, SignedTransaction> {

        public SearchResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.responderFlow_receiveAndSend(UniqueIdentifier.class, this);
        }


        @Override
        @Suspendable
        public TestSimpleState search(FlowHelper<TestSimpleState> flowHelper, UniqueIdentifier valueToSearch) throws FlowException {
            StateAndRef<TestSimpleState> stateByLinearId = flowHelper.getLastStateByLinearId(TestSimpleState.class, valueToSearch);
            return (stateByLinearId == null) ? null : stateByLinearId.getState().getData();
        }

        @Override
        @Suspendable
        public FlowLogic<SignedTransaction> createShareStateFlow(TestSimpleState state, Party counterparty) {
            return new Share(state.getLinearId(), counterparty);
        }
    }


    /* running in counter party node */
    @InitiatedBy(SearchByKey.class)
    public static class SearchByKeyResponder extends SearchResponderBaseFlow implements SimpleFlow.SearchResponder<TestSimpleState, String, SignedTransaction> {

        public SearchByKeyResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.responderFlow_receiveAndSend(String.class, this);
        }



        @Override
        @Suspendable
        public TestSimpleState search(FlowHelper<TestSimpleState> flowHelper, String valueToSearch) throws FlowException {
            StateAndRef<TestSimpleState> simpleByKey = getSimpleByKey(flowHelper, valueToSearch);
            return simpleByKey == null ? null : simpleByKey.getState().getData();
        }

        @Override
        @Suspendable
        public FlowLogic<SignedTransaction> createShareStateFlow(TestSimpleState state, Party counterparty) {
            return new Share(state.getLinearId(), this.otherFlow.getCounterparty());
        }
    }

}