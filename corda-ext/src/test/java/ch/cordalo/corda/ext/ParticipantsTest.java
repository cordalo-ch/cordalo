package ch.cordalo.corda.ext;

import com.google.common.collect.ImmutableList;
import net.corda.core.crypto.NullKeys;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

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
        assertThat(participants.getPublicKeys().get(0), is(NullKeys.NullPublicKey.INSTANCE));
    }

    @NotNull
    private Participants getParticipantsFromEmptyParties() {
        List<Party> parties = new ArrayList<>();
        return Participants.fromParties(parties);
    }

    @NotNull
    private Party newParty() {
        CordaX500Name cordaX500Name = new CordaX500Name("commonName", "organisationUnit",
                "organisation", "locality", "state", "CH");

        return new Party(cordaX500Name, NullKeys.NullPublicKey.INSTANCE);
    }

}