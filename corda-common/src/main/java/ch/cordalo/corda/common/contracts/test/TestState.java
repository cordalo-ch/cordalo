/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.contracts.test;

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;


@BelongsToContract(TestContract.class)
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

    public Party getCloneProvider() {
        return cloneProvider;
    }

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
    protected Parties getParties() {
        return Parties.fromParties(this.owner, this.provider, this.cloneProvider);
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
