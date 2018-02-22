package sk.accerek.hamlet.desktop;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.Platform;

import javax.swing.*;
import java.awt.*;

@RequiredArgsConstructor
public class DesktopLauncher implements Platform {
	public static void main (String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("d").longOpt("debug").desc("Enable debug logging").build());
        options.addOption(Option.builder("h").longOpt("help").desc("Show a list of arguments").build());
        options.addOption(Option.builder("nfpst").longOpt("no-fps-throttle").desc("Disable FPS-throttling").build());

        CommandLineParser parser = new DefaultParser();

        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
            if(commandLine.hasOption("help")) {
                new HelpFormatter().printHelp("hamlet", "Hamlet: The Game", options, "", true);
                System.exit(0);
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

	    try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
	        e.printStackTrace();
        }

        boolean isDebugEnabled = true;
        boolean isFpsCapped = true;

		if(System.getProperty("java.class.path").contains("idea_rt.jar") || (commandLine != null && commandLine.hasOption("debug"))) {
			ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			root.setLevel(Level.DEBUG);
			isDebugEnabled = true;
		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.allowSoftwareMode = true;
		config.width = (int) Hamlet.WINDOW_SIZE.x;
		config.height = (int) Hamlet.WINDOW_SIZE.y;
		config.title = "Hamlet";
		//config.resizable = false;

		config.addIcon("icon/icon_512.png", Files.FileType.Internal);
        config.addIcon("icon/icon_128.png", Files.FileType.Internal);
        config.addIcon("icon/icon_32.png", Files.FileType.Internal);
        config.addIcon("icon/icon_16.png", Files.FileType.Internal);

		if(commandLine != null && commandLine.hasOption("no-fps-throttle")) {
            config.vSyncEnabled = false;
            config.foregroundFPS = 0;
            config.backgroundFPS = 0;
            isFpsCapped = false;
        } else {
		    config.vSyncEnabled = true;
        }

		new LwjglApplication(Hamlet.initialise(new DesktopLauncher(isFpsCapped, isDebugEnabled)), config);
	}

	private final boolean isFpsCapped;
	private final boolean isDebugEnabled;

    @Override
    public void nativeUpdate() {

    }

    @Override
    public void showNativeDialog(String title, String message, int type) {
        new Thread(() -> JOptionPane.showMessageDialog(null, message, title, type)).start();
    }

    @Override
    public boolean isFpsCapped() {
        return isFpsCapped;
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }
}
