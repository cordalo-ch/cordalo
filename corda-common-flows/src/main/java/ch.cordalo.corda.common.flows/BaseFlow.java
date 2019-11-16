package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.corda.confidential.IdentitySyncFlow;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseFlow extends FlowLogic<SignedTransaction> {
    public BaseFlow() {
        super();
    }

    @Suspendable
    protected TransactionBuilder getTransactionBuilderSignedBySigners(ImmutableList<PublicKey> requiredSigner, CommandData command) throws FlowException {
        List<PublicKey> uniqueSigners = new ArrayList<>(Sets.newLinkedHashSet(requiredSigner));
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.setNotary(getFirstNotary());
        transactionBuilder.addCommand(command, uniqueSigners);
        return transactionBuilder;
    }
    @Suspendable
    protected TransactionBuilder getTransactionBuilderSignedByParties(List<AbstractParty> parties, CommandData command) throws FlowException {
        List<AbstractParty> uniqueParties = new ArrayList<>(Sets.newLinkedHashSet(parties));
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.setNotary(getFirstNotary());
        transactionBuilder.addCommand(command, uniqueParties.stream().map(x -> x.getOwningKey()).collect(Collectors.toList()));
        return transactionBuilder;
    }

    @Suspendable
    protected TransactionBuilder getTransactionBuilderSignedByParticipants(ContractState state, CommandData command) throws FlowException {
        List<PublicKey> publicKeys = state.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
        ImmutableList<PublicKey> requiredSigner = new ImmutableList.Builder<PublicKey>()
                .addAll(publicKeys)
                .build();
        return getTransactionBuilderSignedBySigners(requiredSigner, command);
    }

    @Suspendable
    protected TransactionBuilder getMyTransactionBuilderSignedByMe(CommandData command) throws FlowException {
        return getTransactionBuilderSignedBySigners(
                ImmutableList.of(getOurIdentity().getOwningKey()),
                command);
    }

    @Suspendable
    protected SignedTransaction signAndFinalize(TransactionBuilder transactionBuilder) throws FlowException {
        return this.signSyncCollectAndFinalize(false, Collections.EMPTY_LIST, transactionBuilder);
    }

    @Suspendable
    protected SignedTransaction signSyncCollectAndFinalize(Party counterparty, TransactionBuilder transactionBuilder) throws FlowException {
        List<AbstractParty> set = Collections.EMPTY_LIST;
        if (counterparty != null) {
            set = Collections.singletonList(counterparty);
        }
        return signSyncCollectAndFinalize(true, set, transactionBuilder);
    }
    @Suspendable
    protected SignedTransaction signSyncCollectAndFinalize(List<AbstractParty> counterparties, TransactionBuilder transactionBuilder) throws FlowException {
        return signSyncCollectAndFinalize(true, counterparties, transactionBuilder);
    }


    @Suspendable
    protected SignedTransaction signCollectAndFinalize(Party counterparty, TransactionBuilder transactionBuilder) throws FlowException {
        List<AbstractParty> set = Collections.EMPTY_LIST;
        if (counterparty != null) {
            set = Collections.singletonList(counterparty);
        }
        return signCollectAndFinalize(set, transactionBuilder);
    }

    @Suspendable
    protected SignedTransaction signCollectAndFinalize(List<AbstractParty> counterparties, TransactionBuilder transactionBuilder) throws FlowException {
        return signSyncCollectAndFinalize(false, counterparties, transactionBuilder);
    }

    @Suspendable
    private SignedTransaction signSyncCollectAndFinalize(boolean syncIdentities, List<AbstractParty> counterparties, TransactionBuilder transactionBuilder) throws FlowException {
        List<AbstractParty> counterPartiesWithoutMe = new ArrayList<>(Sets.newLinkedHashSet(counterparties));;
        if (counterPartiesWithoutMe.contains(this.getOurIdentity())) {
            counterPartiesWithoutMe.remove(this.getOurIdentity());
        }
        ProgressTracker tracker = this.getProgressTracker();
        tracker.setCurrentStep(VERIFYING);
        transactionBuilder.verify(getServiceHub());

        tracker.setCurrentStep(SIGNING);
        // We sign the transaction with our private key, making it immutable.
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(transactionBuilder);

        Integer otherPartiesFlowVersion = 2;
        Set<FlowSession> otherPartySessions = new LinkedHashSet<>();

        //sync and collect counterPartiesWithoutMe only if counterPartiesWithoutMe exist
        if (counterPartiesWithoutMe != null && !counterPartiesWithoutMe.isEmpty()) {
            // prepare counterPartiesWithoutMe flow sessions to sync and / or collec
            for (int i = 0; i < counterPartiesWithoutMe.size(); i++) {
                AbstractParty counterparty = counterPartiesWithoutMe.get(i);
                if (counterparty instanceof Party) {
                    FlowSession flowSession = initiateFlow((Party)counterparty);
                    otherPartiesFlowVersion = flowSession.getCounterpartyFlowInfo().getFlowVersion();
                    otherPartySessions.add(flowSession);
                }
            }

            // Send any keys and certificates so the signers can verify each other's identity
            if (syncIdentities) {
                tracker.setCurrentStep(SYNCING);
                subFlow(new IdentitySyncFlow.Send(otherPartySessions, signedTx.getTx(), SYNCING.childProgressTracker()));
            }

            // Send the state to all counterPartiesWithoutMe, and receive it back with their signature.
            tracker.setCurrentStep(COLLECTING);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(
                            signedTx,
                            otherPartySessions,
                            ImmutableSet.of(getOurIdentity().getOwningKey()),
                            COLLECTING.childProgressTracker()));
            signedTx = fullySignedTx;
        }
        // We get the transaction notarised and recorded automatically by the platform.
        // send a copy to current issuer
        tracker.setCurrentStep(FINALISING);
        if (otherPartiesFlowVersion == 1) {
            return subFlow(new FinalityFlow(signedTx, Collections.emptyList(), FINALISING.childProgressTracker()));
        } else {
            return subFlow(new FinalityFlow(signedTx, otherPartySessions, FINALISING.childProgressTracker()));
        }
    }

    @Suspendable
    protected Party getFirstNotary() throws FlowException {
        List<Party> notaries = getServiceHub().getNetworkMapCache().getNotaryIdentities();
        if (notaries.isEmpty()) {
            throw new FlowException("No available notary.");
        }
        return notaries.get(0);
    }

    @Suspendable
    protected <T extends ContractState> StateAndRef<T> getLastStateByLinearId(Class<T> stateClass, UniqueIdentifier linearId) throws FlowException {
        StateAndRef stateRef = new FlowHelper<T>(getServiceHub()).getLastStateByLinearId(stateClass, linearId);
        if (stateRef == null) {
            throw new FlowException(String.format("State of class '%s' with id %s not found.", stateClass.getName(), linearId));
        }
        return stateRef;
    }

    @Suspendable
    protected <T extends ContractState>T getStateByRef(StateAndRef<T> ref){
        return ref.getState().getData();
    }

    protected final ProgressTracker.Step PREPARATION = new ProgressTracker.Step("Obtaining data from vault.");
    protected final ProgressTracker.Step BUILDING = new ProgressTracker.Step("Building transaction.");
    protected final ProgressTracker.Step VERIFYING = new ProgressTracker.Step("Verifying transaction.");
    protected final ProgressTracker.Step SIGNING = new ProgressTracker.Step("Signing transaction.");
    protected final ProgressTracker.Step SYNCING = new ProgressTracker.Step("Syncing identities.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return IdentitySyncFlow.Send.Companion.tracker();
        }
    };
    protected final ProgressTracker.Step COLLECTING = new ProgressTracker.Step("Collecting counterparty signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    protected final ProgressTracker.Step FINALISING = new ProgressTracker.Step("Finalising transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    protected final ProgressTracker progressTracker_sync = new ProgressTracker(
            PREPARATION,    // none
            BUILDING,       // none
            VERIFYING,      // none
            SIGNING,        // none
            SYNCING,        // + Identity Sync Flow: Unit / Void
            COLLECTING,     // + Collect Signatures Flow: SignedTransaction
            FINALISING      // + Finality Flow: SignedTransaction
    );
    protected final ProgressTracker progressTracker_nosync = new ProgressTracker(
            PREPARATION,    // none
            BUILDING,       // none
            VERIFYING,      // none
            SIGNING,        // none
            COLLECTING,     // + Collect Signatures Flow: SignedTransaction
            FINALISING      // + Finality Flow: SignedTransaction
    );
    protected final ProgressTracker progressTracker_nosync_nocollect = new ProgressTracker(
            PREPARATION,    // none
            BUILDING,       // none
            VERIFYING,      // none
            SIGNING,        // none
            FINALISING      // + Finality Flow: SignedTransaction
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return this.progressTracker_sync;
    }

    public static class SignTxFlowNoChecking extends SignTransactionFlow {
        public SignTxFlowNoChecking(FlowSession otherFlow, ProgressTracker progressTracker) {
            super(otherFlow, progressTracker);
        }

        @Suspendable
        @Override
        public void checkTransaction(SignedTransaction tx) {
            // no checking
        }
    }


}
