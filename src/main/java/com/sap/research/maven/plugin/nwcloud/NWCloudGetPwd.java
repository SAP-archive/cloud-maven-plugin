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
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

/**
 * This class defines the goal (mojo) "getpwd" of the Maven plugin for SAP NetWeaver Cloud.
 * 
 * Its main use is to be used internally by other mojos in case operations should be executed
 * which require a password, but no password was provided in the "nwcloud.properties" file.
 * 
 * When in need of a password, e.g. in case of deploying to a host, the maven plugin tries
 * to get the password in the following way (where this mojo here implements step 2):
 * 
 * 1. If password is defined in "nwcloud.properties" then use this one.
 * 
 * 2. Only if password was not found in 1st step, check servers defined in Maven's
 *    "settings.xml" file and extract password of server with the best fit to the
 *    current situation. For the matching of a server, we have a look at all servers
 *    that define a password and provide a configuration, like in this example:
 *    
 *	<servers>
 *		[...]
 *		<server>
 *			<id>nwcloud-01</id>
 *			<!-- How to encrypt passwords: http://maven.apache.org/guides/mini/guide-encryption.html -->
 *			<password>{cSDAp/w23ewHGH7Xghvqnk7sAT5GVS+dw71yP5FY28Y=}</password>
 *			<configuration>
 *				<host>https://nwtrial.ondemand.com</host>
 *				<account>myaccount</account>
 *				<user>myuser</user>
 *			</configuration>
 *		</server>
  *		[...]
 *	</servers>
 *    
 *    We compare each of the attributes "host", "account" and "user" from each server
 *    with the according settings in "nwcloud.properties". The server having the most
 *    matching attributes (must be at least one attribute) "wins" and we decrypt its
 *    password and use it.
 *   
 * 3. Only if password can't be found in step 1 and 2, we finally query the user on
 *    the console to enter the password manually.
 * 
 * Note: Using encrypted passwords stored in Maven's "settings.xml" (see step 2 above)
 *       allows you storage of encrypted password separate from the "nwcloud.properties"
 *       file in the project directory. This is especially useful in scenarios where
 *       you want to fully automate processes, but do not want to provide password
 *       in clear text in "nwcloud.properties". In addition it is comfortable, as it
 *       e.g. allows you to define a server just defining your user in its configuration
 *       part and then will match all requests using this users, so you neither need
 *       to type in your password all the time you deploy, start, stop, nor do you
 *       need to store it in clear text in each and every project.
 *
 * Usage:
 * 
 * The goal (mojo) "getpwd" is mainly used internally by other mojos which depend on a
 * password. See target "password" defined in "src/main/scripts/usage.build.xml" on how
 * it is called from JavaScript embedded in an Ant script.
 * 
 * For debugging your settings and finding out which servers are searched and which is
 * chosen as the best matching server, you can also execute this goal manually. In this
 * case you have to prove the parameters "host", "account" and "user" to match the server
 * against manually on the command line, like this:
 * 
 *    mvn nwcloud:getpwd -Dhost=https://myhost -Daccount=myaccount -Duser=myuser
 *    
 * You will then be shown which servers in "settings.xml" have password and configuration
 * defined, and which of these is the one matching the best to your provided parameters.
 * Please be aware that in case of this manual execution, the decrypted password of the
 * best matching server will be decrypted and shown in clear text on the console!
 * 
 */
@Mojo(name="getpwd")
public class NWCloudGetPwd extends AbstractMojo {
	
	// --- Attributes ---
	
	/**
	 * Is set to true if executed from another Mojo and data is exchanged via PluginContext instead of parameters.
	 */
	private boolean internal = false;
	
	/**
     * Host parameter
     */
    @Parameter(readonly=true, required=false, defaultValue = "${host}")
    private String host;	

	/**
     * Account parameter
     */
    @Parameter(readonly=true, required=false, defaultValue = "${account}")
    private String account;

    /**
     * User parameter
     */
    @Parameter(readonly=true, required=false, defaultValue = "${user}")
    private String user;

