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
import com.google.common.collect.Lists;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@BelongsToContract(TestSimpleContract.class)
public class TestSimpleState extends CordaloLinearState implements QueryableState {

    Party owner;
    String key;
    String value;
    List<Party> partners;

    @ConstructorForDeserialization
    public TestSimpleState(UniqueIdentifier linearId, Party owner, String key, String value, List<Party> partners) {
        super(linearId);
        this.owner = owner;
        this.key = key;
        this.value = value;
        this.partners = partners;
    }

    public Party getOwner() {
        return owner;
    }
    public String getOwnerX500() {
        return Parties.partyToX500(this.owner);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public List<Party> getPartners() {
        return partners;
    }
    public List<String> getPartnersX500() {
        return Parties.fromParties(this.partners).getPartiesX500();
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema != null) {
            return new TestSimpleSchemaV1.PersistentTestSimple(
                    this.getLinearId().getId(),
                    this.getOwnerX500(),
                    this.getKey(),
                    this.getValue(),
                    this.getPartnersX500().stream().collect(Collectors.joining("|"))
            );
        } else {
            throw new IllegalArgumentException("Unrecognised schema " + schema);
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return Lists.newArrayList(new TestSimpleSchemaV1());
    }


    @NotNull
    @Override
    protected Parties getParties() {
        return Parties.fromParties(this.getOwner()).add(this.getPartners());
    }

    public TestSimpleState share(Party partner) {
        Set<Party> set = new HashSet<>(this.partners);
        set.add(partner);
        return new TestSimpleState(this.getLinearId(), this.getOwner(), this.getKey(), this.getValue(), Lists.newArrayList(set));
    }

    public TestSimpleState update(String key, String value) {
        return new TestSimpleState(this.getLinearId(), this.getOwner(), key, value, this.getPartners());
    }


}
