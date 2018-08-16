package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.repository.Branch;
import sonia.scm.repository.NamespaceAndName;

import java.util.Collection;
import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class BranchCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;
  private final BranchToBranchDtoMapper branchToDtoMapper;

  @Inject
  public BranchCollectionToDtoMapper(BranchToBranchDtoMapper branchToDtoMapper, ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
    this.branchToDtoMapper = branchToDtoMapper;
  }

  public HalRepresentation map(NamespaceAndName namespaceAndName, Collection<Branch> branches) {
    List<BranchDto> dtos = branches.stream().map(branch -> branchToDtoMapper.map(branch, namespaceAndName)).collect(toList());
    return new HalRepresentation(createLinks(namespaceAndName), embedDtos(dtos));
  }

  private Links createLinks(NamespaceAndName namespaceAndName) {
    String baseUrl = resourceLinks.branchCollection().self(namespaceAndName.getNamespace(), namespaceAndName.getName());

    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(baseUrl).build());
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<BranchDto> dtos) {
    return embeddedBuilder()
      .with("branches", dtos)
      .build();
  }
}
