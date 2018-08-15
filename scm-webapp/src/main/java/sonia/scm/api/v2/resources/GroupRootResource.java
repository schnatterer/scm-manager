package sonia.scm.api.v2.resources;

import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

/**
 * RESTful Web Service Resource to manage groups and their members.
 */
@Path(GroupRootResource.GROUPS_PATH_V2)
public class GroupRootResource {

  static final String GROUPS_PATH_V2 = "v2/groups/";

  private final Provider<GroupCollectionResource> groupCollectionResource;
  private final GroupResourceFactory groupResourceFactory;
  private final GroupManager manager;

  @Inject
  public GroupRootResource(Provider<GroupCollectionResource> groupCollectionResource,
    GroupResourceFactory groupResourceFactory, GroupManager manager) {
    this.groupCollectionResource = groupCollectionResource;
    this.groupResourceFactory = groupResourceFactory;
    this.manager = manager;
  }

  @Path("")
  public GroupCollectionResource getGroupCollectionResource() {
    return groupCollectionResource.get();
  }

  /**
   * @param id the id/name of the group
   */
  @Path("{id}")
  public GroupResource getGroupResource(@PathParam("id") String id, @Context Request request) {
    Group group = manager.get(id);
    if (group == null && !"DELETE".equals(request.getMethod())) {
      throw new NotFoundException();
    }
    return groupResourceFactory.create(group);
  }
}
