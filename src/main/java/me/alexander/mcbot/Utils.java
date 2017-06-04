package me.alexander.mcbot;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Utils {

	public static void initLogger() {
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.DEBUG);
		PatternLayout layout = new PatternLayout("%d{HH:mm:ss} => { %m } %n");
		rootLogger.addAppender(new ConsoleAppender(layout));
	}

}
