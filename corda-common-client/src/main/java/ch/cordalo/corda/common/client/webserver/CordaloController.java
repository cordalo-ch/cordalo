/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.cordalo.corda.common.client.webserver;

import ch.cordalo.corda.ext.CordaProxy;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.LinearState;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class CordaloController {

    private final CordaProxy rpc;

    private final static Logger logger = LoggerFactory.getLogger(CordaloController.class);

    public CordaloController(RpcConnection rpcConnection) {
        if (CordaProxy.getInstance() == null) {
            this.rpc = new CordaRpcProxy(rpcConnection).register();
        } else {
            this.rpc = CordaProxy.getInstance();
        }
        if (rpc == null || !rpc.isValid()) {
            logger.error("NodeRPC connection + proxy is not initialized (null)");
            return;
        }
        JacksonSupport.createDefaultMapper(this.getProxy());
    }

    public CordaRPCOps getProxy() {
        return this.rpc.getProxy();
    }

    public Party getMe() { return this.rpc.getMe(); }

    public Party getNotary() {  return this.rpc.getNotary(); }

    public boolean isValid() {
        return this.getProxy() != null;
    }

    public <T extends LinearState> ResponseEntity<StateAndLinks<T>> buildResponseFromException(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new StateAndLinks<T>().error(message));
    }
    public <T extends LinearState> ResponseEntity<StateAndLinks<T>> buildResponseFromException(HttpStatus status, Throwable exception) {
        return ResponseEntity.status(status)
                .body(new StateAndLinks<T>().error(exception));
    }
    public <T extends LinearState> ResponseEntity<List<StateAndLinks<T>>> buildResponsesFromException(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Arrays.asList(new StateAndLinks<T>().error(message)));
    }
    public <T extends LinearState> ResponseEntity<List<StateAndLinks<T>>> buildResponsesFromException(HttpStatus status, Throwable exception) {
        return ResponseEntity.status(status)
                .body(Arrays.asList(new StateAndLinks<T>().error(exception)));
    }

    public Party partyFromString(String partyString) {
        return partyString == null ?
                null : this.getProxy().wellKnownPartyFromX500Name(CordaX500Name.parse(partyString));
    }

    protected <T> void checkStartFlow(@NotNull Class<? extends FlowLogic<? extends T>> logicType, @NotNull Object... args) {
        List<? extends Class<?>> collect = Arrays.stream(args).map(x -> x.getClass()).collect(Collectors.toList());
        Class<?>[] classes = new Class<?>[collect.size()];
        collect.toArray(classes);
        logger.info("Check flow constructor: public %s(%s)",
                logicType.getCanonicalName(),
                String.join(", ", Arrays.stream(classes).map(c -> c.getName()).collect(Collectors.toList())));
        try {
            Constructor<? extends FlowLogic<? extends T>> constructor = logicType.getConstructor(classes);
            if (constructor != null) {
                constructor.newInstance(args);
            } else {
                throw new RuntimeException("No constructor found for "+logicType.getCanonicalName());
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error while instantiating flow", e);
        }
    }

    public <T> T startFlow(@NotNull Class<? extends FlowLogic<? extends T>> logicType, @NotNull Object... args) throws ExecutionException, InterruptedException {
        checkStartFlow(logicType, args);
        return this.getProxy().startTrackedFlowDynamic(logicType, args).getReturnValue().get();
    }




}