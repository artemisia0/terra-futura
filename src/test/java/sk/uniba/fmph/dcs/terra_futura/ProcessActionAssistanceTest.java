package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


class GameResourcesFake implements InterfaceGameResources {
    private final Map<Resource, Integer> pool = new HashMap<>();

    public void set(Resource r, int v) {
        pool.put(r, v);
    }

    public int get(Resource r) { return pool.getOrDefault(r, 0); }

    @Override
    public boolean tryConsume(Resource r, int count) {
        int have = pool.getOrDefault(r, 0);
        if (have < count) return false;
        pool.put(r, have - count);
        return true;
    }
}

public class ProcessActionAssistanceTest {
    private GameResourcesFake res;
    private ProcessActionAssistance assistance;
    private List<Action> actions;

    @Before
    public void setUp() {
        res = new GameResourcesFake();
        actions = new ArrayList<>();

        // given actions: ACTIVATE, BUILD, TRADE in that order
        actions.add(Action.ACTIVATE);
        actions.add(Action.BUILD);
        actions.add(Action.TRADE);

        assistance = new ProcessActionAssistance(actions, res);
    }


    // helpers
    private void assertState(JSONObject obj, int expectedCurrent, boolean expectedDone) {
        assertEquals(expectedCurrent, obj.getInt("currentIndex"));
        assertEquals(expectedDone, obj.getBoolean("completed"));
    }

    //tests
    @Test
    public void testStateInitial() {
        JSONObject state = new JSONObject(assistance.state());
        assertState(state, 0, false);
        JSONArray arr = state.getJSONArray("orderedActions");
        assertEquals("ACTIVATE", arr.getString(0));
        assertEquals("BUILD", arr.getString(1));
        assertEquals("TRADE", arr.getString(2));
    }

    @Test
    public void testPerformValidActionAdvanceIndex() {
        res.set(Resource.ACTION_POINT, 3);

        assertTrue(assistance.perform(Action.ACTIVATE));
        JSONObject s1 = new JSONObject(assistance.state());
        assertState(s1, 1, false);

        assertTrue(assistance.perform(Action.BUILD));
        JSONObject s2 = new JSONObject(assistance.state());
        assertState(s2, 2, false);

        assertTrue(assistance.perform(Action.TRADE));
        JSONObject s3 = new JSONObject(assistance.state());
        assertState(s3, 3, true); // done
    }

    @Test
    public void testPerformWrongActionFails() {
        res.set(Resource.ACTION_POINT, 3);

        // First expected: ACTIVATE
        assertFalse(assistance.perform(Action.BUILD));
        JSONObject s1 = new JSONObject(assistance.state());
        assertState(s1, 0, false); // remains unchanged

        // Still expected: ACTIVATE
        assertTrue(assistance.perform(Action.ACTIVATE));
        JSONObject s2 = new JSONObject(assistance.state());
        assertState(s2, 1, false);
    }

    @Test
    public void testResourceConsumption() {
        res.set(Resource.ACTION_POINT, 2);

        assertTrue(assistance.perform(Action.ACTIVATE));
        assertEquals(1, res.get(Resource.ACTION_POINT));

        assertTrue(assistance.perform(Action.BUILD));
        assertEquals(0, res.get(Resource.ACTION_POINT));
    }

    @Test
    public void testStopsIfOutOfResources() {
        res.set(Resource.ACTION_POINT, 1);

        // ACTIVATE consumes 1 → OK
        assertTrue(assistance.perform(Action.ACTIVATE));
        assertEquals(0, res.get(Resource.ACTION_POINT));

        // BUILD consumes 1 → cannot
        assertFalse(assistance.perform(Action.BUILD));

        JSONObject state = new JSONObject(assistance.state());
        assertState(state, 1, false);
    }

    @Test
    public void testCompleteCannotPerformMore() {
        res.set(Resource.ACTION_POINT, 10);
        assertTrue(assistance.perform(Action.ACTIVATE));
        assertTrue(assistance.perform(Action.BUILD));
        assertTrue(assistance.perform(Action.TRADE));
        assertTrue(assistance.isCompleted());
        assertFalse(assistance.perform(Action.ACTIVATE));
        assertFalse(assistance.perform(Action.BUILD));
        JSONObject obj = new JSONObject(assistance.state());
        assertState(obj, 3, true);
    }

    @Test
    public void testJsonStateAfterPartialProgress() {
        res.set(Resource.ACTION_POINT, 10);

        assistance.perform(Action.ACTIVATE);

        JSONObject obj = new JSONObject(assistance.state());

        JSONArray steps = obj.getJSONArray("orderedActions");
        assertEquals("ACTIVATE", steps.getString(0));
        assertEquals("BUILD",    steps.getString(1));
        assertEquals("TRADE",    steps.getString(2));

        assertEquals(1, obj.getInt("currentIndex"));
        assertFalse(obj.getBoolean("completed"));
    }
}
