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



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.IntegrationTestUtil.createResource;
import static sonia.scm.it.IntegrationTestUtil.readJson;
import static sonia.scm.it.RepositoryITUtil.createRepository;
import static sonia.scm.it.RepositoryITUtil.deleteRepository;

//~--- JDK imports ------------------------------------------------------------

@RunWith(Parameterized.class)
public class GetRepositoriesITCase extends AbstractAdminITCaseBase {

  private RepositoryDto repository;
  private final String repositoryType;

  public GetRepositoriesITCase(String repositoryType)
  {
    this.repositoryType = repositoryType;
  }

  @Parameters
  public static Collection<String[]> createParameters() {
    Collection<String[]> params = new ArrayList<String[]>();

    params.add(new String[] { "git" });
    params.add(new String[] { "svn" });

    if (IOUtil.search("hg") != null) {
      params.add(new String[] { "hg" });
    }

    return params;
  }

  @After
  public void cleanup() {
    if (repository != null) {
      deleteRepository(client, repository);
    }
  }

  @Test
  public void testGetById() throws IOException {
    repository = createRepository(client, readJson("repository-" + repositoryType + ".json"));

    assertEquals("HeartOfGold-" + repositoryType, repository.getName());
  }

  @Test
  public void testGetByNamespaceAndName() throws IOException {
    testGetByNamespaceAndName(readJson("repository-" + repositoryType + ".json"));
  }

  private void testGetByNamespaceAndName(String json)
  {
    repository = createRepository(client, json);

    String name = repository.getName();
    WebResource.Builder wr = createResource(client, "repositories/scmadmin/" + name);
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);

    RepositoryDto r = response.getEntity(RepositoryDto.class);

    response.close();
    assertNotNull(r);
    assertEquals(repository.getName(), r.getName());
    assertEquals(repository.getType(), r.getType());
  }
}
