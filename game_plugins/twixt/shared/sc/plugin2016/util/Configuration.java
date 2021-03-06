package sc.plugin2016.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;

import sc.plugin2016.Board;
import sc.plugin2016.Connection;
import sc.plugin2016.Field;
import sc.plugin2016.FieldType;
import sc.plugin2016.Game;
import sc.plugin2016.GameState;
import sc.plugin2016.Move;
import sc.plugin2016.Player;
import sc.plugin2016.PlayerColor;
import sc.plugin2016.WelcomeMessage;
import sc.protocol.LobbyProtocol;

import com.thoughtworks.xstream.XStream;

/**
 * Configuration
 * 
 * @author sca
 * 
 */
public class Configuration {
	/*
	 * The XStream which is used to translate Objects to XML and vice versa
	 */

	private static XStream xStream;

	static {
		/*xStream = new XStream(new JettisonMappedXmlDriver());*/
	  xStream = new XStream();
		xStream.setMode(XStream.NO_REFERENCES);
		xStream.setClassLoader(Configuration.class.getClassLoader());
		LobbyProtocol.registerMessages(xStream);
		LobbyProtocol.registerAdditionalMessages(xStream,
				getClassesToRegister());
	}

	public static XStream getXStream() {
		return xStream;
	}

	public static List<Class<?>> getClassesToRegister() {
	  /*Board board = new Board();
	  xStream.alias("board", board.getClass() );
	  System.out.println(xStream.toXML(board));*/
		return Arrays.asList(new Class<?>[] { Game.class,
				GameState.class, Constants.class, Move.class,
				Player.class, WelcomeMessage.class, PlayerColor.class,
				Condition.class, Board.class, Field.class, FieldType.class,
				Connection.class
				});
	}
}
