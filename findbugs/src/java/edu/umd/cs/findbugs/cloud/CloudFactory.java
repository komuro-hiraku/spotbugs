/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.cloud;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.IGuiCallback;
import edu.umd.cs.findbugs.PluginLoader;
import edu.umd.cs.findbugs.SystemProperties;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;


/**
 * @author pwilliam
 */
public class CloudFactory {
    
    private static final String FINDBUGS_NAMELOOKUP_CLASSNAME = "findbugs.namelookup.classname";
    private static final String FINDBUGS_NAMELOOKUP_REQUIRED = "findbugs.namelookup.required";

	public static boolean DEBUG = SystemProperties.getBoolean("findbugs.cloud.debug",false);
	
    private static final String DEFAULT_CLOUD_CLASS = "edu.umd.cs.findbugs.cloud.db.DBCloud";

    

	public static Cloud createCloudWithoutInitializing(BugCollection bc) {
		CloudPlugin plugin = defaultPlugin;
		
		try {
			Class<? extends Cloud> cloudClass = plugin.getCloudClass();
	        Constructor<? extends Cloud> constructor = cloudClass.getConstructor(CloudPlugin.class, BugCollection.class);
			Cloud cloud = constructor.newInstance(plugin, bc);
			if (DEBUG)
				bc.getProject().getGuiCallback().showMessageDialog("constructed " + cloud.getClass().getName());
			return cloud;
		} catch (Exception e) {
			if (DEBUG) {
				bc.getProject().getGuiCallback().showMessageDialog("failed " + e.getMessage() + e.getClass().getName());
				e.printStackTrace();
			}
			
			assert true;
        }
		if (SystemProperties.getBoolean("findbugs.failIfUnableToConnectToDB"))
			System.exit(1);
        return getPlainCloud(bc);
	}
	
	public static void initializeCloud(BugCollection bc, Cloud cloud) {
        IGuiCallback callback = bc.getProject().getGuiCallback();

        if (cloud.availableForInitialization()) {
            if (DEBUG)
                callback.showMessageDialog("attempting to initialize " + cloud.getClass().getName());

            if (cloud.initialize()) {
                if (DEBUG)
                    callback.showMessageDialog("initialized " + cloud.getClass().getName());

                return;
            }
            callback.showMessageDialog("Unable to connect to " + cloud.getClass().getSimpleName());

        }
	}


    public static Cloud getPlainCloud(BugCollection bc) {
	    Cloud cloud = new BugCollectionStorageCloud(bc);
        if (cloud.initialize())
        	return cloud;
        throw new IllegalStateException("Unable to initialize plain cloud");
    }

    static  Map<String, CloudPlugin> registeredClouds = new HashMap<String, CloudPlugin>();
    static CloudPlugin defaultPlugin;
	/**
     * @param cloudPlugin
	 * @param enabled TODO
     */
    public static void registerCloud(CloudPlugin cloudPlugin, boolean enabled) {
    	registeredClouds.put(cloudPlugin.getId(), cloudPlugin);
    	if (enabled) 
    		defaultPlugin = cloudPlugin;
	    
    }
	
}
