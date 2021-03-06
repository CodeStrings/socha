package sc.plugin2013;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.api.plugins.IPlayer;
import sc.api.plugins.exceptions.GameLogicException;
import sc.api.plugins.exceptions.TooManyPlayersException;
import sc.api.plugins.host.GameLoader;
import sc.framework.plugins.ActionTimeout;
import sc.framework.plugins.RoundBasedGameInstance;
import sc.plugin2013.util.Configuration;
import sc.plugin2013.util.Constants;
import sc.plugin2013.util.InvalidMoveException;
import sc.shared.PlayerScore;
import sc.shared.ScoreCause;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Minimal game. Basis for new plugins. This class holds the game logic.
 * 
 * @author fdu
 * 
 */
@XStreamAlias(value = "cartagena:game")
public class Game extends RoundBasedGameInstance<Player> {
	private static Logger logger = LoggerFactory.getLogger(Game.class);

	@XStreamOmitField
	private List<PlayerColor> availableColors = new LinkedList<PlayerColor>();

	private GameState gameState = new GameState(false);

	public GameState getGameState() {
		return gameState;
	}

	public Player getActivePlayer() {
		return activePlayer;
	}

	/**
	 * Creates a new Game, adds Colors RED and Blue to availableColors
	 * 
	 */
	public Game() {
		availableColors.add(PlayerColor.RED);
		availableColors.add(PlayerColor.BLUE);
	}

	@Override
	protected Object getCurrentState() {
		return gameState;
	}

	/**
	 * Someone did something, check out what it was (move maybe? Then check the
	 * move)
	 */
	@Override
	protected void onRoundBasedAction(IPlayer fromPlayer, Object data)
			throws GameLogicException {

		final Player author = (Player) fromPlayer;
		final Player expectedPlayer = gameState.getCurrentPlayer();

		try {
			if (author.getPlayerColor() != expectedPlayer.getPlayerColor()) {
				throw new InvalidMoveException(author.getDisplayName()
						+ " war nicht am Zug");
			}

			if (!(data instanceof MoveContainer)) {
				throw new InvalidMoveException(author.getDisplayName()
						+ " hat kein Zug-Objekt gesendet");
			}

			final MoveContainer move = (MoveContainer) data;
			move.perform(gameState, expectedPlayer);
			gameState.prepareNextTurn(move);

			if (gameState.getTurn() == Constants.ROUND_LIMIT * 2) {
				PlayerColor winner = null;
				String winnerName = "Gleichstand nach Punkten.";
				if (gameState.getRedPlayer().getPoints() > gameState
						.getBluePlayer().getPoints()) {
					winner = PlayerColor.RED;
					winnerName = "Sieg nach Punkten.";
				} else if (gameState.getRedPlayer().getPoints() < gameState
						.getBluePlayer().getPoints()) {
					winner = PlayerColor.BLUE;
					winnerName = "Sieg nach Punkten.";
				}
				gameState.endGame(winner, "Das Rundenlimit wurde erreicht.\\n"
						+ winnerName);
			} else if (gameState.playerFinished(PlayerColor.RED)) {
				// Rot hat alle Piraten im Zielfeld
				PlayerColor winner = PlayerColor.RED;
				String winningString = "Rot hat alle Piraten im Ziel";
				gameState.endGame(winner, winningString);
			} else if (gameState.playerFinished(PlayerColor.BLUE)) {
				// Blau hat alle Piraten im Zielfeld
				PlayerColor winner = PlayerColor.BLUE;
				String winningString = "Blau hat alle Piraten im Ziel";
				gameState.endGame(winner, winningString);
			}

			next(gameState.getCurrentPlayer());
			//next();

		} catch (InvalidMoveException e) {
			author.setViolated(true);
			String err = "Ungültiger Zug von '" + author.getDisplayName()
					+ "'.\\n" + e.getMessage() + ".";
			gameState.endGame(author.getPlayerColor().opponent(), err);
			logger.error(err);
			throw new GameLogicException(err);
		}

	}

	@Override
	public IPlayer onPlayerJoined() throws TooManyPlayersException {
		if (this.players.size() >= GamePlugin.MAX_PLAYER_COUNT)
			throw new TooManyPlayersException();

		final Player player = new Player(this.availableColors.remove(0));
		this.players.add(player);
		this.gameState.addPlayer(player);

		return player;
	}

