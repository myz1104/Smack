/**
 *
 * Copyright the original author or authors
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
package org.jivesoftware.smack.initializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.provider.ProviderFileLoader;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.FileUtils;

/**
 * Loads the provider file defined by the URL returned by {@link #getProviderUrl()}.  This file will be loaded on Smack initialization.
 * 
 * @author Robin Collier
 *
 */
public abstract class UrlInitializer implements SmackInitializer {
    private static final Logger LOGGER = Logger.getLogger(UrlInitializer.class.getName());

    private List<Exception> exceptions = new LinkedList<Exception>();

    public void initialize() {
        initialize(null);
    }

    @Override
    public void initialize(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getClassLoader();
        }

        InputStream is;
        final String providerUrl = getProvidersUrl();
        if (providerUrl != null) {
            try {
                is = FileUtils.getStreamForUrl(providerUrl, classLoader);

                if (is != null) {
                    LOGGER.log(Level.FINE, "Loading providers for providerUrl [" + providerUrl
                                    + "]");
                    ProviderFileLoader pfl = new ProviderFileLoader(is);
                    ProviderManager.getInstance().addLoader(pfl);
                    exceptions.addAll(pfl.getLoadingExceptions());
                }
                else {
                    LOGGER.log(Level.WARNING, "No input stream created for " + providerUrl);
                    exceptions.add(new IOException("No input stream created for " + providerUrl));
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error trying to load provider file " + providerUrl, e);
                exceptions.add(e);
            }
        }
        final String configUrl = getConfigUrl();
        if (configUrl != null) {
            try {
                is = FileUtils.getStreamForUrl(configUrl, null);
                SmackConfiguration.processConfigFile(is, exceptions);
            }
            catch (Exception e) {
                exceptions.add(e);
            }
        }
    }

    @Override
    public List<Exception> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    protected String getProvidersUrl() {
        return null;
    }

    protected String getConfigUrl() {
        return null;
    }

    /**
     * Returns an array of class loaders to load resources from.
     * 
     * @return an array of ClassLoader instances.
     */
    protected ClassLoader getClassLoader() {
        return null;
    }
    
    
}
