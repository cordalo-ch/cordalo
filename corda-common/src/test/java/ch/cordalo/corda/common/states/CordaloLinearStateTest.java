package ch.cordalo.corda.common.states;

import net.corda.core.contracts.UniqueIdentifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class CordaloLinearStateTest {

    private class UnderTest extends CordaloLinearState {

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
            return null;
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

}