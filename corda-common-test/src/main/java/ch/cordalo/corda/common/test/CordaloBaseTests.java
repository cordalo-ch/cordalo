package ch.cordalo.corda.common.test;

import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.*;
import org.junit.After;

import java.security.PublicKey;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

abstract public class CordaloBaseTests {
    public abstract CordaTestNetwork setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses);
    public abstract CordaTestNetwork getNetwork();
    public void tearDown() {
        if (this.getNetwork() != null) this.getNetwork().stopNodes();
    };

}
