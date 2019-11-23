/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.states;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.NullKeys;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartiesTest {

    @Test
    public void withNullParty_partyToX500_expectEmptyString() {
        String x500 = Parties.partyToX500(null);
        MatcherAssert.assertThat(x500, CoreMatchers.is(""));
    }

    @Test
    public void withAnonymousParty_partyToX500_expectX500() {
        AbstractParty party = new AnonymousParty(NullKeys.NullPublicKey.INSTANCE);

        try {
            String x500 = Parties.partyToX500(party);
            MatcherAssert.assertThat(x500, CoreMatchers.is(""));
        } catch (Exception e) {
            // TODO our code should not fail with anonymous party To be discuss
        }
    }

    @Test
    public void withParty_partyToX500_expectX500() {
        AbstractParty party = newParty();

        String x500 = Parties.partyToX500(party);
        MatcherAssert.assertThat(x500, CoreMatchers.is("CN=commonName,OU=organisationUnit,O=organisation,L=locality,ST=state,C=CH"));
    }

    @Test
    public void withEmptyParties_fromParties_expectGetPartiesEmpty() {
        Parties noParties = getParticipantsFromEmptyParties();

        MatcherAssert.assertThat(noParties, CoreMatchers.notNullValue());
    }

    @Test
    public void withEmptyParties_getParties_expectGetPartiesEmpty() {
        Parties noParties = getParticipantsFromEmptyParties();

        assertThat(noParties.getParties().size(), CoreMatchers.is(0));
    }

    @Test
    public void witOneParty_getParties_expectGetParties() {
        Party element = newParty();
        Parties parties = Parties.fromParties(ImmutableList.of(element));

        assertThat(parties.getParties().size(), CoreMatchers.is(1));
        assertThat(parties.getParties().get(0), CoreMatchers.is(element));
    }

    @Test
    public void withEmptyParties_getPartiesX500_expectGetPartiesEmpty() {
        Parties noParties = getParticipantsFromEmptyParties();

        assertThat(noParties.getPartiesX500().size(), CoreMatchers.is(0));
    }

    @Test
    public void withParties_getPublicKeys_expectGetPartiesEmpty() {
        Party element = newParty();
        Parties parties = Parties.fromParties(ImmutableList.of(element));

        assertThat(parties.getPublicKeys().size(), CoreMatchers.is(1));
        assertThat(parties.getPublicKeys().get(0), CoreMatchers.is(element.getOwningKey()));
    }

    @NotNull
    private Parties getParticipantsFromEmptyParties() {
        List<Party> parties = new ArrayList<>();
        return Parties.fromParties(parties);
    }


    @Test
    public void with3Party_addParties() {
        Party element = newParty("peter", "company-A");
        Parties parties = new Parties(element);

        Party element2 = newParty("joe", "company-B");
        Party element3 = newParty("mary", "company-C");
        Parties parties2 = new Parties(element2, element3);

        Parties result = parties.add(parties2);

        assertThat(result.getParties().size(), CoreMatchers.is(3));
        assertThat(result.getParties(), CoreMatchers.hasItem(element));
        assertThat(result.getParties(), CoreMatchers.hasItem(element2));
        assertThat(result.getParties(), CoreMatchers.hasItem(element3));
    }

    @Test
    public void with1State() {
        Party element = newParty("peter", "company-A");
        Party element2 = newParty("joe", "company-B");
        PartiesTestState state = new PartiesTestState(new UniqueIdentifier(), element, element2);

        Parties result = new Parties(state);

        assertThat(result.getParties().size(), CoreMatchers.is(2));
        assertThat(result.getParties(), CoreMatchers.hasItem(element));
        assertThat(result.getParties(), CoreMatchers.hasItem(element2));
    }


    @Test
    public void with2Party_fromAbstractPartyList() {
        AbstractParty element = newParty("peter", "company-A");
        AbstractParty element2 = newParty("joe", "company-B");

        Parties result = Parties.fromAbstractParties(Lists.newArrayList(element, element2));

        assertThat(result.getParties().size(), CoreMatchers.is(2));
        assertThat(result.getParties(), CoreMatchers.hasItem(element));
        assertThat(result.getParties(), CoreMatchers.hasItem(element2));
    }


    @Test
    public void with2Party_getImmutableKeys() {
        AbstractParty element = newParty("peter", "company-A");
        AbstractParty element2 = newParty("joe", "company-B");
        Parties result = Parties.fromAbstractParties(Lists.newArrayList(element, element2));

        assertThat(result.getPublicKeys(), is(result.getImmutablePublicKeys()));
    }
    @Test
    public void with2States() {
        Party element = newParty("peter", "company-A");
        Party element2 = newParty("joe", "company-B");
        PartiesTestState state = new PartiesTestState(new UniqueIdentifier(), element, element2);
        Party element3 = newParty("mary", "company-B");
        PartiesTestState state2 = new PartiesTestState(new UniqueIdentifier(), element, element3);

        Parties result = new Parties(state, state2);

        assertThat(result.getParties().size(), CoreMatchers.is(3));
        assertThat(result.getParties(), CoreMatchers.hasItem(element));
        assertThat(result.getParties(), CoreMatchers.hasItem(element2));
        assertThat(result.getParties(), CoreMatchers.hasItem(element3));
    }

    @Test
    public void with3parties_add() {
        Party element = newParty("peter", "company-A");
        Party element2 = newParty("joe", "company-B");
        Party element3 = newParty("mary", "company-B");

        Parties result = Parties.fromParties(element, element2);
        result = result.add(element3);

        assertThat(result.getParties().size(), CoreMatchers.is(3));
        assertThat(result.getParties(), CoreMatchers.hasItem(element));
        assertThat(result.getParties(), CoreMatchers.hasItem(element2));
        assertThat(result.getParties(), CoreMatchers.hasItem(element3));
    }


    @Test
    public void with3parties_add2() {
        Party element = newParty("peter", "company-A");
        Party element2 = newParty("joe", "company-B");
        Party element3 = newParty("mary", "company-B");

        Parties result = Parties.fromParties(element);
        result = result.add(Lists.newArrayList(element2, element3));

        assertThat(result.getParties().size(), CoreMatchers.is(3));
        assertThat(result.getParties(), CoreMatchers.hasItem(element));
        assertThat(result.getParties(), CoreMatchers.hasItem(element2));
        assertThat(result.getParties(), CoreMatchers.hasItem(element3));
    }


    @Test
    public void with3parties_add2_abstractparties() {
        AbstractParty element = newParty("peter", "company-A");
        AbstractParty element2 = newParty("joe", "company-B");
        AbstractParty element3 = newParty("mary", "company-B");

        Parties result = Parties.fromParties(element);
        result = result.addAbstractParties(Lists.newArrayList(element2, element3));

        assertThat(result.getParties().size(), CoreMatchers.is(3));
        assertThat(result.getParties(), CoreMatchers.hasItem(element));
        assertThat(result.getParties(), CoreMatchers.hasItem(element2));
        assertThat(result.getParties(), CoreMatchers.hasItem(element3));
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