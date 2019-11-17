package ch.cordalo.corda.common.states;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

@CordaSerializable
public abstract class CordaloState implements ContractState {

    public CordaloState() {
    }

    @NotNull
    @JsonIgnore
    protected abstract Parties getParties();

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        return this.getParties().getParties();
    }

    @NotNull
    public List<String> getParticipantsX500() {
        return this.getParties().getPartiesX500();
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return new Parties(this.getParticipants()).getPublicKeys();
    }
}
