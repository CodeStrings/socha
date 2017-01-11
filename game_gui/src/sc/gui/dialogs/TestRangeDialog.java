package sc.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import sc.api.plugins.IPlayer;
import sc.common.HelperMethods;
import sc.common.UnsupportedFileExtensionException;
import sc.gui.PresentationFacade;
import sc.gui.dialogs.renderer.CenteredTableCellRenderer;
import sc.gui.stuff.KIInformation;
import sc.guiplugin.interfaces.IGamePreparation;
import sc.guiplugin.interfaces.IGuiPlugin;
import sc.guiplugin.interfaces.IObservation;
import sc.guiplugin.interfaces.ISlot;
import sc.guiplugin.interfaces.listener.IGameEndedListener;
import sc.guiplugin.interfaces.listener.INewTurnListener;
import sc.guiplugin.interfaces.listener.IReadyListener;
import sc.logic.save.GUIConfiguration;
import sc.plugin.GUIPluginInstance;
import sc.shared.GameResult;
import sc.shared.PlayerScore;
import sc.shared.ScoreAggregation;
import sc.shared.ScoreDefinition;
import sc.shared.ScoreFragment;
import sc.shared.SharedConfiguration;
import sc.shared.SlotDescriptor;
//import sun.awt.VerticalBagLayout;

@SuppressWarnings("serial")
public class TestRangeDialog extends JDialog {

	private static final String DEFAULT_HOST = "localhost";
	private JPanel pnlTop;
	private JTable statTable;
	private JPanel pnlBottom;
	private JButton testStart;
	private JButton testCancel;
	private final Properties lang;
	private final PresentationFacade presFac;
	private JComboBox cmbGameType;
	private List<GUIPluginInstance> plugins;
	private JTextField[] txfclient;
	private JTextField[] txfparams;
	private JLabel[] lblclient;
	private JButton[] btnclient;
	private JPanel[] pnlclient;
	/**
	 * 0 indicates "no testing"<br>
	 * 1..* indicates the current running test, i.e. "testing"
	 */
	private int curTest;
	private int numTest;
	private JPanel pnlPref;
	private JCheckBox ckbDebug;
	private JTextField txfNumTest;
	private JTextArea txtarea;
	private JPanel pnlCenter;
	private JLabel lblCenter;
	private IObservation obs;
	private JScrollPane scrollTextArea;
	private JProgressBar progressBar;
	private JPanel pnlBottomRight;
	private JCheckBox cb_showLog;
	private JPanel pnl_saveReplay;
	private JPanel pnlBottomTop;
	private int freePort;
	private boolean testStarted;

	private List<List<BigDecimal>> absoluteValues;

	public TestRangeDialog() {
		super();
		presFac = PresentationFacade.getInstance();
		lang = presFac.getLogicFacade().getLanguageData();
		initCurTest(); // not testing
		testStarted = false;
		createGUI();
	}

