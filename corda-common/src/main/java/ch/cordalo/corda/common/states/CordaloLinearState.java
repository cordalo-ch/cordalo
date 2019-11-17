package ch.cordalo.corda.common.states;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

public abstract class CordaloLinearState extends CordaloState implements LinearState {
    protected final UniqueIdentifier linearId;
    @ConstructorForDeserialization
    public CordaloLinearState(UniqueIdentifier linearId) {
        this.linearId = linearId;
    }
    public CordaloLinearState(String externalId) {
        this.linearId = new UniqueIdentifier(externalId);
    }
    public CordaloLinearState() {
        this.linearId = new UniqueIdentifier();
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }

}
