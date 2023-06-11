package org.apache.openjpa.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(value = Parameterized.class)
public class CacheMapPuttingTest {
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
    private final boolean isKeyPinned;

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
        this.isKeyPinned = putInputTuple.isKeyPinned();
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
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, false, STATE_OF_VALUE.VALID));              //[1]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, false, STATE_OF_VALUE.NULL));               //[2]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, false, STATE_OF_VALUE.INVALID));            //[3]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, false, STATE_OF_VALUE.VALID));          //[4]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, false, STATE_OF_VALUE.NULL));           //[5]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, false, STATE_OF_VALUE.INVALID));        //[6]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, false, STATE_OF_VALUE.VALID));      //[7]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, false, STATE_OF_VALUE.NULL));       //[8]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, false, STATE_OF_VALUE.INVALID));    //[9]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, false, STATE_OF_VALUE.VALID));           //[10]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, false, STATE_OF_VALUE.NULL));            //[11]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, false, STATE_OF_VALUE.INVALID));         //[12]
        //AFTER JACOCO and ba-dua REPORT                                                                           //
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, true, STATE_OF_VALUE.VALID));            //[13]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, true, STATE_OF_VALUE.NULL));             //[14]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, true, STATE_OF_VALUE.INVALID));          //[15]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, true, STATE_OF_VALUE.VALID));        //[16]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, true, STATE_OF_VALUE.NULL));         //[17]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, true, STATE_OF_VALUE.INVALID));      //[18]
        //AFTER PIT REPORT -> used mockito verify() to kill some mutations, no need to add tests

        return  putInputTupleList;
    }

    private static final class PutInputTuple {
        private final STATE_OF_KEY stateOfKey;
        private final STATE_OF_VALUE stateOfValue;
        private final boolean isKeyPinned;

        private PutInputTuple(STATE_OF_KEY stateOfKey,
                              boolean isKeyPinned,
                              STATE_OF_VALUE stateOfValue) {
            this.stateOfKey = stateOfKey;
            this.isKeyPinned = isKeyPinned;
            this.stateOfValue = stateOfValue;
        }
        public STATE_OF_KEY stateOfKey() {
            return stateOfKey;
        }
        public boolean isKeyPinned() {
            return isKeyPinned;
        }
        public STATE_OF_VALUE stateOfValue() {
            return stateOfValue;
        }
    }

    @Before
    public void setUpEachTime() {
        this.cacheMap = spy(new CacheMap());
        switch (stateOfKey){
            case NULL:
                this.key = null;
                break;
            case INVALID:
                this.key = new MyInvalidObject();
                break;
            case EXISTENT:
                this.key = new Object();
                this.cacheMap.put(this.key, this.existingValue);
                if(this.isKeyPinned) {
                    this.cacheMap.pin(key);
                }
                break;
            case NOT_EXISTENT:
                this.key = new Object();
                if(this.isKeyPinned) {
                    this.cacheMap.pin(key);
                }
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
        if(this.stateOfKey == STATE_OF_KEY.NOT_EXISTENT){
            verify(this.cacheMap).entryAdded(this.key, this.value);
        } else if (this.stateOfKey == STATE_OF_KEY.EXISTENT) {
            verify(this.cacheMap).entryRemoved(this.key, this.existingValue, false);
            verify(this.cacheMap).entryAdded(this.key, this.value);
        }
        if(this.isKeyPinned){
            if(this.stateOfKey == STATE_OF_KEY.NOT_EXISTENT){
                verify(this.cacheMap, times(2)).writeLock();
                verify(this.cacheMap, times(2)).writeUnlock();
            } else if (this.stateOfKey == STATE_OF_KEY.EXISTENT) {
                verify(this.cacheMap, times(3)).writeLock();
                verify(this.cacheMap, times(3)).writeUnlock();
            }
            Assert.assertEquals(this.value, this.cacheMap.pinnedMap.get(this.key));
        }else {
            if(this.stateOfKey == STATE_OF_KEY.NOT_EXISTENT){
                verify(this.cacheMap).writeLock();
                verify(this.cacheMap).writeUnlock();
            } else if (this.stateOfKey == STATE_OF_KEY.EXISTENT) {
                verify(this.cacheMap, times(2)).writeLock();
                verify(this.cacheMap, times(2)).writeUnlock();
            }
            Assert.assertEquals(this.value, this.cacheMap.cacheMap.get(this.key));
        }
        if(this.stateOfKey == STATE_OF_KEY.EXISTENT){
            Assert.assertEquals(this.existingValue, retVal);
        }
    }
}