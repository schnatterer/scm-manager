/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
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
 */


package sonia.scm.repository.spi;

import org.junit.Test;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.spi.SyncAsyncExecutors.AsyncExecutorStepper;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.OptionalLong.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.spi.SyncAsyncExecutors.stepperAsynchronousExecutor;
import static sonia.scm.repository.spi.SyncAsyncExecutors.synchronousExecutor;

/**
 * Unit tests for {@link GitBrowseCommand}.
 *
 * @author Sebastian Sdorra
 */
public class GitBrowseCommandTest extends AbstractGitCommandTestBase {

  private final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);

  @Test
  public void testDefaultBranch() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setPath("a.txt");
    BrowserResult result = createCommand().getBrowserResult(request);
    FileObject fileObject = result.getFile();
    assertEquals("a.txt", fileObject.getName());
    assertFalse(fileObject.isTruncated());
  }

  @Test
  public void testDefaultDefaultBranch() throws IOException {
    // without default branch, the repository head should be used
    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();
    assertNotNull(foList);
    assertFalse(foList.isEmpty());

    assertThat(foList)
      .extracting("name")
      .containsExactly("a.txt", "b.txt", "c", "f.txt");
  }

  @Test
  public void testExplicitDefaultBranch() throws IOException {
    createContext().setConfig(new GitRepositoryConfig("test-branch"));

    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();
    assertThat(foList)
      .extracting("name")
      .containsExactly("a.txt", "c");
  }

  @Test
  public void testBrowse() throws IOException {
    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();

    FileObject a = findFile(foList, "a.txt");
    FileObject c = findFile(foList, "c");

    assertFalse(a.isDirectory());
    assertEquals("a.txt", a.getName());
    assertEquals("a.txt", a.getPath());
    assertEquals("added new line for blame", a.getDescription().get());
    assertTrue(a.getLength().getAsLong() > 0);
    checkDate(a.getCommitDate().getAsLong());

    assertTrue(c.isDirectory());
    assertEquals("c", c.getName());
    assertEquals("c", c.getPath());
  }

  @Test
  public void testAsynchronousBrowse() throws IOException {
    try (AsyncExecutorStepper executor = stepperAsynchronousExecutor()) {
      GitBrowseCommand command = new GitBrowseCommand(createContext(), repository, null, executor);
      List<BrowserResult> updatedResults = new LinkedList<>();
      BrowseCommandRequest request = new BrowseCommandRequest(updatedResults::add);
      FileObject root = command.getBrowserResult(request).getFile();
      assertNotNull(root);

      Collection<FileObject> foList = root.getChildren();

      FileObject a = findFile(foList, "a.txt");
      FileObject b = findFile(foList, "b.txt");

      assertTrue(a.isPartialResult());
      assertFalse("expected empty name before commit could have been read", a.getDescription().isPresent());
      assertFalse("expected empty date before commit could have been read", a.getCommitDate().isPresent());
      assertTrue(b.isPartialResult());
      assertFalse("expected empty name before commit could have been read", b.getDescription().isPresent());
      assertFalse("expected empty date before commit could have been read", b.getCommitDate().isPresent());

      executor.next();

      assertEquals(1, updatedResults.size());
      assertFalse(a.isPartialResult());
      assertNotNull("expected correct name after commit could have been read", a.getDescription());
      assertTrue("expected correct date after commit could have been read", a.getCommitDate().isPresent());
      assertTrue(b.isPartialResult());
      assertFalse("expected empty name before commit could have been read", b.getDescription().isPresent());
      assertFalse("expected empty date before commit could have been read", b.getCommitDate().isPresent());

      executor.next();

      assertEquals(2, updatedResults.size());
      assertFalse(b.isPartialResult());
      assertNotNull("expected correct name after commit could have been read", b.getDescription());
      assertTrue("expected correct date after commit could have been read", b.getCommitDate().isPresent());
    }
  }

  @Test
  public void testBrowseSubDirectory() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setPath("c");

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).hasSize(2);

    FileObject d = findFile(foList, "d.txt");
    FileObject e = findFile(foList, "e.txt");

    assertFalse(d.isDirectory());
    assertEquals("d.txt", d.getName());
    assertEquals("c/d.txt", d.getPath());
    assertEquals("added file d and e in folder c", d.getDescription().get());
    assertTrue(d.getLength().getAsLong() > 0);
    checkDate(d.getCommitDate().getAsLong());

    assertFalse(e.isDirectory());
    assertEquals("e.txt", e.getName());
    assertEquals("c/e.txt", e.getPath());
    assertEquals("added file d and e in folder c", e.getDescription().get());
    assertTrue(e.getLength().getAsLong() > 0);
    checkDate(e.getCommitDate().getAsLong());
  }

  @Test
  public void testRecursive() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("a.txt", "b.txt", "c", "f.txt");

    FileObject c = findFile(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");
  }

  @Test
  public void testLfsSupport() throws IOException {
    BlobStore blobStore = mock(BlobStore.class);
    Blob blob = mock(Blob.class);
    when(lfsBlobStoreFactory.getLfsBlobStore(repository)).thenReturn(blobStore);
    when(blobStore.get("d2252bd9fde1bb2ae7531b432c48262c3cbe4df4376008986980de40a7c9cf8b")).thenReturn(blob);
    when(blob.getSize()).thenReturn(42L);

    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setRevision("lfs-test");
    FileObject root = createCommand().getBrowserResult(request).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();
    assertThat(foList)
      .filteredOn(f -> "lfs-image.png".equals(f.getName()))
      .extracting("length")
      .containsExactly(of(42L));
  }

  @Test
  public void testBrowseLimit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(2);
    FileObject root = createCommand()
      .getBrowserResult(request).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).hasSize(2);
    assertTrue(root.isTruncated());
  }

  @Test
  public void testBrowseProceedFrom() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(2);
    request.setProceedFrom(2);
    FileObject root = createCommand()
      .getBrowserResult(request).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).extracting("name").contains("c", "f.txt");
    assertFalse(root.isTruncated());
  }

  private FileObject findFile(Collection<FileObject> foList, String name) {
    return foList.stream()
      .filter(f -> name.equals(f.getName()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("file " + name + " not found"));
  }

  private GitBrowseCommand createCommand() {
    return new GitBrowseCommand(createContext(), repository, lfsBlobStoreFactory, synchronousExecutor());
  }
}
