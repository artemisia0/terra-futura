package sk.uniba.fmph.dcs.terra_futura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ProcessActionAssistance {
    public ProcessActionAssistance() {
    }

    public boolean activateCard(Card card,
                                Grid grid,
                                int assistingPlayer,
                                Card assistingCard,
                                List<Pair<Resource, GridPosition>> inputs,
                                List<Pair<Resource, GridPosition>> outputs,
                                List<GridPosition> pollution) {
        if (inputs == null) inputs = List.of();
        if (outputs == null) outputs = List.of();
        if (pollution == null) pollution = List.of();

        // Basic sanity checks
        if (card == null || grid == null || assistingCard == null) {
            return false;
        }

        if (!assistingCard.hasAssistance()) {
            return false;
        }

        Map<Card, List<Resource>> inputsByCard = new HashMap<>();
        Map<Card, List<Resource>> outputsByCard = new HashMap<>();
        Map<Card, Integer> pollutionByCard = new HashMap<>();

        // Helper to record a resource required from a particular card
        for (Pair<Resource, GridPosition> p : inputs) {
            Resource res = p.getFirst();
            GridPosition pos = p.getSecond();
            Optional<Card> optSource = grid.getCard(pos);
            if (optSource.isEmpty()) return false; // invalid input position
            Card src = optSource.get();
            inputsByCard.computeIfAbsent(src, k -> new ArrayList<>()).add(res);
        }

        // Helper to record a resource to be put onto a particular card
        for (Pair<Resource, GridPosition> p : outputs) {
            Resource res = p.getFirst();
            GridPosition pos = p.getSecond();
            Optional<Card> optDest = grid.getCard(pos);
            if (optDest.isEmpty()) return false; // invalid output position
            Card dest = optDest.get();
            outputsByCard.computeIfAbsent(dest, k -> new ArrayList<>()).add(res);
        }

        // Pollution aggregated per-card: each GridPosition gets one pollution token
        for (GridPosition polPos : pollution) {
            Optional<Card> opt = grid.getCard(polPos);
            if (opt.isEmpty()) return false; // invalid pollution target
            Card polCard = opt.get();
            pollutionByCard.merge(polCard, 1, Integer::sum);
        }

        boolean cardFound = false;
        // check inputs
        for (Card c : inputsByCard.keySet()) if (c == card) cardFound = true;
        // check outputs
        for (Card c : outputsByCard.keySet()) if (c == card) cardFound = true;
        // check pollution targets
        for (Card c : pollutionByCard.keySet()) if (c == card) cardFound = true;
        if (!cardFound) {
            outer:
            for (int rx = -2; rx <= 2; rx++) {
                for (int ry = -2; ry <= 2; ry++) {
                    GridPosition pos = new GridPosition(rx, ry);
                    Optional<Card> opt = grid.getCard(pos);
                    if (opt.isPresent() && opt.get() == card) {
                        cardFound = true;
                        break outer;
                    }
                }
            }
        }
        if (!cardFound) {
            return false;
        }

        for (Map.Entry<Card, List<Resource>> entry : inputsByCard.entrySet()) {
            Card src = entry.getKey();
            List<Resource> requiredResources = List.copyOf(entry.getValue());
            if (!src.canGetResources(requiredResources)) {
                return false;
            }
        }

        for (Map.Entry<Card, List<Resource>> entry : outputsByCard.entrySet()) {
            Card dest = entry.getKey();
            List<Resource> produceResources = List.copyOf(entry.getValue());
            if (!dest.canPutResources(produceResources)) {
                return false;
            }
        }

        Map<Card, List<Resource>> unionInputs = new HashMap<>(inputsByCard);
        Map<Card, List<Resource>> unionOutputs = new HashMap<>(outputsByCard);

        List<Card> affectedCards = new ArrayList<>();
        affectedCards.addAll(unionInputs.keySet());
        for (Card c : unionOutputs.keySet()) if (!affectedCards.contains(c)) affectedCards.add(c);
        for (Card c : pollutionByCard.keySet()) if (!affectedCards.contains(c)) affectedCards.add(c);

        for (Card affected : affectedCards) {
            List<Resource> in = unionInputs.getOrDefault(affected, List.of());
            List<Resource> out = unionOutputs.getOrDefault(affected, List.of());
            int polCount = pollutionByCard.getOrDefault(affected, 0);

            boolean ok = false;
            try {
                ok = affected.check(in, out, polCount);
            } catch (Exception ex) {
                // fall through to checkLower attempt
                ok = false;
            }
            if (!ok) {
                try {
                    ok = affected.checkLower(in, out, polCount);
                } catch (Exception ex2) {
                    ok = false;
                }
            }
            if (!ok) {
                return false;
            }
        }

        final List<Runnable> rollback = new ArrayList<>();

        try {
            for (Map.Entry<Card, List<Resource>> e : inputsByCard.entrySet()) {
                Card src = e.getKey();
                List<Resource> resourcesToTake = List.copyOf(e.getValue());
                if (!resourcesToTake.isEmpty()) {
                    src.getResources(resourcesToTake);
                    rollback.add(() -> {
                        try {
                            src.putResources(resourcesToTake);
                        } catch (Exception ignored) { }
                    });
                }
            }
            for (Map.Entry<Card, List<Resource>> e : outputsByCard.entrySet()) {
                Card dest = e.getKey();
                List<Resource> resourcesToPut = List.copyOf(e.getValue());
                if (!resourcesToPut.isEmpty()) {
                    dest.putResources(resourcesToPut);
                    rollback.add(0, () -> {
                        try {
                            dest.getResources(resourcesToPut);
                        } catch (Exception ignored) { }
                    });
                }
            }

            if (!pollutionByCard.isEmpty()) {
                for (Map.Entry<Card, Integer> e : pollutionByCard.entrySet()) {
                    Card tgt = e.getKey();
                    int count = e.getValue();
                    List<Resource> pollTokens = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) pollTokens.add(Resource.Polution);
                    tgt.putResources(pollTokens);
                    // rollback: remove same pollTokens
                    rollback.add(0, () -> {
                        try {
                            tgt.getResources(pollTokens);
                        } catch (Exception ignored) { }
                    });
                }
            }

            return true;

        } catch (RuntimeException ex) {
            for (int i = 0; i < rollback.size(); i++) {
                try {
                    rollback.get(i).run();
                } catch (Exception ignored) { }
            }
            return false;
        } catch (Exception ex) {
            for (int i = 0; i < rollback.size(); i++) {
                try {
                    rollback.get(i).run();
                } catch (Exception ignored) { }
            }
            return false;
        }
    }
}
