/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package ch.cordalo.corda.common.test;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatedBy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FindResponderClasses {
    public static List<Class<? extends FlowLogic>> find(Class clazz) {

        List<Class<? extends FlowLogic>> list = new ArrayList<>();
        for(Class<?> c : clazz.getClasses()) {
            validateAllMethodsMustHaveSuspendable(c);
            InitiatedBy annotation = c.getAnnotation(InitiatedBy.class);
            if (annotation != null) {
                list.add((Class<? extends FlowLogic>) c);
            }
        }
        return list;
    }

    private static void validateAllMethodsMustHaveSuspendable(Class clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            Suspendable annotation = m.getAnnotation(Suspendable.class);
            if (annotation == null) {
                throw new RuntimeException("Clazz " + clazz.getName() + "::" + m.getName() + " does not have @Suspendable Annotation");
            }
        }
    }
}
