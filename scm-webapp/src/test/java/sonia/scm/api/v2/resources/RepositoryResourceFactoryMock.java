package sonia.scm.api.v2.resources;

import sonia.scm.repository.RepositoryManager;

import javax.inject.Provider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryResourceFactoryMock {
  public static RepositoryResourceFactory get(RepositoryToRepositoryDtoMapper repositoryToDtoMapper,
    RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, RepositoryManager manager,
    Provider<TagRootResource> tagRootResource,
    Provider<BranchRootResource> branchRootResource,
    Provider<ChangesetRootResource> changesetRootResource,
    Provider<SourceRootResource> sourceRootResource,
    Provider<PermissionRootResource> permissionRootResource) {
    RepositoryResourceFactory repositoryResourceFactory = mock(RepositoryResourceFactory.class);
    when(repositoryResourceFactory.create(any())).thenAnswer(invocation -> new RepositoryResource(repositoryToDtoMapper, dtoToRepositoryMapper, manager, tagRootResource, branchRootResource, changesetRootResource, sourceRootResource, permissionRootResource, invocation.getArgument(0)));
    return repositoryResourceFactory;
  }
}
