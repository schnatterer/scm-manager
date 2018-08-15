package sonia.scm.api.v2.resources;

import sonia.scm.user.User;

public interface UserResourceFactory {
  UserResource create(User user);
}
