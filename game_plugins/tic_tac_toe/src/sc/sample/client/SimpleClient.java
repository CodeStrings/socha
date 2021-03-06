package sc.sample.client;

import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import sc.api.plugins.IPlayer;
import sc.networking.clients.ILobbyClientListener;
import sc.networking.clients.LobbyClient;
import sc.protocol.responses.ErrorResponse;
import sc.protocol.responses.PrepareGameResponse;
import sc.sample.server.GamePluginImpl;
import sc.sample.shared.Board;
import sc.sample.shared.GameState;
import sc.sample.shared.Move;
import sc.shared.GameResult;

public class SimpleClient implements ILobbyClientListener
{
	private static Object	consoleLock	= new Object();
	private LobbyClient		client;
	private GameState		state;

	public SimpleClient(XStream xStream) throws IOException
	{
		this.client = new LobbyClient(xStream);
		this.client.addListener(this);
	}

	public void start()
	{
		this.client.start();
	}

	@Override
	public void onRoomMessage(String roomId, Object data)
	{
		for (int y = 0; y < Board.HEIGHT; y++)
		{
			for (int x = 0; x < Board.WIDTH; x++)
			{
				if (state.board.getOwner(x, y) == null)
				{
					client.sendMessageToRoom(roomId, new Move(x, y));
					return;
				}
			}
		}

		throw new RuntimeException("Couldn't find a valid move.");
	}
	
	@Override
	public void onError(String roomId, ErrorResponse error)
	{
		System.err.println(error);		
	}

	@Override
	public void onNewState(String roomId, Object state)
	{
		this.state = (GameState) state;
	}

	public void joinAnyGame()
	{
		client.joinAnyGame(GamePluginImpl.PLUGIN_UUID);
	}

	public void joinPreparedGame(String reservation)
	{
		client.joinPreparedGame(reservation);
	}

	@Override
	public void onGameJoined(String roomId)
	{

	}

	@Override
	public void onGameLeft(String roomId)
	{
		synchronized (consoleLock)
		{
			System.out.println("Game is over. Good night.");
			this.client.close();
		}
	}

	@Override
	public void onGamePrepared(PrepareGameResponse response)
	{

	}

	@Override
	public void onGameOver(String roomId, GameResult data)
	{
		synchronized (consoleLock)
		{
			System.out.println("--------------------------");
			System.out.println("-      RESULTS           -");
			System.out.print(data.toString());
			System.out.println("--------------------------");
			System.out.println();
		}
	}

	@Override
	public void onGamePaused(String roomId, IPlayer nextPlayer)
	{
		// TODO Auto-generated method stub

	}
}
