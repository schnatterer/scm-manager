/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm;

import com.google.common.base.Strings;
import sonia.scm.util.SystemUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Determines the base directory for SCM-Manager.
 * This class should not be used directory, use {@link SCMContextProvider#getBaseDirectory()} instead.
 *
 * @since 2.0.0
 */
final class BaseDirectory {

  /** Environment variable for the SCM-Manager base directory */
  static final String ENVIRONMENT_VARIABLE = "SCM_HOME";

  /** Java system property for the SCM-Manager base directory */
  static final String SYSTEM_PROPERTY = "scm.home";

  /** Classpath resource for the SCM-Manager base directory */
  @SuppressWarnings("java:S1075") // it is already configurable
  static final String CLASSPATH_RESOURCE = "/scm.properties";

  /** Property name in resource file */
  static final String RESOURCE_PROPERTY = "scm.home";

  private final Platform platform;
  private final String classPathResource;
  private final Map<String,String> environment;
  private final Properties systemProperties;

  BaseDirectory(Platform platform, String classPathResource, Map<String, String> environment, Properties systemProperties) {
    this.platform = platform;
    this.classPathResource = classPathResource;
    this.environment = environment;
    this.systemProperties = systemProperties;
  }

  /**
   * Returns the determined base directory.
   *
   * @return base directory
   */
  @SuppressWarnings("java:S5304") // it is safe to use environment in this case
  static Path get() {
    return new BaseDirectory(
      SystemUtil.getPlatform(),
      CLASSPATH_RESOURCE,
      System.getenv(),
      System.getProperties()
    ).find();
  }

  Path find() {
    String directory = getFromResource();
    if (Strings.isNullOrEmpty(directory)) {
      directory = getFromSystemProperty();
    }
    if (Strings.isNullOrEmpty(directory)) {
      directory = getFromEnvironmentVariable();
    }
    if (Strings.isNullOrEmpty(directory)) {
      directory = getOsSpecificDefault();
    }

    return Paths.get(directory);
  }

  private String getFromResource() {
    try (InputStream input = BasicContextProvider.class.getResourceAsStream(classPathResource))
    {
      if (input != null)
      {
        Properties properties = new Properties();
        properties.load(input);
        return properties.getProperty(RESOURCE_PROPERTY);
      }
    }
    catch (IOException ex)
    {
      throw new ConfigurationException("could not load properties form resource " + CLASSPATH_RESOURCE, ex);
    }
    return null;
  }

  private String getFromEnvironmentVariable() {
    return environment.get(ENVIRONMENT_VARIABLE);
  }

  private String getFromSystemProperty() {
    return systemProperties.getProperty(SYSTEM_PROPERTY);
  }

  private String getOsSpecificDefault() {
    if (platform.isMac()) {
      return getOsxDefault();
    } else if (platform.isWindows()) {
      return getWindowsDefault();
    }
    return systemProperties.getProperty("user.home") + "/.scm";
  }

  private String getOsxDefault() {
    return systemProperties.getProperty("user.home")  + "/Library/Application Support/SCM-Manager";
  }

  private String getWindowsDefault() {
    return environment.get("APPDATA") + "\\SCM-Manager";
  }

}
