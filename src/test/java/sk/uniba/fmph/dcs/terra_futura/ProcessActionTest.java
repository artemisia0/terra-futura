package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


// mocks
class FakeResources implements InterfaceGameResources {
    public int costPaid = 0;
    public int available = 99;

    @Override
    public boolean canAfford(Action action) {
        // ACTIVATE costs 1, BUILD costs 2, others free
        int cost = 0;
        switch (action) {
            case ACTIVATE: cost = 1; break;
            case BUILD: cost = 2; break;
            default: cost = 0; break;
        }
        return available >= cost;
    }

    @Override
    public void pay(Action action) {
        int cost = 0;
        switch (action) {
            case ACTIVATE: cost = 1; break;
            case BUILD: cost = 2; break;
            default: cost = 0; break;
        }
        available -= cost;
        costPaid += cost;
    }
}

class FakeActivateGrid implements InterfaceActivateGrid {
    public List<Integer> activations = new ArrayList<>();

    @Override
    public void setActivationPattern(java.util.Collection<java.util.AbstractMap.SimpleEntry<Integer,Integer>> pattern) {
        activations.add(pattern.size());
    }
}

public class ProcessActionTest {
    private FakeResources resources;
    private FakeActivateGrid grid;
    private ActivationPattern activationPattern;
    private List<Action> sequence;
    private ProcessAction process;

    @Before
    public void setUp() {
        resources = new FakeResources();
        grid = new FakeActivateGrid();
        ArrayList<java.util.AbstractMap.SimpleEntry<Integer,Integer>> pattern = new ArrayList<>();
        pattern.add(new java.util.AbstractMap.SimpleEntry<>(0,0));
        pattern.add(new java.util.AbstractMap.SimpleEntry<>(1,1));
        activationPattern = new ActivationPattern(grid, pattern);
        sequence = new ArrayList<>();
        sequence.add(Action.BUILD);
        sequence.add(Action.ACTIVATE);
        process = new ProcessAction(
                resources,
                grid,
                activationPattern,
                sequence
        );
    }

    @Test
    public void testInitialState() {
        String state = process.state();
        JSONObject json = new JSONObject(state);

        assertFalse(json.getBoolean("completed"));
        assertTrue(json.has("requiredSequence"));
        assertTrue(json.has("assistancet")); // Provided inside the state()
    }

    @Test
    public void testWrongFirstStepRejected() {
        assertFalse(process.perform(Action.ACTIVATE)); // must begin with BUILD
        assertFalse(process.isCompleted());
        assertEquals(99, resources.available);
    }

    @Test
    public void testCorrectFirstStep() {
        assertTrue(process.perform(Action.BUILD));
        assertFalse(process.isCompleted());
        assertEquals(97, resources.available); // BUILD cost = 2
    }

    @Test
    public void testFullSequenceCompletes() {
        assertTrue(process.perform(Action.BUILD));
        assertTrue(process.perform(Action.ACTIVATE));
        assertTrue(process.isCompleted());
        assertEquals(96, resources.available);
        assertEquals(1, grid.activations.size());
        assertEquals(Integer.valueOf(2), grid.activations.get(0));
    }

    @Test
    public void testNoActionsAfterCompletion() {
        assertTrue(process.perform(Action.BUILD));
        assertTrue(process.perform(Action.ACTIVATE));
        assertTrue(process.isCompleted());

        assertFalse(process.perform(Action.ACTIVATE)); // extra call rejected
    }

    @Test
    public void testResourceFailureBlocksAction() {
        // Simulate no resources
        resources.available = 0;

        assertFalse(process.perform(Action.BUILD)); // not enough resources
        assertFalse(process.isCompleted());
    }

    @Test
    public void testJSONStateReflectsProgress() {
        process.perform(Action.BUILD);  // first of required two

        JSONObject json = new JSONObject(process.state());
        JSONObject assist = json.getJSONObject("assistance");

        // Assistance stores indices → 0 consumed means 1 step done
        assertEquals(1, assist.getInt("consumedSteps"));
        assertFalse(json.getBoolean("completed"));
    }
}
