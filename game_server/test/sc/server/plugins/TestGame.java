package sc.server.plugins;

import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import sc.api.plugins.exceptions.GameLogicException;
import sc.api.plugins.exceptions.TooManyPlayersException;
import sc.framework.plugins.RoundBasedGameInstance;
import sc.framework.plugins.SimplePlayer;
import sc.shared.PlayerScore;
import sc.shared.ScoreCause;
import sc.shared.WinCondition;

/*XXX should not be abstract*/

public class TestGame extends RoundBasedGameInstance<TestPlayer>
{
	private TestGameState	state	= new TestGameState();

	public TestGame()
	{
		this.state.setController(this);
	}

	@Override
	protected void onRoundBasedAction(SimplePlayer fromPlayer, Object data)
			throws GameLogicException
	{
		if (data instanceof TestMove)
		{
			int newSecret = ((TestMove) data).value;

			if (fromPlayer == this.players.get(0))
			{
				this.state.secret0 = newSecret;
			}
			else if (fromPlayer == this.players.get(1))
			{
				this.state.secret1 = newSecret;
			}
			else
			{
				throw new RuntimeException("Unknown Player");
			}
		}
		this.state.round++;
		next();
	}

  @Override
  protected WinCondition checkWinCondition() {
	  // TODO implement test for it
    return null;
  }

  @Override
	public SimplePlayer onPlayerJoined() throws TooManyPlayersException
	{
		if (this.players.size() < 2)
		{
			TestPlayer player = new TestPlayer();
			this.players.add(player);
			return player;
		}
		else
		{
			throw new TooManyPlayersException();
		}
	}

	@Override
	protected boolean checkGameOverCondition()
	{
		return (this.state.round == 4);
	}

	public List<TestPlayer> getPlayers()
	{
		return this.players;
	}

	@Override
	protected Object getCurrentState()
	{
		return this.state;
	}
	
	@Override
	public void onPlayerLeft(SimplePlayer player, ScoreCause cause) {
		// this.players.remove(player);
		LoggerFactory.getLogger(this.getClass())
				.debug("Player left {}", player);
		Map<SimplePlayer, PlayerScore> result = generateScoreMap();
		result.put(player, new PlayerScore(false, "Spieler hat das Spiel verlassen."));
		result.get(player).setCause(cause);
		notifyOnGameOver(result);
	}

	@Override
	public void onPlayerLeft(SimplePlayer player)
	{
		onPlayerLeft(player, ScoreCause.LEFT);
	}

	@Override
	public boolean ready()
	{
		return this.players.size() == TestPlugin.MAXIMUM_PLAYER_SIZE;
	}

	@Override
	protected void onNewTurn()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected PlayerScore getScoreFor(TestPlayer p)
	{
		return new PlayerScore(true, "Spieler hat gewonnen.");
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
	public List<SimplePlayer> getWinners()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
