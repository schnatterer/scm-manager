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

import com.google.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;
import sonia.scm.repository.client.RepositoryClientFactory;
import sonia.scm.store.MemoryStoreFactory;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class HgChangesetViewerTestBase
{

  /**
   * Method description
   *
   *
   * @param handler
   * @param repositoryDirectory
   *
   * @return
   */
  protected abstract ChangesetViewer createChangesetViewer(
          HgRepositoryHandler handler, File repositoryDirectory);

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Before
  public void before() throws IOException, RepositoryClientException
  {
    Provider<HgContext> contextProvider = new Provider<HgContext>()
    {
      @Override
      public HgContext get()
      {
        return new HgContext();
      }
    };

    repositoryDirectory = tempFolder.newFolder();

    SCMContextProvider context = mock(SCMContextProvider.class);

    when(context.getBaseDirectory()).thenReturn(repositoryDirectory);
    when(context.getVersion()).thenReturn("X.Test");
    handler = new HgRepositoryHandler(new MemoryStoreFactory(),
                                      new DefaultFileSystem(), contextProvider);
    handler.init(context);
    client = RepositoryClientFactory.createClient("hg", repositoryDirectory,
            "http://www.scm-manager.de/hg/dummy", null, null, false);
    client.init();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   * @throws RepositoryException
   */
  @Test
  public void simpleTest()
          throws RepositoryClientException, IOException, RepositoryException
  {
    String a1 = createDummyFile("a1").getName();
    String a2 = createDummyFile("a2").getName();

    client.add(a1, a2);
    client.commit("added files a1 and a2");

    ChangesetViewer viewer = createChangesetViewer(handler,
                               repositoryDirectory);
    ChangesetPagingResult result = viewer.getChangesets(0, 1);

    checkPagingResult(result, 1);

    Changeset changeset = result.iterator().next();

    checkChangeset(changeset, "added files a1 and a2");

    List<String> added = changeset.getModifications().getAdded();

    checkModificationList(added, "a1", "a2");
  }

  /**
   * Method description
   *
   *
   * @param changeset
   * @param description
   */
  private void checkChangeset(Changeset changeset, String description)
  {
    assertNotNull(changeset);
    assertNotNull(changeset.getId());
    assertEquals(description, changeset.getDescription());
    assertNotNull(changeset.getDate());
    assertNotNull(changeset.getModifications());
  }

  /**
   * Method description
   *
   *
   * @param mods
   * @param files
   */
  private void checkModificationList(List<String> mods, String... files)
  {
    assertNotNull(mods);
    assertEquals(files.length, mods.size());

    for (String f : files)
    {
      assertTrue(mods.contains(f));
    }
  }

  /**
   * Method description
   *
   *
   * @param result
   * @param total
   */
  private void checkPagingResult(ChangesetPagingResult result, int total)
  {
    assertNotNull(result);
    assertEquals(result.getTotal(), 1);
    assertNotNull(result.getChangesets());
    assertNotNull(result.iterator());
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private File createDummyFile() throws IOException
  {
    return createDummyFile(String.valueOf(counter++));
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  private File createDummyFile(String name) throws IOException
  {
    File file = new File(repositoryDirectory, name);
    byte[] data = new byte[1024];

    random.nextBytes(data);

    FileOutputStream fos = null;

    try
    {
      fos = new FileOutputStream(file);
      fos.write(data);
    }
    finally
    {
      IOUtil.close(fos);
    }

    return file;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryClient client;

  /** Field description */
  private int counter = 0;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private Random random = new Random();

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /** Field description */
  private File repositoryDirectory;
}
