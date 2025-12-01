package sk.uniba.fmph.dcs.terra_futura;

import java.util.List;
import java.util.Optional;

public class Game implements TerraFuturaInterface {

    public GameState state;

    public Player[] players;

    int onTurn;

    int startingPlayer;

    int turnNumber;

    private final Pile[] piles = new Pile[2];
    private final MoveCard moveCard;
    private final GameObserver gameObserver;
    private final ProcessAction processAction;
    private final ProcessActionAssistance processActionAssistance;
    private final SelectReward selectReward;

    public Game(Player[] players,
                Pile pile1,
                Pile pile2,
                MoveCard moveCard,
                GameObserver gameObserver,
                ProcessAction processAction,
                ProcessActionAssistance processActionAssistance,
                SelectReward selectReward) {

        this.players = players;
        piles[0] = pile1;
        piles[1] = pile2;
        this.moveCard = moveCard;
        this.gameObserver = gameObserver;
        this.processAction = processAction;
        this.processActionAssistance = processActionAssistance;
        this.selectReward = selectReward;

        this.state = GameState.TakeCardNoCardDiscarded;
        this.startingPlayer = 0;
        this.onTurn = 0;
        this.turnNumber = 1;
    }


    private Player getPlayerById(int playerId) {
        for (Player p : players) {
            if (p.id == playerId) {
                return p;
            }
        }
        throw new IllegalArgumentException("Wrong PlayerID");
    }

    private boolean isPlayerOnTurn(int playerId) {
        return players[onTurn].id == playerId;
    }

    private void ensurePlayerOnTurn(int playerId) {
        if (!isPlayerOnTurn(playerId)) {
            throw new IllegalStateException("Wrong player");
        }
    }

    private void ensureState(GameState... allowed) {
        for (GameState s : allowed) {
            if (s == this.state) {
                return;
            }
        }
        throw new IllegalStateException("Wrong state");
    }

    private boolean hasSelectedActivationPattern(Player player) {
        for (ActivationPattern ap : player.activationPatterns) {
            if (ap != null && ap.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private boolean allPlayersHaveSelectedPattern() {
        for (Player p : players) {
            if (!hasSelectedActivationPattern(p)) {
                return false;
            }
        }
        return true;
    }


    private boolean hasSelectedScoring(Player player) {
        for (ScoringMethod sm : player.scoringMethods) {
            if (sm != null
                    && sm.calculatedTotal != null
                    && sm.calculatedTotal.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private boolean allPlayersHaveSelectedScoring() {
        for (Player p : players) {
            if (!hasSelectedScoring(p)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean takeCard(int playerId, CardSource source, GridPosition destination) {
        ensurePlayerOnTurn(playerId);
        ensureState(GameState.TakeCardNoCardDiscarded, GameState.TakeCardCardDiscarded);

        Player player = getPlayerById(playerId);
        Grid grid = player.grid;

        Pile pile = piles[source.deck.getIndex()];

        Optional<Card> cardOpt = pile.getCard(source.index);
        if (cardOpt.isEmpty()) {
            return false;
        }
        Card card = cardOpt.get();

        if (!grid.canPutCard(destination)) {
            return false;
        }

        grid.putCard(destination, card);
        pile.takeCard(source.index);

        this.state = GameState.ActivateCard;
        return true;
    }

    @Override
    public boolean discardLastCardFromDeck(int playerId, Deck deck) {
        ensurePlayerOnTurn(playerId);
        ensureState(GameState.TakeCardNoCardDiscarded);
        piles[deck.getIndex()].removeLastCard();
        state = GameState.TakeCardCardDiscarded;
        return true;
    }

    @Override
    public void activateCard(
            int playerId,
            GridPosition card,
            List<Pair<Resource, GridPosition>> inputs,
            List<Pair<Resource, GridPosition>> outputs,
            List<GridPosition> pollution,
            Optional<Integer> otherPlayerId,
            Optional<GridPosition> otherCard
    ) {
        ensureState(GameState.ActivateCard);
        ensurePlayerOnTurn(playerId);
        Player player = getPlayerById(playerId);
        Grid grid = player.grid;

        if (!grid.canBeActivated(card)) {
            return;
        }

        Optional<Card> cardOpt = grid.getCard(card);
        if (cardOpt.isEmpty()) {
            return;
        }

        Card cardEnt = cardOpt.get();

        boolean hasAssistance = otherPlayerId.isPresent() && otherCard.isPresent();
        boolean success = false;

        if (!hasAssistance) {
            success = processAction.activateCard(
                    cardEnt,
                    grid,
                    inputs,
                    outputs,
                    pollution
            );

            if (success) {
                grid.setActivated(card);
            }
        } else {
            int assistingId = otherPlayerId.get();
            Player assistingPlayer = getPlayerById(assistingId);
            Grid assistingGrid = assistingPlayer.grid;

            Optional<Card> assistingCardOpt = assistingGrid.getCard(otherCard.get());
            if (assistingCardOpt.isEmpty()) {
                return;
            }
            Card assistingCard = assistingCardOpt.get();

            success = processActionAssistance.activateCard(
                    cardEnt,
                    grid,
                    assistingId,
                    assistingCard,
                    inputs,
                    outputs,
                    pollution
            );

            if (success) {
                grid.setActivated(card);
                this.state = GameState.SelectReward;
            }
        }

    }

    @Override
    public void selectReward(int playerId, Resource resource) {
        ensurePlayerOnTurn(playerId);
        ensureState(GameState.SelectReward);
        if (!selectReward.canSelectReward(resource)) {
            return;
        }
        selectReward.selectReward(resource);

        this.state = GameState.ActivateCard;
    }

    @Override
    public boolean turnFinished(int playerId) {
        ensurePlayerOnTurn(playerId);
        ensureState(GameState.ActivateCard);

        Player player = getPlayerById(playerId);
        player.grid.endTurn();

        if(turnNumber < 10){
            if(onTurn < players.length - 1){
                onTurn++;
            }
            else{
                turnNumber++;
                onTurn = 0;
            }
            if(turnNumber < 10){
                state = GameState.TakeCardNoCardDiscarded;
            }
            else{
                state = GameState.SelectActivationPattern;
            }
            return true;
        }
        else{
            if (!allPlayersHaveSelectedPattern()) {
                onTurn = (onTurn + 1) % players.length;
                state = GameState.SelectActivationPattern;
                return true;
            } else {
                state = GameState.SelectScoringMethod;
                return true;
            }
        }
    }

    @Override
    public boolean selectActivationPattern(int playerId, int card) {
        ensurePlayerOnTurn(playerId);
        ensureState(GameState.SelectActivationPattern);

        Player player = getPlayerById(playerId);

        if (card < 0 || card >= player.activationPatterns.length) {
            return false;
        }

        ActivationPattern pattern = player.activationPatterns[card];

        pattern.select();

        this.state = GameState.ActivateCard;
        return true;
    }

    @Override
    public boolean selectScoring(int playerId, int card) {
        ensurePlayerOnTurn(playerId);
        ensureState(GameState.SelectScoringMethod);

        Player player = getPlayerById(playerId);

        if (card < 0 || card >= player.scoringMethods.length) {
            return false;
        }

        ScoringMethod method = player.scoringMethods[card];

        method.selectThisMethodAndCalculate();

        if (allPlayersHaveSelectedScoring()) {
            this.state = GameState.Finish;
        } else {
            this.onTurn = (onTurn + 1) % players.length;
            this.state = GameState.SelectScoringMethod;
        }

        return true;
    }
}

