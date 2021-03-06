package sc.plugin2014.moves;

import java.util.LinkedList;
import java.util.List;

import sc.plugin2014.GameState;
import sc.plugin2014.entities.Player;
import sc.plugin2014.entities.PlayerColor;
import sc.plugin2014.entities.Stone;
import sc.plugin2014.exceptions.InvalidMoveException;
import sc.plugin2014.exceptions.StoneBagIsEmptyException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Allgemeiner Zug. Oberklasse von {@link ExchangeMove} und {@link LayMove}.
 * 
 * @author ffi
 * 
 */
@XStreamAlias(value = "move")
public abstract class Move implements Cloneable {

	@XStreamImplicit(itemFieldName = "hint")
	private List<DebugHint> hints;

	/**
	 * Fuegt eine Debug-Hilfestellung hinzu.<br/>
	 * Diese kann waehrend des Spieles vom Programmierer gelesen werden, wenn
	 * der Client einen Zug macht.
	 * 
	 * @param hint
	 *            hinzuzufuegende Debug-Hilfestellung
	 */
	public void addHint(DebugHint hint) {
		if (hints == null) {
			hints = new LinkedList<DebugHint>();
		}
		hints.add(hint);
	}

	/**
	 * 
	 * Fuegt eine Debug-Hilfestellung hinzu.<br/>
	 * Diese kann waehrend des Spieles vom Programmierer gelesen werden, wenn
	 * der Client einen Zug macht.
	 * 
	 * @param key
	 *            Schluessel
	 * @param value
	 *            zugehöriger Wert
	 */
	public void addHint(String key, String value) {
		addHint(new DebugHint(key, value));
	}

	/**
	 * Fuegt eine Debug-Hilfestellung hinzu.<br/>
	 * Diese kann waehrend des Spieles vom Programmierer gelesen werden, wenn
	 * der Client einen Zug macht.
	 * 
	 * @param content
	 *            Debug-Hilfestellung
	 */
	public void addHint(String content) {
		addHint(new DebugHint(content));
	}

	/**
	 * Gibt die Liste der hinzugefuegten Debug-Hilfestellungen zurueck
	 * 
	 * @return Liste der hinzugefuegten Debug-Hilfestellungen
	 */
	public List<DebugHint> getHints() {
		return hints == null ? new LinkedList<DebugHint>() : hints;
	}

	/**
	 * Führt diesen Zug aus. Modifiziert wird dabei das übergebene
	 * Spielerobjekt, nicht das korrespondierende Spielerobjekt aus dem
	 * GameState. Bei einem Aufruf sollte also immer
	 * {@code move.perform(gameState,
	 * gameState.getCurrentPlayer())} aufgerufen werden. </br> Bei einem
	 * {@link LayMove} wird der Vorrat, des übergebenen Spielerobjektes wieder
	 * mit Steinen aus dem Beutel aufgefüllt.
	 * 
	 * @param state
	 *            Der Spielzustand, auf welchen diese Zug angewandt werden soll.
	 * @param player
	 *            Der Spieler, welcher den Zug ausführen soll.
	 * @throws InvalidMoveException
	 *             Wird geworfen, falls der Zug ungültig ist.
	 * @throws StoneBagIsEmptyException
	 *             Wird geworfen, falls versucht wird Steine aus einem leeren
	 *             Beutel zu ziehen.
	 */
	public void perform(GameState state, Player player)
			throws InvalidMoveException, StoneBagIsEmptyException {
		checkGameStateAndPlayerNotNull(state, player);
	}

	/**
	 * Diese Methode funktioniert analog zu {@link #perform(GameState, Player)}.
	 * Modifiziert wird in dieser Methode jedoch das Player-Objekt im GameState,
	 * welche die gleiche Farbe besitzt wie die übergebene.
	 * 
	 * @param state
	 * @param color
	 */
	public void perform(GameState state, PlayerColor color)
			throws InvalidMoveException, StoneBagIsEmptyException {
		if (state == null) {
			throw new IllegalArgumentException("GameState darf nicht null sein");
		}

		if (color == null) {
			throw new IllegalArgumentException(
					"Spielerfarbe darf nicht null sein");
		}
	}

	/**
	 * Diese Methode funktioniert analog zu {@link #perform(GameState, Player)}.
	 * Modifiziert wird in dieser Methode der Player, welcher gerade am Zug ist,
	 * also durch {@link GameState#getCurrentPlayer()} zurückgeliefert wird.
	 * 
	 * @param state
	 * @throws InvalidMoveException
	 * @throws StoneBagIsEmptyException
	 */
	public void perform(GameState state) throws InvalidMoveException,
			StoneBagIsEmptyException {
		if (state == null) {
			throw new IllegalArgumentException("GameState darf nicht null sein");
		}
	}

	/**
	 * Überprüft ob einer der übergebenen Parameter null ist.
	 * 
	 * @param state
	 * @param player
	 */
	private void checkGameStateAndPlayerNotNull(GameState state, Player player) {
		if (state == null) {
			throw new IllegalArgumentException("GameState darf nicht null sein");
		}

		if (player == null) {
			throw new IllegalArgumentException("Spieler darf nicht null sein");
		}
	}

	/**
	 * Überprüft ob der Spieler die übergeben Steine auf der Hand hat.
	 * 
	 * @param stoneList
	 *            Die Liste von Steinen.
	 * @param player
	 *            Der Spieler.
	 * @throws InvalidMoveException
	 */
	protected void checkIfStonesAreFromPlayerHand(List<Stone> stoneList,
			Player player) throws InvalidMoveException {
		for (Stone stone : stoneList) {
			boolean notFound = true;
			for (Stone stoneOnHand : player.getStones()) {
				if (stone.equals(stoneOnHand)) {
					notFound = false;
				}
			}

			if (notFound) {
				throw new InvalidMoveException(
						"Die Steine müssen von der Hand sein");
			}
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Move clone = (Move) super.clone();
		if (hints != null) {
			clone.hints = new LinkedList<DebugHint>(hints);
		}
		return clone;
	}
}
