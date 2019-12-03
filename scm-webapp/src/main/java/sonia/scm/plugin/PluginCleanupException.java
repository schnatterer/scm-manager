package sonia.scm.plugin;

class PluginCleanupException extends PluginInstallException {
  PluginCleanupException(Exception cause) {
    super("failed to cleanup after broken installation", cause);
  }

  @Override
  public String getCode() {
    return "G1RjbJAJV1";
  }
}
