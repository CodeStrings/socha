/**
 * 
 */
package sc.plugin2016.gui;

import sc.plugin2016.GameState;
import sc.plugin2016.GuiClient;
import sc.plugin2016.IGameHandler;
import sc.plugin2016.Move;
import sc.plugin2016.Player;
import sc.plugin2016.PlayerColor;
import sc.plugin2016.gui.renderer.RenderFacade;
import sc.shared.GameResult;

/**
 * @author ffi
 * 
 */
public class HumanGameHandler implements IGameHandler {

	private GuiClient client;

	public HumanGameHandler(GuiClient client) {
		this.client = client;
	}

	@Override
	public void onUpdate(GameState gameState) {
		RenderFacade.getInstance().updateGameState(gameState);
	}

	@Override
	public void onUpdate(Player player, Player otherPlayer) {
		RenderFacade.getInstance().updatePlayer(player, otherPlayer,
				client.getID());
	}

	public void onUpdate(String chat) {
		RenderFacade.getInstance().updateChat(chat, client.getID());
	}

	@Override
	public void onRequestAction() {
		RenderFacade.getInstance().requestMove(client.getID());
	}

	@Override
	public void sendAction(Move move) {
		client.sendMove(move);
	}

	@Override
	public void gameEnded(GameResult data, PlayerColor color,
			String errorMessage) {
		RenderFacade.getInstance().gameEnded(
				data,
				client.getID(),
				(color == PlayerColor.RED ? PlayerColor.BLUE
						: PlayerColor.RED), errorMessage);
	}
}
