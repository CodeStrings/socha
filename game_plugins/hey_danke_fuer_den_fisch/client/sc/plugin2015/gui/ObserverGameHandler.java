/**
 * 
 */
package sc.plugin2015.gui;

import sc.plugin2015.EPlayerId;
import sc.plugin2015.GameState;
import sc.plugin2015.IGameHandler;
import sc.plugin2015.Move;
import sc.plugin2015.Player;
import sc.plugin2015.PlayerColor;
import sc.plugin2015.gui.renderer.RenderFacade;
import sc.shared.GameResult;

/**
 * 
 * @author ffi
 * 
 */
public class ObserverGameHandler implements IGameHandler {

	public ObserverGameHandler() {
	}

	@Override
	public void onUpdate(GameState gameState) {
		RenderFacade.getInstance().updateGameState(gameState);
	}

	@Override
	public void onUpdate(Player player, Player otherPlayer) {
		RenderFacade.getInstance().updatePlayer(player, otherPlayer,
				EPlayerId.OBSERVER);
	}

	public void onUpdate(String chat) {
		RenderFacade.getInstance().updateChat(chat, EPlayerId.OBSERVER);
	}

	@Override
	public void onRequestAction() {
		//RenderFacade.getInstance().switchToPlayer(EPlayerId.OBSERVER);
		RenderFacade.getInstance().requestMove(EPlayerId.OBSERVER);
	}

	@Override
	public void sendAction(Move move) {
		// observer cant send moves
	}

	@Override
	public void gameEnded(GameResult data, PlayerColor color,
			String errorMessage) {
		RenderFacade.getInstance().gameEnded(data, EPlayerId.OBSERVER, color,
				errorMessage);
	}
}
