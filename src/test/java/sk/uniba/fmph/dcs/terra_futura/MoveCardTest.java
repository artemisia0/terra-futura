package sk.uniba.fmph.dcs.terra_futura;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MoveCardTest {
    @Test
    void testMoveFromDisplaySuccess() {
        Grid grid = new Grid();

        List<Card> display = List.of(new Card(), new Card(), new Card(), new Card());
        List<Card> deck = List.of(new Card());
        Pile pile = new Pile(new ArrayList<>(display), new ArrayList<>(deck));

        MoveCard mc = new MoveCard(pile, grid);

        GridPosition pos = new GridPosition(0, 0);

        boolean result = mc.moveFromDisplay(0, pos);

        assertTrue(result);
        assertTrue(grid.getCard(pos).isPresent());
        assertEquals(3, pile.getDisplayCards().size());
    }

    @Test
    void testMoveFromDisplayInvalidIndex() {
        Grid grid = new Grid();

        List<Card> display = List.of(new Card(), new Card(), new Card(), new Card());
        List<Card> deck = List.of();
        Pile pile = new Pile(new ArrayList<>(display), new ArrayList<>(deck));

        MoveCard mc = new MoveCard(pile, grid);

        boolean result = mc.moveFromDisplay(10, new GridPosition(1, 1));

        assertFalse(result);
        assertEquals(4, pile.getDisplayCards().size());
        assertTrue(grid.getCard(new GridPosition(1, 1)).isEmpty());
    }

    @Test
    void testMoveFromDisplayCellOccupied() {
        Grid grid = new Grid();

        List<Card> display = List.of(new Card(), new Card(), new Card(), new Card());
        List<Card> deck = List.of();
        Pile pile = new Pile(new ArrayList<>(display), new ArrayList<>(deck));

        MoveCard mc = new MoveCard(pile, grid);
        GridPosition position = new GridPosition(0, 0);
        grid.putCard(position, new Card());

        boolean result = mc.moveFromDisplay(0, position);

        assertFalse(result);
        assertEquals(4, pile.getDisplayCards().size());
    }

    @Test
    void testMoveFromDisplayRowLimit() {
        Grid grid = new Grid();

        grid.putCard(new GridPosition(0, 0), new Card());
        grid.putCard(new GridPosition(1, 0), new Card());
        grid.putCard(new GridPosition(2, 0), new Card());

        List<Card> display = List.of(new Card(), new Card(), new Card(), new Card());
        List<Card> deck = List.of();
        Pile pile = new Pile(new ArrayList<>(display), new ArrayList<>(deck));

        MoveCard mc = new MoveCard(pile, grid);

        boolean result = mc.moveFromDisplay(0, new GridPosition(1, 0));

        assertFalse(result);
        assertEquals(4, pile.getDisplayCards().size());
    }

    @Test
    void testMoveFromDeckSuccess() {
        Grid grid = new Grid();

        List<Card> display = List.of();
        List<Card> deck = List.of(new Card());
        Pile pile = new Pile(new ArrayList<>(display), new ArrayList<>(deck));

        MoveCard mc = new MoveCard(pile, grid);

        boolean result = mc.moveFromDeck(new GridPosition(2, 2));

        assertTrue(result);
        assertTrue(grid.getCard(new GridPosition(2, 2)).isPresent());
        assertEquals(0, pile.discardPileSize());
    }

    @Test
    void testMoveFromEmptyDeck() {
        Grid grid = new Grid();

        List<Card> display = List.of();
        List<Card> deck = List.of();
        Pile pile = new Pile(new ArrayList<>(display), new ArrayList<>(deck));

        MoveCard moveCard = new MoveCard(pile, grid);
        boolean result = moveCard.moveFromDeck(new GridPosition(0, 1));

        assertFalse(result);
    }


    @Test
    void testFailureDoesNotChangePileOrGrid() {
        Grid grid = new Grid();

        grid.putCard(new GridPosition(0, 0), new Card());
        grid.putCard(new GridPosition(1, 0), new Card());
        grid.putCard(new GridPosition(2, 0), new Card());

        List<Card> display = List.of(new Card(), new Card(), new Card(), new Card());
        Pile pile = new Pile(new ArrayList<>(display), new ArrayList<>());

        MoveCard moveCard = new MoveCard(pile, grid);
        boolean result = moveCard.moveFromDisplay(0, new GridPosition(0, 0));

        assertFalse(result);
        assertEquals(4, pile.getDisplayCards().size());
        assertTrue(grid.getCard(new GridPosition(1, 1)).isEmpty());
    }
}