    /**
     * Maven settings.
     */
	@Component
    private Settings settings;

    /**
     * The decrypter for passwords.
     */
	@Component
    private SettingsDecrypter settingsDecrypter;

	// --- Getters/Setters ---

    /**
	 * @return the internal
	 */
	public boolean isInternal() {
		return internal;
	}

	/**
	 * @param internal the internal to set
	 */
	public void setInternal(boolean internal) {
		this.internal = internal;
	}

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

	// --- Methods/Functions ---

    /**
     * Returns List of Server objects defined by Maven settings having the passwords decrypted.
     * @return List of Servers with decrypted passwords.
     */
    private List<Server> getDecryptedServers() {

    	List<Server> result = null;

    	if (settings!=null) {
    		result = settings.getServers();
    		if (result!=null) {
    	    	if (settingsDecrypter!=null) {
	            	SettingsDecryptionRequest settingsDecryptionRequest = new DefaultSettingsDecryptionRequest();
	                settingsDecryptionRequest.setServers(result);
	                SettingsDecryptionResult decrypt = settingsDecrypter.decrypt(settingsDecryptionRequest);
	    			result=decrypt.getServers();
    	    	} else {
    	    		getLog().info("Cannot decrypt the server settings as the SettingsDecrypter provided by Maven is null.");
    	    	}
    		} else {
    			getLog().info("Servers defined in Maven settings are null.");
    		}
    	} else {
    		getLog().info("Maven settings are null.");
    	}

        return result;

    }

    /**
     * Executes the goal 'getpwds'.
     */
    public void execute() throws MojoExecutionException {

    	getLog().info("Trying to automatically retrieve password from servers defined in Maven's 'settings.xml'.");
    	
    	// Get PluginContext to exchange key-value-pairs with other Mojos in this Maven plugin.
		@SuppressWarnings("unchecked")
		Map<String,Object> pluginContext = this.getPluginContext();
		if (pluginContext.containsKey("internal")) {
			setInternal(((String)pluginContext.get("internal")).equalsIgnoreCase("true"));
		}

		// Get attributes (host, account, user) to match against servers - either from
		// PluginContext (in case of execution from other Mojo), or from parameters
		// passed to this Mojo (in case of normal execution of our Mojo).
		NWCloudGetPwdToMatch matcher = new NWCloudGetPwdToMatch();
		matcher.setServerList(this.getDecryptedServers());
		matcher.setAttribsToMatch(new NWCloudGetPwdToMatchData());
		if (this.isInternal()) {
			matcher.getAttribsToMatch().setFromPluginContext(pluginContext);
		} else {
			matcher.getAttribsToMatch().setFromMojo(this);
			getLog().info(" - Server config should match as many attributes as possible compared to these:");
			getLog().info("    - host:    '"+matcher.getAttribsToMatch().getHost()+"'");
			getLog().info("    - account: '"+matcher.getAttribsToMatch().getAccount()+"'");
			getLog().info("    - user:    '"+matcher.getAttribsToMatch().getUser()+"'");
		}
		
		// If at least one of the attributes is not null, we search for a matching server with password.
		if (!matcher.getAttribsToMatch().isSet()) {
			getLog().info(" - No attributes to match specified. Does not make sense to search a matching server.");
			if (!this.isInternal()) {
				getLog().info("    - Hint: You can specify attributes to match like this:");
				getLog().info("            mvn nwcloud:getpwd -Dhost=myhost -Daccount=myaccount -Duser=myuser");
			}
		} else {
			// Get password of server with the most matching attributes
			String password = matcher.match(this);
	    	if (password!=null) {
	    		getLog().info(" - Matching server found. Password extracted.");
	    		if (this.isInternal()) {
	    			pluginContext.put("password", password);
	    		} else {
	    			getLog().info(" -> Password: '"+password+"'");
	    		}
	    	} else {
	    		getLog().info(" - No matching server found, or matching server(s) had no password specified.");
	    	}			
		}

    }

}