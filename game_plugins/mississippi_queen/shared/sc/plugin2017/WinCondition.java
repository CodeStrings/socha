package sc.plugin2017;

import sc.plugin2017.gui.renderer.primitives.GuiConstants;

/**
 * Beinhaltet Informationen zum Spielende: Farbe des Gewinners und Gewinngrund.
 *
 */
public class WinCondition implements Cloneable {

	/**
	 * Farbe des Gewinners
	 */
	private final PlayerColor winner;

	/**
	 * Sieggrund
	 */
	private final String reason;

  /**
   * Erzeugt eine neue Condition, die anzeigt, dass das Spiel noch nicht zu Ende ist.
   */
  public WinCondition() {
    this.winner = null;
    this.reason = GuiConstants.GAME_NOT_ENDED;
  }

  /**
	 * Erzeugt eine neue Condition mit Sieger und Gewinngrund
	 *
	 * @param winner
	 *            Farbe des Siegers
	 * @param reason
	 *            Text, der Sieg beschreibt
	 */
	public WinCondition(PlayerColor winner, String reason) {
		this.winner = winner;
		this.reason = reason;
	}



  public PlayerColor getWinner() {
    return winner;
  }

  public String getReason() {
    return reason;
  }

	/**
	 * klont dieses Objekt
	 *
	 * @return ein neues Objekt mit gleichen Eigenschaften
	 * @throws CloneNotSupportedException falls Objekt nicht geklont werden kann
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new WinCondition(winner, reason);
	}

}
