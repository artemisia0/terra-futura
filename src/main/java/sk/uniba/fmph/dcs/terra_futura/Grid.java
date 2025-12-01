package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Grid {

    private final Card[][] grid;
    private List<GridPosition> activationPattern;

    public Grid() {
        activationPattern = new ArrayList<>();
        grid = new Card[3][3];
    }

    public Optional<Card> getCard(GridPosition coordinate) {
        if (!isPossibleIndex(coordinate)) return Optional.empty();
        return Optional.ofNullable(grid[coordinate.x][coordinate.y]);
    }

    public boolean canPutCard(GridPosition coordinate) {
        if (!isPossibleIndex(coordinate)) return false;

        if (grid[coordinate.x][coordinate.y] != null) return false;

        if (getCurrentRow(coordinate.y).size() >= 3) return false;
        if (getCurrentColumn(coordinate.x).size() >= 3) return false;

        return true;
    }

    public void putCard(GridPosition coordinate, Card card) {
        if (!isPossibleIndex(coordinate)) {
            throw new IllegalArgumentException("Out of bounds");
        }
        grid[coordinate.x][coordinate.y] = card;
    }

    public List<Card> getCurrentRow(int y) {
        List<Card> result = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            if (grid[x][y] != null) result.add(grid[x][y]);
        }
        return result;
    }

    public List<Card> getCurrentColumn(int x) {
        List<Card> result = new ArrayList<>();
        for (int y = 0; y < 3; y++) {
            if (grid[x][y] != null) result.add(grid[x][y]);
        }
        return result;
    }

    private boolean isPossibleIndex(GridPosition pos) {
        return pos.x >= 0 && pos.x < 3 && pos.y >= 0 && pos.y < 3;
    }

    public void setActivationPattern(List<GridPosition> pattern) {
        this.activationPattern = (pattern == null) ?
                Collections.emptyList() : new ArrayList<>(pattern);
    }

    public boolean canBeActivated(GridPosition coordinate) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setActivated(GridPosition coordinate) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void endTurn() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String state() {
        JSONObject cardsObject = new JSONObject();

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                GridPosition coordinate = new GridPosition(x, y);
                Card card = grid[x][y];

                cardsObject.put(
                        coordinate.toString(),
                        card == null ? JSONObject.NULL : card.state()
                );
            }
        }

        JSONArray patternArray = new JSONArray();
        for (GridPosition gp : activationPattern) {
            patternArray.put(gp == null ? JSONObject.NULL : gp.toString());
        }

        JSONObject result = new JSONObject();
        result.put("cards", cardsObject);
        result.put("card_count", cardsCount());
        result.put("activation_pattern", patternArray);

        return result.toString();
    }

    private int cardsCount() {
        int count = 0;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (grid[x][y] != null) count++;
            }
        }
        return count;
    }
}

