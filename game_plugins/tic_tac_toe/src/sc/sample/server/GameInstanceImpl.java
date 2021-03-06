package sc.sample.server;

import java.util.List;

import sc.api.plugins.IPlayer;
import sc.api.plugins.exceptions.GameLogicException;
import sc.api.plugins.exceptions.TooManyPlayersException;
import sc.framework.plugins.RoundBasedGameInstance;
import sc.sample.shared.GameState;
import sc.sample.shared.Move;
import sc.sample.shared.Player;
import sc.shared.PlayerScore;
import sc.shared.ScoreCause;

public class GameInstanceImpl extends RoundBasedGameInstance<PlayerImpl>
{
	private GameState	gameState;
	private ServerBoard	board;

	public GameInstanceImpl()
	{
		board = new ServerBoard();
		gameState = new GameState();
		gameState.board = board;
	}

	@Override
	public IPlayer onPlayerJoined() throws TooManyPlayersException
	{
		PlayerImpl newPlayer = new PlayerImpl(this.players.size() == 0 ? "X"
				: "O");
		this.players.add(newPlayer);
		return newPlayer;
	}

	@Override
	public void onPlayerLeft(IPlayer player)
	{
		this.players.remove(player);
	}

	@Override
	protected boolean checkGameOverCondition()
	{
		return board.isGameOver();
	}

	@Override
	protected void onRoundBasedAction(IPlayer fromPlayer, Object data)
			throws GameLogicException
	{
		if (data instanceof Move)
		{
			board.apply(resolve(fromPlayer), (Move) data);
			next();
		}
	}

	protected Player resolve(IPlayer player)
	{
		return ((PlayerImpl) player).getData();
	}

	@Override
	protected Object getCurrentState()
	{
		return this.gameState;
	}

	@Override
	public boolean ready()
	{
		return this.players.size() == GamePluginImpl.MAXIMUM_PLAYER_SIZE;
	}

	@Override
	protected void onNewTurn()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected PlayerScore getScoreFor(PlayerImpl p)
	{
		return p.getScore();
	}

	@Override
	public void loadFromFile(String file)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadGameInfo(Object gameInfo)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerLeft(IPlayer player, ScoreCause cause)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<IPlayer> getWinners()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
