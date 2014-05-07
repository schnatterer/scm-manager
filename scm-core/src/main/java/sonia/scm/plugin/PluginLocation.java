/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Throwables;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.39
 */
public final class PluginLocation
{

  /**
   * Constructs ...
   *
   *
   * @param file
   */
  public PluginLocation(File file)
  {
    this.file = file;
    this.url = null;
  }

  /**
   * Constructs ...
   *
   *
   * @param url
   */
  public PluginLocation(URL url)
  {
    this.url = url;
    this.file = null;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    return getPath();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public File getFile()
  {
    return file;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public InputStream getInputStream() throws IOException
  {
    InputStream stream = null;

    if ((file != null) &&!file.isDirectory())
    {
      stream = new FileInputStream(file);
    }
    else if (url != null)
    {
      stream = url.openStream();
    }
    else
    {
      throw new IllegalStateException(
        "could not create a stream from location");
    }

    return stream;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPath()
  {
    return (file != null)
      ? file.getPath()
      : url.getPath();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public URL getUrl()
  {
    try
    {
      return (url != null)
        ? url
        : file.toURI().toURL();
    }
    catch (MalformedURLException ex)
    {
      throw Throwables.propagate(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isDirectory()
  {
    return (file != null) && file.isDirectory();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final File file;

  /** Field description */
  private final URL url;
}
