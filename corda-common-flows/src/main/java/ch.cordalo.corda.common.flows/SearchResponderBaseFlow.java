/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package ch.cordalo.corda.common.flows;

import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.LinearState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;

public abstract class SearchResponderBaseFlow<T extends LinearState, V extends Object, X> extends ResponderBaseFlow<T>  {

    public SearchResponderBaseFlow(FlowSession otherFlow) {
        super(otherFlow);
    }

    @Suspendable
    private static <V extends Object> V unwrapper(V data) {
        return data;
    }

    @Suspendable
    public Unit responderFlow_receiveAndSend(Class<V> receiverClass, SimpleFlow.SearchResponder<T, V, X> searcher) throws FlowException {
        FlowHelper<T> flowHelper = new FlowHelper<>(this.getServiceHub());

        /* receive the requested value from sender */
        V searchValue = this.otherFlow.receive(receiverClass).unwrap(SearchResponderBaseFlow::unwrapper);

        /* search unconsumed state by value */
        T localState = searcher.search(flowHelper, searchValue);
        if (localState == null) {
            /* send no result back to sender */
            this.otherFlow.send(null);
            return null;
        }

        /* try to share officially with corda the state and send back. Using this principle.
        Updates will be shared in the future --> sigle point of truth and not copy */
        FlowLogic<X> shareStateFlow = searcher.createShareStateFlow(localState, this.otherFlow.getCounterparty());
        if (shareStateFlow != null) {
            subFlow(shareStateFlow);
        }

        /* send back state linear id to counter party */
        this.otherFlow.send(localState.getLinearId());
        return null;
    }
}
