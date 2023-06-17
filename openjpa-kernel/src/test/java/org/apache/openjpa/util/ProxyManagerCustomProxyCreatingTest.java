package org.apache.openjpa.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Timestamp;
import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.spy;

@RunWith(value = Parameterized.class)
public class ProxyManagerCustomProxyCreatingTest{
    /**
     * CustomProxies documentation:
     * <p>
     * 6.4.3.  Custom Proxies <br>
     * OpenJPA manages proxies through the org.apache.openjpa.util.ProxyManager interface. <br>
     * OpenJPA includes a default proxy manager, the org.apache.openjpa.util.ProxyManagerImpl <br>
     * (with a plugin alias name of default), that will meet the needs of most users.<br>
     * The default proxy manager understands the following configuration properties:<br>
     * <p>
     * TrackChanges: <br>
     * Whether to use smart proxies.<br>
     * (Defaults to true.)<br>
     * <p>
     * AssertAllowedType: <br>
     * Whether to immediately throw an exception if you attempt to add an element to <br>
     * a collection or map that is not assignable to the element type declared in metadata.<br>
     * (Defaults to false.)<br>
     * <p>
     * The default proxy manager can proxy the standard methods of any <br>
     * Collection, List, Map, Queue, Date, or Calendar class, including custom implementations. <br>
     * It can also proxy custom classes whose accessor and mutator methods follow JavaBean naming conventions. <br>
     * Your custom types must, however, meet the following criteria:
     * <p>
     * Custom container types must have a public no-arg constructor or a public constructor that takes a single Comparator parameter.
     * <p>
     * Custom date types must have a public no-arg constructor or a public constructor that <br>
     * takes a single long parameter representing the current time.<br>
     * <p>
     * Other custom types must have a public no-arg constructor or a public copy constructor. <br>
     * If a custom types does not have a copy constructor, <br>
     * it must be possible to fully copy an instance A by creating a new instance B<br>
     * and calling each of B's setters with the value from the corresponding getter on A.<br>
     */
    private final STATE_OF_ORIG stateOfOrig;
    /**
     * Category partitioning for orig is: <br>
     * Object orig: {null}, {valid_obj}, {invalid_obj}<br>
     */
    private Object orig;
    /**
     * seeing the documentation automatic tracking of modifications my guess <br>
     * is that this parameter supposedly will turn that off... <br>
     * <p>
     * for what we know though is just a boolean so
     * Category partitioning for autoOff is: <br>
     * boolean autoOff: {true}, {false}<br>
     */
    private final boolean autoOff;
    private ProxyManagerImpl proxyManagerImpl;
    private final EXPECTED expected;
    private long timeInMillisForCalendar;
    private long timeForDate;
    private int nanosForTimeStamp;
    private Collection<String> collection;
    private SortedSet<String> sortedSet;
    private Map<Integer, String> map;
    private SortedMap<Integer, String> sortedMap;

    private enum STATE_OF_ORIG {
        NULL,
        VALID,
        INVALID,
        PROXY,
        COLLECTION,
        SORTED_SET,
        MAP,
        SORTED_MAP,
        DATE,
        TIMESTAMP,
        CALENDAR,
        FINAL
    }

    private enum EXPECTED {
        SUCCESS,
        FAILURE
    }

    public ProxyManagerCustomProxyCreatingTest(InputTuple inputTuple) {
        this.stateOfOrig = inputTuple.stateOfOrig();
        this.autoOff = inputTuple.autoOff();
        this.expected = inputTuple.expected();
    }

