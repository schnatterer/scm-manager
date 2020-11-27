/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.HandlerEventType;
import sonia.scm.PageResult;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.CustomNamespaceStrategy;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.user.User;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static java.util.stream.Stream.of;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class RepositoryRootResourceTest extends RepositoryTestBase {

  private static final String REALM = "AdminRealm";

  private final RestDispatcher dispatcher = new RestDispatcher();

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private RepositoryHandler repositoryHandler;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;
  @Mock
  private ScmPathInfo uriInfo;
  @Mock
  private RepositoryInitializer repositoryInitializer;
  @Mock
  private ScmConfiguration configuration;
  @Mock
  private Set<NamespaceStrategy> strategies;
  @Mock
  private ScmEventBus eventBus;

  @Captor
  private ArgumentCaptor<Predicate<Repository>> filterCaptor;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl repositoryToDtoMapper;
  @InjectMocks
  private RepositoryDtoToRepositoryMapperImpl dtoToRepositoryMapper;

  @Before
  public void prepareEnvironment() {
    initMocks(this);
    super.repositoryToDtoMapper = repositoryToDtoMapper;
    super.dtoToRepositoryMapper = dtoToRepositoryMapper;
    super.manager = repositoryManager;
    RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper = new RepositoryCollectionToDtoMapper(repositoryToDtoMapper, resourceLinks);
    super.repositoryCollectionResource = new RepositoryCollectionResource(repositoryManager, repositoryCollectionToDtoMapper, dtoToRepositoryMapper, resourceLinks, repositoryInitializer);
    super.repositoryImportResource = new RepositoryImportResource(repositoryManager, serviceFactory, resourceLinks, eventBus);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(scmPathInfoStore.get()).thenReturn(uriInfo);
    when(uriInfo.getApiRestUri()).thenReturn(URI.create("/x/y"));
    doReturn(ImmutableSet.of(new CustomNamespaceStrategy()).iterator()).when(strategies).iterator();
    SimplePrincipalCollection trillian = new SimplePrincipalCollection("trillian", REALM);
    trillian.add(new User("trillian"), REALM);
    shiro.setSubject(
      new Subject.Builder()
        .principals(trillian)
        .authenticated(true)
        .buildSubject());
  }

  @Test
  public void shouldFailForNotExistingRepository() throws URISyntaxException {
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);
    mockRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/other");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldFindExistingRepository() throws URISyntaxException, UnsupportedEncodingException {
    mockRepository("space", "repo");
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"repo\""));
  }

  @Test
  public void shouldGetAll() throws URISyntaxException, UnsupportedEncodingException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(mockRepository("space", "repo"));
    when(repositoryManager.getPage(any(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"repo\""));
  }

  @Test
  public void shouldCreateFilterForSearch() throws URISyntaxException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(mockRepository("space", "repo"));
    when(repositoryManager.getPage(filterCaptor.capture(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "?q=Rep");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "all_repos", "x")));
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "x", "repository")));
    assertFalse(filterCaptor.getValue().test(new Repository("rep", "rep", "x", "x")));
  }

  @Test
  public void shouldCreateFilterForNamespace() throws URISyntaxException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(mockRepository("space", "repo"));
    when(repositoryManager.getPage(filterCaptor.capture(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "space", "repo")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "spaceX", "repository")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "x", "space")));
  }

  @Test
  public void shouldCreateFilterForNamespaceWithQuery() throws URISyntaxException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(mockRepository("space", "repo"));
    when(repositoryManager.getPage(filterCaptor.capture(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space?q=Rep");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "space", "repo")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "space", "other")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "Rep", "Repository")));
  }

  @Test
  public void shouldHandleUpdateForNotExistingRepository() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldHandleUpdateForExistingRepository() throws Exception {
    mockRepository("space", "repo");

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).modify(anyObject());
  }

  @Test
  public void shouldHandleUpdateForConcurrentlyChangedRepository() throws Exception {
    mockRepository("space", "repo", 1337);

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_CONFLICT, response.getStatus());
    assertThat(response.getContentAsString()).contains("space/repo");
    verify(repositoryManager, never()).modify(anyObject());
  }

  @Test
  public void shouldHandleUpdateForExistingRepositoryForChangedNamespace() throws Exception {
    mockRepository("wrong", "repo");

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "wrong/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_BAD_REQUEST, response.getStatus());
    verify(repositoryManager, never()).modify(anyObject());
  }

  @Test
  public void shouldHandleDeleteForExistingRepository() throws Exception {
    mockRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest.delete("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).delete(anyObject());
  }

  @Test
  public void shouldCreateNewRepositoryInCorrectNamespace() throws Exception {
    when(repositoryManager.create(any())).thenAnswer(invocation -> {
      Repository repository = (Repository) invocation.getArguments()[0];
      repository.setNamespace("otherspace");
      return repository;
    });

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repositoryJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY)
      .content(repositoryJson);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
    assertEquals("/v2/repositories/otherspace/repo", response.getOutputHeaders().get("Location").get(0).toString());
    verify(repositoryManager).create(any(Repository.class));
    verify(repositoryInitializer, never()).initialize(any(Repository.class), anyMap());
  }

  @Test
  public void shouldCreateNewRepositoryAndInitialize() throws Exception {
    when(repositoryManager.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repositoryJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "?initialize=true")
      .contentType(VndMediaType.REPOSITORY)
      .content(repositoryJson);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());

    ArgumentCaptor<Repository> captor = ArgumentCaptor.forClass(Repository.class);
    verify(repositoryInitializer).initialize(captor.capture(), anyMap());

    Repository repository = captor.getValue();
    assertEquals("space", repository.getNamespace());
    assertEquals("repo", repository.getName());
  }

  @Test
  public void shouldSetCurrentUserAsOwner() throws Exception {
    ArgumentCaptor<Repository> createCaptor = ArgumentCaptor.forClass(Repository.class);
    when(repositoryManager.create(createCaptor.capture())).thenAnswer(invocation -> {
      Repository repository = (Repository) invocation.getArguments()[0];
      repository.setNamespace("otherspace");
      return repository;
    });

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repositoryJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY)
      .content(repositoryJson);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(createCaptor.getValue().getPermissions())
      .hasSize(1)
      .allSatisfy(p -> {
        assertThat(p.getName()).isEqualTo("trillian");
        assertThat(p.getRole()).isEqualTo("OWNER");
      });
  }

  @Test
  public void shouldCreateArrayOfProtocolUrls() throws Exception {
    mockRepository("space", "repo");
    when(service.getSupportedProtocols()).thenReturn(of(new MockScmProtocol("http", "http://"), new MockScmProtocol("ssh", "ssh://")));
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"protocol\":[{\"href\":\"http://\",\"name\":\"http\"},{\"href\":\"ssh://\",\"name\":\"ssh\"}]"));
  }

  @Test
  public void shouldRenameRepository() throws Exception {
    String namespace = "space";
    String name = "repo";
    Repository repository1 = mockRepository(namespace, name);
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository1);

    URL url = Resources.getResource("sonia/scm/api/v2/rename-repo.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/rename")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).rename(repository1, "space", "x");
  }

  @Test
  public void shouldImportRepositoryFromUrl() throws URISyntaxException, IOException {
    when(manager.getHandler("git")).thenReturn(repositoryHandler);
    when(repositoryHandler.getType()).thenReturn(new RepositoryType("git", "git", ImmutableSet.of(Command.PULL)));
    when(manager.create(any(Repository.class), any())).thenReturn(RepositoryTestData.create42Puzzle());

    URL url = Resources.getResource("sonia/scm/api/v2/import-repo.json");
    byte[] importRequest = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/git/url")
      .contentType(VndMediaType.REPOSITORY)
      .content(importRequest);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_CREATED, response.getStatus());
  }

  @Test
  public void shouldImportRepositoryFromUrlWithCredentials() throws URISyntaxException, IOException {
    when(manager.getHandler("git")).thenReturn(repositoryHandler);
    when(repositoryHandler.getType()).thenReturn(new RepositoryType("git", "git", ImmutableSet.of(Command.PULL)));
    when(manager.create(any(Repository.class), any())).thenReturn(RepositoryTestData.create42Puzzle());

    URL url = Resources.getResource("sonia/scm/api/v2/import-repo-with-credentials.json");
    byte[] importRequest = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/git/url")
      .contentType(VndMediaType.REPOSITORY)
      .content(importRequest);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_CREATED, response.getStatus());
  }

  @Test
  public void shouldFailOnImportRepositoryFromUrl() throws URISyntaxException, IOException {
    when(manager.getHandler("git")).thenReturn(repositoryHandler);
    when(repositoryHandler.getType()).thenReturn(new RepositoryType("git", "git", ImmutableSet.of(Command.PULL)));
    doThrow(ImportFailedException.class).when(manager).create(any(Repository.class), any());

    URL url = Resources.getResource("sonia/scm/api/v2/import-repo.json");
    byte[] importRequest = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/git/url")
      .contentType(VndMediaType.REPOSITORY)
      .content(importRequest);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(500, response.getStatus());
  }

  @Test
  public void shouldPullChangesFromRemoteUrl() {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);

    Repository repository = RepositoryTestData.createHeartOfGold();
    RepositoryImportResource.RepositoryImportDto repositoryImportDto = new RepositoryImportResource.RepositoryImportDto();
    repositoryImportDto.setUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
    repositoryImportDto.setNamespace("scmadmin");
    repositoryImportDto.setName("scm-manager");

    Consumer<Repository> repositoryConsumer = repositoryImportResource.pullChangesFromRemoteUrl(repositoryImportDto);
    repositoryConsumer.accept(repository);

    verify(eventBus).post(new RepositoryImportEvent(HandlerEventType.CREATE, repository, false));
  }

  @Test
  public void shouldPullChangesFromRemoteUrlWithCredentials() {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);

    Repository repository = RepositoryTestData.createHeartOfGold();
    RepositoryImportResource.RepositoryImportDto repositoryImportDto = new RepositoryImportResource.RepositoryImportDto();
    repositoryImportDto.setUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
    repositoryImportDto.setNamespace("scmadmin");
    repositoryImportDto.setName("scm-manager");
    repositoryImportDto.setUsername("trillian");
    repositoryImportDto.setPassword("secret");

    Consumer<Repository> repositoryConsumer = repositoryImportResource.pullChangesFromRemoteUrl(repositoryImportDto);
    repositoryConsumer.accept(repository);

    verify(pullCommandBuilder).withUsername("trillian");
    verify(pullCommandBuilder).withPassword("secret");
    verify(eventBus).post(new RepositoryImportEvent(HandlerEventType.CREATE, repository, false));
  }

  private PageResult<Repository> createSingletonPageResult(Repository repository) {
    return new PageResult<>(singletonList(repository), 0);
  }

  private Repository mockRepository(String namespace, String name) {
    return mockRepository(namespace, name, 0);
  }

  private Repository mockRepository(String namespace, String name, long lastModified) {
    Repository repository = new Repository();
    repository.setNamespace(namespace);
    repository.setName(name);
    if (lastModified > 0) {
      repository.setLastModified(lastModified);
    }
    String id = namespace + "-" + name;
    repository.setId(id);
    when(repositoryManager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    when(repositoryManager.get(id)).thenReturn(repository);
    return repository;
  }
}
