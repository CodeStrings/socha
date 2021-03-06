package sc.plugin2018;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.framework.plugins.SimplePlayer;
import sc.framework.plugins.protocol.MoveRequest;
import sc.networking.clients.IControllableGame;
import sc.networking.clients.ILobbyClientListener;
import sc.networking.clients.LobbyClient;
import sc.plugin2018.util.Configuration;
import sc.protocol.responses.ProtocolErrorMessage;
import sc.protocol.responses.PrepareGameProtocolMessage;
import sc.shared.GameResult;
import sc.shared.PlayerColor;
import sc.shared.WelcomeMessage;

/**
 * Abstrakter Client nach Vorschrift des SDKs. Beinhaltet einen LobbyClient, der
 * den tatsächlichen Client darstellt.

 */
public abstract class AbstractClient implements ILobbyClientListener {
  private static final Logger logger = LoggerFactory
      .getLogger(AbstractClient.class);
	// The handler reacts to messages from the server received by the lobby
	// client
	protected IGameHandler handler;

	// The lobby client, that connects to the room
	private LobbyClient client;

	private String gameType;

	// If the client made an error (rule violation), store reason here
	private String error;

	// current id to identify the client instance internal
	private EPlayerId id;
	// the current room in which the player is
	private String roomId;
	// the current host
	private String host;
	// the current port
	private int port;
	// current figurecolor to identify which client belongs to which player
	private PlayerColor color;

	public AbstractClient(String host, int port, EPlayerId id) throws IOException {
		this.gameType = GamePlugin.PLUGIN_UUID;
		this.client = new LobbyClient(Configuration.getXStream(), Configuration.getClassesToRegister(), host, port);
		this.client.addListener(this);
		this.client.start();
		this.id = id;
		this.port = port;
		this.host = host;
		this.error = null;
	}

	// wenn es nur einen client gibt
	public AbstractClient(String host, int port) throws IOException {
		this(host, port, EPlayerId.PLAYER_ONE);
	}

	public void setHandler(IGameHandler handler) {
		this.handler = handler;
	}

	/**
	 * Tell this client to observe the game given by the preparation handler
	 *
	 * @param handle Handle
	 * @return controllable game
	 */
	public IControllableGame observeGame(PrepareGameProtocolMessage handle) {
		return this.client.observe(handle);
	}

	/**
	 * Called when a new message is sent to the room, e.g. move requests
	 */
	@Override
	public void onRoomMessage(String roomId, Object data) {
		if (data instanceof MoveRequest) {
			this.handler.onRequestAction();
		} else if (data instanceof WelcomeMessage) {
			WelcomeMessage welc = (WelcomeMessage) data;
			this.color = welc.getPlayerColor();
		}
		this.roomId = roomId;
	}

	/**
	 * sends the <code>move</code> to the server
	 *
	 * @param move
	 *            the move you want to do
	 */
	public void sendMove(Move move) {
		this.client.sendMessageToRoom(this.roomId, move);
	}

	/**
	 * Called, when an error is sent to the room
	 */
	@Override
	public void onError(String roomId, ProtocolErrorMessage response) {
	  logger.debug("onError: Client {} received error {}", this, response.getMessage());
		this.error = response.getMessage();
	}

	/**
	 * Called when game state has been received Happens, after a client made a
	 * move.
	 */
	@Override
	public void onNewState(String roomId, Object state) {

		GameState gameState = (GameState) state;
	  logger.debug("{} got new state {}", this, gameState);

		if (this.id != EPlayerId.OBSERVER) {
			this.handler.onUpdate(gameState);

			if (gameState.getCurrentPlayer().getPlayerColor() == this.color) {
				// active player is own
				this.handler.onUpdate(gameState.getCurrentPlayer(), gameState.getOtherPlayer());
			} else {
				// active player is the enemy
				this.handler.onUpdate(gameState.getOtherPlayer(), gameState.getCurrentPlayer());
			}
		}
	}

	public void joinAnyGame() {
		this.client.joinRoomRequest(this.gameType);
	}

	@Override
	public void onGameJoined(String roomId) {

	}

	@Override
	public void onGameLeft(String roomId) {
    logger.info("{} got game left {}", this, roomId);

    this.client.stop();
	}

	public void joinPreparedGame(String reservation) {
		this.client.joinPreparedGame(reservation);
	}

	@Override
	public void onGamePrepared(PrepareGameProtocolMessage response) {
		// not needed
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public void onGameOver(String roomId, GameResult data) {
		logger.debug("{} onGameOver got game result {}", this, data);
		if (this.handler != null) {
			this.handler.gameEnded(data, this.color, this.error);
		}
	}

	@Override
	public void onGamePaused(String roomId, SimplePlayer nextPlayer) {
		// not needed
	}

	public String getError() {
		return this.error;
	}

	public PlayerColor getColor() {
		return this.color;
	}

}
