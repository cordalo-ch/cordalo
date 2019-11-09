package ch.cordalo.corda.common.client.webserver;

import net.corda.core.contracts.LinearState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.DataFeed;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

/* based on example
    https://www.callicoder.com/spring-boot-task-scheduling-with-scheduled-annotation/
 */

public abstract class TrackVaultChanges<T extends LinearState> {
    private static final Logger logger = LoggerFactory.getLogger(TrackVaultChanges.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final CordaRPCOps proxy;
    private final CordaX500Name myLegalName;
    private final Class<T> typeOfT;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    protected TrackVaultChanges(RpcConnection rpc, Class<T> typeOfT) {
        this.typeOfT = typeOfT;
        if (rpc.getProxy() == null) {
            this.proxy = null;
            this.myLegalName = null;
            logger.error("NodeRPC connection + proxy is not initialized (null)");
            return;
        }
        this.proxy = rpc.getProxy();
        this.myLegalName = rpc.getProxy().nodeInfo().getLegalIdentities().get(0).getName();

    }

    public void installVaultFeedAndSubscribeToTopic(String topicName) {
        if (proxy != null) {
            PageSpecification pageSpec = new PageSpecification(1, 1);
            DataFeed<Vault.Page<T>, Vault.Update<T>> dataFeed = proxy.vaultTrackByWithPagingSpec(typeOfT, new QueryCriteria.VaultQueryCriteria(), pageSpec);
            logger.info("Vault Update Feed :: {} - subscribed for {}", dateTimeFormatter.format(LocalDateTime.now()), this.typeOfT.getSimpleName());
            dataFeed.getUpdates().subscribe(
                    next -> {
                        this.triggerChanged(topicName, typeOfT);
                        logger.info("Vault Feed Updated :: {} - name={} topic={}", dateTimeFormatter.format(LocalDateTime.now()), this.typeOfT.getSimpleName(), topicName);
                    },
                    error -> {
                        logger.info("Vault Feed Exception :: {} - name={} - topic={} - error={} - message={}",
                                dateTimeFormatter.format(LocalDateTime.now()),
                                this.typeOfT.getSimpleName(),
                                topicName,
                                error.getClass().getSimpleName(),
                                error.getMessage());
                    },
                    () -> {

                    }
            );
        }
    }

    protected void triggerChanged(String topicName, Class<T> typeOfT) {
        LinkedHashMap<String, Object> trigger = new LinkedHashMap<>();
        trigger.put("stateClass", typeOfT.getName());
        this.messagingTemplate.convertAndSend(topicName, trigger);
    }

}
