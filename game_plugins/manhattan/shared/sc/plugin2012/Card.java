package sc.plugin2012;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Wrapper fuer einen Integer, der eine Baugenehmigungskarte beschreibt.
 * 
 * @author tkra
 *
 */

public class Card {

	@XStreamAsAttribute
    public final int slot;

      /**
         * XStream benötigt eventuell einen parameterlosen Konstruktor
         * bei der Deserialisierung von Objekten aus XML-Nachrichten.
         */
        public Card() {
            this.slot = -1; // könnte Probleme geben
        }
     /**
     * Erzeugt eine Karte fuer eine Position
     * @param slot Index der Position
     */
	public Card(int slot){
		this.slot = slot;
	}
	/**
	 * Prueft, ob die Karte mit der uebergebenen an der Position uebereinstimmt
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Card && ((Card) obj).slot == slot;
	}

	@Override
	public int hashCode() {
		return slot;
	}
	
}
