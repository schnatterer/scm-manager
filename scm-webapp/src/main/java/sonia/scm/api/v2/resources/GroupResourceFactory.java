package sonia.scm.api.v2.resources;

import sonia.scm.group.Group;

public interface GroupResourceFactory {
  GroupResource create(Group group);
}
