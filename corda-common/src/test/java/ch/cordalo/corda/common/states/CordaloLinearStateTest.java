package ch.cordalo.corda.common.states;

import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.NullKeys;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.security.PublicKey;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class CordaloLinearStateTest {

    private class UnderTest extends CordaloLinearState {

        private Party owner = newTestParty();

        public UnderTest(UniqueIdentifier linearId) {
            super(linearId);
        }

        public UnderTest(String externalId) {
            super(externalId);
        }

        public UnderTest() {
        }

        @NotNull
        @Override
        protected Parties getParties() {
            return Parties.fromParties(this.owner);
        }

        public Party getOwner() {
            return owner;
        }

    }

    @Test
    public void newCordaloLinearState_getLinearId_expectNotNull() {
        // Arrange
        UnderTest cordaloLinearState = new UnderTest();

        //Act
        UniqueIdentifier linearId = cordaloLinearState.getLinearId();

        // Assert
        assertThat(linearId, is(notNullValue()));
    }

    @Test
    public void differentCordaloLinearState_equals_expectFalse() {
        // Arrange
        UnderTest cordaloLinearState1 = new UnderTest();
        UnderTest cordaloLinearState2 = new UnderTest();

        //Act
        boolean equals = cordaloLinearState1.equals(cordaloLinearState2);

        // Assert
        assertThat(equals, is(false));
    }

    @Test
    public void differentCordaloLinearState_equals_expectTrue() {
        // Arrange
        UnderTest cordaloLinearState1 = new UnderTest();
        UnderTest cordaloLinearState2 = cordaloLinearState1;

        //Act
        boolean equals = cordaloLinearState1.equals(cordaloLinearState2);

        // Assert
        assertThat(equals, is(true));
    }

    @Test
    public void newCordaloLinearStateByExternalId_getLinearId_expectExternalId() {
        // Arrange
        UnderTest cordaloLinearState1 = new UnderTest("externalId");

        //Act
        UniqueIdentifier linearId = cordaloLinearState1.getLinearId();

        // Assert
        assertThat(linearId.getExternalId(), is("externalId"));
    }

    @Test
    public void newCordaloLinearStateByUniqueIdentifier_getLinearId_expectExternalId() {
        // Arrange
        UnderTest cordaloLinearState1 = new UnderTest(new UniqueIdentifier("uniqueId"));

        //Act
        UniqueIdentifier linearId = cordaloLinearState1.getLinearId();

        // Assert
        assertThat(linearId.getExternalId(), is("uniqueId"));
    }

    @Test
    public void newCordaloLinearStateByUniqueIdentifier_hashCode_expectValue() {
        // Arrange
        UnderTest cordaloLinearState = new UnderTest();

        //Act
        int hashCode = cordaloLinearState.hashCode();

        // Assert
        assertThat(hashCode, is(notNullValue()));
    }

    @Test
    public void newCordaloLinearState_getParties_expectParty() {
        // Arrange
        UnderTest cordaloLinearState = new UnderTest();

        //Act
        Parties parties = cordaloLinearState.getParties();

        // Assert
        assertThat(parties, is(notNullValue()));
    }

    @Test
    public void newCordaloLinearState_getParticipants() {
        // Arrange
        UnderTest cordaloLinearState = new UnderTest();

        //Act
        List<AbstractParty> participants = cordaloLinearState.getParticipants();

        // Assert
        assertThat(participants.size(), is(1));
        assertThat(participants.get(0), is(cordaloLinearState.getOwner()));
    }


    @Test
    public void newCordaloLinearState_getParticipantKeys() {
        // Arrange
        UnderTest cordaloLinearState = new UnderTest();

        //Act
        List<PublicKey> participantKeys = cordaloLinearState.getParticipantKeys();

        // Assert
        assertThat(participantKeys.size(), is(1));
        assertThat(participantKeys.get(0), is(cordaloLinearState.getOwner().getOwningKey()));
    }


    @Test
    public void newCordaloLinearState_getParticipantsX500() {
        // Arrange
        UnderTest cordaloLinearState = new UnderTest();

        //Act
        List<String> partyStrings = cordaloLinearState.getParticipantsX500();

        // Assert
        assertThat(partyStrings.size(), is(1));
        assertThat(partyStrings.get(0), is(partyNameToCompare()));
    }


    private String partyNameToCompare() {
        return newTestParty().nameOrNull().getX500Principal().getName();
    }

    private Party newTestParty() {
        return newParty("TestName","TestOE");
    }
    @NotNull
    private Party newParty(String commonName, String organizationUnit) {
        CordaX500Name cordaX500Name = new CordaX500Name(commonName, organizationUnit,
                "organisation", "locality", "state", "CH");
        return new TestIdentity(cordaX500Name).getParty();
    }

}