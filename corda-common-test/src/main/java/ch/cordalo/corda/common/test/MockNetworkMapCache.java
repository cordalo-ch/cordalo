/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.test;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.messaging.DataFeed;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.NetworkMapCache;
import net.corda.core.node.services.PartyInfo;
import net.corda.core.utilities.NetworkHostAndPort;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rx.Observable;

import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MockNetworkMapCache implements NetworkMapCache {

    private final CordaTestNetwork network;

    public MockNetworkMapCache(CordaTestNetwork network) {
        this.network = network;
    }
    @Nullable
    @Override
    public NodeInfo getNodeByLegalIdentity(@NotNull AbstractParty party) {
        throw new NotImplementedException("#MockNetworkMapCache not supported yet");
    }

    @NotNull
    @Override
    public List<NodeInfo> getAllNodes() {
        return this.network.networkMapSnapshot();
    }

    @NotNull
    @Override
    public Observable<MapChange> getChanged() {
        throw new NotImplementedException("#MockNetworkMapCache not supported yet");
    }

    @NotNull
    @Override
    public CordaFuture<Void> getNodeReady() {
        throw new NotImplementedException("#MockNetworkMapCache not supported yet");
    }

    @NotNull
    @Override
    public List<Party> getNotaryIdentities() {
        return this.network.getNotaryIdentities();
    }

    @Override
    public void clearNetworkMapCache() {
        // no operation
    }

    @Nullable
    @Override
    public NodeInfo getNodeByAddress(@NotNull NetworkHostAndPort address) {
        throw new NotImplementedException("#MockNetworkMapCache not supported yet");
    }

    @Nullable
    @Override
    public NodeInfo getNodeByLegalName(@NotNull CordaX500Name name) {
        List<NodeInfo> nodesByLegalName = this.getNodesByLegalName(name);
        return nodesByLegalName.isEmpty() ?  null : nodesByLegalName.get(0);
    }

    @NotNull
    @Override
    public List<NodeInfo> getNodesByLegalIdentityKey(@NotNull PublicKey identityKey) {
        return this.network.networkMapSnapshot().stream()
                .filter(x -> x.getLegalIdentities().get(0).getOwningKey().equals(identityKey))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public List<NodeInfo> getNodesByLegalName(@NotNull CordaX500Name name) {
        return this.network.networkMapSnapshot().stream()
                .filter(x -> x.isLegalIdentity(name))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Party getNotary(@NotNull CordaX500Name name) {
        Optional<Party> first = this.getNotaryIdentities().stream()
                .filter(x -> x.getName().equals(name))
                .findFirst();
        return first.isPresent() ? first.get() : null;
    }

    @Nullable
    @Override
    public PartyInfo getPartyInfo(@NotNull Party party) {
        throw new NotImplementedException("#MockNetworkMapCache not supported yet");
    }

    @Nullable
    @Override
    public Party getPeerByLegalName(@NotNull CordaX500Name name) {
        NodeInfo nodeByLegalName = this.getNodeByLegalName(name);
        return nodeByLegalName == null ? null : nodeByLegalName.getLegalIdentities().get(0);
    }

    @Nullable
    @Override
    public PartyAndCertificate getPeerCertificateByLegalName(@NotNull CordaX500Name name) {
        throw new NotImplementedException("#MockNetworkMapCache not supported yet");
    }

    @Override
    public boolean isNotary(@NotNull Party party) {
        return this.getNotaryIdentities().stream().anyMatch(x -> x.equals(party));
    }

    @Override
    public boolean isValidatingNotary(@NotNull Party party) {
        return isNotary(party);
    }

    @NotNull
    @Override
    public DataFeed<List<NodeInfo>, MapChange> track() {
        throw new NotImplementedException("#MockNetworkMapCache not supported yet");
    }
}