	/**
	 * Creates the test range GUI
	 */
	private void createGUI() {

		this.setLayout(new BorderLayout());

		plugins = presFac.getLogicFacade().getAvailablePluginsSorted();
		final Vector<String> items = presFac.getLogicFacade().getPluginNames(
				plugins);
		cmbGameType = new JComboBox(items);
		cmbGameType.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					GUIPluginInstance selPlugin = getSelectedPlugin();
					drawSelectedPluginView(selPlugin);
				}
			}
		});

		txfNumTest = new JTextField(5);
		// only numbers and at least number 0
		txfNumTest.setDocument(new PlainDocument() {
			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {

				String wholeText = getText(0, getLength()) + str;

				try {
					int value = new Integer(wholeText);
					if (value < 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					java.awt.Toolkit.getDefaultToolkit().beep();
					return;
				}

				super.insertString(offs, str, a);
			}
		});
		// must be set after setDocument()
		txfNumTest.setText(String.valueOf(GUIConfiguration.instance()
				.getNumTest())); // default

		final JLabel lblNumTest = new JLabel(lang
				.getProperty("dialog_test_lbl_numtest"));
		lblNumTest.setLabelFor(lblNumTest);

		ckbDebug = new JCheckBox(lang.getProperty("dialog_create_pref_debug"));
		ckbDebug.setToolTipText(lang
				.getProperty("dialog_create_pref_debug_hint"));

		pnlPref = new JPanel();
		pnlPref.add(cmbGameType);
		pnlPref.add(lblNumTest);
		pnlPref.add(txfNumTest);
		pnlPref.add(ckbDebug);
		// -----------------------------------------------------------
		statTable = new JTable(new MyTableModel());
		statTable.setRowHeight(25);
		// set single selection on one cell
		statTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statTable.setCellSelectionEnabled(false);
		// don't let the user change the columns' order or width
		statTable.getTableHeader().setReorderingAllowed(false);
		statTable.getTableHeader().setResizingAllowed(true);
		// -----------------------------------------------------------
		pnlTop = new JPanel();
		pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.PAGE_AXIS));
		// -----------------------------------------------------------
		GUIPluginInstance selPlugin = getSelectedPlugin();
		drawSelectedPluginView(selPlugin);

		// -----------------------------------------------------------

		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		progressBar.setStringPainted(true); // draw procent

		lblCenter = new JLabel(lang.getProperty("dialog_test_tbl_log"),
				JLabel.CENTER);
		Font font = new Font(lblCenter.getFont().getName(), lblCenter.getFont()
				.getStyle(), lblCenter.getFont().getSize() + 4);
		lblCenter.setFont(font);

		txtarea = new JTextArea();
		scrollTextArea = new JScrollPane(txtarea);
		scrollTextArea.setAutoscrolls(true);

		// -----------------------------------------------------------

		cb_showLog = new JCheckBox(lang.getProperty("dialog_test_cb_showLog"));
		cb_showLog.setSelected(GUIConfiguration.instance().showTestLog());
		cb_showLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIConfiguration.instance().setShowTestLog(
						cb_showLog.isSelected());
			}
		});
		JPanel pnl_showLogLeft = new JPanel(new GridLayout(1, 0));
		pnl_showLogLeft.add(cb_showLog);

		createButtonsAtBottom();

		createSaveReplayCheckboxGroup();
		// -------------------------------------------
		pnlBottomTop = new JPanel(new GridLayout());
		pnlBottomTop.add(pnl_showLogLeft);
		pnlBottomTop.add(pnl_saveReplay);

		pnlBottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlBottomRight.add(testStart);
		pnlBottomRight.add(testCancel);

		pnlBottom = new JPanel();
		setVerticalFlowLayout(pnlBottom);
		pnlBottom.add(pnlBottomTop);
		pnlBottom.add(new JSeparator());
		pnlBottom.add(pnlBottomRight);
		// -------------------------------------------
		pnlCenter = new JPanel();
		pnlCenter.setBorder(BorderFactory.createEtchedBorder());
		pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.PAGE_AXIS));
		pnlCenter.add(progressBar);
		pnlCenter.add(lblCenter);
		pnlCenter.add(scrollTextArea);

		// add components
		this.add(pnlTop, BorderLayout.PAGE_START);
		this.add(pnlCenter, BorderLayout.CENTER);
		this.add(pnlBottom, BorderLayout.PAGE_END);

		// set dialog preferences
		this.setTitle(lang.getProperty("dialog_test_title"));
		this.setIconImage(new ImageIcon(getClass().getResource(
				PresentationFacade.getInstance().getClientIcon())).getImage());
		this.setModal(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(650, 500));
		this.setMinimumSize(getPreferredSize());
		this.pack();
		this.setLocationRelativeTo(null);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelTestAndSave();
			}
		});
	}

	private void createSaveReplayCheckboxGroup() {
		pnl_saveReplay = new JPanel(new GridLayout());
		//setVerticalFlowLayout(pnl_saveReplay);

		JPanel pnl_saveReplayLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lbl_saveReplay = new JLabel(lang
				.getProperty("dialog_test_lbl_saveReplay"));
		pnl_saveReplayLeft.add(lbl_saveReplay);

		JPanel pnl_ckbGroup = new JPanel(new GridLayout(3, 0));
		pnl_ckbGroup.setBorder(BorderFactory.createLineBorder(Color.black));

		final JCheckBox ckb_errorGames = new JCheckBox(lang
				.getProperty("dialog_test_ckb_error"));
		ckb_errorGames.setSelected(GUIConfiguration.instance().saveErrorGames());
		ckb_errorGames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIConfiguration.instance().setSaveErrorGames(
						ckb_errorGames.isSelected());
			}
		});
		final JCheckBox ckb_lostGames = new JCheckBox(lang
				.getProperty("dialog_test_ckb_lost"));
		ckb_lostGames.setSelected(GUIConfiguration.instance().saveLostGames());
		ckb_lostGames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIConfiguration.instance().setSaveLostGames(
						ckb_lostGames.isSelected());
			}
		});
		final JCheckBox ckb_wonGames = new JCheckBox(lang
				.getProperty("dialog_test_ckb_won"));
		ckb_wonGames.setSelected(GUIConfiguration.instance().saveWonGames());
		ckb_wonGames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIConfiguration.instance().setSaveWonGames(
						ckb_wonGames.isSelected());
			}
		});

		pnl_ckbGroup.add(ckb_errorGames);
		pnl_ckbGroup.add(ckb_lostGames);
		pnl_ckbGroup.add(ckb_wonGames);

		pnl_saveReplay.add(pnl_saveReplayLeft);
		pnl_saveReplay.add(pnl_ckbGroup);
	}

	private void setVerticalFlowLayout(Container target) {
		target.setLayout(new BoxLayout(target, BoxLayout.PAGE_AXIS));
	}

	/**
	 * Creates the test and cancel button.
	 */
	private void createButtonsAtBottom() {
		testStart = new JButton(lang.getProperty("dialog_test_btn_start"));
		testStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isTesting()) { // testing
					cancelTest(lang.getProperty("dialog_test_msg_cancel"));
				} else {
					if (prepareTest()) {
						updateGUI(false);
						// first game with first player at the first position
						startNewTest();
					}
				}
			}
		});

		testCancel = new JButton(lang.getProperty("dialog_test_btn_cancel"));
		testCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelTestAndSave();
				TestRangeDialog.this.setVisible(false);
				TestRangeDialog.this.dispose();
			}
		});
	}

	/**
	 * Returns true if a test is running, otherwise false.
	 *
	 * @return
	 */
	private boolean isTesting() {
		return curTest > 0;
	}

	/**
	 * Updates the start/stop button and the text area.
	 *
	 * @param endOfTest
	 *            true if it should reset the gui, otherwise false.
	 */
	protected void updateGUI(boolean endOfTest) {
		if (endOfTest) {
			testStart.setText(lang.getProperty("dialog_test_btn_restart"));
			cmbGameType.setEnabled(true);
		} else {
			testStart.setText(lang.getProperty("dialog_test_btn_stop"));
			cmbGameType.setEnabled(false);
			txtarea.setText("");
		}
		TestRangeDialog.this.validate();
	}

	/**
	 * Cancels the test if active and saves the number of tests.<br>
	 * <i>Invoked before closing the dialog.</i>
	 */
	protected void cancelTestAndSave() {
		cancelTest("");
		try {
			GUIConfiguration.instance().setNumberOfTests(
					new Integer(txfNumTest.getText()));
		} catch (NumberFormatException ex) {
			// don't save the invalid value
		}
	}

	/**
	 * According to the Checkbox's index, which selects a specific plugin, this
	 * dialog is painted.
	 *
	 * @param selPlugin
	 */
	protected void drawSelectedPluginView(GUIPluginInstance selPlugin) {

		// remove old rows
		DefaultTableModel model = (DefaultTableModel) statTable.getModel();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}

		// remove columns
		model.setColumnCount(0);

		int prefSize = 0;

		// add columns
		model.addColumn(lang.getProperty("dialog_test_stats_pos"));
		model.addColumn(lang.getProperty("dialog_test_stats_name"));
		ScoreDefinition statColumns = selPlugin.getPlugin()
				.getScoreDefinition();
		for (int i = 0; i < statColumns.size(); i++) {
			ScoreFragment column = statColumns.get(i);
			String columnTitle = column.getName();
			if (column.getAggregation() == ScoreAggregation.AVERAGE) {
				columnTitle = "\u00D8 " + columnTitle;
			}
			model.addColumn(columnTitle);
		}
		model.addColumn(lang.getProperty("dialog_test_stats_invalid"));
		statTable.getColumnModel().getColumn(statTable.getColumnCount() - 1).setMaxWidth(150);
		model.addColumn(lang.getProperty("dialog_test_stats_crashed"));
		statTable.getColumnModel().getColumn(statTable.getColumnCount() - 1).setMaxWidth(150);

		setTableHeaderRenderer(statTable);

		/*
		 * set minimum and maximum width for each column to enable correct
		 * resizablity
		 */
		statTable.getColumnModel().getColumn(0).setCellRenderer(
				new CenteredTableCellRenderer());
		statTable.getColumnModel().getColumn(0).setMinWidth(0);
		statTable.getColumnModel().getColumn(0).setMaxWidth(100);

		statTable.getColumnModel().getColumn(1).setMaxWidth(300);

		for (int i = 0; i < statTable.getColumnCount() - 2; i++) {
			int index = i + 2;
			statTable.getColumnModel().getColumn(index).setMinWidth(10);
			//statTable.getColumnModel().getColumn(index).setMaxWidth(100);
		}

		// set width of columns
		statTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		statTable.getColumnModel().getColumn(1).setPreferredWidth(170);
		prefSize += 200;
		for (int i = 0; i < statTable.getColumnCount() - 2; i++) {
			if (i < statColumns.size()) {
				ScoreFragment column = statColumns.get(i);
				statTable.getColumnModel().getColumn(i + 2).setPreferredWidth(column.getName().length() * 10);
				prefSize += column.getName().length() * 10;
			} else {
				statTable.getColumnModel().getColumn(i + 2).setPreferredWidth(80);
				prefSize += 80;
			}
		}

		// -------------------------------------------------------------

		setPlayerRows(selPlugin);

		// show table without extra space
		//statTable.setPreferredScrollableViewportSize(statTable
		//		.getPreferredSize());
		Dimension prefDim = statTable.getPreferredSize();
		prefDim.width = prefSize;
		statTable.setPreferredSize(prefDim);
		statTable.setPreferredScrollableViewportSize(prefDim);

		// display
		pnlTop.removeAll();
		pnlTop.add(pnlPref);
		for (int i = 0; i < txfclient.length; i++) {
			pnlTop.add(pnlclient[i]);
		}
		for (int i = 0; i < txfparams.length; i++) {
			pnlTop.add(pnlclient[i]);
		}
		JScrollPane statScrollPane = new JScrollPane(statTable);
		statScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		statScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnlTop.add(statScrollPane);
		pnlTop.validate();
		// pnlTop.invalidate();// TODO order?

		System.out.println("UPDATE: test range dialog");
	}

	/**
	 * Adds the necessary input components for each player.
	 *
	 * @param selPlugin
	 */
	private void setPlayerRows(GUIPluginInstance selPlugin) {
		int ki_count = selPlugin.getPlugin().getMinimalPlayerCount();

		txfclient = new JTextField[ki_count];
		txfparams = new JTextField[ki_count];
		lblclient = new JLabel[ki_count];
		btnclient = new JButton[ki_count];
		pnlclient = new JPanel[ki_count];

		// add new text fields, labels and rows
		for (int i = 0; i < ki_count; i++) {
			txfclient[i] = new JTextField(20);
			txfparams[i] = new JTextField(10);
			final JTextField txfClient = txfclient[i];

			String playerNumber = String.valueOf(i + 1);
			lblclient[i] = new JLabel(lang.getProperty("dialog_test_lbl_ki")
					+ " " + playerNumber);
			lblclient[i].setLabelFor(txfclient[i]);

			btnclient[i] = new JButton(lang.getProperty("dialog_test_btn_file"));
			btnclient[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					loadClient(txfClient);
				}
			});

			pnlclient[i] = new JPanel();
			pnlclient[i].add(lblclient[i]);
			pnlclient[i].add(txfclient[i]);
			pnlclient[i].add(btnclient[i]);
			pnlclient[i].add(new JLabel("Parameter:"));
			pnlclient[i].add(txfparams[i]);
			// ------------------------------------------------
			Vector<String> rowData = new Vector<String>(); // default
			rowData.add(playerNumber); // set position#
			((DefaultTableModel) statTable.getModel()).addRow(rowData);
		}
	}

	/**
	 * Sets the specific table header renderer.
	 *
	 * @param table
	 */
	private void setTableHeaderRenderer(JTable table) {
		final JTableHeader header = table.getTableHeader();
		final TableCellRenderer headerRenderer = header.getDefaultRenderer();

		header.setDefaultRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {

				JLabel c = (JLabel) headerRenderer
						.getTableCellRendererComponent(table, value,
								isSelected, hasFocus, row, column);

				if (column == 0) {
					c.setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					c.setHorizontalAlignment(SwingConstants.LEADING);
				}

				return c;
			}
		});
	}

	/**
	 * Prepares the test range.
	 */
	private boolean prepareTest() {

		testStarted = true;
		if (presFac.getLogicFacade().isGameActive()) {
			presFac.getContextDisplay().cancelCurrentGame();
		}

		IGuiPlugin selPlugin = getSelectedPlugin().getPlugin();

		// on-demand number checking -> here: no checking required
		numTest = new Integer(txfNumTest.getText());

		progressBar.setMaximum(numTest);
		progressBar.setValue(0);

		absoluteValues = new LinkedList<List<BigDecimal>>();

		for (JTextField element : txfclient) {
			absoluteValues.add(new LinkedList<BigDecimal>());
			File file = new File(element.getText());
			if (!file.exists()) {
				JOptionPane.showMessageDialog(this, lang
						.getProperty("dialog_test_error_path_msg"), lang
						.getProperty("dialog_test_error_path_title"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		// display the clients' positions and names
		MyTableModel model = (MyTableModel) statTable.getModel();
		for (int i = 0; i < txfclient.length; i++) {
			model.setValueAt(new Integer(i + 1), i, 0);
			String name = new File(txfclient[i].getText()).getName();
			// without file ext and with a number
			name = HelperMethods.getFilenameWithoutFileExt(name) + " "
					+ (i + 1);
			model.setValueAt(name, i, 1);
			for (int j = 0; j < selPlugin.getScoreDefinition().size() + 2; j++) {
				model.setValueAt(BigDecimal.ZERO, i, 2 + j); // set default 0
			}
		}
		statTable.validate();

		// start server
		boolean portInUse;
		final int START_PORT = SharedConfiguration.DEFAULT_PORT;
		this.freePort = START_PORT;
		do {
			portInUse = false;
			freePort++;
			try {
				presFac.getLogicFacade().startServer(freePort);
			} catch (BindException e) { // port in use
				if (freePort > START_PORT + 10) {
					if (JOptionPane
							.showConfirmDialog(
									null,
									lang
											.getProperty("dialog_test_error_retryPort_msg"),
									lang
											.getProperty("dialog_test_error_retryPort_title"),
									JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						return false;
					}
				}
				portInUse = true;
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), lang
						.getProperty("dialog_error_title"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} while (portInUse);

		// disable rendering
		selPlugin.setRenderContext(null);

		return true;
	}

	/**
	 * Starts a prepared test range.
	 *
	 * @param ascending
	 */
	protected void startNewTest() {

		final ConnectingDialog connectionDialog = new ConnectingDialog(this);

		curTest++;

		final int rotation = getRotation(txfclient.length);

		final List<SlotDescriptor> slotDescriptors = prepareSlots(
				preparePlayerNames(), rotation);
		List<KIInformation> KIs = null;

		try {
			final IGamePreparation prep = prepareGame(getSelectedPlugin(),
					slotDescriptors);

			addObsListeners(rotation, slotDescriptors, prep, connectionDialog);

			// only display message after the first round
			if (curTest > 1) {
				addLogMessage(">>> " + lang.getProperty("dialog_test_switch"));
			}

			KIs = prepareClientProcesses(slotDescriptors, prep, rotation);
		} catch (IOException e) {
			e.printStackTrace();
			cancelTest(lang.getProperty("dialog_test_msg_prepare"));
			return;
		}

		try {
			runClientProcesses(KIs);
		} catch (IOException e) {
			e.printStackTrace();
			cancelTest(lang.getProperty("dialog_test_msg_run"));
			return;
		}

		// show connecting dialog
		if (this.isActive()) {
			if (connectionDialog.showDialog() == JOptionPane.CANCEL_OPTION) {
				cancelTest(lang.getProperty("dialog_test_msg_cancel"));
			}
		}
	}

	private int getRotation(int playerCount) {
		return (curTest + 1) % playerCount;
	}

	private List<KIInformation> prepareClientProcesses(
			final List<SlotDescriptor> descriptors,
			final IGamePreparation prep, final int rotation) {
		final List<ISlot> slots = new ArrayList<ISlot>(prep.getSlots());
		final List<String> paths = prepareProcessPaths(rotation);

		final List<KIInformation> KIs = new ArrayList<KIInformation>();
		for (int i = 0; i < slots.size(); i++) {
			ISlot slot = slots.get(i);
			String path = paths.get(i);

			String[] slotParams = slot.asClient();
			// FIXME: refactoring: erst alle informationen sammel und dann nur
			// noch die infor d urchrotieren statt alles immer mit neuem
			// rotationsindex neu zu erstellen
			String paramLine = txfparams[(rotation + i) % slots.size()]
					.getText();
			paramLine = paramLine == null ? "" : paramLine;
			String[] lineParams = paramLine.split(" ");
			String[] params = new String[slotParams.length + lineParams.length];
			for (int j = 0; j < slotParams.length; j++) {
				params[j] = slotParams[j];
			}
			for (int j = 0; j < lineParams.length; j++) {
				params[j + slotParams.length] = lineParams[j];
			}

			KIs.add(new KIInformation(params, path));

			addLogMessage(descriptors.get(i).getDisplayName() + " "
					+ lang.getProperty("dialog_test_switchpos") + " " + (i + 1));
		}
		return KIs;
	}

	private List<String> prepareProcessPaths(int rotation) {
		final List<String> paths = new LinkedList<String>();

		for (int i = 0; i < txfclient.length; i++) {
			paths.add(txfclient[i].getText());
		}

		Collections.rotate(paths, rotation);

		return paths;
	}

	private void runClientProcesses(final List<KIInformation> KIs)
			throws IOException {
		if (KIs == null) {
			throw new IllegalArgumentException(
					"Parameter 'KIs' may not be null");
		}

		// start KI (intern) clients
		for (int i = 0; i < KIs.size(); i++) {
			KIInformation kinfo = KIs.get(i);

			String file = kinfo.getPath();
			String[] params = kinfo.getParameters();

			try {
				HelperMethods.exec(file, params);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, lang
						.getProperty("dialog_test_error_client_msg"), lang
						.getProperty("dialog_test_error_client_title"),
						JOptionPane.ERROR_MESSAGE);
				throw new IOException(e);
			} catch (UnsupportedFileExtensionException e) {
				JOptionPane.showMessageDialog(this, lang
						.getProperty("dialog_error_fileext_msg"), lang
						.getProperty("dialog_error_fileext_msg"),
						JOptionPane.ERROR_MESSAGE);
				throw new IOException(e);
			}
		}
	}

	private void addObsListeners(final int rotation,
			final List<SlotDescriptor> slotDescriptors,
			final IGamePreparation prep, final ConnectingDialog connectionDialog) {
		// get observer

		final GUIPluginInstance plugin = getSelectedPlugin();
		final List<SlotDescriptor> descriptors = slotDescriptors;
		obs = prep.getObserver();
		obs.addGameEndedListener(new IGameEndedListener() {
			@Override
			public void onGameEnded(GameResult result, String gameResultString) {
				if (null == result) // happens after a game has been canceled
					return;

				addLogMessage(lang.getProperty("dialog_test_end") + " "
						+ curTest + "/" + numTest);
				addLogMessage(gameResultString); // game over information
				// purpose
				updateStatistics(rotation, result);
				// update progress bar
				progressBar.setValue(progressBar.getValue() + 1);

				// create replay file name
				String replayFilename = null;
				Collections.rotate(descriptors, -rotation); // Undo rotation
				boolean winner = false;
				for (IPlayer player : result.getWinners()) {
					if (player.getDisplayName().equals(descriptors.get(0).getDisplayName())) {
						winner = true;
					}
				}
				// Draw counts as win
				if (result.getWinners().size() == 0) {
					winner = true;
				}
				boolean saveReplay = false;
				if (!result.isRegular()
						&& GUIConfiguration.instance().saveErrorGames()) {
					saveReplay = true;
				} else if (result.isRegular()) {
					if (GUIConfiguration.instance().saveWonGames() && winner) {
						saveReplay = true;
					}
					if (GUIConfiguration.instance().saveLostGames() && !winner) {
						saveReplay = true;
					}
				}
				if (saveReplay) {
					replayFilename = HelperMethods.generateReplayFilename(plugin, slotDescriptors);
					try {
						obs.saveReplayToFile(replayFilename);
						addLogMessage(lang
								.getProperty("dialog_test_log_replay"));
					} catch (IOException e) {
						e.printStackTrace();
						addLogMessage(lang
								.getProperty("dialog_test_log_replay_error"));
					}
				}

				// start new test if number of tests is not still reached
				if (curTest < numTest) {
					// FIXME: Perhaps "Recursive" Execution of Tests might be
					// REALLY bad
					// for big N
					new Thread(new Runnable() {
						@Override
						public void run() {
							startNewTest();
						}
					}).start();
				} else {
					finishTest();
				}
			}
		});
		obs.addNewTurnListener(new INewTurnListener() {
			@Override
			public void newTurn(int playerid, String info) {
				String clientName = slotDescriptors.get(playerid)
						.getDisplayName();
				// add log
				if (!info.equals("")) {
					addLogMessage(clientName + ": " + info);
				}
			}
		});
		obs.addReadyListener(new IReadyListener() {
			@Override
			public void ready() {
				connectionDialog.close();
				obs.start();
			}
		});
	}

	private List<SlotDescriptor> prepareSlots(final List<String> playerNames,
			int rotation) {
		final List<SlotDescriptor> descriptors = new LinkedList<SlotDescriptor>();

		for (String playerName : playerNames) {
			descriptors.add(new SlotDescriptor(playerName, !ckbDebug
					.isSelected()));
		}

		Collections.rotate(descriptors, rotation);

		return descriptors;
	}

	private List<String> preparePlayerNames() {
		// get player names
		final List<String> playerNames = new LinkedList<String>();

		for (int i = 0; i < txfclient.length; i++) {
			String path = txfclient[i].getText();
			String clientName = HelperMethods
					.getFilenameWithoutFileExt(new File(path).getName())
					+ " " + (i + 1);
			playerNames.add(clientName);
		}

		return playerNames;
	}

	/**
	 * Prepares a new game with the given parameters.
	 *
	 * @param selPlugin
	 * @param descriptors
	 * @return
	 * @throws IOException
	 *             (also displays an error message)
	 */
	private IGamePreparation prepareGame(GUIPluginInstance selPlugin,
			final List<SlotDescriptor> descriptors) throws IOException {
		try {
			IGamePreparation prep = selPlugin.getPlugin()
					.prepareBackgroundGame(
							DEFAULT_HOST,
							freePort,
							descriptors.toArray(new SlotDescriptor[descriptors
									.size()]));
			return prep;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, lang
					.getProperty("dialog_test_error_network_msg"), lang
					.getProperty("dialog_test_error_network_title"),
					JOptionPane.ERROR_MESSAGE);
			throw e;
		}
	}

	/**
	 * Updates the statistics table
	 *
	 * @param rotation
	 * @param result
	 */
	protected void updateStatistics(final int rotation, final GameResult result) {
		MyTableModel model = (MyTableModel) statTable.getModel();

		// display wins/losses etc.
		for (int i = 0; i < result.getScores().size(); i++) {
			int statRow = Math.abs(rotation - i);
			PlayerScore curPlayer = result.getScores().get(i);

			List<BigDecimal> stats = curPlayer.getValues();
			for (int j = 0; j < stats.size(); j++) {
				List<BigDecimal> absVals = absoluteValues.get(statRow);
				BigDecimal newStat = stats.get(j);
				if (absVals.size() <= j) {
					absVals.add(new BigDecimal(0));
				}
				//BigDecimal old = (BigDecimal) model.getValueAt(statRow, j + 2);
				BigDecimal abs = absVals.get(j);

				ScoreAggregation action = result.getDefinition().get(j)
						.getAggregation();
				switch (action) {
				case SUM:
					newStat = abs.add(newStat);
					absVals.set(j, newStat);
					break;
				case AVERAGE:
					// restore old absolute value
					//old = old.multiply(BigDecimal.valueOf(curTest - 1));
					// add newStat to absolute value
					newStat = abs.add(newStat);
					absVals.set(j, newStat);
					// divide with curTest (rounded down)
					newStat = newStat.divideToIntegralValue(BigDecimal
							.valueOf(curTest));
					break;
				default:
					throw new RuntimeException("Unknown aggregation type ("
							+ action + ")");
				}
				// set to model
				model.setValueAt(newStat, statRow, j + 2);
			}

			// display invalid, crashed
			switch (curPlayer.getCause()) {
			case REGULAR:
				break;
			case SOFT_TIMEOUT:
			case RULE_VIOLATION:
				int invalidCol = model.getColumnCount() - 2;
				BigDecimal oldValue = (BigDecimal) model.getValueAt(statRow,
						invalidCol);
				model.setValueAt(oldValue.add(BigDecimal.ONE), statRow,
						invalidCol);
				break;
			case HARD_TIMEOUT:
			case LEFT:
				int crashedCol = model.getColumnCount() - 1;
				oldValue = (BigDecimal) model.getValueAt(statRow, crashedCol);
				model.setValueAt(oldValue.add(BigDecimal.ONE), statRow,
						crashedCol);
				break;
			case UNKNOWN:
				final String player = (String) model.getValueAt(statRow, 1);
				addLogMessage(player + " "
						+ lang.getProperty("dialog_test_gamecause_unknown"));
				break;
			default:
				throw new RuntimeException(
						"Unknown or unimplemented game cause.");
			}
		}
	}

	/**
	 * Cancels the active test range.
	 */
	private void cancelTest(final String err_msg) {
		if (null != obs && !obs.isFinished()) {
			obs.cancel();
		}
		finishTest();
		addLogMessage(err_msg);
	}

	/**
	 * Stops the server and reset the button's status.
	 */
	private void finishTest() {
		if (testStarted) {
			stopServer();
		}
		updateGUI(true);
		initCurTest();
	}

	/**
	 * Initializes curTest to 0.
	 */
	private void initCurTest() {
		curTest = 0;
	}

	/**
	 * Loads a client, i.e. opens a file choose dialog
	 *
	 * @param txf
	 */
	private void loadClient(JTextField txf) {
		final JFileChooser chooser = new JFileChooser(GUIConfiguration
				.instance().getTestDialogPath());
		chooser.setDialogTitle(lang.getProperty("dialog_test_dialog_title"));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			txf.setText(f.getAbsolutePath());
			GUIConfiguration.instance().setTestDialogPath(f.getParent());
		}
	}

	private GUIPluginInstance getSelectedPlugin() {
		return plugins.get(cmbGameType.getSelectedIndex());
	}

	/**
	 * Closes the server.
	 */
	private void stopServer() {
		presFac.getLogicFacade().stopServer();
	}

	/**
	 * Non-editable table model.
	 *
	 * @author chw
	 *
	 */
	private class MyTableModel extends DefaultTableModel {

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}

	/**
	 * Adds the given <code>msg</code> to the log text area.
	 *
	 * @param msg
	 */
	private void addLogMessage(final String msg) {
		if (cb_showLog.isSelected()) {
			txtarea.append(msg + "\n");
			txtarea.setCaretPosition(txtarea.getText().length());
		}
	}
}