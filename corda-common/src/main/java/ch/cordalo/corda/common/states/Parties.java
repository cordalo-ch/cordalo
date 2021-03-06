/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.states;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CordaSerializable
public class Parties {

    public static String partyToX500(AbstractParty party) {
        return party == null ? "" : party.nameOrNull().getX500Principal().getName();
    }

    public static Parties fromParties(List<Party> parties) {
        return new Parties(
                parties.stream().map(x -> (AbstractParty) x).collect(Collectors.toList()));
    }

    public static Parties fromParties(Party... parties) {
        return new Parties(parties);
    }

    public static Parties fromParties(AbstractParty... parties) {
        return new Parties(parties);
    }

    public static Parties fromAbstractParties(List<AbstractParty> parties) {
        return new Parties(parties);
    }

    private final List<AbstractParty> parties;

    public Parties(List<AbstractParty> parties) {
        this.parties = parties;
    }

    public Parties(ContractState state) {
        this(state.getParticipants());
    }

    public Parties(ContractState... states) {
        Set<AbstractParty> set = new HashSet<>();
        if (states != null) {
            for (ContractState state : states) {
                if (state != null) {
                    set.addAll(state.getParticipants());
                }
            }
        }
        this.parties = Lists.newArrayList(set);
    }

    public Parties(AbstractParty... parties) {
        this.parties = unique(parties);
    }

    private static final List<AbstractParty> unique(AbstractParty... parties) {
        return unique(Lists.newArrayList(parties));
    }

    private static final List<AbstractParty> unique(List<AbstractParty> parties) {
        Set<AbstractParty> set = new HashSet<>();
        if (parties != null) {
            for (AbstractParty party : parties) {
                if (party != null) {
                    set.add(party);
                }
            }
        }
        return Lists.newArrayList(set);
    }

    public List<AbstractParty> getParties() {
        return this.parties;
    }

    public List<String> getPartiesX500() {
        return this.getParties().stream().map(Parties::partyToX500).collect(Collectors.toList());
    }

    public List<PublicKey> getPublicKeys() {
        return this.getParties().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    public ImmutableList<PublicKey> getImmutablePublicKeys() {
        return new ImmutableList.Builder<PublicKey>()
                .addAll(this.getPublicKeys())
                .build();
    }

    public Parties add(Parties parties) {
        Set<AbstractParty> set = new HashSet<>(this.getParties());
        set.addAll(parties.getParties());
        return new Parties(Lists.newArrayList(set));
    }

    public Parties addAbstractParties(List<AbstractParty> list) {
        List<AbstractParty> newParties = Lists.newArrayList(this.parties);
        newParties.addAll(list);
        return new Parties(Lists.newArrayList(newParties));
    }

    public Parties add(List<Party> list) {
        List<AbstractParty> newParties = Lists.newArrayList(this.parties);
        newParties.addAll(list);
        return new Parties(Lists.newArrayList(newParties));
    }

    public Parties add(AbstractParty party) {
        List<AbstractParty> newParties = Lists.newArrayList(this.parties);
        newParties.add(party);
        return new Parties(Lists.newArrayList(newParties));
    }

}
