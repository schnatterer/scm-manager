package sonia.scm.plugin;

class PluginDownloadException extends PluginInstallException {
  PluginDownloadException(PluginInformation information, Exception cause) {
    super(information, "failed to download plugin", cause);
  }

  @Override
  public String getCode() {
    return "8lRjbIQM51";
  }
}
