package sc.plugin2013;

import java.util.LinkedList;
import java.util.List;

import sc.plugin2013.DebugHint;
import sc.plugin2013.Move;
import sc.plugin2013.util.InvalidMoveException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Abstrakte Zugklasse. {@link ForwardMove} und {@link BackwardMove} erben von
 * dieser Klasse. Beiden Zugarten gemeinsam ist die Liste mit {@link DebugHint}
 * -Objekten.
 * 
 * @author fdu
 * 
 */
@XStreamAlias(value = "move")
public abstract class Move implements Cloneable {
	@XStreamImplicit(itemFieldName = "hint")
	private List<DebugHint> hints;
	
	@XStreamAsAttribute
	public int fieldIndex;

	/**
	 * klont dieses Objekt
	 * 
	 * @return ein neues Objekt mit gleichen Eigenschaften
	 * @throws CloneNotSupportedException
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		Move clone = (Move) super.clone();
		if (this.hints != null)
			clone.hints = new LinkedList<DebugHint>(this.hints);
		return clone;
	}

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
	 * @param string
	 *            Debug-Hilfestellung
	 */
	public void addHint(String string) {
		addHint(new DebugHint(string));
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
	 * Abstrakte Methode, welche von ForwardMove und BackwardMove implementiert
	 * wird. Hierbei wird überprüft ob der Zug gültig ist. Die Spielkarten der
	 * Spieler werden aktualisiert.
	 * 
	 * @param state
	 * @param player
	 * @throws InvalidMoveException
	 */
	public abstract void perform(GameState state, Player player)
			throws InvalidMoveException;
}