    /**
     * -----------------------------------------------------------------------------<br>
     * Boundary analysis:<br>
     * -----------------------------------------------------------------------------<br>
     * Object orig: null, validInstance, invalidInstance<br>
     * boolean autoOff: true, false<br>
     */
    @Parameterized.Parameters
    public static Collection<InputTuple> getInputTuples() {
        List<InputTuple> inputTupleList = new ArrayList<>();
        //inputTupleList.add(new InputTuple(stateOfOrig, autoOff, expected));
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.NULL, false, EXPECTED.FAILURE));           // [1]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.NULL, true, EXPECTED.FAILURE));            // [2]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.VALID, false, EXPECTED.SUCCESS));          // [3]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.VALID, true, EXPECTED.SUCCESS));           // [4]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.INVALID, false, EXPECTED.FAILURE));        // [5]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.INVALID, true, EXPECTED.FAILURE));         // [6]
        //AFTER JACOCO REPORT:                                                                     //
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.PROXY, false, EXPECTED.SUCCESS));          // [7]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.PROXY, true, EXPECTED.SUCCESS));           // [8]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.COLLECTION, false, EXPECTED.SUCCESS));     // [9]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.COLLECTION, true, EXPECTED.SUCCESS));      // [10]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.SORTED_SET, false, EXPECTED.SUCCESS));     // [11]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.SORTED_SET, true, EXPECTED.SUCCESS));      // [12]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.MAP, false, EXPECTED.SUCCESS));            // [13]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.MAP, true, EXPECTED.SUCCESS));             // [14]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.SORTED_MAP, false, EXPECTED.SUCCESS));     // [15]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.SORTED_MAP, true, EXPECTED.SUCCESS));      // [16]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.DATE, false, EXPECTED.SUCCESS));           // [17]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.DATE, true, EXPECTED.SUCCESS));            // [18]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.TIMESTAMP, false, EXPECTED.SUCCESS));      // [19]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.TIMESTAMP, true, EXPECTED.SUCCESS));       // [20]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.CALENDAR, false, EXPECTED.SUCCESS));       // [21]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.CALENDAR, true, EXPECTED.SUCCESS));        // [22]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.FINAL, false, EXPECTED.FAILURE));          // [23]
        inputTupleList.add(new InputTuple(STATE_OF_ORIG.FINAL, true, EXPECTED.FAILURE));           // [24]
        //AFTER PIT REPORT --> no test cases added but added state to object and then verified them
        // it's a pity that we can't use Mockito spy() and verify() because the calls are on other instances
        return inputTupleList;
    }

    private static final class InputTuple {
        private final STATE_OF_ORIG stateOfOrig;
        private final boolean autoOff;
        private final EXPECTED expected;

        private InputTuple(STATE_OF_ORIG stateOfOrig,
                           boolean autoOff,
                           EXPECTED expected) {
            this.stateOfOrig = stateOfOrig;
            this.autoOff = autoOff;
            this.expected = expected;
        }
        public STATE_OF_ORIG stateOfOrig() {
            return stateOfOrig;
        }
        public boolean autoOff() {
            return autoOff;
        }
        public EXPECTED expected() {
            return expected;
        }
    }

    @Before
    public void setUpEachTime(){
        this.proxyManagerImpl = spy(new ProxyManagerImpl());
        ThisIsAProxyablePerson thisIsAProxyablePerson;
        ThisIsAnUnproxyableCar thisIsAnUnproxyableCar;
        switch (this.stateOfOrig){
            case NULL:
                this.orig = null;
                break;
            case VALID:
                thisIsAProxyablePerson = getPerson();
                this.orig = thisIsAProxyablePerson;
                break;
            case INVALID:
                thisIsAnUnproxyableCar = getCar();
                this.orig = thisIsAnUnproxyableCar;
                break;
            case PROXY:
                thisIsAProxyablePerson = getPerson();
                this.orig = this.proxyManagerImpl.newCustomProxy(thisIsAProxyablePerson, true);
                break;
            case COLLECTION:
                this.collection = new ArrayList<>();
                collection.add("Where");
                collection.add("Are");
                collection.add("We");
                this.orig = collection;
                break;
            case SORTED_SET:
                this.sortedSet = new TreeSet<>();
                sortedSet.add("Here");
                sortedSet.add("We");
                sortedSet.add("Are");
                this.orig = sortedSet;
                break;
            case MAP:
                this.map = new HashMap<>();
                map.put(1,"a");
                map.put(2,"b");
                map.put(3,"c");
                this.orig = map;
                break;
            case SORTED_MAP:
                this.sortedMap = new TreeMap<>();
                sortedMap.put(4,"d");
                sortedMap.put(5,"e");
                sortedMap.put(6,"f");
                this.orig = sortedMap;
                break;
            case DATE:
                Date date = new Date();
                this.timeForDate = System.currentTimeMillis();
                date.setTime(this.timeForDate);
                this.orig = date;
                break;
            case TIMESTAMP:
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                this.nanosForTimeStamp = 123;
                timestamp.setNanos(this.nanosForTimeStamp);
                this.orig = timestamp;
                break;
            case CALENDAR:
                Calendar calendar = Calendar.getInstance();
                this.timeInMillisForCalendar = System.currentTimeMillis();
                calendar.setTimeInMillis(timeInMillisForCalendar);
                this.orig = calendar;
                break;
            case FINAL:
                ThisIsJustAFinalClass thisIsJustAFinalClass = new ThisIsJustAFinalClass();
                this.orig = thisIsJustAFinalClass;
                break;
        }
    }

    private static ThisIsAProxyablePerson getPerson() {
        ThisIsAProxyablePerson thisIsAProxyablePerson;
        thisIsAProxyablePerson = new ThisIsAProxyablePerson();
        thisIsAProxyablePerson.sayHello();
        return thisIsAProxyablePerson;
    }

    private static ThisIsAnUnproxyableCar getCar() {
        ThisIsAnUnproxyableCar thisIsAnUnproxyableCar;
        Random random = new Random(System.currentTimeMillis());
        if(random.nextInt()%2 == 0){
            thisIsAnUnproxyableCar = new ThisIsAnUnproxyableCar("Ford", "Mustang");
        }else{
            thisIsAnUnproxyableCar = new ThisIsAnUnproxyableCar("Chevrolet", "Camaro");
        }
        thisIsAnUnproxyableCar.broomBroom();
        return thisIsAnUnproxyableCar;
    }

    @Test//@Ignore
    public void testNewCustomProxy() {
        Object proxyOrNot = this.proxyManagerImpl.newCustomProxy(this.orig, this.autoOff);
        if(this.expected == EXPECTED.SUCCESS){
            Assert.assertThat(proxyOrNot, instanceOf(Proxy.class));
            //ADDITIONS AFTER PIT REPORT:
            switch (this.stateOfOrig){
                case COLLECTION:
                    Assert.assertEquals(this.collection, proxyOrNot);
                    break;
                case SORTED_SET:
                    Assert.assertEquals(this.sortedSet, proxyOrNot);
                    break;
                case MAP:
                    Assert.assertEquals(this.map, proxyOrNot);
                    break;
                case SORTED_MAP:
                    Assert.assertEquals(this.sortedMap, proxyOrNot);
                    break;
                case DATE:
                    Assert.assertEquals(this.timeForDate, ((Date)proxyOrNot).getTime());
                    break;
                case TIMESTAMP:
                    Assert.assertEquals(this.nanosForTimeStamp, ((Timestamp)proxyOrNot).getNanos());
                    break;
                case CALENDAR:
                    Assert.assertEquals(this.timeInMillisForCalendar, ((Calendar)proxyOrNot).getTimeInMillis());
                    break;
            }
        }else{
            Assert.assertNull(proxyOrNot);
        }
    }

}