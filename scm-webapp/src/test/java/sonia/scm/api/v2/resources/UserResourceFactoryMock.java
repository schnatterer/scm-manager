package sonia.scm.api.v2.resources;

import sonia.scm.user.UserManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserResourceFactoryMock {
  public static UserResourceFactory get(UserDtoToUserMapperImpl dtoToUserMapper, UserToUserDtoMapperImpl userToDtoMapper, UserManager userManager) {
    UserResourceFactory mock = mock(UserResourceFactory.class);
    when(mock.create(any())).thenAnswer(invocation -> new UserResource(dtoToUserMapper, userToDtoMapper, userManager, invocation.getArgument(0)));
    return mock;
  }
}
