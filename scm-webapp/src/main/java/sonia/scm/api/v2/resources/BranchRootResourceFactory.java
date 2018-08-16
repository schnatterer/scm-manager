package sonia.scm.api.v2.resources;

import sonia.scm.repository.Repository;

public interface BranchRootResourceFactory {
  BranchRootResource create(Repository repository);
}
