package ch.cordalo.corda.common.contracts;

import net.corda.core.identity.AbstractParty;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Participants {

    public static String partyToX500(AbstractParty party) {
        return party.nameOrNull().getX500Principal().getName();
    }

    private final List<AbstractParty> parties;

    public Participants(AbstractParty... parties) {
        List<AbstractParty> list = new ArrayList<>();
        if (parties != null) {
            for (AbstractParty party : parties) {
                if (party != null && !list.contains(party)) {
                    list.add(party);
                }
            }
        }
        this.parties = list;
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

}
