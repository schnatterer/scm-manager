package sonia.scm.api.v2.resources;

import sonia.scm.repository.Repository;

public interface RepositoryResourceFactory {
  RepositoryResource create(Repository repository);
}
