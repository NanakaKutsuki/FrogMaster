package org.kutsuki.frogmaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrogMaster {
	private final Logger logger = LoggerFactory.getLogger(FrogMaster.class);
	
	public void run() {
		logger.info("hey there!");
	}
	
	public static void main(String[] args) {
		FrogMaster frogMaster = new FrogMaster();
		frogMaster.run();
	}
}
