package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HgConfigPackageDto extends HalRepresentation {

  private String arch;
  private HgConfigDto hgConfigTemplate;
  private String hgVersion;
  private String id;
  private String platform;
  private String pythonVersion;
  private long size;
  private String url;
}
