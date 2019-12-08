/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package ch.cordalo.corda.common.contracts.test;

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

@BelongsToContract(TestSimpleDependentContract.class)
public class TestSimpleDependentState extends CordaloLinearState {

    @NotNull
    private UniqueIdentifier simpleId;
    @NotNull
    @JsonIgnore
    private Party owner;
    @NotNull
    private String key;

    public TestSimpleDependentState(UniqueIdentifier linearId, @NotNull UniqueIdentifier simpleId, @NotNull Party owner, @NotNull String key) {
        super(linearId);
        this.simpleId = simpleId;
        this.owner = owner;
        this.key = key;
    }

    @NotNull
    public UniqueIdentifier getSimpleId() {
        return simpleId;
    }

    @NotNull
    public Party getOwner() {
        return owner;
    }

    public String getOwnerX500() {
        return Parties.partyToX500(this.owner);
    }


    @NotNull
    public String getKey() {
        return key;
    }

    @NotNull
    @Override
    protected Parties getParties() {
        return Parties.fromParties(this.owner);
    }
}
