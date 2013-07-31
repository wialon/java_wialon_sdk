package com.wialon.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Debug {
	public static Logger log;
	static {
		log=Logger.getLogger("DEBUG");
		log.setLevel(Level.OFF);
	}
}
