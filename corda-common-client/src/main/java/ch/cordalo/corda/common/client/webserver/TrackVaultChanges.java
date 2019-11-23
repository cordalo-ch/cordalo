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
            DataFeed<Vault.Page<T>, Vault.Update<T>> dataFeed = proxy.vaultTrackByWithPagingSpec(typeOfT, new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL), pageSpec);
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
