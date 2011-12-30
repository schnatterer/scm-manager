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

import org.tmatesoft.hg.core.HgFileRevision;
import org.tmatesoft.hg.core.HgManifestCommand.Handler;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.repo.HgChangelog.RawChangeset;
import org.tmatesoft.hg.repo.HgDataFile;
import org.tmatesoft.hg.repo.HgRepository;
import org.tmatesoft.hg.util.Path;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class FileObjectManifestHandler implements Handler
{

  /** the logger for FileObjectManifestHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(FileObjectManifestHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param facade
   * @param directory
   */
  public FileObjectManifestHandler(HgRepoFacade facade, String directory)
  {
    this.repository = facade.getRepository();
    this.directory = Util.nonNull(directory);
    this.fileObjects = new ArrayList<FileObject>();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param nodeid
   */
  @Override
  public void begin(Nodeid nodeid) {}

  /**
   * Method description
   *
   *
   * @param path
   */
  @Override
  public void dir(Path path)
  {
    String value = path.toString();

    if (Util.isNotEmpty(value))
    {
      int length = directory.length();

      value = value.substring(length);

      if (value.startsWith("/"))
      {
        length++;
        value = value.substring(1);
      }

      int index = value.indexOf("/");

      if (index > 0)
      {
        String dir = path.toString().substring(0, length + index);

        directories.add(dir);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param nodeid
   */
  @Override
  public void end(Nodeid nodeid) {}

  /**
   * Method description
   *
   *
   * @param file
   */
  @Override
  public void file(HgFileRevision file)
  {
    Path path = file.getPath();
    String value = path.toString();

    if (value.startsWith(directory))
    {
      value = value.substring(directory.length());

      if (value.startsWith("/"))
      {
        value = value.substring(1);
      }

      int index = value.indexOf("/");

      if (index <= 0)
      {
        try
        {
          FileObject fo = createFilObject(file.getPath(), false);
          HgDataFile data = repository.getFileNode(path);
          Nodeid node = data.getChangesetRevision(file.getRevision());
          RawChangeset changeset = repository.getChangelog().changeset(node);
          Date date = changeset.date();

          if (date != null)
          {
            fo.setLastModified(date.getTime());
          }

          fo.setLength(data.length(file.getRevision()));
          fo.setDescription(changeset.comment());
          fileObjects.add(fo);
        }
        catch (Exception ex)
        {
          logger.error("could not read revision of manifest", ex);
        }
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<FileObject> getFileObjects()
  {
    for (String dir : directories)
    {
      FileObject fo = new FileObject();

      fo.setDirectory(true);
      fo.setPath(dir);
      fo.setName(extractName(dir));
      fileObjects.add(fo);
    }

    return fileObjects;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   * @param directory
   *
   * @return
   */
  private FileObject createFilObject(Path path, boolean directory)
  {
    String p = path.toString();

    if (logger.isTraceEnabled())
    {
      logger.trace("create file object for {}", path);
    }

    FileObject fo = new FileObject();

    fo.setPath(p);
    fo.setName(extractName(p));
    fo.setDirectory(directory);

    return fo;
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String extractName(String path)
  {
    String name = path;
    int index = path.lastIndexOf("/");

    if (index > 0)
    {
      name = path.substring(index + 1);
    }

    return name;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<String> directories = new HashSet<String>();

  /** Field description */
  private String directory;

  /** Field description */
  private List<FileObject> fileObjects;

  /** Field description */
  private HgRepository repository;
}
