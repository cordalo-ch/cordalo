package ch.cordalo.corda.common.contracts;

import kotlin.Pair;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CommandVerifier {

    private final StateVerifier verifier;

    public CommandVerifier(@NotNull StateVerifier verifier) {
        this.verifier = verifier;
    }

    public <T extends ContractState> T verify_create1(Class<T> stateClass) {
        verifier.input().empty();
        return this.verifier
                .output()
                .one()
                .one(stateClass)
                .object();
    }
    public <T extends LinearState> Pair<T, T> verify_update1(Class<T> stateClass, Function<T, Object> ...mappers) {
        return this.verify_update1(stateClass, new Parameters<T>().equal(mappers));
    }
    public <T extends LinearState> Pair<T, T> verify_update1(Class<T> stateClass, Parameters<T> parameters) {
        T input = verifier.input().one().one(stateClass).object();
        T output = verifier.output().one().one(stateClass).object();
        parameters.verify(input, output);
        return new Pair<>(input, output);
    }

    public <T extends ContractState> T verify_delete1(Class<T> stateClass) {
        verifier.output().empty();
        return this.verifier
                .input()
                .one()
                .one(stateClass)
                .object();
    }

    public static class Parameters<T extends LinearState> {
        private Collection<Function<T, Object>> equalMappers = new ArrayList<>();
        private Collection<Function<T, Object>> notEqualMappers = new ArrayList<>();
        public Parameters() {
        }
        public Parameters<T> equal(Function<T, Object> ...newMappers) {
            if (newMappers != null) {
                // concrete due issue with static mapping
                for(Function<T, Object> mapper : newMappers) equalMappers.add(mapper);
            }
            return this;
        }
        public Parameters<T> notEqual(Function<T, Object> ...newMappers) {
            if (newMappers != null) {
                // concrete due issue with static mapping
                for(Function<T, Object> mapper : newMappers) notEqualMappers.add(mapper);
            }
            return this;
        }
        public Parameters<T> verify(T object1, T object2){
            requireThat(req -> {
                for (Function<T, Object> mapper : equalMappers) {
                    Object inputVal = mapper.apply(object1);
                    Object outputVal = mapper.apply(object2);
                    if (inputVal != null) {
                        req.using("field " + mapper.toString() + " must be the same",
                                inputVal.equals(outputVal));
                    } else {
                        req.using("field " + mapper.toString() + " must be empty for both",
                                outputVal == null);
                    }
                }
                for (Function<T, Object> mapper : notEqualMappers) {
                    Object inputVal = mapper.apply(object1);
                    Object outputVal = mapper.apply(object2);
                    if (inputVal != null) {
                        req.using("field " + mapper.toString() + " must be different",
                                !inputVal.equals(outputVal));
                    } else if (outputVal != null) {
                        // skip: all fine
                    } else {
                        throw new IllegalArgumentException("field "+mapper.toString()+" are both null");
                   }
                }
                return null;
            });
            return this;
        }

    }

}
