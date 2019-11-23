package ch.cordalo.corda.common.contracts.test;

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import com.google.common.collect.Lists;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@BelongsToContract(TestSimpleContract.class)
public class TestSimpleState extends CordaloLinearState {

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

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public List<Party> getPartners() {
        return partners;
    }


    @NotNull
    @Override
    protected Parties getParties() {
        return Parties.fromParties(this.getOwner()).add(this.getPartners());
    }

    public TestSimpleState share(Party partner) {
        Set<Party> set = new HashSet<>(this.partners);
        set.add(partner);
        return new TestSimpleState(this.getLinearId(), this.getOwner(),this.getKey(), this.getValue(), Lists.newArrayList(set));
    }

    public TestSimpleState update(String key, String value) {
        return new TestSimpleState(this.getLinearId(), this.getOwner(), key, value, this.getPartners());
    }
}