	@Override
	public void onPlayerLeft(IPlayer player) {
		if (!player.hasViolated()) {
			onPlayerLeft(player, ScoreCause.LEFT);
		} else {
			onPlayerLeft(player, ScoreCause.RULE_VIOLATION);
		}
	}

	@Override
	public void onPlayerLeft(IPlayer player, ScoreCause cause) {
		Map<IPlayer, PlayerScore> res = generateScoreMap();

		for (Entry<IPlayer, PlayerScore> entry : res.entrySet()) {
			PlayerScore score = entry.getValue();

			if (entry.getKey() == player) {
				score.setCause(cause);
				score.setValueAt(0, new BigDecimal(0));
			} else {
				score.setValueAt(0, new BigDecimal(2));
			}
		}

		if (!gameState.gameEnded()) {
			gameState.endGame(((Player) player).getPlayerColor().opponent(),
					"Der Spieler '" + player.getDisplayName()
							+ "' hat das Spiel verlassen.");
		}

		notifyOnGameOver(res);

	}

	@Override
	public boolean ready() {
		return this.players.size() == GamePlugin.MAX_PLAYER_COUNT;
	}

	@Override
	public void start() {
		for (final Player p : players) {
			p.notifyListeners(new WelcomeMessage(p.getPlayerColor()));
		}

		//super.start();
		if (this.listeners.size() == 0)
		{
			logger.warn("Couldn't find any listeners. Is this intended?");
		}
		
		if(this.gameState.getCurrentPlayerColor() != PlayerColor.RED){
			this.activePlayer = this.players.get(1);
		}else{
			this.activePlayer = this.players.get(0);
		}
		
		onActivePlayerChanged(this.activePlayer);
		notifyOnNewState(getCurrentState());
		notifyActivePlayer();
	}

	@Override
	protected void onNewTurn() {
		// never implemented in last plugins

	}

	@Override
	protected PlayerScore getScoreFor(Player p) {
		int matchPoints = 1;
		Player player = (p.getPlayerColor() == PlayerColor.RED) ? gameState
				.getRedPlayer() : gameState.getBluePlayer();
		Player opponent = (p.getPlayerColor() == PlayerColor.RED) ? gameState
				.getBluePlayer() : gameState.getRedPlayer();
		if (player.getPoints() > opponent.getPoints()) {
			matchPoints = 2;
		} else if (player.getPoints() < opponent.getPoints()) {
			matchPoints = 0;
		}

		return p.hasViolated() ? new PlayerScore(ScoreCause.RULE_VIOLATION, 0,
				player.getPoints()) : new PlayerScore(ScoreCause.REGULAR,
				matchPoints, player.getPoints());
	}

	@Override
	protected ActionTimeout getTimeoutFor(Player player) {
		return new ActionTimeout(true, 10000l, 4000l);
	}

	@Override
	protected boolean checkGameOverCondition() {
		return gameState.gameEnded();
	}

	@Override
	public void loadFromFile(String file) {
		GameLoader gl = new GameLoader(Configuration.getClassesToRegister());
		Object gameInfo = gl.loadGame(Configuration.getXStream(), file);
		if (gameInfo != null) {
			loadGameInfo(gameInfo);
		}
	}

	@Override
	public void loadGameInfo(Object gameInfo) {
		if (gameInfo instanceof GameState) {
			this.gameState = (GameState) gameInfo;
			//Initialisiere den Kartenstapel
			this.gameState.initCardStack();
			//Entferne die offen liegenden Karten und die Karten der Spieler
			for(Card c:this.gameState.getOpenCards()){
				this.gameState.getCardStack().remove(c);
			}
			for(Card c:this.gameState.getRedPlayer().getCards()){
				this.gameState.getCardStack().remove(c);
			}
			for(Card c:this.gameState.getBluePlayer().getCards()){
				this.gameState.getCardStack().remove(c);
			}
			//Zum Schluss noch einmal mischen:
			Collections.shuffle(this.gameState.getCardStack(), new SecureRandom());
			//turn richtig setzen:
			//this.turn = this.gameState.getTurn();
		}
	}

	@Override
	public List<IPlayer> getWinners() {
		if (gameState.gameEnded()) {
			List<IPlayer> winners = new LinkedList<IPlayer>();
			if (gameState.winner() != null) {
				for (Player player : players) {
					if (player.getPlayerColor() == gameState.winner()) {
						winners.add(player);
						break;
					}
				}
			}
			return winners;
		} else {
			return null;
		}
	}

}
