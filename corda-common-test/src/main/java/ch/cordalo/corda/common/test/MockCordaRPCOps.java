/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.test;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StateMachineRunId;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.*;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NodeDiagnosticInfo;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.NetworkMapCache;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.*;
import net.corda.core.transactions.SignedTransaction;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rx.Observable;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileAlreadyExistsException;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MockCordaRPCOps implements CordaRPCOps {

    private final CordaNodeEnvironment env;

    public MockCordaRPCOps(CordaNodeEnvironment env) {
        this.env = env;
    }

    @NotNull
    @Override
    public NetworkParameters getNetworkParameters() {
        return this.env.ledgerServices.getNetworkParameters();
    }

    @Override
    public void acceptNewNetworkParameters(@NotNull SecureHash parametersHash) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public void addVaultTransactionNote(@NotNull SecureHash txnId, @NotNull String txnNote) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public boolean attachmentExists(@NotNull SecureHash id) {
        return this.env.ledgerServices.getAttachments().hasAttachment(id);
    }

    @Override
    public void clearNetworkMapCache() {
        this.env.ledgerServices.getNetworkMapCache().clearNetworkMapCache();
    }

    @NotNull
    @Override
    public Instant currentNodeTime() {
        throw new NotImplementedException("#currentNodeTime");
    }

    @NotNull
    @Override
    public Iterable<String> getVaultTransactionNotes(@NotNull SecureHash txnId) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Nullable
    @Override
    public SignedTransaction internalFindVerifiedTransaction(@NotNull SecureHash txnId) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public DataFeed<List<SignedTransaction>, SignedTransaction> internalVerifiedTransactionsFeed() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public List<SignedTransaction> internalVerifiedTransactionsSnapshot() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public boolean isFlowsDrainingModeEnabled() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public boolean isWaitingForShutdown() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public boolean killFlow(@NotNull StateMachineRunId id) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public DataFeed<List<NodeInfo>, NetworkMapCache.MapChange> networkMapFeed() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public List<NodeInfo> networkMapSnapshot() {
        return this.env.network.networkMapSnapshot();
    }

    @NotNull
    @Override
    public DataFeed<ParametersUpdateInfo, ParametersUpdateInfo> networkParametersFeed() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public NodeInfo nodeInfo() {
        return this.env.ledgerServices.getMyInfo();
    }

    @Nullable
    @Override
    public NodeInfo nodeInfoFromParty(@NotNull AbstractParty party) {
        return this.env.ledgerServices.getNetworkMapCache().getNodeByLegalIdentity(party);
    }

    @NotNull
    @Override
    public List<Party> notaryIdentities() {
        return this.env.ledgerServices.getNetworkMapCache().getNotaryIdentities();
    }

    @Nullable
    @Override
    public Party notaryPartyFromX500Name(@NotNull CordaX500Name x500Name) {
        return this.env.ledgerServices.getNetworkMapCache().getNotary(x500Name);
    }

    @NotNull
    @Override
    public InputStream openAttachment(@NotNull SecureHash id) {
        return this.env.ledgerServices.getAttachments().openAttachment(id).open();
    }

    @NotNull
    @Override
    public Set<Party> partiesFromName(@NotNull String query, boolean exactMatch) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Nullable
    @Override
    public Party partyFromKey(@NotNull PublicKey key) {
        List<NodeInfo> nodesByLegalIdentityKey = this.env.ledgerServices.getNetworkMapCache().getNodesByLegalIdentityKey(key);
        return nodesByLegalIdentityKey.isEmpty() ? null : nodesByLegalIdentityKey.get(0).getLegalIdentities().get(0);
    }

    @NotNull
    @Override
    public List<SecureHash> queryAttachments(@NotNull AttachmentQueryCriteria query, @Nullable AttachmentSort sorting) {
        return this.env.ledgerServices.getAttachments().queryAttachments(query, sorting);
    }

    @Override
    public void refreshNetworkMapCache() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public List<String> registeredFlows() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public void setFlowsDrainingModeEnabled(boolean enabled) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public void shutdown() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public <T> FlowHandle<T> startFlowDynamic(@NotNull Class<? extends FlowLogic<? extends T>> logicType, @NotNull Object... args) {
        FlowLogic<? extends T> flowLogic = startFlow(logicType, args);
        CordaFuture<T> cordaFuture = this.env.node.startFlow(flowLogic);
        this.env.network.runNetwork();
        return new FlowHandleImpl<T>(flowLogic.getRunId(), cordaFuture);
    }

    private <T> FlowLogic<? extends T> startFlow(@NotNull Class<? extends FlowLogic<? extends T>> logicType, @NotNull Object... args) {
        List<? extends Class<?>> collect = Arrays.stream(args).map(x -> x.getClass()).collect(Collectors.toList());
        Class<?>[] classes = new Class<?>[collect.size()];
        collect.toArray(classes);
        try {
            Constructor<? extends FlowLogic<? extends T>> constructor = logicType.getConstructor(classes);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("error while instantiating flow", e);
        }
    }

    @NotNull
    @Override
    public <T> FlowProgressHandle<T> startTrackedFlowDynamic(@NotNull Class<? extends FlowLogic<? extends T>> logicType, @NotNull Object... args) {
        FlowLogic<? extends T> flowLogic = startFlow(logicType, args);
        CordaFuture<T> cordaFuture = this.env.node.startFlow(flowLogic);
        this.env.network.runNetwork();
        return new FlowProgressHandleImpl<>(
                flowLogic.getRunId(),
                cordaFuture,
                Observable.empty()
        );
    }

    @NotNull
    @Override
    public DataFeed<List<StateMachineTransactionMapping>, StateMachineTransactionMapping> stateMachineRecordedTransactionMappingFeed() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public List<StateMachineTransactionMapping> stateMachineRecordedTransactionMappingSnapshot() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public DataFeed<List<StateMachineInfo>, StateMachineUpdate> stateMachinesFeed() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public List<StateMachineInfo> stateMachinesSnapshot() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Override
    public void terminate(boolean drainPendingFlows) {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @NotNull
    @Override
    public SecureHash uploadAttachment(@NotNull InputStream jar) throws FileAlreadyExistsException {
        return this.env.ledgerServices.getAttachments().importAttachment(jar);
    }

    @NotNull
    @Override
    public SecureHash uploadAttachmentWithMetadata(@NotNull InputStream jar, @NotNull String uploader, @NotNull String filename) throws FileAlreadyExistsException {
        return this.env.ledgerServices.getAttachments().importAttachment(jar, uploader, filename);
    }

    @NotNull
    @Override
    public <T extends ContractState> Vault.Page<T> vaultQuery(@NotNull Class<? extends T> contractStateType) {
        return this.env.ledgerServices.getVaultService().queryBy(contractStateType);
    }

    @NotNull
    @Override
    public <T extends ContractState> Vault.Page<T> vaultQueryBy(@NotNull QueryCriteria criteria, @NotNull PageSpecification paging, @NotNull Sort sorting, @NotNull Class<? extends T> contractStateType) {
        return this.env.ledgerServices.getVaultService().queryBy(contractStateType, criteria, paging, sorting);
    }

    @NotNull
    @Override
    public <T extends ContractState> Vault.Page<T> vaultQueryByCriteria(@NotNull QueryCriteria criteria, @NotNull Class<? extends T> contractStateType) {
        return this.env.ledgerServices.getVaultService().queryBy(contractStateType, criteria);
    }

    @NotNull
    @Override
    public <T extends ContractState> Vault.Page<T> vaultQueryByWithPagingSpec(@NotNull Class<? extends T> contractStateType, @NotNull QueryCriteria criteria, @NotNull PageSpecification paging) {
        return this.env.ledgerServices.getVaultService().queryBy(contractStateType, criteria, paging);
    }

    @NotNull
    @Override
    public <T extends ContractState> Vault.Page<T> vaultQueryByWithSorting(@NotNull Class<? extends T> contractStateType, @NotNull QueryCriteria criteria, @NotNull Sort sorting) {
        return this.env.ledgerServices.getVaultService().queryBy(contractStateType, criteria, sorting);
    }

    @NotNull
    @Override
    public <T extends ContractState> DataFeed<Vault.Page<T>, Vault.Update<T>> vaultTrack(@NotNull Class<? extends T> contractStateType) {
        return this.env.ledgerServices.getVaultService().trackBy(contractStateType);
    }

    @NotNull
    @Override
    public <T extends ContractState> DataFeed<Vault.Page<T>, Vault.Update<T>> vaultTrackBy(@NotNull QueryCriteria criteria, @NotNull PageSpecification paging, @NotNull Sort sorting, @NotNull Class<? extends T> contractStateType) {
        return this.env.ledgerServices.getVaultService().trackBy(contractStateType, criteria, paging, sorting);
    }

    @NotNull
    @Override
    public <T extends ContractState> DataFeed<Vault.Page<T>, Vault.Update<T>> vaultTrackByCriteria(@NotNull Class<? extends T> contractStateType, @NotNull QueryCriteria criteria) {
        return this.env.ledgerServices.getVaultService().trackBy(contractStateType, criteria);
    }

    @NotNull
    @Override
    public <T extends ContractState> DataFeed<Vault.Page<T>, Vault.Update<T>> vaultTrackByWithPagingSpec(@NotNull Class<? extends T> contractStateType, @NotNull QueryCriteria criteria, @NotNull PageSpecification paging) {
        return this.env.ledgerServices.getVaultService().trackBy(contractStateType, criteria, paging);
    }

    @NotNull
    @Override
    public <T extends ContractState> DataFeed<Vault.Page<T>, Vault.Update<T>> vaultTrackByWithSorting(@NotNull Class<? extends T> contractStateType, @NotNull QueryCriteria criteria, @NotNull Sort sorting) {
        return this.env.ledgerServices.getVaultService().trackBy(contractStateType, criteria, sorting);
    }

    @NotNull
    @Override
    public CordaFuture<Void> waitUntilNetworkReady() {
        throw new NotImplementedException("#CordaRPCOpsMock not supported yet");
    }

    @Nullable
    @Override
    public Party wellKnownPartyFromAnonymous(@NotNull AbstractParty party) {
        return this.env.ledgerServices.getIdentityService().wellKnownPartyFromAnonymous(party);
    }

    @Nullable
    @Override
    public Party wellKnownPartyFromX500Name(@NotNull CordaX500Name x500Name) {
        return this.env.network.getNetworkMapCache().getPeerByLegalName(x500Name);
    }

    @Override
    public int getProtocolVersion() {
        return this.env.node.getInfo().getPlatformVersion();
    }

    @NotNull
    @Override
    public NodeDiagnosticInfo nodeDiagnosticInfo() {
        throw new NotImplementedException("#nodeDiagnosticInfo not supported yet");
    }
}
