package ch.cordalo.corda.common.states;

import ch.cordalo.corda.ext.Participants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

public abstract class CordaloState implements ContractState {

    public CordaloState() {
    }

    @NotNull
    @JsonIgnore
    public abstract Participants participants();

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        return this.participants().getParties();
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return new Participants(this.getParticipants()).getPublicKeys();
    }
}
