package sc.plugin2016;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import sc.plugin2016.util.Constants;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Ein {@code GameState} beinhaltet alle Informationen die den Spielstand zu
 * einem gegebenen Zeitpunkt, das heisst zwischen zwei Spielzuegen, beschreiben.
 * Dies umfasst eine fortlaufende Zugnummer ({@link #getTurn() getTurn()}), die
 * der Spielserver als Antwort von einem der beiden Spieler (
 * {@link #getCurrentPlayer() getCurrentPlayer()}) erwartet. Weiterhin gehoeren
 * die Informationen ueber die beiden Spieler und das Spielfeld zum Zustand.
 * Zuseatzlich wird ueber den zuletzt getaetigeten Spielzung und ggf. ueber das
 * Spielende informiert.
 * 
 * 
 * Der {@code GameState} ist damit das zentrale Objekt ueber das auf alle
 * wesentlichen Informationen des aktuellen Spiels zugegriffen werden kann.
 * 
 * 
 * Der Spielserver sendet an beide teilnehmenden Spieler nach jedem getaetigten
 * Zug eine neue Kopie des {@code GameState}, in dem der dann aktuelle Zustand
 * beschrieben wird. Informationen ueber den Spielverlauf sind nur bedingt ueber
 * den {@code GameState} erfragbar und muessen von einem Spielclient daher bei
 * Bedarf selbst mitgeschrieben werden.
 * 
 * 
 * Zusaetzlich zu den eigentlichen Informationen koennen bestimmte
 * Teilinformationen abgefragt werden. Insbesondere kann mit der Methode
 * {@link #getPossibleMoves() getPossibleMoves()} eine Liste aller fuer den
 * aktuellen Spieler legalen Zuege abgefragt werden. So kann ein Spieleclient
 * diese Liste aus dem {@code GameState} erfragen und muss dann lediglich einen
 * Zug aus dieser Liste auswaehlen.
 * 
 * @author Niklas, Sören
 */
@XStreamAlias(value = "state")
public class GameState implements Cloneable {

  /**
   * momentane Rundenzahl
   */
  @XStreamAsAttribute
  private int turn;

  /**
   * Farbe des Startspielers
   */
  @XStreamAsAttribute
  private PlayerColor startPlayer;

  /**
   * Farbe des aktuellen Spielers
   */
  @XStreamAsAttribute
  private PlayerColor currentPlayer;

  /**
   * der rote Spieler
   */
  private Player red;
  /**
   * der blaue Spieler
   */
  private Player blue;

  /**
   * Das Spielbrett
   */
  private Board board;

  /**
   * letzter getaetigter Zug
   */
  private Move lastMove;

  /**
   * Endbedingung
   */
  private Condition condition = null;

  /**
   * Erzeugt einen neuen {@code GameState} in dem alle Informationen so gesetzt
   * sind, wie sie zu Beginn eines Spiels, bevor die Spieler beigetreten sind,
   * gueltig sind.
   * 
   * 
   * Dieser Konstruktor ist nur fuer den Spielserver relevant und sollte vom
   * Spielclient i.A. nicht aufgerufen werden!
   * 
   * Das Spielfeld wird zufällig aufgebaut.
   */
  public GameState() {

    currentPlayer = PlayerColor.RED;
    startPlayer = PlayerColor.RED;
    board = new Board();
  }

  /**
   * erzeugt eine Deepcopy dieses Objekts
   * 
   * @return ein neues Objekt mit gleichen Eigenschaften
   * @throws CloneNotSupportedException falls klonen fehlschlaegt
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    GameState clone = (GameState) super.clone();
    if (red != null)
      clone.red = (Player) red.clone();
    if (blue != null)
      clone.blue = (Player) blue.clone();
    if (lastMove != null)
      clone.lastMove = (Move) lastMove.clone();
    if (board != null)
      clone.board = (Board) this.board.clone();
    if (condition != null)
      clone.condition = (Condition) condition.clone();
    if (currentPlayer != null)
      clone.currentPlayer = currentPlayer;

    return clone;
  }

  /**
   * Fuegt einem Spiel einen weiteren Spieler hinzu.
   * 
   * 
   * Diese Methode ist nur fuer den Spielserver relevant und sollte vom
   * Spielclient i.A. nicht aufgerufen werden!
   * 
   * @param player
   *          Der hinzuzufuegende Spieler.
   */
  public void addPlayer(Player player) {

    if (player.getPlayerColor() == PlayerColor.RED) {
      red = player;
    } else if (player.getPlayerColor() == PlayerColor.BLUE) {
      blue = player;
    }
  }

  /**
   * Gibt das Spielfeld zurueck
   * 
   * @return das Spielfeld
   */
  public Board getBoard() {
    return this.board;
  }

  /**
   * Liefert den Spieler, also ein {@code Player}-Objekt, der momentan am Zug
   * ist.
   * 
   * @return Der Spieler, der momentan am Zug ist.
   */
  public Player getCurrentPlayer() {
    return (currentPlayer == PlayerColor.RED ? red : blue);
  }

  /**
   * Liefert die {@code PlayerColor}-Farbe des Spielers, der momentan am Zug
   * ist. Dies ist aequivalent zum Aufruf
   * {@code getCurrentPlayer().getPlayerColor()}, aber etwas effizienter.
   * 
   * @return Die Farbe des Spielers, der momentan am Zug ist.
   */
  public PlayerColor getCurrentPlayerColor() {
    return currentPlayer;
  }

  /**
   * Liefert den Spieler, also ein {@code Player}-Objekt, der momentan nicht am
   * Zug ist.
   * 
   * @return Der Spieler, der momentan nicht am Zug ist.
   */
  public Player getOtherPlayer() {
    return currentPlayer == PlayerColor.RED ? blue : red;
  }

  /**
   * Liefert die {@code PlayerColor}-Farbe des Spielers, der momentan nicht am
   * Zug ist. Dies ist aequivalent zum Aufruf @
   * {@code getCurrentPlayerColor.opponent()} oder
   * {@code getOtherPlayer().getPlayerColor()}, aber etwas effizienter.
   * 
   * @return Die Farbe des Spielers, der momentan nicht am Zug ist.
   */
  public PlayerColor getOtherPlayerColor() {
    return currentPlayer.opponent();
  }

  /**
   * Liefert den Spieler, also eine {@code Player}-Objekt, des Spielers, der dem
   * Spiel als erstes beigetreten ist und demzufolge mit der Farbe
   * {@code PlayerColor.RED} spielt.
   * 
   * @return Der rote Spieler.
   */
  public Player getRedPlayer() {
    return red;
  }

  /**
   * Liefert den Spieler, also eine {@code Player}-Objekt, des Spielers, der dem
   * Spiel als zweites beigetreten ist und demzufolge mit der Farbe
   * {@code PlayerColor.BLUE} spielt.
   * 
   * @return Der blaue Spieler.
   */
  public Player getBluePlayer() {
    return blue;
  }

  /**
   * Liefert den Spieler, also eine {@code Player}-Objekt, der das Spiel
   * begonnen hat.
   * 
   * @return Der Spieler, der momentan Startspieler ist.
   */
  public Player getStartPlayer() {
    return startPlayer == PlayerColor.RED ? red : blue;
  }

  /**
   * Liefert die {@code PlayerColor}-Farbe des Spielers, der den aktuellen
   * Abschnitt begonnen hat. Dies ist aequivalent zum Aufruf
   * {@code getStartPlayer().getPlayerColor()}, aber etwas effizienter.
   * 
   * @return Die Farbe des Spielers, der den aktuellen Abschnitt begonnen
   *         hat.
   */
  public PlayerColor getStartPlayerColor() {
    return startPlayer;
  }

  /**
   * wechselt den Spieler, der aktuell an der Reihe ist.
   */
  private void switchCurrentPlayer() {
    currentPlayer = currentPlayer == PlayerColor.RED ? PlayerColor.BLUE
        : PlayerColor.RED;
  }

  /**
   * wechselt den Spieler, der den aktuellen Abschnitt begonnen hat.
   */
  /*
   * private void switchStartPlayer() { startPlayer = startPlayer ==
   * PlayerColor.RED ? PlayerColor.BLUE : PlayerColor.RED; }
   */

  /**
   * liefert die aktuelle Zugzahl
   * @return Nummer des aktuellen Zuges (Zaehlung beginnt mit 0)
   */
  public int getTurn() {
    return turn;
  }

  /**
   * Simuliert einen uebergebenen Zug. Dabei werden folgende Informationen
   * aktualisiert:
   * <ul>
   * <li>Zugzahl
   * <li>Welcher Spieler an der Reihe ist
   * <li>Was der letzte Zug war
   * <li>die Punkte der Spieler
   * </ul>
   * 
   * @param lastMove
   *          auszufuehrender Zug
   */
  public void prepareNextTurn(Move lastMove) {
    turn++;
    this.lastMove = lastMove;
    this.getCurrentPlayer().setPoints(getPointsForPlayer(currentPlayer));
    switchCurrentPlayer();
    
  }

  /**
   * liefert die aktuelle Rundenzahl
   * 
   * @return aktuelle Rundenzahl
   */
  public int getRound() {
    return turn / 2;
  }

  /**
   * Liefert eine Liste aller aktuell erlaubten Zuege, des Spielers der
   * aktuell an der Reihe ist.
   * 
   * @return Liste erlaubter Spielzuege
   */
  public List<Move> getPossibleMoves() {
    FieldType enemyFieldType = currentPlayer == PlayerColor.RED ? FieldType.BLUE : FieldType.RED;
    List<Move> moves = new ArrayList<Move>();
    for(int x = 0; x < Constants.SIZE; x++) {
      for(int y = 0; y < Constants.SIZE; y++) {
        if(getBoard().getField(x, y).getOwner() == null
            && getBoard().getField(x, y).getType() != FieldType.SWAMP
            && getBoard().getField(x, y).getType() != enemyFieldType) {
          moves.add(new Move(x, y));
        }
      }
    }
    return moves;
  }

  /**
   * Liefert den zuletzt ausgefuehrten Zug
   * 
   * @return letzter Zug
   */
  public Move getLastMove() {
    return lastMove;
  }

  /**
   * Liefert Statusinformationen zu einem Spieler als Array mit folgenden
   * Einträgen
   * <ul>
   * <li>[0] - Punktekonto des Spielers (Längste leitung in Spielrichtung)
   * </ul>
   * 
   * @param player
   *          Spieler
   * @return Array mit Statistiken
   */
  public int[] getPlayerStats(Player player) {
    assert player != null;
    return getPlayerStats(player.getPlayerColor());
  }

  /**
   * Liefert Statusinformationen zu einem Spieler als Array mit folgenden
   * Einträgen
   * <ul>
   * <li>[0] - Punktekonto des Spielers (Längste Verbindung in Spielrichtung)
   * </ul>
   * 
   * @param playerColor
   *          Farbe des Spielers
   * @return Array mit Statistiken
   */
  public int[] getPlayerStats(PlayerColor playerColor) {
    assert playerColor != null;

    if (playerColor == PlayerColor.RED) {
      return getGameStats()[0];
    } else {
      return getGameStats()[1];
    }
  }

  /**
   * Liefert Statusinformationen zum Spiel. Diese sind ein Array der
   * {@link #getPlayerStats(PlayerColor) Spielerstats}, wobei getGameStats()[0],
   * einem Aufruf von getPlayerStats(PlayerColor.RED) entspricht.
   * 
   * @see #getPlayerStats(PlayerColor)
   * @return Statusinformationen beider Spieler
   */
  public int[][] getGameStats() {

    int[][] stats = new int[2][1];

    stats[0][0] = this.red.getPoints();
    stats[1][0] = this.blue.getPoints();
    
    return stats;

  }

  /**
   * liefert die Namen den beiden Spieler
   * @return Namen der Spieler
   */
  public String[] getPlayerNames() {
    return new String[] { red.getDisplayName(), blue.getDisplayName() };

  }

  /**
   * Legt das Spiel als beendet fest, setzt dabei einen Sieger und Gewinngrund
   * 
   * @param winner
   *          Farbe des Siegers
   * @param reason
   *          Gewinngrund
   */
  public void endGame(PlayerColor winner, String reason) {
    if (condition == null) {
      condition = new Condition(winner, reason);
    }
  }

  /**
   * gibt an, ob das Spiel beendet ist
   * 
   * @return wahr, wenn beendet
   */
  public boolean gameEnded() {
    return condition != null;
  }

  /**
   * liefert die Farbe des Siegers, falls das Spiel beendet ist.
   * 
   * @see #gameEnded()
   * @return Siegerfarbe
   */
  public PlayerColor winner() {
    return condition == null ? null : condition.winner;
  }

  /**
   * liefert den Gewinngrund, falls das Spiel beendet ist.
   * 
   * @see #gameEnded()
   * @return Gewinngrund
   */
  public String winningReason() {
    return condition == null ? "" : condition.reason;
  }

  /**
   * Gibt die angezeigte Punktzahl des Spielers zurueck.
   * @param playerColor Farbe des Spielers
   * @return Punktzahl des Spielers
   */
  public int getPointsForPlayer(PlayerColor playerColor) {
    int longestPath = 0;
    
    if(playerColor == PlayerColor.RED) {
      List<Field[]> bottomAndTopmostFieldsInCircuit = new LinkedList<Field[]>();
      boolean[][] visited = new boolean[Constants.SIZE][Constants.SIZE]; // all by default initialized to false
      for(int x = 0; x < Constants.SIZE; x++) {
        for(int y = 0; y < Constants.SIZE; y++) {
          if(visited[x][y] == false) {
            if(getBoard().getField(x, y).getOwner() == PlayerColor.RED) {
              List<Field> startOfCircuit = new LinkedList<Field>();
              startOfCircuit.add(board.getField(x, y));
              List<Field> circuit = getCircuit(startOfCircuit, new LinkedList<Field>());
              for(Field f: circuit) {
                visited[f.getX()][f.getY()] = true;
              }
              Field[] bottomAndTopmost = new Field[2];
              bottomAndTopmost[0] = getBottomMostFieldInCircuit(circuit);
              bottomAndTopmost[1] = getTopMostFieldInCircuit(circuit);
              bottomAndTopmostFieldsInCircuit.add(bottomAndTopmost);
            }
            visited[x][y] = true;
          }
        }
      }
      for(Field[] fields : bottomAndTopmostFieldsInCircuit) {
        if(fields[1].getY() - fields[0].getY() > longestPath) {
          longestPath = fields[1].getY() - fields[0].getY();
        }
      }
    } else if(playerColor == PlayerColor.BLUE) {
      List<Field[]> leftAndRightmostFieldsInCircuit = new LinkedList<Field[]>();
      boolean[][] visited = new boolean[Constants.SIZE][Constants.SIZE]; // all by default initialized to false
      for(int x = 0; x < Constants.SIZE; x++) {
        for(int y = 0; y < Constants.SIZE; y++) {
          if(visited[x][y] == false) {
            if(getBoard().getField(x, y).getOwner() == PlayerColor.BLUE) {
              List<Field> startOfCircuit = new LinkedList<Field>();
              startOfCircuit.add(board.getField(x, y));
              List<Field> circuit = getCircuit(startOfCircuit, new LinkedList<Field>());
              for(Field f: circuit) {
                visited[f.getX()][f.getY()] = true;
              }
              Field[] leftAndRightmost = new Field[2];
              leftAndRightmost[0] = getLeftMostFieldInCircuit(circuit);
              leftAndRightmost[1] = getRightMostFieldInCircuit(circuit);
              leftAndRightmostFieldsInCircuit.add(leftAndRightmost);
            }
            visited[x][y] = true;
          }
        }
      }
      for(Field[] fields : leftAndRightmostFieldsInCircuit) {
        if(fields[1].getX() - fields[0].getX() > longestPath) {
          longestPath = fields[1].getX() - fields[0].getX();
        }
      }
    } else {
      throw new IllegalArgumentException();
    }
    return longestPath; // return longestPath
  }
  
  /**
   * Nur fuer den Server relevant
   * returns the bottommost field in a given circuit
   * @param circuit given circuit
   * @return bottommost field
   */
  private Field getBottomMostFieldInCircuit(List<Field> circuit) {
    Field bottomMostField = circuit.get(0);
    for(Field f : circuit) {
      if(f.getY() < bottomMostField.getY()) {
        bottomMostField = f;
      }
    }
    return bottomMostField;
  }
  
  /**
   * Nur fuer den Server relevant
   * returns the topmost field in a given circuit
   * @param circuit given circuit
   * @return topmost field
   */
  private Field getTopMostFieldInCircuit(List<Field> circuit) {
    Field topMostField = circuit.get(0);
    for(Field f : circuit) {
      if(f.getY() > topMostField.getY()) {
        topMostField = f;
      }
    }
    return topMostField;
  }
  
  /**
   * Nur fuer den Server relevant
   * returns the leftmost field in a given circuit
   * @param circuit given circuit
   * @return leftmost field
   */
  private Field getLeftMostFieldInCircuit(List<Field> circuit) {
    Field leftMostField = circuit.get(0);
    for(Field f : circuit) {
      if(f.getX() < leftMostField.getX()) {
        leftMostField = f;
      }
    }
    return leftMostField;
  }

  /**
   * Nur fuer den Server relevant
   * returns the rightmost field in a given circuit
   * @param circuit given circuit
   * @return rightmost field
   */
  private Field getRightMostFieldInCircuit(List<Field> circuit) {
    Field rightMostField = circuit.get(0);
    for(Field f : circuit) {
      if(f.getX() > rightMostField.getX()) {
        rightMostField = f;
      }
    }
    return rightMostField;
  }

  /**
   * Nur fuer den Server relevant
   * calculates a List of poles, that form a circuit
   * @param circuit initial circuit, should contain only one initial field in the beginning
   * @param visited initial List of already visited fields, should be empty in the beginning
   * @return the circuit sorrounding the given initial field
   */
  private List<Field> getCircuit(List<Field> circuit, List<Field> visited) {
    boolean changed = false;
    List<Field> toBeAddedFields = new LinkedList<Field>();
    for(Field f : circuit) {
      if(!visited.contains(f)) {
        changed = true;
        visited.add(f);
        for(Connection c : getBoard().getConnections(f.getX(), f.getY())) {
          if(!circuit.contains(getBoard().getField(c.x2, c.y2))) {
            toBeAddedFields.add(getBoard().getField(c.x2, c.y2));
          }
        }
      }
    }
    circuit.addAll(toBeAddedFields);
    if(changed) {
      return getCircuit(circuit, visited);
    } else {
      return circuit;
    }
  }
}














