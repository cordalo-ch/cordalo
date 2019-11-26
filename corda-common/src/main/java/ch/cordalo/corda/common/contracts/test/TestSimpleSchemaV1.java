/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package ch.cordalo.corda.common.contracts.test;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@CordaSerializable
public class TestSimpleSchemaV1 extends MappedSchema {

    public TestSimpleSchemaV1() {
        super(TestSimpleSchema.class, 1, ImmutableList.of(PersistentTestSimple.class));
    }

    @Entity
    @Table(name = "simple", indexes = @Index(name = "key_idx", columnList = "key", unique = false))
    public static class PersistentTestSimple extends PersistentState implements Serializable {
        @Column(name = "linearId", nullable = false)
        UUID linearId;

        @Column(name = "owner", nullable = false)
        String owner;
        @Column(name = "key", nullable = false)
        String key;
        @Column(name = "value", nullable = false)
        String value;
        @Column(name = "partners")
        String partners;

        public PersistentTestSimple(UUID linearId, String owner, String key, String value, String partners) {
            this.linearId = linearId;
            this.owner = owner;
            this.key = key;
            this.value = value;
            this.partners = partners;
        }

        public PersistentTestSimple() {
            this(UUID.randomUUID(), "", "", "", "");
        }
    }
}
