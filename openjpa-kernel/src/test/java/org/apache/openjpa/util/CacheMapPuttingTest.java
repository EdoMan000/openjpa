/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(value = Parameterized.class)
public class CacheMapPuttingTest {
    public static final Class<? extends Exception> SUCCESS = null;
    private CacheMap cacheMap;
    /**
     * Category partitioning for key is: <br>
     * Object key: {null}, {existent(valid)}, {notExistent(valid)}, {invalid}
     */
    private Object key;
    /**
     * Category partitioning for value is: <br>
     * Object value: {null}, {valid}, {invalid}
     */
    private Object value;
    private final STATE_OF_KEY stateOfKey;
    private final STATE_OF_VALUE stateOfValue;
    private final Object existingValue;

    private enum STATE_OF_KEY {
        NULL,
        EXISTENT,
        NOT_EXISTENT,
        INVALID
    }
    private enum STATE_OF_VALUE {
        NULL,
        VALID,
        INVALID
    }

    public CacheMapPuttingTest(PutInputTuple putInputTuple) {
        this.stateOfKey = putInputTuple.stateOfKey();
        this.stateOfValue = putInputTuple.stateOfValue();
        this.existingValue = new Object();
    }

    /**
     * -----------------------------------------------------------------------------<br>
     * Boundary analysis:<br>
     * -----------------------------------------------------------------------------<br>
     * Object key: null, existent_key, not_existent_key, invalid_obj<br>
     * Object value: null, valid_obj, invalid_obj<br>
     */
    @Parameterized.Parameters
    public static Collection<PutInputTuple> getReadInputTuples() {
        List<PutInputTuple> putInputTupleList = new ArrayList<>();
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, STATE_OF_VALUE.VALID));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, STATE_OF_VALUE.NULL));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, STATE_OF_VALUE.INVALID));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, STATE_OF_VALUE.VALID));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, STATE_OF_VALUE.NULL));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, STATE_OF_VALUE.INVALID));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, STATE_OF_VALUE.VALID));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, STATE_OF_VALUE.NULL));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, STATE_OF_VALUE.INVALID));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, STATE_OF_VALUE.VALID));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, STATE_OF_VALUE.NULL));
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, STATE_OF_VALUE.INVALID));

        return  putInputTupleList;
    }

    private static final class PutInputTuple {
        private final STATE_OF_KEY stateOfKey;
        private final STATE_OF_VALUE stateOfValue;

        private PutInputTuple(STATE_OF_KEY stateOfKey,
                              STATE_OF_VALUE stateOfValue) {
            this.stateOfKey = stateOfKey;
            this.stateOfValue = stateOfValue;
        }
        public STATE_OF_KEY stateOfKey() {
            return stateOfKey;
        }
        public STATE_OF_VALUE stateOfValue() {
            return stateOfValue;
        }
    }

    @Before
    public void setUpEachTime() {
        this.cacheMap = new CacheMap();
        switch (stateOfKey){
            case NULL:
                this.key = null;
                break;
            case INVALID:
                this.key = new MyInvalidObject();
                break;
            case EXISTENT:
                this.key = new Object();
                cacheMap.put(this.key, this.existingValue);
                break;
            case NOT_EXISTENT:
                this.key = new Object();
                break;
        }
        switch (stateOfValue){
            case NULL:
                this.value = null;
                break;
            case INVALID:
                this.value = new MyInvalidObject();
                break;
            case VALID:
                this.value = new Object();
                break;
        }
    }

    @Test
    public void put() {
        Object retVal = this.cacheMap.put(this.key, this.value);
        Assert.assertEquals(this.value, this.cacheMap.get(this.key));
        if(this.stateOfKey == STATE_OF_KEY.EXISTENT){
            Assert.assertEquals(this.existingValue, retVal);
        }
    }
}
