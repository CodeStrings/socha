package sc.plugin2010;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author rra
 * @since Jul 4, 2009
 * 
 */
public class PlayerTest
{
	/**
	 * Überprüft das ein Spieler mit den richtigen Anfangswerten erstellt wird.
	 */
	@Test
	public void testPlayer()
	{
		Player p = new Player(FigureColor.RED);

		Assert.assertEquals(FigureColor.RED, p.getColor());
		Assert.assertEquals(0, p.getFieldNumber());
		Assert.assertEquals(5, p.getSaladsToEat());
		Assert.assertEquals(68, p.getCarrotsAvailable());
		
		List<Action> actions = p.getActions();
		Assert.assertTrue(actions.contains(Action.EAT_SALAD));
		Assert.assertTrue(actions.contains(Action.HURRY_AHEAD));
		Assert.assertTrue(actions.contains(Action.FALL_BACK));
		Assert.assertTrue(actions.contains(Action.TAKE_OR_DROP_CARROTS));
	}
}
