package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    @Test
    void testPutResourcesStoresCorrectCounts() {
        Card card = new Card();
        card.putResources(List.of(Resource.Green, Resource.Green, Resource.Red));

        assertTrue(card.canGetResources(List.of(Resource.Green, Resource.Red)));
        assertFalse(card.canGetResources(List.of(Resource.Yellow)));
        assertFalse(card.canGetResources(List.of(Resource.Green, Resource.Green, Resource.Green)));
    }

    @Test
    void testPutResourcesNullNotAllowed() {
        Card card = new Card();
        assertThrows(IllegalStateException.class, () -> card.putResources(null));
    }

    @Test
    void testGetResourcesRemovesCorrectly() {
        Card card = new Card();
        card.putResources(List.of(Resource.Green, Resource.Green, Resource.Red));

        assertTrue(card.canGetResources(List.of(Resource.Green, Resource.Red)));

        card.getResources(List.of(Resource.Green, Resource.Red));

        assertTrue(card.canGetResources(List.of(Resource.Green)));
        assertFalse(card.canGetResources(List.of(Resource.Red)));
    }

    @Test
    void testGetResourcesFailsWhenNotEnough() {
        Card card = new Card();
        card.putResources(List.of(Resource.Yellow));

        assertThrows(IllegalStateException.class,
                () -> card.getResources(List.of(Resource.Yellow, Resource.Yellow)));
    }

    @Test
    void testCanAddPollution() {
        Card card = new Card(2);
        assertTrue(card.canAddPollution(1));
        card.addPollution(1);
        assertTrue(card.canAddPollution(1));
        assertFalse(card.canAddPollution(2));
    }

    @Test
    void testAddPollutionFailsIfExceedsCapacity() {
        Card card = new Card(1);
        assertThrows(IllegalStateException.class, () -> card.addPollution(2));
    }

    @Test
    void testUpperEffectCheck() {
        Card card = new Card(2, new TransformationFixed(), null);

        assertTrue(card.check(List.of(), List.of(), 1));
    }

    @Test
    void testUpperEffectCheckFailsWhenNoEffect() {
        Card card = new Card();
        assertFalse(card.check(List.of(), List.of(), 0));
    }

    @Test
    void testUpperEffectCheckFailsDueToPollutionLimit() {
        Card card = new Card(1, new TransformationFixed(), null);
        assertFalse(card.check(List.of(), List.of(), 2));
    }

    @Test
    void testLowerEffectCheck() {
        Card card = new Card(2, null, new TransformationFixed());
        assertTrue(card.checkLower(List.of(), List.of(), 1));
    }

    @Test
    void testLowerEffectCheckFailsWhenNoEffect() {
        Card card = new Card();
        assertFalse(card.checkLower(List.of(), List.of(), 0));
    }

    @Test
    void testLowerEffectCheckFailsDueToPollutionLimit() {
        Card card = new Card(0, null, new TransformationFixed());
        assertFalse(card.checkLower(List.of(), List.of(), 1));
    }

    @Test
    void testHasAssistanceFalse() {
        Card card = new Card();
        assertFalse(card.hasAssistance());
    }

    @Test
    void testHasAssistanceTrueWhenUpperProvidesIt() {
        Card card = new Card(0, new ArbitraryBasic(), null);
        assertTrue(card.hasAssistance());
    }

    @Test
    void testHasAssistanceTrueWhenLowerProvidesIt() {
        Card card = new Card(0, null, new ArbitraryBasic());
        assertTrue(card.hasAssistance());
    }

    @Test
    void testStateJsonCorrect() {
        Card card = new Card(3);
        card.putResources(List.of(Resource.Red, Resource.Red, Resource.Green));
        card.addPollution(2);

        JSONObject json = new JSONObject(card.state());

        assertEquals(3, json.getInt("pollution_spaces"));
        assertEquals(2, json.getInt("pollution_used"));
        assertEquals(2, json.getJSONObject("resources").getInt("Red"));
        assertEquals(1, json.getJSONObject("resources").getInt("Green"));
        assertFalse(json.getBoolean("has_upper"));
        assertFalse(json.getBoolean("has_lower"));
        assertFalse(json.getBoolean("has_assistance"));
    }
}

