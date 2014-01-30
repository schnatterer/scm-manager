/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Repository;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgCommandContext implements Closeable
{

  /** Field description */
  private static final String ENCODING = "UTF-8";

  /** Field description */
  private static final String PROPERTY_ENCODING = "hg.encoding";

  /**
   * the logger for HgCommandContext
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgCommandContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   *
   * @param configuration
   * @param hookManager
   * @param handler
   * @param repository
   * @param directory
   */
  public HgCommandContext(ScmConfiguration configuration,
    HgHookManager hookManager, HgRepositoryHandler handler,
    sonia.scm.repository.Repository repository, File directory)
  {
    this(configuration, hookManager, handler, repository, directory,
      handler.getHgContext().isPending());
  }

  /**
   * Constructs ...
   *
   *
   *
   *
   * @param configuration
   * @param hookManager
   * @param hanlder
   * @param repository
   * @param directory
   * @param pending
   */
  public HgCommandContext(ScmConfiguration configuration,
    HgHookManager hookManager, HgRepositoryHandler hanlder,
    sonia.scm.repository.Repository repository, File directory, boolean pending)
  {
    this.hookManager = hookManager;
    this.hanlder = hanlder;
    this.directory = directory;
    this.encoding = getRepositoryEncoding(configuration, hanlder, repository);
    this.pending = pending;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    if (repository != null)
    {
      repository.close();
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Repository open()
  {
    if (repository == null)
    {
      repository = HgUtil.open(hanlder, hookManager, directory, encoding,
        pending);
    }

    return repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public HgConfig getConfig()
  {
    return hanlder.getConfig();
  }

  /**
   * Method description
   *
   *
   * @param configuration
   * @param handler
   * @param repository
   *
   * @return
   */
  private String getRepositoryEncoding(ScmConfiguration configuration,
    HgRepositoryHandler handler, sonia.scm.repository.Repository repository)
  {
    String enc = repository.getEncoding();

    if (Strings.isNullOrEmpty(enc))
    {
      enc = repository.getProperty(PROPERTY_ENCODING);

      if (Strings.isNullOrEmpty(enc))
      {
        enc = handler.getConfig().getEncoding();

        if (Strings.isNullOrEmpty(enc))
        {
          enc = configuration.getDefaultRepositoryEncoding();

          if (Strings.isNullOrEmpty(enc))
          {
            enc = ENCODING;
            logger.trace("could not find configured encoding, use default {}",
              enc);
          }
          else
          {
            logger.trace("use encoding {} from scm configuration", enc);
          }
        }
        else
        {
          logger.trace("use encoding {} from repository handler", enc);
        }
      }
      else
      {
        logger.trace("use encoding {} from repository property {}", enc);
      }
    }
    else
    {
      logger.trace("use encoding {} from repository", enc);
    }

    return enc;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File directory;

  /** Field description */
  private String encoding;

  /** Field description */
  private HgRepositoryHandler hanlder;

  /** Field description */
  private HgHookManager hookManager;

  /** Field description */
  private boolean pending;

  /** Field description */
  private Repository repository;
}
