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

import org.tmatesoft.hg.core.HgChangeset;
import org.tmatesoft.hg.core.HgDataStreamException;
import org.tmatesoft.hg.core.HgInvalidControlFileException;
import org.tmatesoft.hg.core.HgLogCommand;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.repo.HgChangelog;
import org.tmatesoft.hg.repo.HgRepository;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class Hg4jChangesetViewer implements HgChangesetViewer
{

  /** the logger for Hg4jChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(Hg4jChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param directory
   */
  public Hg4jChangesetViewer(HgContext context, File directory)
  {
    this.context = context;
    this.directory = directory;
  }

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param context
   * @param repository
   */
  public Hg4jChangesetViewer(HgRepositoryHandler handler, HgContext context,
                             Repository repository)
  {
    this(context, handler.getDirectory(repository));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param startNode
   * @param endNode
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getChangesets(final String startNode,
          final String endNode)
          throws IOException, RepositoryException
  {
    return new Hg4jChangesetHandler(context, directory)
    {
      @Override
      protected List<HgChangeset> getChangesets(HgRepoFacade facade,
              HgLogCommand lc, int total)
              throws HgInvalidControlFileException, HgDataStreamException
      {
        HgRepository repository = facade.getRepository();
        HgChangelog hgc = repository.getChangelog();
        int startRev = hgc.getLocalRevision(Nodeid.fromAscii(startNode));
        int endRev = hgc.getLocalRevision(Nodeid.fromAscii(endNode));

        if (logger.isDebugEnabled())
        {
          logger.debug("fetch changesets from {} to {}", startRev, endRev);
        }

        return lc.range(startRev, endRev).execute();
      }
    }.getChangesets();
  }

  /**
   * Method description
   *
   *
   * @param start
   * @param max
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getChangesets(final int start, final int max)
          throws IOException, RepositoryException
  {
    return new Hg4jChangesetHandler(context, directory)
    {
      @Override
      protected List<HgChangeset> getChangesets(HgRepoFacade facade,
              HgLogCommand lc, int total)
              throws HgDataStreamException
      {
        int startRev = total - start;
        int endRev = total - start - (max - 1);

        if (endRev < 0)
        {
          endRev = 0;
        }
        else if (endRev > total)
        {
          endRev = total;
        }

        if (logger.isDebugEnabled())
        {
          logger.debug("fetch changesets from {} to {}", startRev, endRev);
        }

        return lc.range(startRev, endRev).execute();
      }
    }.getChangesets();
  }

  /**
   * Method description
   *
   *
   * @param path
   * @param revision
   * @param start
   * @param max
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getChangesets(final String path,
          final String revision, final int start, final int max)
          throws IOException, RepositoryException
  {
    return new Hg4jChangesetHandler(context, directory)
    {
      @Override
      protected List<HgChangeset> getChangesets(HgRepoFacade facade,
              HgLogCommand lc, int total)
              throws HgInvalidControlFileException, HgDataStreamException
      {
        List<HgChangeset> changesets = facade.createLogCommand().file(
                                           path, false).changeset(
                                           Nodeid.fromAscii(
                                             revision)).execute();
        int listStart = start;

        if (listStart < 0)
        {
          listStart = 0;
        }

        int listMax = max;
        int listLength = changesets.size() - start;

        if ((listMax <= 0) || (listMax > listLength))
        {
          listMax = listLength;
        }

        changesets = changesets.subList(listStart, listMax);

        return changesets;
      }
    }.getChangesets();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgContext context;

  /** Field description */
  private File directory;
}
