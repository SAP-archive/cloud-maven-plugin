/*
 * Copyright 2012 SAP AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sap.research.maven.plugin.nwcloud;

import java.util.List;

import org.apache.maven.settings.Server;

/**
 * This class is used by NWCloudGetPwd to
 *  - store attributes to match against + the list of servers with decrypted passwords,
 *  - do the actual matching to find the server which matches best to the given attributes.
 */
public class NWCloudGetPwdToMatch {

	// --- Attributes ---

	/**
	 * Triple of attributes "host", "account", "user" that should be matched.
	 */
	private NWCloudGetPwdToMatchData attribsToMatch = null;
	
	/**
	 * List of servers that should be matched against the defined triple.
	 */
	private List<Server> serverList = null;

	// --- Getters/Setters ---

	/**
	 * @return the attribsToMatch
	 */
	public NWCloudGetPwdToMatchData getAttribsToMatch() {
		return attribsToMatch;
	}

	/**
	 * @param attribsToMatch the attribsToMatch to set
	 */
	public void setAttribsToMatch(NWCloudGetPwdToMatchData attribsToMatch) {
		this.attribsToMatch = attribsToMatch;
	}

	/**
	 * @return the serverList
	 */
	public List<Server> getServerList() {
		return serverList;
	}

	/**
	 * @param serverList the serverList to set
	 */
	public void setServerList(List<Server> serverList) {
		this.serverList = serverList;
	}

	// --- Methods/Functions ---

	/**
	 * Dump list of servers to given logger.
	 * @param log org.apache.maven.plugin.logging.Log logger to use for dumping.
	 */
	public void dumpServerList(org.apache.maven.plugin.logging.Log log) {
	    if (this.getServerList()!=null) {
	    	for (Server server : this.getServerList()) {
	    		log.info(" - Server ID '"+server.getId()+"'");
	    		if (server.getPassword()!=null) {
	    			log.info("    - Password is defined (but not shown)");
	    		} else {
	    			log.info("    - Password not defined");
	    		}
	    		log.info("    - Config '"+server.getConfiguration().toString()+"'");
	    	}
	    } else {
	    	log.info("Returned list of servers was null.");
	    }
	}

	/**
	 * Search for best matching server (with a password) in the list and return the password.
	 * If no match of a server with a defined password is found, null will be returned.
	 * @param log org.apache.maven.plugin.logging.Log logger to use for status output.
	 * @return decrypted password of best matching server, or null if none was found. 
	 */
	public String match(NWCloudGetPwd parent) {
		
		String result = null;
		
	    if (this.getServerList()!=null) {	    	
	    	if (!this.getServerList().isEmpty()) {

		    	int maxMatches = 0;
		    	for (Server server : this.getServerList()) {
		    		
		    		// We are only interested in servers that have a password and a configuration
		    		if ((server.getPassword()!=null) && (server.getConfiguration()!=null)) {
	
			    		NWCloudGetPwdToMatchData serverData = new NWCloudGetPwdToMatchData();
			    		serverData.setFromServer(server);
		    			
		    			if (!parent.isInternal()) {
				    		parent.getLog().info(" - Server ID '"+server.getId()+"'");
				    		parent.getLog().info("    - Password is defined (but not shown)");
				    		parent.getLog().info("    - Config:");
				    		parent.getLog().info("       - host    '"+serverData.getHost()+"'");
				    		parent.getLog().info("       - account '"+serverData.getAccount()+"'");
				    		parent.getLog().info("       - user    '"+serverData.getUser()+"'");	    				
		    			}
			    		
			    		int thisMatches = this.getAttribsToMatch().numOfMatches(serverData);
			    		if (!parent.isInternal()) parent.getLog().info("    - Matching attributes: " + thisMatches);
			    		if (thisMatches>maxMatches) {
			    			maxMatches=thisMatches;
			    			result = server.getPassword();
			    			if (!parent.isInternal()) parent.getLog().info("    -> New best match");
			    		}
			    		
		    		}	    		
		    		
		    	}

	    	} else {
	    		parent.getLog().info(" - No servers defined in 'settings.xml'.");
	    	}
	    } else {
	    	parent.getLog().info(" - List of servers taken from 'settings.xml' is null.");
	    }
    	
		return (result);

	}

}
