/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.client.webserver;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

public class StateBuilder<T extends LinearState> {

    private final List<StateAndLinks<T>> states;
    private final BodyBuilder builder;
    private RestHelper apiHelper;

    public StateBuilder(T state, BodyBuilder builder) {
        if (state != null) {
            this.states = Collections.singletonList(new StateAndLinks<>(state));
        } else {
            this.states = Collections.EMPTY_LIST;
        }
        this.builder = builder;
    }

    private List<StateAndLinks<T>> wrapIntoStateAndLinks(List<T> originalStates) {
        List<StateAndLinks<T>> newStates = new ArrayList<>();
        for (T state : originalStates) {
            newStates.add(new StateAndLinks<>(state));
        }
        return newStates;
    }

    public StateBuilder(List<T> states, BodyBuilder builder) {
        this.states = wrapIntoStateAndLinks(states);
        this.builder = builder;
    }

    private class RestHelper {
        private final String mappingPath;
        private final String bashPath;
        private final HttpServletRequest request;
        //private final Map<String, URI> links = new LinkedHashMap<>();
        //private URI linkSelf;

        public RestHelper(String mappingPath, String basePath, HttpServletRequest request) {
            this.mappingPath = mappingPath;
            this.bashPath = basePath;
            this.request = request;
        }

        protected URI getRoot() throws URISyntaxException {
            return new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), null, null, null);
        }

        protected URI createURI(String subpath) throws URISyntaxException {
            return this.getRoot().resolve(this.mappingPath + this.bashPath + "/" + subpath);
        }

        public URI self(UniqueIdentifier id) throws URISyntaxException {
            return this.createURI(id.getId().toString());
        }

        public Map.Entry<String, URI> link(UniqueIdentifier id, String action) throws URISyntaxException {
            return new AbstractMap.SimpleImmutableEntry<>(
                    action,
                    createURI(id.getId().toString() + "/" + action));
        }

        public Map<String, URI> links(UniqueIdentifier id, List<String> actions) throws URISyntaxException {
            Map<String, URI> map = new LinkedHashMap<>();
            for (String action : actions) {
                map.put(
                        action,
                        createURI(id.getId().toString() + "/" + action)
                );
            }
            return map;
        }

        public Map<String, URI> links(UniqueIdentifier id, String[] actions) throws URISyntaxException {
            Map<String, URI> map = new LinkedHashMap<>();
            for (String action : actions) {
                map.put(
                        action,
                        createURI(id.getId().toString() + "/" + action)
                );
            }
            return map;
        }
    }

    public StateBuilder<T> stateMapping(String mappingPath, String basePath, HttpServletRequest request) {
        this.apiHelper = new RestHelper(mappingPath, basePath, request);
        return this;
    }


    public StateBuilder<T> link(String action) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.link(
                        apiHelper.link(stateAndLink.getState().getLinearId(), action));
            }
        }
        return this;
    }

    public StateBuilder<T> links(Function<? super T, List<String>> mapper) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.links(
                        apiHelper.links(stateAndLink.getState().getLinearId(),
                                mapper.apply(stateAndLink.getState())));
            }
        }
        return this;
    }

    public StateBuilder<T> links(String[] actions) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.links(
                        apiHelper.links(stateAndLink.getState().getLinearId(), actions));
            }
        }
        return this;
    }

    public StateBuilder<T> links(List<String> actions) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.links(
                        apiHelper.links(stateAndLink.getState().getLinearId(), actions));
            }
        }
        return this;
    }

    public StateBuilder<T> self() throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.self(
                        apiHelper.self(stateAndLink.getState().getLinearId()));
            }
        }
        return this;
    }

    public ResponseEntity<StateAndLinks<T>> build() {
        return this.builder.body(this.states.isEmpty() ? null : this.states.get(0));
    }

    public ResponseEntity<List<StateAndLinks<T>>> buildList() {
        return this.builder.body(this.states);
    }
}
