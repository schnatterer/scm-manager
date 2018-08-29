package sonia.scm.it;

import org.apache.http.HttpStatus;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.RestUtil.given;
import static sonia.scm.it.ScmTypes.availableScmTypes;

@RunWith(Parameterized.class)
public class RepositoryAccessITCase {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private final String repositoryType;
  private File folder;

  public RepositoryAccessITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return availableScmTypes();
  }

  @Before
  public void initClient() {
    TestData.createDefault();
    folder = tempFolder.getRoot();
  }

  @Test
  public void shouldFindBranches() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);

    Assume.assumeTrue("There are no branches for " + repositoryType, repositoryClient.isCommandSupported(ClientCommand.BRANCH));

    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "a.txt", "a");

    String branchesUrl = given()
      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.branches.href");

    Object branchName = given()
      .when()
      .get(branchesUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_embedded.branches[0].name");

    assertNotNull(branchName);
  }

  @Test
  public void shouldFindChangesets() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);

    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "a.txt", "a");
    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "b.txt", "b");

    String changesetsUrl = given()
      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.changesets.href");

    List changesets = given()
      .when()
      .get(changesetsUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_embedded.changesets.id");

    assertThat(changesets).size().isBetween(2, 3); // svn has an implicit root revision '0' that is extra to the two commits
  }
}
