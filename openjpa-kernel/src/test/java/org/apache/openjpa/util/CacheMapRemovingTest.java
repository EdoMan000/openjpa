package org.apache.openjpa.util;

import org.junit.After;
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
public class CacheMapRemovingTest {
    private CacheMap cacheMap;
    /**
     * Category partitioning for key is: <br>
     * Object key: {null}, {existent(valid)}, {notExistent(valid)}, {invalid}
     */
    private Object key;
    private final STATE_OF_KEY stateOfKey;
    private final Object existingValue;
    private final boolean isKeyPinned;

    private enum STATE_OF_KEY {
        NULL,
        EXISTENT,
        NOT_EXISTENT,
        INVALID
    }

    public CacheMapRemovingTest(PutInputTuple putInputTuple) {
        this.stateOfKey = putInputTuple.stateOfKey();
        this.isKeyPinned = putInputTuple.isKeyPinned();
        this.existingValue = new Object();
    }

    /**
     * -----------------------------------------------------------------------------<br>
     * Boundary analysis:<br>
     * -----------------------------------------------------------------------------<br>
     * Object key: null, existent_key, not_existent_key, invalid_obj<br>
     */
    @Parameterized.Parameters
    public static Collection<PutInputTuple> getReadInputTuples() {
        List<PutInputTuple> putInputTupleList = new ArrayList<>();
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, false));              //[1]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, false));          //[2]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, false));      //[3]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, false));           //[4]
        //AFTER JACOCO REPORT
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NULL, true));              //[5]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.EXISTENT, true));          //[6]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.NOT_EXISTENT, true));      //[7]
        putInputTupleList.add(new PutInputTuple(STATE_OF_KEY.INVALID, true));           //[8]
        return  putInputTupleList;
    }

    private static final class PutInputTuple {
        private final STATE_OF_KEY stateOfKey;
        private final boolean isKeyPinned;

        private PutInputTuple(STATE_OF_KEY stateOfKey,
                              boolean isKeyPinned) {
            this.stateOfKey = stateOfKey;
            this.isKeyPinned = isKeyPinned;
        }
        public STATE_OF_KEY stateOfKey() {
            return stateOfKey;
        }
        public boolean isKeyPinned() {
            return isKeyPinned;
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
    }

    @Test
    public void remove() {
        Object deletedVal = this.cacheMap.remove(this.key);
        if(this.stateOfKey == STATE_OF_KEY.EXISTENT) {
            verify(this.cacheMap).entryRemoved(this.key, deletedVal, false);
        }
        if(this.isKeyPinned){
            if(this.stateOfKey == STATE_OF_KEY.NOT_EXISTENT){
                verify(this.cacheMap, times(2)).writeLock();
                verify(this.cacheMap, times(2)).writeUnlock();
            } else if (this.stateOfKey == STATE_OF_KEY.EXISTENT) {
                verify(this.cacheMap, times(3)).writeLock();
                verify(this.cacheMap, times(3)).writeUnlock();
            }
        }else{
            if(this.stateOfKey == STATE_OF_KEY.NOT_EXISTENT){
                verify(this.cacheMap).writeLock();
                verify(this.cacheMap).writeUnlock();
            } else if (this.stateOfKey == STATE_OF_KEY.EXISTENT) {
                verify(this.cacheMap, times(2)).writeLock();
                verify(this.cacheMap, times(2)).writeUnlock();
            }
        }
        Assert.assertNull(this.cacheMap.get(this.key));
        if(this.stateOfKey == STATE_OF_KEY.EXISTENT){
            Assert.assertEquals(this.existingValue, deletedVal);
        }else{
            Assert.assertNull(deletedVal);
        }
    }

    @After
    public void cleanUpEachTime(){
        this.cacheMap.clear();
    }
}