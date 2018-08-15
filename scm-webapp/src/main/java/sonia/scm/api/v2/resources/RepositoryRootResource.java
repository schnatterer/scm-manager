package sonia.scm.api.v2.resources;

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

/**
 *  RESTful Web Service Resource to manage repositories.
 */
@Path(RepositoryRootResource.REPOSITORIES_PATH_V2)
public class RepositoryRootResource {
  static final String REPOSITORIES_PATH_V2 = "v2/repositories/";

  private final RepositoryResourceFactory repositoryResourceFactory;
  private final Provider<RepositoryCollectionResource> repositoryCollectionResource;
  private final RepositoryManager manager;

  @Inject
  public RepositoryRootResource(RepositoryResourceFactory repositoryResourceFactory, Provider<RepositoryCollectionResource> repositoryCollectionResource, RepositoryManager manager) {
    this.repositoryResourceFactory = repositoryResourceFactory;
    this.repositoryCollectionResource = repositoryCollectionResource;
    this.manager = manager;
  }

  /**
   * @param namespace the namespace of the repository
   * @param name the name of the repository
   */
  @Path("{namespace}/{name}")
  public RepositoryResource getRepositoryResource(@PathParam("namespace") String namespace, @PathParam("name") String name, @Context Request request) {
    Repository repository = manager.get(new NamespaceAndName(namespace, name));
    if (repository == null && !"DELETE".equals(request.getMethod())) {
      throw new NotFoundException();
    }
    return repositoryResourceFactory.create(repository);
  }

  @Path("")
  public RepositoryCollectionResource getRepositoryCollectionResource() {
    return repositoryCollectionResource.get();
  }
}
