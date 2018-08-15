package sonia.scm.api.v2.resources;

import com.google.inject.assistedinject.Assisted;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class GroupResource {

  private final GroupToGroupDtoMapper groupToGroupDtoMapper;
  private final GroupDtoToGroupMapper dtoToGroupMapper;
  private final IdResourceManagerAdapter<Group, GroupDto, GroupException> adapter;

  @Inject
  public GroupResource(
    GroupManager manager,
    GroupToGroupDtoMapper groupToGroupDtoMapper,
    GroupDtoToGroupMapper groupDtoToGroupMapper,
    @Nullable @Assisted Group group) {
    this.groupToGroupDtoMapper = groupToGroupDtoMapper;
    this.dtoToGroupMapper = groupDtoToGroupMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, group, Group.class);
  }

  /**
   * Returns a group.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   */
  @GET
  @Path("")
  @Produces(VndMediaType.GROUP)
  @TypeHint(GroupDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the group"),
    @ResponseCode(code = 404, condition = "not found, no group with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get() {
    return adapter.get(groupToGroupDtoMapper::map);
  }

  /**
   * Deletes a group.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   */
  @DELETE
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success or nothing to delete"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete() {
    return adapter.delete();
  }

  /**
   * Modifies the given group.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param groupDto group object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.GROUP)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 400, condition = "Invalid body, e.g. illegal change of id/group name"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no group with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(GroupDto groupDto) {
    return adapter.update(existing -> dtoToGroupMapper.map(groupDto));
  }
}
