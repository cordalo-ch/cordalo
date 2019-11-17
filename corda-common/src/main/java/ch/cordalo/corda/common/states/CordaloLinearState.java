package ch.cordalo.corda.common.states;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CordaloLinearState)) return false;
        CordaloLinearState that = (CordaloLinearState) o;
        return getLinearId().equals(that.getLinearId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLinearId());
    }
}
