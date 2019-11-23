package ch.cordalo.corda.common.contracts.test;

import ch.cordalo.corda.common.contracts.CommandVerifier;
import ch.cordalo.corda.common.contracts.StateVerifier;
import com.google.common.collect.Sets;
import kotlin.Pair;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.util.Set;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class TestSimpleContract implements Contract {
    public static final String ID = TestSimpleContract.class.getName();

    public TestSimpleContract() {}

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, TestSimpleContract.Commands.class);
        TestSimpleContract.Commands commandData = (TestSimpleContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        class Create implements TestSimpleContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    CommandVerifier.Parameters<TestSimpleState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            TestSimpleState::getLinearId,
                            TestSimpleState::getOwner,
                            TestSimpleState::getKey,
                            TestSimpleState::getValue,
                            TestSimpleState::getPartners
                    );

                    TestSimpleState simple = new CommandVerifier(verifier)
                            .verify_create1(TestSimpleState.class, params);
                    return null;
                });

            }
        }

        class Share implements TestSimpleContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {

                    CommandVerifier.Parameters<TestSimpleState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            TestSimpleState::getLinearId,
                            TestSimpleState::getOwner,
                            TestSimpleState::getKey,
                            TestSimpleState::getValue,
                            TestSimpleState::getPartners
                    );

                    Pair<TestSimpleState, TestSimpleState> simplePair = new CommandVerifier(verifier)
                            .verify_update1(TestSimpleState.class, params);

                    TestSimpleState updateState = simplePair.component2();
                    Set<Party> set1 = Sets.newLinkedHashSet(simplePair.component1().getPartners());
                    Set<Party> set2 = Sets.newLinkedHashSet(updateState.getPartners());
                    Sets.SetView<Party> diffUpdates = Sets.difference(set2, set1);
                    req.using("1 new partner in partners", diffUpdates.size() == 1);
                    for (Party p: diffUpdates) {
                        req.using("partner cannot be owner", !p.equals(updateState.getOwner()));
                    }
                    return null;
                });

            }
        }


        class Update implements TestSimpleContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {

                    CommandVerifier.Parameters<TestSimpleState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            TestSimpleState::getLinearId,
                            TestSimpleState::getOwner,
                            TestSimpleState::getKey,
                            TestSimpleState::getValue,
                            TestSimpleState::getPartners
                    );

                    Pair<TestSimpleState, TestSimpleState> simplePair = new CommandVerifier(verifier)
                            .verify_update1(TestSimpleState.class, params);
                    String k1 = simplePair.component1().getKey();
                    String k2 = simplePair.component2().getKey();
                    String v1 = simplePair.component1().getValue();
                    String v2 = simplePair.component2().getValue();
                    req.using("key and / value must be different",
                            !k1.equals(k2) || !v1.equals(v2));

                    return null;
                });

            }
        }


        class Delete implements TestSimpleContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {

                    verifier.input()
                            .one()
                            .one(TestSimpleState.class)
                            .object();
                    verifier.output().empty();

                    return null;
                });

            }
        }

    }

}
