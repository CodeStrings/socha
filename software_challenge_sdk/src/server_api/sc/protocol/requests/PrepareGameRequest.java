package sc.protocol.requests;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import sc.protocol.responses.PrepareGameProtocolMessage;
import sc.protocol.responses.ProtocolMessage;
import sc.shared.DisplayNameConverter;
import sc.shared.SlotDescriptor;

import java.util.LinkedList;
import java.util.List;

@XStreamAlias("prepare")
public class PrepareGameRequest extends ProtocolMessage implements ILobbyRequest,
        IRequest<PrepareGameProtocolMessage>
{
  @XStreamAsAttribute
  private final String				gameType;

  @XStreamImplicit(itemFieldName = "slot")
  private final List<SlotDescriptor>	slotDescriptors;

  // i.e. GameState
  private Object loadGameInfo = null;

  /**
   * Create a Prepared Game
   * @param gameType name of the game as String
   */
  public PrepareGameRequest(String gameType)
  {
    this.gameType = gameType;
    this.slotDescriptors = new LinkedList<>();

    // Add two players, named Player1 and Player2
    this.slotDescriptors.add(new SlotDescriptor("Player1"));
    this.slotDescriptors.add(new SlotDescriptor("Player2"));
  }

  /**
   * Create PrePareGameRequest with name and Descriptors for each player
   * @param gameType name of the Game as String
   * @param descriptor1 descriptor for Player 1
   * @param descriptor2 descriptor for Player 2
   */
  public PrepareGameRequest(String gameType, SlotDescriptor descriptor1, SlotDescriptor descriptor2)
  {
    this.gameType = gameType;
    this.slotDescriptors = new LinkedList<>();

    // Add two players, named Player1 and Player2
    this.slotDescriptors.add(descriptor1);
    this.slotDescriptors.add(descriptor2);
  }

  public Object getLoadGameInfo() {
    return this.loadGameInfo;
  }

  public String getGameType()
  {
    return this.gameType;
  }

  public List<SlotDescriptor> getSlotDescriptors()
  {
    return this.slotDescriptors;
  }
}
