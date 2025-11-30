package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONObject;
import java.util.*;

public class Card {
    private final Map<Resource, Integer> resources = new EnumMap<>(Resource.class);

    private final int pollutionSpaces;
    private int pollutionUsed = 0;

    private final Effect upperEffect;
    private final Effect lowerEffect;

    public Card(int pollutionSpacesL, Effect upperEffect, Effect lowerEffect) {
        this.pollutionSpaces = pollutionSpacesL;
        this.upperEffect = upperEffect;
        this.lowerEffect = lowerEffect;
    }

    public Card(int pollutionSpaces) {
        this(pollutionSpaces, null, null);
    }

    public Card() {
        this(0, null, null);
    }

    public boolean canGetResources(List<Resource> requiredResources) {
        Map<Resource, Integer> needed = count(requiredResources);

        for (Map.Entry<Resource,Integer> resource : needed.entrySet()) {
            if (resources.getOrDefault(resource.getKey(), 0) < resource.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void getResources(List<Resource> requiredResources) {
        if (!canGetResources(requiredResources)) {
            throw new IllegalStateException("Not enough resources on card");
        }

        for (Resource resource : requiredResources) {
            resources.put(resource, resources.get(resource) - 1);
        }
    }

    public boolean canPutResources(List<Resource> newResources) {
        return newResources != null;
    }

    public void putResources(List<Resource> newResources) {
        if (!canPutResources(newResources)) {
            throw new IllegalStateException("Invalid resources");
        }

        Map<Resource, Integer> result = new EnumMap<>(Resource.class);
        for (Resource resource : newResources) {
            result.put(resource, result.getOrDefault(resource, 0) + 1);
        }
        resources.clear();
        resources.putAll(result);
    }

    private Map<Resource, Integer> count(List<Resource> items) {
        Map<Resource, Integer> counter = new EnumMap<>(Resource.class);
        if (items == null) return counter;

        for (Resource resource : items) {
            counter.put(resource, counter.getOrDefault(resource, 0) + 1);
        }
        return counter;
    }

    public boolean canAddPollution(int pollution) {
        if (pollution < 0) return false;
        return pollutionUsed + pollution <= pollutionSpaces;
    }

    public void addPollution(int pollution) {
        if (!canAddPollution(pollution)) {
            throw new IllegalStateException("Pollution exceeds capacity");
        }
        pollutionUsed += pollution;
    }

    public boolean check(List<Resource> input, List<Resource> output, int pollution) {
        if (upperEffect == null) return false;
        return upperEffect.check(input, output, pollution) && canAddPollution(pollution);
    }

    public boolean checkLower(List<Resource> input, List<Resource> output, int pollution) {
        if (lowerEffect == null) return false;
        return lowerEffect.check(input, output, pollution) && canAddPollution(pollution);
    }

    public boolean hasAssistance() {
        return (upperEffect != null && upperEffect.hasAssistance())
                || (lowerEffect != null && lowerEffect.hasAssistance());
    }

    public String state() {
        JSONObject o = new JSONObject();

        JSONObject resJson = new JSONObject();
        for (Map.Entry<Resource,Integer> resource : resources.entrySet()) {
            resJson.put(resource.getKey().name(), resource.getValue());
        }

        o.put("resources", resJson);
        o.put("pollution_spaces", pollutionSpaces);
        o.put("pollution_used", pollutionUsed);
        o.put("has_upper", upperEffect != null);
        o.put("has_lower", lowerEffect != null);
        o.put("has_assistance", hasAssistance());

        return o.toString();
    }

    public Effect getUpperEffect() { return upperEffect; }
    public Effect getLowerEffect() { return lowerEffect; }
}

