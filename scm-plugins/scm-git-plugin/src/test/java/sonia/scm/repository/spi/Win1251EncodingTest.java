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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.BeforeClass;
import org.junit.Test;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.RepositoryException;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.URL;

import java.nio.charset.Charset;

import java.util.zip.ZipInputStream;

/**
 *
 * @author Sebastian Sdorra
 *
 * https://bitbucket.org/sdorra/scm-manager/issue/505/non-ascii-characters-shows-as-a-mess-when
 */
public class Win1251EncodingTest extends AbstractGitCommandTestBase
{

  /** Field description */
  private static final ScmConfiguration config = new ScmConfiguration();

  /** Field description */
  private static final Charset charset = Charset.forName("Windows-1251");

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @BeforeClass
  public static void prepareConfig()
  {
    config.setDefaultRepositoryEncoding(charset.name());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testBlame() throws IOException, RepositoryException
  {
    BlameCommandRequest req = new BlameCommandRequest();

    req.setPath("Новый текстовый документ.txt");

    BlameLine line = new GitBlameCommand(config, createContext(),
                       repository).getBlameResult(req).getLine(0);

    //J-
    assertEquals(
      "Однажды, в студеную зимнюю пору... (Из поэмы \"Крестьянские дети\")",
      line.getCode()
    );
    assertEquals("Начальный коммит", line.getDescription());
    //J+
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testBrowser() throws IOException, RepositoryException
  {

    BrowserResult result =
      new GitBrowseCommand(createContext(),
        repository).getBrowserResult(new BrowseCommandRequest());
    FileObject file = result.getFiles().get(0);

    assertEquals("Новый текстовый документ.txt", file.getName());
    assertEquals("Новый текстовый документ.txt", file.getPath());
    assertEquals("Начальный коммит", file.getDescription());

  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testCat() throws IOException, RepositoryException
  {
    CatCommandRequest req = new CatCommandRequest();

    req.setPath("Новый текстовый документ.txt");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    new GitCatCommand(createContext(), repository).getCatResult(req, baos);

    String cat = baos.toString(charset.name());

    //J-
    assertThat(
      cat, 
      allOf(
        containsString("Автор: Николай Некрасов"),
        containsString("Однажды, в студеную зимнюю пору")
      )
    );
    //J+
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testDiff() throws IOException, RepositoryException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DiffCommandRequest request = new DiffCommandRequest();

    request.setRevision("368a4d1643da41069ee8e62fe005022daff507ee");
    new GitDiffCommand(config, createContext(),
      repository).getDiffResult(request, baos);
    //J-
    assertThat(
      baos.toString("UTF-8"), 
      allOf(
        containsString("+Однажды, в студеную зимнюю пору"),
        containsString("+Лошадка, везущая хворосту воз.")
      )
    );
    //J+
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testLog() throws IOException, RepositoryException
  {
    ChangesetPagingResult cpr =
      new GitLogCommand(createContext(),
        repository).getChangesets(new LogCommandRequest());

    Changeset changeset = cpr.getChangesets().get(0);

    assertNotNull(changeset);
    assertEquals("Начальный коммит", changeset.getDescription());
    assertEquals("Новый текстовый документ.txt",
      changeset.getModifications().getAdded().get(0));
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  protected ZipInputStream open(URL url) throws IOException
  {
    return open(url, charset);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getZippedRepositoryResource()
  {
    return "sonia/scm/repository/spi/scm-git-spi-issue-505.zip";
  }
}
