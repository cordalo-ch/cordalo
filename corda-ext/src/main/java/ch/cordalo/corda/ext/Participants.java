package ch.cordalo.corda.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

public class Participants {

    public static String partyToX500(AbstractParty party) {
        return party == null ? "" : party.nameOrNull().getX500Principal().getName();
    }

    public static Participants fromParties(List<Party> parties) {
        return new Participants(
                parties.stream().map(x -> (AbstractParty)x).collect(Collectors.toList()));
    }
    public static Participants fromParties(Party... parties) {
        return new Participants(parties);
    }
    public static Participants fromAbstractParties(List<AbstractParty> parties) {
        return new Participants(parties);
    }

    private final List<AbstractParty> parties;

    public Participants(List<AbstractParty> parties) {
        this.parties = parties;
    }
    public Participants(ContractState state) {
        this(state.getParticipants());
    }
    public Participants(ContractState... states) {
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

    public Participants(AbstractParty... parties) {
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
        return this.getParties().stream().map(Participants::partyToX500).collect(Collectors.toList());
    }
    public List<PublicKey> getPublicKeys() {
        return this.getParties().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }
    public ImmutableList<PublicKey> getImmutablePublicKeys() {
        return new ImmutableList.Builder<PublicKey>()
                .addAll(this.getPublicKeys())
                .build();
    }

    public Participants add(Participants participants) {
        Set<AbstractParty> set = new HashSet<>(this.getParties());
        set.addAll(participants.getParties());
        return new Participants(Lists.newArrayList(set));
    }

}
