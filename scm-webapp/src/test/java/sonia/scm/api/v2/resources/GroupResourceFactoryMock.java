package sonia.scm.api.v2.resources;

import sonia.scm.group.GroupManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupResourceFactoryMock {
  public static GroupResourceFactory get(GroupManager groupManager, GroupToGroupDtoMapperImpl groupToDtoMapper, GroupDtoToGroupMapperImpl dtoToGroupMapper) {
    GroupResourceFactory mock = mock(GroupResourceFactory.class);
    when(mock.create(any())).thenAnswer(invocation -> new GroupResource(groupManager, groupToDtoMapper, dtoToGroupMapper, invocation.getArgument(0)));
    return mock;
  }
}
