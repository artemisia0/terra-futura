package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class ProcessAction {
    private final InterfaceGameResources resources;
    private final InterfaceActivateGrid activateGrid;
    private final List<Action> fullActionSequence;   // Sequence required for the action
    private final ProcessActionAssistance assistance; // Assistance for multi-step cases

    private final ActivationPattern activationPattern; // For ACTIVATE actions
    private boolean completed;                      

    public ProcessAction(
            InterfaceGameResources resources,
            InterfaceActivateGrid activateGrid,
            ActivationPattern activationPattern,
            List<Action> requiredSequence
    ) {
        this.resources = resources;
        this.activateGrid = activateGrid;
        this.activationPattern = activationPattern;
        this.fullActionSequence = requiredSequence;
        this.assistance = new ProcessActionAssistance(requiredSequence, resources);
        this.completed = false;
    }

    public boolean perform(Action action) {
        if (completed) {
            return false; 
        }

        boolean accepted = assistance.perform(action);

        if (!accepted) {
            return false; // Wrong step or lacking resources
        }

        applySideEffects(action);

        if (assistance.isCompleted()) {
            completed = true;
        }

        return true;
    }

    private void applySideEffects(Action a) {
        switch (a) {
            case ACTIVATE:
                activationPattern.select();
                break;
            case BUILD:
                break;
            case TRADE:
                break;
            case PRODUCE:
                break;
            default:
                break;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public String state() {
        JSONObject obj = new JSONObject();
        obj.put("completed", completed);
        obj.put("assistance", new JSONObject(assistance.state()));
        JSONArray seq = new JSONArray();
        for (Action a : fullActionSequence) {
            seq.put(a.toString());
        }
        obj.put("requiredSequence", seq);
        obj.put("activationPattern", new JSONObject(activationPattern.state()));
        return obj.toString();
    }
}
