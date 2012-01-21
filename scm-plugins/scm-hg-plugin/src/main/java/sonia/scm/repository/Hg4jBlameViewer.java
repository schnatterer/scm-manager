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

import org.tmatesoft.hg.core.HgChangeset;
import org.tmatesoft.hg.core.HgDataStreamException;
import org.tmatesoft.hg.core.HgInvalidControlFileException;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.internal.ByteArrayChannel;
import org.tmatesoft.hg.repo.HgDataFile;
import org.tmatesoft.hg.repo.HgRepository;
import org.tmatesoft.hg.util.CancelledException;
import org.tmatesoft.hg.util.Path;

import sonia.scm.io.LineBasedText;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Hg4jUtil;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO improve performance
 *
 * @author Sebastian Sdorra
 */
public class Hg4jBlameViewer implements BlameViewer
{

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param directory
   */
  public Hg4jBlameViewer(HgContext context, File directory)
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
  public Hg4jBlameViewer(HgRepositoryHandler handler, HgContext context,
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
   * @param filePath
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public BlameResult getBlame(String revision, String filePath)
          throws IOException, RepositoryException
  {
    AssertUtil.assertIsNotEmpty(filePath);
    revision = HgUtil.getRevision(revision);

    BlameResult result = null;

    try
    {
      HgRepoFacade facade = new HgRepoFacade();

      facade.initFrom(directory);

      HgDataFile file = facade.getRepository().getFileNode(filePath);

      if (file.exists())
      {
        HgRepository repository = facade.getRepository();
        Path path = file.getPath();

        // changeset
        int rev = Hg4jUtil.getFileRevision(repository, revision, file, path);
        List<HgChangeset> changesets =
          facade.createLogCommand().pending(context.isPending()).range(0,
            rev).file(path, false).execute();
        LineBasedText content = null;
        Map<String, HgChangeset> lineMap = new HashMap<String, HgChangeset>();

        for (HgChangeset c : changesets)
        {
          content = getContent(repository, file, c.getRevision(), path);

          LineBasedText parent = getParentContent(repository, file, c, path);
          LineBasedText addedLines = parent.getNewLines(content);

          for (String line : addedLines)
          {
            lineMap.put(line, c);
          }
        }

        result = createBlameResult(lineMap, content);
      }
    }
    catch (Exception ex)
    {
      throw new RepositoryException("could not create blame view", ex);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param nr
   * @param c
   * @param line
   *
   * @return
   */
  private BlameLine createBlameLine(int nr, HgChangeset c, String line)
  {
    return new BlameLine(nr, c.getNodeid().toString(),
                         c.getDate().getRawTime(),
                         Person.toPerson(c.getUser()), c.getComment(), line);
  }

  /**
   * Method description
   *
   *
   * @param lineMap
   * @param content
   *
   * @return
   */
  private BlameResult createBlameResult(Map<String, HgChangeset> lineMap,
          LineBasedText content)
  {
    List<BlameLine> lines = new ArrayList<BlameLine>();
    int i = 0;

    for (String line : content)
    {
      i++;

      HgChangeset c = lineMap.get(line);

      lines.add(createBlameLine(i, c, line));
    }

    return new BlameResult(lines);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param file
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws CancelledException
   * @throws HgDataStreamException
   * @throws HgInvalidControlFileException
   * @throws IOException
   */
  private LineBasedText getContent(HgRepository repository, HgDataFile file,
                                   int revision, Path path)
          throws HgInvalidControlFileException, HgDataStreamException,
                 CancelledException, IOException
  {
    LineBasedText content = null;
    Nodeid id = repository.getManifest().getFileRevision(revision, path);

    if (id != null)
    {
      int fileRevision = file.getLocalRevision(id);
      ByteArrayChannel channel = new ByteArrayChannel();

      file.content(fileRevision, channel);
      content = LineBasedText.create(channel.toArray());
    }
    else
    {
      content = LineBasedText.create();
    }

    return content;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param file
   * @param c
   * @param path
   *
   * @return
   *
   * @throws CancelledException
   * @throws HgDataStreamException
   * @throws HgInvalidControlFileException
   * @throws IOException
   */
  private LineBasedText getParentContent(HgRepository repository,
          HgDataFile file, HgChangeset c, Path path)
          throws HgInvalidControlFileException, HgDataStreamException,
                 CancelledException, IOException
  {
    LineBasedText parentContent = null;
    Nodeid parent = c.getFirstParentRevision();

    if ((parent != null) && (parent != Nodeid.NULL))
    {
      parentContent =
        getContent(repository, file,
                   repository.getChangelog().getLocalRevision(parent), path);
    }
    else
    {
      parentContent = LineBasedText.create();
    }

    return parentContent;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgContext context;

  /** Field description */
  private File directory;
}
