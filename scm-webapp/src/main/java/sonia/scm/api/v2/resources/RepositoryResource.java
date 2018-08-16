package sonia.scm.api.v2.resources;

import com.google.inject.assistedinject.Assisted;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryIsNotArchivedException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RepositoryResource {

  private final RepositoryToRepositoryDtoMapper repositoryToDtoMapper;
  private final RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;

  private final RepositoryManager manager;
  private final SingleResourceManagerAdapter<Repository, RepositoryDto, RepositoryException> adapter;
  private final Provider<TagRootResource> tagRootResource;
  private final BranchRootResourceFactory branchRootResourceFactory;
  private final Provider<ChangesetRootResource> changesetRootResource;
  private final Provider<SourceRootResource> sourceRootResource;
  private final Provider<PermissionRootResource> permissionRootResource;

  private final Repository repository;

  @Inject
  public RepositoryResource(
    RepositoryToRepositoryDtoMapper repositoryToDtoMapper,
    RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, RepositoryManager manager,
    Provider<TagRootResource> tagRootResource,
    BranchRootResourceFactory branchRootResourceFactory,
    Provider<ChangesetRootResource> changesetRootResource,
    Provider<SourceRootResource> sourceRootResource,
    Provider<PermissionRootResource> permissionRootResource,
    @Nullable @Assisted Repository repository) {
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.manager = manager;
    this.repositoryToDtoMapper = repositoryToDtoMapper;
    this.adapter = new SingleResourceManagerAdapter<>(manager, Repository.class, this::handleNotArchived);
    this.tagRootResource = tagRootResource;
    this.branchRootResourceFactory = branchRootResourceFactory;
    this.changesetRootResource = changesetRootResource;
    this.sourceRootResource = sourceRootResource;
    this.permissionRootResource = permissionRootResource;
    this.repository = repository;
  }

  /**
   * Returns a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY)
  @TypeHint(RepositoryDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the repository"),
    @ResponseCode(code = 404, condition = "not found, no repository with the specified name available in the namespace"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get() {
    return adapter.get(repository, repositoryToDtoMapper::map);
  }

  /**
   * Deletes a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   */
  @DELETE
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success or nothing to delete"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete() {
    return adapter.delete(repository);
  }

  /**
   * Modifies the given repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param repositoryDto repository object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.REPOSITORY)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 400, condition = "Invalid body, e.g. illegal change of namespace or name"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no repository with the specified namespace and name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(RepositoryDto repositoryDto) {
    return adapter.update(
      repository,
      existing -> dtoToRepositoryMapper.map(repositoryDto, existing.getId()),
      nameAndNamespaceStaysTheSame()
    );
  }

  @Path("tags/")
  public TagRootResource tags() {
    return tagRootResource.get();
  }

  @Path("branches/")
  public BranchRootResource branches() {
    return branchRootResourceFactory.create(repository);
  }

  @Path("changesets/")
  public ChangesetRootResource changesets() {
    return changesetRootResource.get();
  }

  @Path("sources/")
  public SourceRootResource sources() {
    return sourceRootResource.get();
  }

  @Path("permissions/")
  public PermissionRootResource permissions() {
    return permissionRootResource.get();
  }

  private Optional<Response> handleNotArchived(Throwable throwable) {
    if (throwable instanceof RepositoryIsNotArchivedException) {
      return Optional.of(Response.status(Response.Status.PRECONDITION_FAILED).build());
    } else {
      return Optional.empty();
    }
  }

  private Supplier<Optional<Repository>> loadBy(String namespace, String name) {
    return () -> Optional.ofNullable(manager.get(new NamespaceAndName(namespace, name)));
  }

  private Predicate<Repository> nameAndNamespaceStaysTheSame() {
    return changed -> changed.getName().equals(repository.getName()) && changed.getNamespace().equals(repository.getNamespace());
  }
}
