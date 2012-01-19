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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import difflib.DiffUtils;
import difflib.Patch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.hg.core.HgChangeset;
import org.tmatesoft.hg.core.HgDataStreamException;
import org.tmatesoft.hg.core.HgException;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.internal.ByteArrayChannel;
import org.tmatesoft.hg.repo.HgDataFile;
import org.tmatesoft.hg.repo.HgRepository;
import org.tmatesoft.hg.util.CancelledException;
import org.tmatesoft.hg.util.Path;

import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class Hg4jDiffViewer implements DiffViewer
{

  /**
   * the logger for Hg4jDiffViewer
   */
  private static final Logger logger =
    LoggerFactory.getLogger(Hg4jDiffViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param directory
   */
  public Hg4jDiffViewer(HgContext context, File directory)
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
  public Hg4jDiffViewer(HgRepositoryHandler handler, HgContext context,
                        Repository repository)
  {
    this(context, handler.getDirectory(repository));
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
  public void getDiff(String revision, String path, OutputStream output)
          throws IOException, RepositoryException
  {
    AssertUtil.assertIsNotNull(output);
    revision = HgUtil.getRevision(revision);

    try
    {
      HgRepoFacade facade = new HgRepoFacade();

      facade.initFrom(directory);

      List<HgChangeset> changesets = facade.createLogCommand().pending(
                                         context.isPending()).changeset(
                                         Nodeid.fromAscii(revision)).execute();

      if (Util.isNotEmpty(changesets))
      {
        HgChangeset changeset = changesets.get(0);

        AssertUtil.assertIsNotNull(changeset);
        getDiff(facade.getRepository(), changeset, path, output);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find changeset for revision {}", revision);
      }
    }
    catch (Exception ex)
    {
      throw new RepositoryException("could not create diff", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   * @param originalLines
   * @param patch
   * @param writer
   */
  private void appendDiff(Path path, List<String> originalLines, Patch patch,
                          PrintWriter writer)
  {
    String filename = path.toString();
    List<String> lines = DiffUtils.generateUnifiedDiff(filename, filename,
                           originalLines, patch, 0);

    for (String line : lines)
    {
      writer.println(line);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   * @param filePath
   * @param output
   *
   * @throws CancelledException
   * @throws HgDataStreamException
   * @throws HgException
   * @throws IOException
   */
  private void getDiff(HgRepository repository, HgChangeset changeset,
                       String filePath, OutputStream output)
          throws HgException, HgDataStreamException, CancelledException,
                 IOException
  {
    PrintWriter writer = new PrintWriter(output);
    Nodeid parentId = changeset.getFirstParentRevision();
    int revision = changeset.getRevision();
    int parentRevision = repository.getChangelog().getLocalRevision(parentId);

    if (logger.isDebugEnabled())
    {
      logger.debug("create diff for {} and {} with path {}",
                   new Object[] { parentId,
                                  revision, filePath });
    }

    if (Util.isNotEmpty(filePath))
    {
      getDiff(repository, Path.create(filePath), parentRevision, revision,
              writer);
    }
    else
    {
      List<Path> paths = changeset.getAffectedFiles();

      for (Path path : paths)
      {
        getDiff(repository, path, parentRevision, revision, writer);
      }
    }

    writer.flush();
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param path
   * @param parentRevision
   * @param revision
   * @param writer
   *
   * @throws CancelledException
   * @throws HgDataStreamException
   * @throws HgException
   * @throws IOException
   */
  private void getDiff(HgRepository repository, Path path, int parentRevision,
                       int revision, PrintWriter writer)
          throws HgException, HgDataStreamException, CancelledException,
                 IOException
  {
    List<String> content = getLines(repository, path, revision);
    List<String> parentContent = getLines(repository, path, parentRevision);
    Patch patch = DiffUtils.diff(parentContent, content);

    appendDiff(path, content, patch, writer);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param revision
   * @param path
   *
   * @return
   */
  private Nodeid getFileRevision(HgRepository repository, int revision,
                                 Path path)
  {
    return repository.getManifest().getFileRevision(revision, path);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param path
   * @param revision
   *
   * @return
   *
   * @throws CancelledException
   * @throws HgDataStreamException
   * @throws HgException
   * @throws IOException
   */
  private List<String> getLines(HgRepository repository, Path path,
                                int revision)
          throws HgException, HgDataStreamException, CancelledException,
                 IOException
  {
    List<String> lines = new ArrayList<String>();
    HgDataFile file = repository.getFileNode(path);

    if (file.exists())
    {
      Nodeid fileRevision = getFileRevision(repository, revision, path);

      if (fileRevision != null)
      {
        revision = file.getLocalRevision(fileRevision);

        ByteArrayChannel channel = new ByteArrayChannel();

        file.content(revision, channel);

        BufferedReader reader = null;

        try
        {
          reader = new BufferedReader(
              new InputStreamReader(
                  new ByteArrayInputStream(channel.toArray())));

          String line = reader.readLine();

          while (line != null)
          {
            lines.add(line);
            line = reader.readLine();
          }
        }
        finally
        {
          IOUtil.close(reader);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("file {} is not available in revision {}", path, revision);
      }
    }
    else
    {
      throw new IOException("could not find file revision");
    }

    return lines;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgContext context;

  /** Field description */
  private File directory;
}
