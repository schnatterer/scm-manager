package sonia.scm.api.v2.resources;

import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

/**
 *  RESTful Web Service Resource to manage users.
 */
@Path(UserRootResource.USERS_PATH_V2)
public class UserRootResource {

  static final String USERS_PATH_V2 = "v2/users/";

  private final Provider<UserCollectionResource> userCollectionResource;
  private final UserResourceFactory userResourceFactory;
  private final UserManager manager;

  @Inject
  public UserRootResource(Provider<UserCollectionResource> userCollectionResource,
    UserResourceFactory userResourceFactory, UserManager manager) {
    this.userCollectionResource = userCollectionResource;
    this.userResourceFactory = userResourceFactory;
    this.manager = manager;
  }

  @Path("")
  public UserCollectionResource getUserCollectionResource() {
    return userCollectionResource.get();
  }

  /**
   * @param id the id/name of the user
   */
  @Path("{id}")
  public UserResource getUserResource(@PathParam("id") String id, @Context Request request) {
    User user = manager.get(id);
    if (user == null && !"DELETE".equals(request.getMethod())) {
      throw new NotFoundException();
    }
    return userResourceFactory.create(user);
  }
}
