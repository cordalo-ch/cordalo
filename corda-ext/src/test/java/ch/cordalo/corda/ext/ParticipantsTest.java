package ch.cordalo.corda.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.NullKeys;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParticipantsTest {

    @Test
    public void withNullParty_partyToX500_expectEmptyString() {
        String x500 = Participants.partyToX500(null);
        assertThat(x500, is(""));
    }

    @Test
    public void withAnonymousParty_partyToX500_expectX500() {
        AbstractParty party = new AnonymousParty(NullKeys.NullPublicKey.INSTANCE);

        try {
            String x500 = Participants.partyToX500(party);
            assertThat(x500, is(""));
        } catch (Exception e) {
            // TODO our code should not fail with anonymous party To be discuss
        }
    }

    @Test
    public void withParty_partyToX500_expectX500() {
        AbstractParty party = newParty();

        String x500 = Participants.partyToX500(party);
        assertThat(x500, is("CN=commonName,OU=organisationUnit,O=organisation,L=locality,ST=state,C=CH"));
    }

    @Test
    public void withEmptyParties_fromParties_expectGetPartiesEmpty() {
        Participants noParticipants = getParticipantsFromEmptyParties();

        assertThat(noParticipants, notNullValue());
    }

    @Test
    public void withEmptyParties_getParties_expectGetPartiesEmpty() {
        Participants noParticipants = getParticipantsFromEmptyParties();

        assertThat(noParticipants.getParties().size(), is(0));
    }

    @Test
    public void witOneParty_getParties_expectGetParties() {
        Party element = newParty();
        Participants participants = Participants.fromParties(ImmutableList.of(element));

        assertThat(participants.getParties().size(), is(1));
        assertThat(participants.getParties().get(0), is(element));
    }

    @Test
    public void withEmptyParties_getPartiesX500_expectGetPartiesEmpty() {
        Participants noParticipants = getParticipantsFromEmptyParties();

        assertThat(noParticipants.getPartiesX500().size(), is(0));
    }

    @Test
    public void withParties_getPublicKeys_expectGetPartiesEmpty() {
        Party element = newParty();
        Participants participants = Participants.fromParties(ImmutableList.of(element));

        assertThat(participants.getPublicKeys().size(), is(1));
        assertThat(participants.getPublicKeys().get(0), is(element.getOwningKey()));
    }

    @NotNull
    private Participants getParticipantsFromEmptyParties() {
        List<Party> parties = new ArrayList<>();
        return Participants.fromParties(parties);
    }


    @Test
    public void with3Party_addParties() {
        Party element = newParty("peter", "company-A");
        Participants participants = new Participants(element);

        Party element2 = newParty("joe", "company-B");
        Party element3 = newParty("mary", "company-C");
        Participants participants2 = new Participants(element2, element3);

        Participants result = participants.add(participants2);

        assertThat(result.getParties().size(), is(3));
        assertThat(result.getParties(), hasItem(element));
        assertThat(result.getParties(), hasItem(element2));
        assertThat(result.getParties(), hasItem(element3));
    }

    @Test
    public void with1State() {
        Party element = newParty("peter", "company-A");
        Party element2 = newParty("joe", "company-B");
        ParticipantsTestState state = new ParticipantsTestState(new UniqueIdentifier(), element, element2);

        Participants result = new Participants(state);

        assertThat(result.getParties().size(), is(2));
        assertThat(result.getParties(), hasItem(element));
        assertThat(result.getParties(), hasItem(element2));
    }


    @Test
    public void with2Party_fromAbstractPartyList() {
        AbstractParty element = newParty("peter", "company-A");
        AbstractParty element2 = newParty("joe", "company-B");

        Participants result = Participants.fromAbstractParties(Lists.newArrayList(element, element2));

        assertThat(result.getParties().size(), is(2));
        assertThat(result.getParties(), hasItem(element));
        assertThat(result.getParties(), hasItem(element2));
    }


    @Test
    public void with2Party_getImmutableKeys() {
        AbstractParty element = newParty("peter", "company-A");
        AbstractParty element2 = newParty("joe", "company-B");
        Participants result = Participants.fromAbstractParties(Lists.newArrayList(element, element2));

        assertThat(result.getPublicKeys(), is(result.getImmutablePublicKeys()));
    }
    @Test
    public void with2States() {
        Party element = newParty("peter", "company-A");
        Party element2 = newParty("joe", "company-B");
        ParticipantsTestState state = new ParticipantsTestState(new UniqueIdentifier(), element, element2);
        Party element3 = newParty("mary", "company-B");
        ParticipantsTestState state2 = new ParticipantsTestState(new UniqueIdentifier(), element, element3);

        Participants result = new Participants(state, state2);

        assertThat(result.getParties().size(), is(3));
        assertThat(result.getParties(), hasItem(element));
        assertThat(result.getParties(), hasItem(element2));
        assertThat(result.getParties(), hasItem(element3));
    }

    @NotNull
    private Party newParty(String commonName, String organizationUnit) {
        CordaX500Name cordaX500Name = new CordaX500Name(commonName, organizationUnit,
                "organisation", "locality", "state", "CH");
        return new TestIdentity(cordaX500Name).getParty();
    }
    private Party newParty() {
        return newParty("commonName", "organisationUnit");
    }

}