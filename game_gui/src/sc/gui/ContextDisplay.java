package sc.gui;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.guiplugin.interfaces.IObservation;
import sc.guiplugin.interfaces.listener.INewTurnListener;
import sc.logic.save.GUIConfiguration;

@SuppressWarnings("serial")
public class ContextDisplay extends JPanel implements INewTurnListener {

	private static final Logger logger = LoggerFactory
			.getLogger(ContextDisplay.class);
	private final PresentationFacade presFac;
	private final Properties lang;

	private GameControlBar buttonBar;
	private boolean started;

	/**
	 * Constructor
	 */
	public ContextDisplay() {
		super();
		this.presFac = PresentationFacade.getInstance();
		this.lang = presFac.getLogicFacade().getLanguageData();
		createGUI();
	}

	public GameControlBar getGameControlBar() {
		return buttonBar;
	}

	/**
	 * Creates the <code>ContextDisplay</code>.
	 */
	private void createGUI() {
		this.setLayout(new BorderLayout());

		buttonBar = new GameControlBar();
		bindShortcut(buttonBar.btn_toBegin, KeyEvent.VK_LEFT,
				InputEvent.CTRL_DOWN_MASK);
		bindShortcut(buttonBar.btn_back, KeyEvent.VK_LEFT, 0);
		bindShortcut(buttonBar.btn_next, KeyEvent.VK_RIGHT, 0);
		bindShortcut(buttonBar.btn_toEnd, KeyEvent.VK_RIGHT,
				InputEvent.CTRL_DOWN_MASK);
		disableAllButtons(); // by default

		buttonBar.btn_toBegin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonBar.setStepping(false);
				presFac.getLogicFacade().getObservation().goToFirst();
			}
		});

		buttonBar.btn_toEnd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonBar.setStepping(false);
				presFac.getLogicFacade().getObservation().goToLast();
			}
		});

		buttonBar.btn_back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonBar.setStepping(false);
				presFac.getLogicFacade().getObservation().back();
			}
		});

		ActionListener playPauseActionListner = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!started) {
					started = true;
					presFac.getLogicFacade().getObservation().start();
				} else if (presFac.getLogicFacade().getObservation().isPaused()) {
					presFac.getLogicFacade().getObservation().unpause();
				} else {
					presFac.getLogicFacade().getObservation().pause();
				}
			}
		};

		buttonBar.btn_pause.addActionListener(playPauseActionListner);
		buttonBar.btn_play.addActionListener(playPauseActionListner);

		buttonBar.btn_next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				presFac.getLogicFacade().getObservation().next();
			}
		});

		buttonBar.btn_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (presFac.getLogicFacade().getObservation().isFinished()
						|| JOptionPane.showConfirmDialog(
								null,
								lang.getProperty("context_dialog_cancel_msg"),
								lang.getProperty("context_dialog_cancel_title"),
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					cancelCurrentGame();
				}
			}
		});

		recreateGameField();
	}

	/**
	 * Aborts the current game, clears the context display and disables all
	 * buttons.
	 */
	public void cancelCurrentGame() {
		if (presFac.getLogicFacade().getObservation() != null) {
			presFac.getLogicFacade().getObservation().cancel(); // calls
																// gameEnded
		}
		started = false;

		disableAllButtons();
		buttonBar.setPaused(false);

		presFac.getContextDisplay().recreateGameField();
		// update status bar
		((StatusBar) presFac.getStatusBar()).setStatus(lang
				.getProperty("statusbar_status_nogame"));
	}

	/**
	 * Binds the standard action of the given <code>button</code> to the given
	 * <code>key</code>.
	 *
	 * @param component
	 * @param key
	 * @param modifiers
	 */
	private void bindShortcut(final JButton button, int key, int modifiers) {
		Object actionMapKey = "shortcut1";
		button.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(key, modifiers), actionMapKey);
		button.getActionMap().put(actionMapKey, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button.doClick();
			}
		});
	}

	/**
	 * Disables all 6 buttons in the control bar at the bottom.
	 */
	private void disableAllButtons() {
		buttonBar.disable();
	}

	/**
	 * Recreates an empty game field panel and replaces it with the current game
	 * field. It also resets all other components on this panel in the old
	 * order.
	 *
	 * @return the new game field panel
	 */
	public Panel recreateGameField() {
		Panel gameField = new Panel();

		this.removeAll();
		this.add(gameField, BorderLayout.CENTER);
		this.add(buttonBar, BorderLayout.PAGE_END);
		this.revalidate();
	    try {
	        Thread.sleep(500);
	      } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      }
		this.repaint();
		logger.debug("dimensions of game field: {},{}", gameField.getWidth(), gameField.getHeight());

		return gameField;
	}

	/**
	 * Updates (enables/disables or shows/hides) the buttons, e.g. start game or
	 * next turn.
	 */
	public void updateButtonBar(boolean ended) {
		IObservation obs = presFac.getLogicFacade().getObservation();
		if (obs != null) {
			buttonBar.setActive(!ended);
			syncButtonStates(obs);
			buttonBar.setVisible(true);
		}
	}

	@Override
	public void newTurn(int id, String info) {
		IObservation obs = presFac.getLogicFacade().getObservation();
		if (obs != null) {
			syncButtonStates(obs);
		}
	}

	private void syncButtonStates(IObservation obs) {
		buttonBar.btn_toBegin.setEnabled(!obs.isAtStart()
				&& !GUIConfiguration.finaleMode);
		buttonBar.btn_toEnd.setEnabled(!obs.isAtEnd()
				&& !GUIConfiguration.finaleMode);
		buttonBar.btn_back.setEnabled(obs.hasPrevious());
		buttonBar.btn_next.setEnabled(obs.hasNext());
		buttonBar.btn_pause.setEnabled(obs.canTogglePause());
		buttonBar.btn_play.setEnabled(obs.canTogglePause());
		syncPauseAndPlayState(obs.isPaused());
	}

	/**
	 * Updates the tool tip and the icon.
	 *
	 * @param paused
	 */
	private void syncPauseAndPlayState(boolean paused) {
		buttonBar.setPaused(paused);
	}

	public void startPlaying() {
		buttonBar.startStepping();
	}
}
