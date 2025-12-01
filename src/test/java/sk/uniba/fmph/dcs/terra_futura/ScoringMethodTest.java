package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONObject;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class ScoringMethodTest {

    @Test
    public void simpleCombinationTwoSets() {
        final List<Resource> pattern =
                List.of(Resource.Green, Resource.Green, Resource.Car);

        final Points perCombo = new Points(5);

        final Supplier<Map<Resource, Integer>> provider =
                () -> Map.of(
                        Resource.Green, 5,
                        Resource.Car, 2
                );

        final ScoringMethod method =
                new ScoringMethod(pattern, perCombo, provider);

        method.selectThisMethodAndCalculate();

        assertTrue(method.calculatedTotal.isPresent());
        assertEquals(10, method.calculatedTotal.get().value());
    }

    @Test
    public void notEnoughResourcesGivesZero() {
        final List<Resource> pattern =
                List.of(Resource.Green, Resource.Green, Resource.Car);

        final Points perCombo = new Points(5);

        final Supplier<Map<Resource, Integer>> provider =
                () -> Map.of(
                        Resource.Green, 1,
                        Resource.Car, 10
                );

        final ScoringMethod method =
                new ScoringMethod(pattern, perCombo, provider);

        method.selectThisMethodAndCalculate();

        assertTrue(method.calculatedTotal.isPresent());
        assertEquals(0, method.calculatedTotal.get().value());
    }

    @Test
    public void emptyPatternAlwaysZero() {
        final List<Resource> pattern = List.of();

        final Points perCombo = new Points(7);

        final Supplier<Map<Resource, Integer>> provider =
                () -> Map.of(
                        Resource.Green, 100,
                        Resource.Car, 50
                );

        final ScoringMethod method =
                new ScoringMethod(pattern, perCombo, provider);

        method.selectThisMethodAndCalculate();

        assertTrue(method.calculatedTotal.isPresent());
        assertEquals(0, method.calculatedTotal.get().value());
    }

    @Test
    public void secondSelectIsIdempotent() {
        final List<Resource> pattern =
                List.of(Resource.Green, Resource.Green, Resource.Car);

        final Points perCombo = new Points(5);

        final Supplier<Map<Resource, Integer>> provider =
                () -> Map.of(
                        Resource.Green, 5,
                        Resource.Car, 2
                );

        final ScoringMethod method =
                new ScoringMethod(pattern, perCombo, provider);

        method.selectThisMethodAndCalculate();
        final int first = method.calculatedTotal.get().value();

        method.selectThisMethodAndCalculate();
        final int second = method.calculatedTotal.get().value();

        assertEquals(first, second);
    }

    @Test
    public void stateReflectsSelection() {
        final List<Resource> pattern =
                List.of(Resource.Green, Resource.Green, Resource.Car);

        final Points perCombo = new Points(5);

        final Supplier<Map<Resource, Integer>> provider =
                () -> Map.of(
                        Resource.Green, 5,
                        Resource.Car, 2
                );

        final ScoringMethod method =
                new ScoringMethod(pattern, perCombo, provider);

        final JSONObject before = new JSONObject(method.state());
        assertFalse(before.getBoolean("selected"));
        assertEquals(0, before.getInt("total"));

        method.selectThisMethodAndCalculate();

        final JSONObject after = new JSONObject(method.state());
        assertTrue(after.getBoolean("selected"));
        assertEquals(10, after.getInt("total"));
        assertEquals(5, after.getInt("pointsPerCombination"));
    }
}
