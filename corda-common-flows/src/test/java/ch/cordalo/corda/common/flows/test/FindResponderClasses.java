package ch.cordalo.corda.common.flows.test;

import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatedBy;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FindResponderClasses {
    public static List<Class<? extends FlowLogic>> find(Class clazz) {
        List<Class<? extends FlowLogic>> list = new ArrayList<>();
        for(Class<?> c : clazz.getClasses()) {
            InitiatedBy annotation = c.getAnnotation(InitiatedBy.class);
            if (annotation != null) {
                list.add((Class<? extends FlowLogic>) c);
            }
        }
        return list;
    }

    @Test
    public void find() {
        find(TestBaseFlow.class);
    }
}
