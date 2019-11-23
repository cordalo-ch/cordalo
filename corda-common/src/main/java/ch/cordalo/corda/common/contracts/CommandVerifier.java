/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
        return this.verify_create1(stateClass, new Parameters<T>());
    }
    public <T extends ContractState> T verify_create1(Class<T> stateClass, Parameters<T> parameters) {
        verifier.input().empty();
        T output = this.verifier
                .output()
                .one()
                .one(stateClass)
                .object();
        parameters.verify(output);
        return output;
    }

    public <T extends LinearState> Pair<T, T> verify_update1(Class<T> stateClass, Function<T, Object> ...equalMappers) {
        return this.verify_update1(stateClass, new Parameters<T>().equal(equalMappers));
    }
    public <T extends LinearState> Pair<T, T> verify_update1(Class<T> stateClass, Parameters<T> parameters) {
        T input = verifier
                .input()
                .notEmpty()
                .one()
                .one(stateClass)
                .object();
        T output = verifier
                .output()
                .notEmpty()
                .one()
                .one(stateClass)
                .object();
        parameters.verify(input).verify(output).verify(input, output);
        return new Pair<>(input, output);
    }

    public <T extends ContractState> T verify_delete1(Class<T> stateClass) {
        verifier.output().empty();
        return this.verifier
                .input()
                .notEmpty()
                .one()
                .one(stateClass)
                .object();
    }

    public static class Comparer {
        public Comparer() {

        }


    }

    public static class Parameters<T extends ContractState> {
        public Collection<Function<T, Object>> emptyMappers = new ArrayList<>();
        public Collection<Function<T, Object>> notEmptyMappers = new ArrayList<>();
        public Collection<Function<T, Object>> equalMappers = new ArrayList<>();
        public Collection<Function<T, Object>> notEqualMappers = new ArrayList<>();
        public Parameters() {
        }
        public Parameters<T> notEmpty(Function<T, Object> ...newMappers) {
            if (newMappers != null) {
                // concrete due issue with static mapping
                for(Function<T, Object> mapper : newMappers) notEmptyMappers.add(mapper);
            }
            return this;
        }
        public Parameters<T> empty(Function<T, Object> ...newMappers) {
            if (newMappers != null) {
                // concrete due issue with static mapping
                for(Function<T, Object> mapper : newMappers) emptyMappers.add(mapper);
            }
            return this;
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
        public Parameters<T> verify(T object){
            requireThat(req -> {
                for (Function<T, Object> mapper : notEmptyMappers) {
                    Object value = mapper.apply(object);
                    req.using("field " + mapper.toString() + " cannot be null",
                            value != null);
                    if (value instanceof String) {
                        req.using("field " + mapper.toString() + " cannot be empty",
                                !((String) value).isEmpty());
                    }
                }
                for (Function<T, Object> mapper : emptyMappers) {
                    Object value = mapper.apply(object);
                    if (value instanceof String) {
                        req.using("field " + mapper.toString() + " must be null or empty",
                                value == null || ((String) value).isEmpty());
                    } else {
                        req.using("field " + mapper.toString() + " must be null",
                                value == null);
                    }
                }
                return null;
            });
            return this;
        }
        public Parameters<T> verify(T object1, T object2){
            requireThat(req -> {
                this.verify(object1);
                this.verify(object2);

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
