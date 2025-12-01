package sk.uniba.fmph.dcs.terra_futura;

import java.util.Optional;
import org.json.JSONObject;

public class MoveCard {
    private final Pile pile;
    private final Grid grid;

    public MoveCard(Pile pile, Grid grid) {
        this.pile = pile;
        this.grid = grid;
    }

    public boolean moveFromDisplay(int index, GridPosition pos) {
        if (index < 0 || index >= 4) {
            return false;
        }

        if (grid.getCard(pos).isPresent() || !grid.canPutCard(pos)) {
            return false;
        }

        Optional<Card> oc = pile.getCard(index);
        if (oc.isEmpty()) {
            return false;
        }

        Card card = oc.get();
        pile.takeCard(index);
        grid.putCard(pos, card);

        return true;
    }

    public boolean moveFromDeck(GridPosition pos) {
        if (grid.getCard(pos).isPresent()) {
            return false;
        }

        if (pile.discardPileSize() == 0) {
            return false;
        }

        if (!grid.canPutCard(pos)) {
            return false;
        }

        Card card = pile.takeFromDeck();
        grid.putCard(pos, card);

        return true;
    }

    public String state() {
        JSONObject result = new JSONObject();

        JSONObject pileState = new JSONObject(pile.state());
        JSONObject gridState = new JSONObject(grid.state());

        result.put("pile", pileState);
        result.put("grid", gridState);

        return result.toString();
    }
}

