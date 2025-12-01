package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Pile {

    private final List<Card> display = new ArrayList<>();
    private final Deque<Card> deck = new ArrayDeque<>();

    public Optional<Card> getCard(int index) {
        if (index < 0 || index >= display.size()) return Optional.empty();
        return Optional.of(display.get(index));
    }

    public void takeCard(int index) {
        display.remove(index);
    }

    public int discardPileSize() {
        return deck.size();
    }

    public Card takeFromDeck() {
        return deck.pop();
    }


    public void removeLastCard() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String state() {
        JSONObject result = new JSONObject();

        result.put("visible_count", display.size());
        result.put("hidden_count", deck.size());

        JSONArray visibleCardsArray = new JSONArray();
        for (Card card : display) {
            visibleCardsArray.put(card == null ? JSONObject.NULL : card.state());
        }
        result.put("visible_cards", visibleCardsArray);
        return result.toString();
    }
}

