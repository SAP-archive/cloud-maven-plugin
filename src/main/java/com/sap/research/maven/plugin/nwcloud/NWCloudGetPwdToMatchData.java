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

import java.util.Map;

import org.apache.maven.settings.Server;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * This class is used by NWCloudGetPwdToMatch to
 *  - store a triple of the attributes "host", "account", "user",
 *  - provide convenient setters for the triple,
 *  - compare the triple to other triples and count number or matching attributes.
 */
public class NWCloudGetPwdToMatchData {

	// --- Attributes ---
	
	/**
	 * Host
	 */
	private String host=null;

	/**
	 * Account
	 */
	private String account=null;

	/**
	 * User
	 */
	private String user=null;

	// --- Getters/Setters ---

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}
	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}
	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Extract the attributes "host", "account", and "user" from given PluginContext.
	 * All attributes not existing in the PluginContext will be set to null. 
	 * @param pluginContext Map<String,Object> the PluginContext shared among the Mojos of a Maven plugin.
	 */
	public void setFromPluginContext(Map<String,Object> pluginContext) {
		this.setHost   (pluginContext.containsKey("host")    ? (String)pluginContext.get("host")    : null);
		this.setAccount(pluginContext.containsKey("account") ? (String)pluginContext.get("account") : null);
		this.setUser   (pluginContext.containsKey("user")    ? (String)pluginContext.get("user")    : null);
	}

	/**
	 * Extract the attributes "host", "account", and "user" from given NWCLoudGetPwd instance.
	 * @param mojo NWCLoudGetPwd instance to extract attributes from.
	 */
	public void setFromMojo(NWCloudGetPwd mojo) {
		this.setHost   (mojo.getHost());
		this.setAccount(mojo.getAccount());
		this.setUser   (mojo.getUser());
	}

	/**
	 * Extract the attributes "host", "account", and "user" from given Server instance.
	 * @param server org.apache.maven.settings.Server instance to extract attributes from.
	 */
	public void setFromServer(Server server) {
		if (server!=null) {
			if (server.getConfiguration()!=null) {
				if (server.getConfiguration() instanceof Xpp3Dom) {
					Xpp3Dom config = (Xpp3Dom)server.getConfiguration();
					this.setHost(config.getChild("host")==null ? null : config.getChild("host").getValue());
					this.setAccount(config.getChild("account")==null ? null : config.getChild("account").getValue());
					this.setUser(config.getChild("user")==null ? null : config.getChild("user").getValue());
				}
			}
		}
	}

	// --- Methods/Functions ---
	
	/**
	 * Check if non-null values are stored in the attributes.
	 * @return false, if all attributes have null value, true otherwise 
	 */
	public boolean isSet() {
		return (!((this.getHost()==null)&&(this.getAccount()==null)&&(this.getUser()==null)));
	}
	
	/**
	 * Return the number of matching attributes compared to another instance of this class.
	 * @param other NWCloudGetPwdToMatchData instance to compare attributes of this instance to.
	 * @return the number of matching attributes compared to the given other instance of this class.
	 */
	public int numOfMatches(NWCloudGetPwdToMatchData other) {

		int result = 0;

		if (other!=null) {
			if (other.isSet()) {
				if ((this.getHost()!=null) && (other.getHost()!=null)) {
					result += this.getHost().equalsIgnoreCase(other.getHost()) ? 1 : 0;
				}
				if ((this.getAccount()!=null) && (other.getAccount()!=null)) {
					result += this.getAccount().equalsIgnoreCase(other.getAccount()) ? 1 : 0;
				}
				if ((this.getUser()!=null) && (other.getUser()!=null)) {
					result += this.getUser().equalsIgnoreCase(other.getUser()) ? 1 : 0;
				}
			}
		}

		return result;

	}

}