package ch.cordalo.corda.ext;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@BelongsToContract (PartiesTestContract.class)
public class PartiesTestState implements LinearState {

    @NotNull
    private UniqueIdentifier linearId;
    @NotNull
    private Party owner;
    @NotNull
    private Party provider;
    @ConstructorForDeserialization
    public PartiesTestState(@NotNull UniqueIdentifier linearId, @NotNull Party owner, @NotNull Party provider) {
        this.linearId = linearId;
        this.owner = owner;
        this.provider = provider;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }
    public Party getOwner() {
        return owner;
    }
    public Party getProvider() {
        return provider;
    }


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return new Parties(this.owner, this.provider).getParties();
    }

}
