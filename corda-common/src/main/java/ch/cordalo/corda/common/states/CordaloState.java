package ch.cordalo.corda.common.states;

import ch.cordalo.corda.ext.Parties;
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
    protected abstract Parties parties();

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        return this.parties().getParties();
    }

    @NotNull
    public List<String> getParticipantsX500() {
        return this.parties().getPartiesX500();
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return new Parties(this.getParticipants()).getPublicKeys();
    }
}
