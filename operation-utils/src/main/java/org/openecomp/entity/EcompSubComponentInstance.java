
/*-
 * ============LICENSE_START==========================================
 * OPENECOMP - DCAE
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */
	
package org.openecomp.entity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EcompSubComponentInstance {
	private static final Logger logger = Logger.getLogger(EcompSubComponentInstance.class.getName());

	static String uuid = "???";
	public static String getServerIP() {
		initialize();
		return serverIP;
	}

	public static String getServerName() {
		initialize();
		return serverName;
	}

	private static String serverIP, serverName;

	public static String getUuid() {
		return uuid;
	}

	public static void initialize() {
		if (serverIP == null || serverName == null || ("").equals(serverIP) || ("").equals(serverName)) {
			try {
				InetAddress server = InetAddress.getLocalHost();
				serverIP = server.getHostAddress();
				serverName = server.getCanonicalHostName();
			} catch (UnknownHostException e) {
				logger.log(Level.SEVERE, "Could not get local hostname", e);
				serverIP = "";
				serverName = "";
			}
		}
	}
}
