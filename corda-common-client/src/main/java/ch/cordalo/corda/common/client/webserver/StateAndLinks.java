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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class StateAndLinks<T> {

    public static class ErrorMessage {
        private final String errorClassName;
        private final String message;

        public ErrorMessage(String errorClassName, String message) {
            this.errorClassName = errorClassName;
            this.message = message;
        }

        public ErrorMessage(Throwable e) {
            this.errorClassName = e.getClass().getName();
            this.message = e.getMessage();
        }

        public String getErrorClassName() {
            return errorClassName;
        }

        public String getMessage() {
            return message;
        }
    }

    private final T state;
    private final Map<String, String> links;
    private ErrorMessage error;

    public StateAndLinks(T state) {
        this(state, new LinkedHashMap<>());
    }

    public StateAndLinks() {
        this(null, new LinkedHashMap<>());
    }

    public StateAndLinks(T state, Map<String, String> links) {
        this.state = state;
        this.links = links;
    }

    public void link(String key, URI newUri) {
        this.links.put(key, newUri.toString());
    }

    public void link(Map.Entry<String, URI> entry) {
        this.links.put(entry.getKey(), entry.getValue().toString());
    }

    public void links(Map<String, URI> newLinks) {
        for (Map.Entry<String, URI> entry : newLinks.entrySet()) {
            this.link(entry.getKey(), entry.getValue());
        }
    }

    public void self(URI uri) {
        this.link("self", uri);
    }

    public T getState() {
        return state;
    }

    public Map<String, String> getLinks() {
        return this.links;
    }

    public ErrorMessage getError() {
        return this.error;
    }

    public StateAndLinks<T> error(Throwable e) {
        this.error = new ErrorMessage(e);
        return this;
    }

    public StateAndLinks<T> error(String message) {
        this.error = new ErrorMessage("Message", message);
        return this;
    }

}
