package sc.plugin2012.gui.renderer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class RenderConfiguration {

	public static final int ANTIALIASING = 0;
	public static final int TRANSPARANCY = 1;
	public static final int BACKGROUND = 2;
	public static final int MOVEMENT = 3;
	public static final int CRANES = 4;
	public static final int DEBUG_VIEW = 5;

	public static final String[] OPTION_NAMES = new String[] { "Kantenglättung", "Transparenz",
			"Hintergrundbild", "Animationen", "Baukräne", "Debugansicht" };

	private static final boolean[] DEFAULTS = new boolean[] { true, true, true, true, false, false };

	public static final boolean[] OPTIONS = DEFAULTS.clone();

	public static void saveSettings() {

		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (int i = 0; i < OPTIONS.length; i++) {
			map.put(OPTION_NAMES[i], new Boolean(OPTIONS[i]));
		}

		OutputStream fileStream = null;
		try {
			fileStream = new FileOutputStream("manhattan_gui.conf");
			ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(map);
			objectStream.flush();
		} catch (IOException e) {
		} finally {
			try {
				if (fileStream != null) {
					fileStream.close();
				}
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void loadSettings() {

		InputStream fileStream = null;

		try {
			fileStream = new FileInputStream("manhattan_gui.conf");
			ObjectInputStream objectStream = new ObjectInputStream(fileStream);
			HashMap<String, Boolean> map = (HashMap<String, Boolean>) objectStream.readObject();

			for (int i = 0; i < OPTIONS.length; i++) {
				Boolean option = map.get(OPTION_NAMES[i]);
				OPTIONS[i] = option == null ? DEFAULTS[i] : option;
			}

		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		} finally {
			try {
				if (fileStream != null) {
					fileStream.close();
				}
			} catch (Exception e) {
			}
		}
	}

}
