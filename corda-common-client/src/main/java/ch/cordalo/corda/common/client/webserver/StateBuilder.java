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
        this.states = Collections.singletonList(new StateAndLinks<>(state));
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
            return this.getRoot().resolve(this.mappingPath+this.bashPath+"/"+subpath);
        }
        public URI self(String modelPlural, UniqueIdentifier id) throws URISyntaxException {
            return this.createURI(modelPlural + "/" + id.getId().toString());
        }

        public Map.Entry<String, URI> link(String modelPlural, UniqueIdentifier id, String action) throws URISyntaxException {
            return new AbstractMap.SimpleImmutableEntry<>(
                    action,
                    createURI(modelPlural + "/" + id.getId().toString() + "/" + action));
        }
        public Map<String, URI> links(String modelPlural, UniqueIdentifier id, List<String> actions) throws URISyntaxException {
            Map<String, URI> map = new LinkedHashMap<>();
            for (String action: actions) {
                map.put(
                        action,
                        createURI(modelPlural + "/" + id.getId().toString() + "/" + action)
                );
            }
            return map;
        }
        public Map<String, URI> links(String modelPlural, UniqueIdentifier id, String[] actions) throws URISyntaxException {
            Map<String, URI> map = new LinkedHashMap<>();
            for(String action : actions) {
                map.put(
                        action,
                        createURI(modelPlural + "/" + id.getId().toString() + "/" + action)
                );
            }
            return map;
        }
    }

    public StateBuilder<T> stateMapping(String mappingPath, String basePath, HttpServletRequest request) {
        this.apiHelper = new RestHelper(mappingPath, basePath, request);
        return this;
    }



    public StateBuilder<T> link(String modelPlural, String action) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.link(
                    apiHelper.link(modelPlural, stateAndLink.getState().getLinearId(), action));
            }
        }
        return this;
    }

    public StateBuilder<T> links(String modelPlural, Function<? super T, List<String>> mapper) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.links(
                        apiHelper.links(modelPlural, stateAndLink.getState().getLinearId(),
                                mapper.apply(stateAndLink.getState())));
            }
        }
        return this;
    }

    public StateBuilder<T> links(String modelPlural, String[] actions) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.links(
                    apiHelper.links(modelPlural, stateAndLink.getState().getLinearId(), actions));
            }
        }
        return this;
    }
    public StateBuilder<T> links(String modelPlural, List<String> actions) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.links(
                    apiHelper.links(modelPlural, stateAndLink.getState().getLinearId(), actions));
            }
        }
        return this;
    }
    public StateBuilder<T> self(String modelPlural) throws URISyntaxException {
        if (apiHelper != null) {
            for (StateAndLinks<T> stateAndLink : this.states) {
                stateAndLink.self(
                        apiHelper.self(modelPlural, stateAndLink.getState().getLinearId()));
            }
        }
        return this;
    }

    public ResponseEntity<StateAndLinks<T>> build() {
        return this.builder.body(this.states.get(0));
    }
    public ResponseEntity<List<StateAndLinks<T>>> buildList() {
        return this.builder.body(this.states);
    }
}
