package ch.cordalo.corda.common.contracts.test;

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
import java.util.stream.Collectors;


@BelongsToContract (TestContract.class)
public class TestState implements LinearState {

    @NotNull
    private UniqueIdentifier linearId;
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
    public TestState(@NotNull UniqueIdentifier linearId, @NotNull Party owner, @NotNull Party provider, @NotNull Party cloneProvider, String stringValue, Integer intValue, Amount<Currency> amount) {
        this.linearId = linearId;
        this.owner = owner;
        this.provider = provider;
        this.cloneProvider = cloneProvider;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.amount = amount;
    }
    public TestState(@NotNull UniqueIdentifier linearId, @NotNull Party owner, @NotNull Party provider, @NotNull Party cloneProvider, String stringValue, Integer intValue) {
        this.linearId = linearId;
        this.owner = owner;
        this.provider = provider;
        this.cloneProvider = cloneProvider;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.amount = CHF(intValue);
    }
    public TestState(@NotNull UniqueIdentifier linearId, @NotNull Party owner, @NotNull Party provider, String stringValue, Integer intValue) {
        this.linearId = linearId;
        this.owner = owner;
        this.provider = provider;
        this.cloneProvider = provider;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.amount = CHF(intValue);
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
    @Override
    public List<AbstractParty> getParticipants() {
        return Lists.newArrayList(this.owner, this.provider);
    }
    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

}
