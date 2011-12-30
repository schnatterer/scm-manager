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

import org.tmatesoft.hg.core.HgChangeset;
import org.tmatesoft.hg.core.HgException;
import org.tmatesoft.hg.core.HgFileRevision;
import org.tmatesoft.hg.core.HgInvalidFileException;
import org.tmatesoft.hg.core.HgLogCommand;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.util.Path;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class Hg4jChangesetHandler
{

  /** Field description */
  public static final String BRANCH_DEFAULT = "default";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param directory
   */
  public Hg4jChangesetHandler(HgContext context, File directory)
  {
    this.context = context;
    this.directory = directory;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param facade
   * @param logCommand
   * @param total
   *
   * @return
   *
   * @throws HgException
   */
  protected abstract List<HgChangeset> getChangesets(HgRepoFacade facade,
          HgLogCommand logCommand, int total)
          throws HgException;

  /**
   * Method description
   *
   *
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets()
          throws IOException, RepositoryException
  {
    ChangesetPagingResult result = null;
    HgRepoFacade facade = createRepositoryFacade();
    int total = facade.getRepository().getChangelog().getRevisionCount();

    try
    {
      HgLogCommand logCommand =
        facade.createLogCommand().pending(context.isPending());

      total--;

      List<HgChangeset> hgChangesets = getChangesets(facade, logCommand, total);
      List<Changeset> changesets = new ArrayList<Changeset>();

      for (HgChangeset changeset : hgChangesets)
      {
        changesets.add(convertChangeset(changeset));
      }

      total++;
      result = new ChangesetPagingResult(total, changesets);
    }
    catch (HgException ex)
    {
      throw new RepositoryException("could not fetch changesets", ex);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param hgc
   *
   * @return
   */
  private Changeset convertChangeset(HgChangeset hgc)
  {
    Changeset changeset = new Changeset();

    changeset.setId(hgc.getNodeid().toString());
    changeset.setDate(hgc.getDate().getRawTime());
    changeset.setDescription(hgc.getComment());
    changeset.setAuthor(Person.toPerson(hgc.getUser()));

    String branch = hgc.getBranch();

    if (Util.isNotEmpty(branch) &&!BRANCH_DEFAULT.equals(branch))
    {
      List<String> branches = new ArrayList<String>();

      branches.add(branch);
      changeset.setBranches(branches);
    }

    List<HgFileRevision> hgAdded = hgc.getAddedFiles();

    if (Util.isNotEmpty(hgAdded))
    {
      changeset.getModifications().setAdded(convertFileRevision(hgAdded));
    }

    List<HgFileRevision> hgModified = hgc.getModifiedFiles();

    if (Util.isNotEmpty(hgModified))
    {
      changeset.getModifications().setModified(convertFileRevision(hgModified));
    }

    List<Path> hgRemoved = hgc.getRemovedFiles();

    if (Util.isNotEmpty(hgModified))
    {
      changeset.getModifications().setRemoved(convertPath(hgRemoved));
    }

    return changeset;
  }

  /**
   * Method description
   *
   *
   * @param hgFiles
   *
   * @return
   */
  private List<String> convertFileRevision(List<HgFileRevision> hgFiles)
  {
    List<String> paths = new ArrayList<String>();

    for (HgFileRevision file : hgFiles)
    {
      paths.add(file.getPath().toString());
    }

    return paths;
  }

  /**
   * Method description
   *
   *
   * @param hgFiles
   *
   * @return
   */
  private List<String> convertPath(List<Path> hgFiles)
  {
    List<String> paths = new ArrayList<String>();

    for (Path file : hgFiles)
    {
      paths.add(file.toString());
    }

    return paths;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws RepositoryException
   */
  private HgRepoFacade createRepositoryFacade() throws RepositoryException
  {
    HgRepoFacade facade = null;

    try
    {
      facade = new HgRepoFacade();
      facade.initFrom(directory);
    }
    catch (HgInvalidFileException ex)
    {
      throw new RepositoryException("could not open repository", ex);
    }

    return facade;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected HgContext context;

  /** Field description */
  protected File directory;
}
