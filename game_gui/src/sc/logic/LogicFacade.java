package sc.logic;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.SoftwareChallengeGUI;
import sc.common.CouldNotFindAnyLanguageFileException;
import sc.common.CouldNotFindAnyPluginException;
import sc.gui.stuff.YearComparator;
import sc.guiplugin.interfaces.IGuiPluginHost;
import sc.guiplugin.interfaces.IObservation;
import sc.logic.save.GUIConfiguration;
import sc.logic.save.GUIConfiguration.ELanguage;
import sc.plugin.GUIPluginInstance;
import sc.plugin.GUIPluginManager;
import sc.server.Application;
import sc.server.Lobby;

public class LogicFacade {

	private static final Logger	logger = LoggerFactory
			.getLogger(SoftwareChallengeGUI.class);
	/**
	 * Folder of all language files
	 */
	private static final String BASENAME = "/resource/game_gui";

	private static final Comparator<? super GUIPluginInstance> yearComparator = new YearComparator();
	/**
	 * Holds all vailable plugins
	 */
	private final GUIPluginManager pluginMan;
	/**
	 * For multi-language support
	 */
	private Properties languageData;
	private IObservation observation;
	private Lobby server;
	/**
	 * true if a game is currently being played, false otherwise
	 */
	private boolean gameActive;

	/**
	 * Singleton instance
	 */
	private static volatile LogicFacade instance;

	private LogicFacade() { // Singleton
		this.pluginMan = GUIPluginManager.getInstance();
	}

	public static LogicFacade getInstance() {
		if (null == instance) {
			synchronized (LogicFacade.class) {
				if (null == instance) {
					instance = new LogicFacade();
				}
			}
		}
		return instance;
	}

	public void loadLanguageData() throws CouldNotFindAnyLanguageFileException {
		ELanguage language = GUIConfiguration.instance().getLanguage();
		Locale locale;
		switch (language) {
		case DE:
			locale = new Locale("de", "DE");
			break;
		case EN:
			locale = new Locale("en", "EN");
			break;
		default:
			throw new CouldNotFindAnyLanguageFileException();
		}

		this.languageData = new Properties();
		String fileName = BASENAME + "_" + locale.getLanguage() + "_"
				+ locale.getCountry() + ".properties";
		try {
			this.languageData.load(getClass().getResourceAsStream(fileName));
		} catch (Exception e) {
			logger.error("Failed to read {}", fileName);
			//System.err.println("Failed to read " + fileName);
			e.printStackTrace();
			throw new CouldNotFindAnyLanguageFileException();
		}
	}

	public GUIPluginManager getPluginManager() {
		return pluginMan;
	}

	public void loadPlugins() throws CouldNotFindAnyPluginException {
		this.pluginMan.reload();
		if (this.pluginMan.getAvailablePlugins().size() == 0) {
			throw new CouldNotFindAnyPluginException();
		}
		this.pluginMan.activateAllPlugins(new IGuiPluginHost() {

		});
	}

	public Properties getLanguageData() {
		return languageData;
	}

	public GUIConfiguration getConfiguration() {
		return GUIConfiguration.instance();
	}

	public IObservation getObservation() {
		return observation;
	}

	public void setObservation(IObservation observer) {
		this.observation = observer;
	}

	/**
	 * Stops and starts the server.
	 * 
	 * @param port
	 * @throws IOException
	 */
	public void startServer(int port) throws IOException {
		if (null != server) {
			this.stopServer();
		}
		server = Application.startServer(port);
		logger.debug("Server started on {}",port);
	}

	/**
	 * Cancels the observer and closes the server.
	 */
	public void stopServer() {
		if (server != null) {
			server.close();
			server = null;
		}
		logger.debug("Server stopped.");
	}

	public void unloadPlugins() {
		pluginMan.reload();
	}

	/**
	 * Returns available plugins in sorted order.
	 * 
	 * @return plugins
	 */
	public List<GUIPluginInstance> getAvailablePluginsSorted() {
		Collection<GUIPluginInstance> plugins = pluginMan.getAvailablePlugins();
		// sort by plugin's year
		List<GUIPluginInstance> sortedPlugins = new LinkedList<GUIPluginInstance>(plugins);
		Collections.sort(sortedPlugins, yearComparator);
		return sortedPlugins;
	}

	/**
	 * Returns all available plugin names in a sorted order.
	 * 
	 * @param plugins
	 * @return
	 */
	public Vector<String> getPluginNames(List<GUIPluginInstance> plugins) {
		Vector<String> result = new Vector<String>();
		int last = 0;
		for (int i = 0; i < plugins.size(); i++) {
			GUIPluginInstance pluginInstance = plugins.get(i);
			if (pluginInstance.getPlugin().getPluginYear() > last) {
				logger.debug("Show plugin: {}",pluginInstance.getDescription().name());
				result.add(pluginInstance.getDescription().name());
			}
		}

		return result;
	}

	public boolean isGameActive() {
		return gameActive;
	}

	public void setGameActive(boolean b) {
		this.gameActive = b;
	}

}
