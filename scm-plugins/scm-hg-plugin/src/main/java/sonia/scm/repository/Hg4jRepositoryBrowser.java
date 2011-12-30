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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.hg.core.HgCatCommand;
import org.tmatesoft.hg.core.HgInvalidControlFileException;
import org.tmatesoft.hg.core.HgManifestCommand;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.util.Path;

import sonia.scm.util.AssertUtil;
import sonia.scm.util.OutputStreamChannel;
import sonia.scm.util.SubDirectoryPathMatcher;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class Hg4jRepositoryBrowser implements RepositoryBrowser
{

  /** the logger for Hg4jRepositoryBrowser */
  private static final Logger logger =
    LoggerFactory.getLogger(Hg4jRepositoryBrowser.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param directory
   */
  public Hg4jRepositoryBrowser(File directory)
  {
    this.directory = directory;
  }

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public Hg4jRepositoryBrowser(HgRepositoryHandler handler,
                               Repository repository)
  {
    this(handler.getDirectory(repository));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   * @param output
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void getContent(String revision, String path, OutputStream output)
          throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("get content of {} at revision {}", path, revision);
    }

    AssertUtil.assertIsNotEmpty(path);
    AssertUtil.assertIsNotNull(output);

    try
    {
      HgRepoFacade facade = new HgRepoFacade();

      facade.initFrom(directory);

      HgCatCommand cat = facade.createCatCommand();

      if (Util.isNotEmpty(revision))
      {
        Nodeid nodeid = Nodeid.fromAscii(revision);

        cat.changeset(nodeid);
      }

      cat.file(Path.create(path)).execute(new OutputStreamChannel(output));
    }
    catch (Exception ex)
    {
      throw new RepositoryException("could not read file content", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public BrowserResult getResult(String revision, String path)
          throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("browse files of {} at revision {}", path, revision);
    }

    BrowserResult result = null;

    try
    {
      HgRepoFacade facade = new HgRepoFacade();

      facade.initFrom(directory);

      HgManifestCommand cmd = facade.createManifestCommand();

      cmd.dirs(true).revision(getRevision(facade, revision));

      FileObjectManifestHandler handler = new FileObjectManifestHandler(facade,
                                            path);

      if (Util.isNotEmpty(path))
      {
        cmd.match(new SubDirectoryPathMatcher(path));
      }
      else
      {
        cmd.match(new SubDirectoryPathMatcher());
      }

      cmd.execute(handler);
      result = new BrowserResult(revision, null, null,
                                 handler.getFileObjects());
    }
    catch (Exception ex)
    {
      throw new RepositoryException("could not read file content", ex);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param facade
   * @param revision
   *
   * @return
   *
   * @throws HgInvalidControlFileException
   * @throws RepositoryException
   */
  private int getRevision(HgRepoFacade facade, String revision)
          throws HgInvalidControlFileException, RepositoryException
  {
    int rev = -1;

    if (Util.isNotEmpty(revision))
    {

      // TODO find revision for short notation
      Nodeid nodeid = Nodeid.fromAscii(revision);

      rev = facade.getRepository().getChangelog().getLocalRevision(nodeid);
    }
    else
    {
      rev = facade.getRepository().getChangelog().getLastRevision();
    }

    return rev;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File directory;
}
