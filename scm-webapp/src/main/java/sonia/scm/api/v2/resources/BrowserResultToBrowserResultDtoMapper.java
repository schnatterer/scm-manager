package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class BrowserResultToBrowserResultDtoMapper {

  @Inject
  private FileObjectToFileObjectDtoMapper fileObjectToFileObjectDtoMapper;

  @Inject
  private ResourceLinks resourceLinks;

  public BrowserResultDto map(BrowserResult browserResult, NamespaceAndName namespaceAndName) {
    BrowserResultDto browserResultDto = new BrowserResultDto();

    browserResultDto.setTag(browserResult.getTag());
    browserResultDto.setBranch(browserResult.getBranch());
    browserResultDto.setRevision(browserResult.getRevision());

    List<FileObjectDto> fileObjectDtoList = new ArrayList<>();
    for (FileObject fileObject : browserResult.getFiles()) {
      fileObjectDtoList.add(mapFileObject(fileObject, namespaceAndName, browserResult.getRevision()));
    }

    browserResultDto.setFiles(fileObjectDtoList);
    this.addLinks(browserResult, browserResultDto, namespaceAndName);
    return browserResultDto;
  }

  private FileObjectDto mapFileObject(FileObject fileObject, NamespaceAndName namespaceAndName, String revision) {
    return fileObjectToFileObjectDtoMapper.map(fileObject, namespaceAndName, revision);
  }

  private void addLinks(BrowserResult browserResult, BrowserResultDto dto, NamespaceAndName namespaceAndName) {
    String path = "";
    if (browserResult.getPath() != null ) {
      path = removeFirstSlash(browserResult.getPath());
    }

    Links.Builder links = Links.linkingTo();

    if (browserResult.getRevision() == null) {
      links.self(addPath(resourceLinks.source().selfWithoutRevision(namespaceAndName.getNamespace(), namespaceAndName.getName()), path));
    } else {
      links.self(addPath(resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), ""), path));
    }

    dto.add(links.build());
  }

  // we have to add the file path using URI, so that path separators (aka '/') will not be encoded as '%2F'
  private String addPath(String sourceWithPath, String path) {
    return URI.create(sourceWithPath).resolve(path).toASCIIString();
  }

  private String removeFirstSlash(String source) {
    return source.startsWith("/") ? source.substring(1) : source;
  }

}
