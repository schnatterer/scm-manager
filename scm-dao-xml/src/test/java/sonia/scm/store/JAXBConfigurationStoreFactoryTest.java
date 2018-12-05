package sonia.scm.store;

import org.junit.jupiter.api.Test;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JAXBConfigurationStoreFactoryTest {

  @Test
  void x() {
    JAXBConfigurationStoreFactory factory = new JAXBConfigurationStoreFactory(mock(SCMContextProvider.class), mock(RepositoryLocationResolver.class));
    ConfigurationStore<String> name = factory.withType(String.class).withName("name").build();
  }
}
