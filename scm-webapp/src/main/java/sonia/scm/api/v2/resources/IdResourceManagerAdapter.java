package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.PageResult;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Facade for {@link SingleResourceManagerAdapter} and {@link CollectionResourceManagerAdapter}
 * for model objects handled by a single id.
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class IdResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation,
                             EXCEPTION extends Exception> {

  private final Manager<MODEL_OBJECT, EXCEPTION> manager;
  private final MODEL_OBJECT object;

  private final SingleResourceManagerAdapter<MODEL_OBJECT, DTO, EXCEPTION> singleAdapter;
  private final CollectionResourceManagerAdapter<MODEL_OBJECT, DTO, EXCEPTION> collectionAdapter;

  IdResourceManagerAdapter(Manager<MODEL_OBJECT, EXCEPTION> manager, MODEL_OBJECT object, Class<MODEL_OBJECT> type) {
    this.manager = manager;
    this.object = object;
    singleAdapter = new SingleResourceManagerAdapter<>(manager, type);
    collectionAdapter = new CollectionResourceManagerAdapter<>(manager, type);
  }

  Response get(Function<MODEL_OBJECT, DTO> mapToDto) {
    return singleAdapter.get(object, mapToDto);
  }

  public Response update(Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges) {
    return singleAdapter.update(
      object,
      applyChanges,
      idStaysTheSame()
    );
  }

  public Response getAll(int page, int pageSize, String sortBy, boolean desc, Function<PageResult<MODEL_OBJECT>, CollectionDto> mapToDto) {
    return collectionAdapter.getAll(page, pageSize, sortBy, desc, mapToDto);
  }

  public Response create(DTO dto, Supplier<MODEL_OBJECT> modelObjectSupplier, Function<MODEL_OBJECT, String> uriCreator) throws IOException, EXCEPTION {
    return collectionAdapter.create(dto, modelObjectSupplier, uriCreator);
  }

  public Response delete() {
    return singleAdapter.delete(object);
  }

  private Supplier<Optional<MODEL_OBJECT>> loadBy(String id) {
    return () -> Optional.ofNullable(manager.get(id));
  }

  private Predicate<MODEL_OBJECT> idStaysTheSame() {
    return changed -> changed.getId().equals(object.getId());
  }
}
