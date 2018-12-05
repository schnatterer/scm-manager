package sonia.scm.store;

import sonia.scm.repository.Repository;

import java.util.function.Function;

public class TypedFloatingStoreParameters<T, S extends Store> {

  private final TypedStoreParametersImpl<T> parameters = new TypedStoreParametersImpl<>();
  private final Function<TypedStoreParametersImpl<T>, S> factory;

  TypedFloatingStoreParameters(Function<TypedStoreParametersImpl<T>, S> factory) {
    this.factory = factory;
  }

  public class Builder {

    Builder(Class<T> type) {
      parameters.setType(type);
    }

    /**
     * Use this to set the name for the store.
     * @param name The name for the store.
     * @return Floating API to either specify a repository or directly build a global store.
     */
    public TypedFloatingStoreParameters<T, S>.OptionalRepositoryBuilder withName(String name) {
      parameters.setName(name);
      return new TypedFloatingStoreParameters<T, S>.OptionalRepositoryBuilder();
    }
  }

  public class OptionalRepositoryBuilder {

    /**
     * Use this to create or get a store for a specific repository. This step is optional. If you
     * want to have a global store, omit this.
     * @param repository The optional repository for the store.
     * @return Floating API to finish the call.
     */
    public TypedFloatingStoreParameters<T, S>.OptionalRepositoryBuilder forRepository(Repository repository) {
      parameters.setRepository(repository);
      return this;
    }

    /**
     * Creates or gets the store with the given name and (if specified) the given repository. If no
     * repository is given, the store will be global.
     */
    public S build(){
      return factory.apply(parameters);
    }
  }
}
