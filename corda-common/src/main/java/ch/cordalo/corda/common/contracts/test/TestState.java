package ch.cordalo.corda.common.contracts.test;

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.ext.Participants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@BelongsToContract (TestContract.class)
public class TestState extends CordaloLinearState {

    @NotNull
    private Party owner;
    @NotNull
    private Party provider;
    @NotNull
    private final Party cloneProvider;
    private String stringValue;
    private Integer intValue;
    private final Amount<Currency> amount;

    public static Amount<Currency> CHF(Integer intValue) {
        return Amount.fromDecimal(
                BigDecimal.valueOf(intValue.longValue()),
                Currency.getInstance("CHF"));
    }

    @ConstructorForDeserialization
    public TestState(@NotNull UniqueIdentifier linearId, @NotNull Party owner, @NotNull Party provider, @NotNull Party cloneProvider, @NotNull String stringValue, @NotNull Integer intValue, @NotNull Amount<Currency> amount) {
        super(linearId);
        this.owner = owner;
        this.provider = provider;
        this.cloneProvider = cloneProvider;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.amount = amount;
    }
    public TestState(@NotNull UniqueIdentifier linearId, @NotNull Party owner, @NotNull Party provider, @NotNull Party cloneProvider, @NotNull String stringValue, @NotNull Integer intValue) {
        super(linearId);
        this.owner = owner;
        this.provider = provider;
        this.cloneProvider = cloneProvider;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.amount = CHF(intValue);
    }
    public TestState(@NotNull UniqueIdentifier linearId, @NotNull Party owner, @NotNull Party provider, @NotNull String stringValue, @NotNull Integer intValue) {
        super(linearId);
        this.owner = owner;
        this.provider = provider;
        this.cloneProvider = provider;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.amount = CHF(intValue);
    }

    public Party getOwner() {
        return owner;
    }
    public Party getProvider() {
        return provider;
    }
    public Party getCloneProvider() { return cloneProvider; }
    public String getStringValue() {
        return stringValue;
    }
    public Integer getIntValue() {
        return intValue;
    }
    public Amount<Currency> getAmount() {
        return amount;
    }

    public TestState withValues(String stringValue, Integer intValue) {
        return new TestState(this.linearId, this.owner, this.provider, this.provider, stringValue, intValue, CHF(intValue));
    }
    public TestState withProvider(Party newParty) {
        return new TestState(this.linearId, this.owner, newParty, newParty, this.stringValue, this.intValue, CHF(this.intValue));
    }

    @NotNull
    @JsonIgnore
    @Override
    public Participants participants() {
        return Participants.fromParties(this.owner, this.provider, this.cloneProvider);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestState)) return false;
        if (!super.equals(o)) return false;
        TestState testState = (TestState) o;
        return super.equals(o) &&
                getOwner().equals(testState.getOwner()) &&
                getProvider().equals(testState.getProvider()) &&
                getCloneProvider().equals(testState.getCloneProvider()) &&
                Objects.equals(getStringValue(), testState.getStringValue()) &&
                Objects.equals(getIntValue(), testState.getIntValue()) &&
                Objects.equals(getAmount(), testState.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getOwner(), getProvider(), getCloneProvider(), getStringValue(), getIntValue(), getAmount());
    }
}
