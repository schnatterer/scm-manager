package sonia.scm.plugin;

class PluginChecksumMismatchException extends PluginInstallException {
  PluginChecksumMismatchException(PluginInformation information, String calculatedChecksum, String expectedChecksum) {
    super(String.format("downloaded plugin checksum %s does not match expected %s", calculatedChecksum, expectedChecksum), information);
  }

  @Override
  public String getCode() {
    return "DURjbJVOb1";
  }
}
