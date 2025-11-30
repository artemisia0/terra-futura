package sk.uniba.fmph.dcs.terra_futura;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * One way to count bonus points for a scoring card.
 * This class does not know where resources come from.
 * They are obtained from the grid via resourceProvider.
 **/
public final class ScoringMethod {

    private final List<Resource> resources;
    private final Points pointsPerCombination;
    private final Supplier<Map<Resource, Integer>> resourceProvider;

    // null until chosen
    private Points calculatedTotal;

    public ScoringMethod(final List<Resource> resources,
                         final Points pointsPerCombination,
                         final Supplier<Map<Resource, Integer>> resourceProvider) {
        this.resources = List.copyOf(resources);
        this.pointsPerCombination = pointsPerCombination;
        this.resourceProvider = resourceProvider;
    }

    public void selectThisMethodAndCalculate() {
        Map<Resource, Integer> available =
                new EnumMap<>(resourceProvider.get());

        // Count how many full multisets "resources" we can form.
        int combinations = Integer.MAX_VALUE;
        for (Resource r : Resource.values()) {
            long inPattern = resources.stream().filter(x -> x == r).count();
            if (inPattern == 0) {
                continue;
            }
            int availableCount = available.getOrDefault(r, 0);
            int fromThis = availableCount / (int) inPattern;
            combinations = Math.min(combinations, fromThis);
        }
        if (combinations == Integer.MAX_VALUE) {
            combinations = 0;
        }
        calculatedTotal = new Points(combinations * pointsPerCombination.value());
    }

    public Points pointsPerCombination() {
        return pointsPerCombination;
    }

    public Points calculatedTotal() {
        return calculatedTotal;
    }

    public String state() {
        JSONObject json = new JSONObject();
        JSONArray pattern = new JSONArray();
        for (Resource r : resources) {
            pattern.put(r.name());
        }
        json.put("resources", pattern);
        json.put("pointsPerCombination", pointsPerCombination.value());
        json.put("selected", calculatedTotal != null);
        json.put("total",
                calculatedTotal == null ? 0 : calculatedTotal.value());
        return json.toString();
    }
}
