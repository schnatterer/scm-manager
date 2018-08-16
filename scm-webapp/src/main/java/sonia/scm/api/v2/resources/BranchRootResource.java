package sonia.scm.api.v2.resources;

import com.google.inject.assistedinject.Assisted;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.Branches;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.CommandNotSupportedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class BranchRootResource {

  private final RepositoryServiceFactory servicefactory;
  private final BranchToBranchDtoMapper branchToDtoMapper;
  private final BranchCollectionToDtoMapper branchCollectionToDtoMapper;

  private final Repository repository;

  @Inject
  public BranchRootResource(
    RepositoryServiceFactory servicefactory,
    BranchToBranchDtoMapper branchToDtoMapper,
    BranchCollectionToDtoMapper branchCollectionToDtoMapper,
    @Assisted Repository repository) {
    this.servicefactory = servicefactory;
    this.branchToDtoMapper = branchToDtoMapper;
    this.branchCollectionToDtoMapper = branchCollectionToDtoMapper;
    this.repository = repository;
  }

  /**
   * Returns a branch for a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param branchName the name of the branch
   *
   */
  @GET
  @Path("{branch}")
  @Produces(VndMediaType.BRANCH)
  @TypeHint(BranchDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "branches not supported for given repository"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the branch"),
    @ResponseCode(code = 404, condition = "not found, no branch with the specified name for the repository available or repository not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("branch") String branchName) throws IOException, RepositoryException {
    try (RepositoryService repositoryService = servicefactory.create(repository)) {
      Branches branches = repositoryService.getBranchesCommand().getBranches();
      return branches.getBranches()
        .stream()
        .filter(branch -> branchName.equals(branch.getName()))
        .findFirst()
        .map(branch -> branchToDtoMapper.map(branch, repository.getNamespaceAndName()))
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();
    } catch (CommandNotSupportedException ex) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    } catch (RepositoryNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @Path("{branch}/changesets/")
  @GET
  public Response history(@PathParam("branch") String branchName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the branches for a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   */
  @GET
  @Path("")
  @Produces(VndMediaType.BRANCH_COLLECTION)
  @TypeHint(CollectionDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "branches not supported for given repository"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no repository found for the given namespace and name"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll() throws IOException, RepositoryException {
    try (RepositoryService repositoryService = servicefactory.create(repository)) {
      Branches branches = repositoryService.getBranchesCommand().getBranches();
      return Response.ok(branchCollectionToDtoMapper.map(repository.getNamespaceAndName(), branches.getBranches())).build();
    } catch (CommandNotSupportedException ex) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    } catch (RepositoryNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }
}
