package sc.plugin2014;

import sc.plugin2014.entities.PlayerColor;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Nachricht, die zu Beginn eines Spiels an einen Client geschickt wird,
 * um ihm seine Spielerfarbe mitzuteilen
 * 
 */
@XStreamAlias(value = "welcome")
public class WelcomeMessage {

    @XStreamAsAttribute
    private String color;

    /**
     * might be needed by XStream
     */
    public WelcomeMessage() {}

    public WelcomeMessage(PlayerColor c) {
        color = c.toString().toLowerCase();
    }

    public PlayerColor getYourColor() {
        return PlayerColor.valueOf(color.toUpperCase());
    }
}
