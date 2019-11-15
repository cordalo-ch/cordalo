package ch.cordalo.corda.common.client.webserver;

import ch.cordalo.corda.ext.CordaProxy;
import net.corda.core.contracts.LinearState;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowProgressHandle;
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
            CordaProxy.register(new CordaRpcProxy(rpcConnection));
            this.rpc = CordaProxy.getInstance();
        } else {
            this.rpc = CordaProxy.getInstance();
        }
        if (rpc == null || !rpc.isValid()) {
            logger.error("NodeRPC connection + proxy is not initialized (null)");
            return;
        }
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
        return this.getProxy().wellKnownPartyFromX500Name(CordaX500Name.parse(partyString));
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