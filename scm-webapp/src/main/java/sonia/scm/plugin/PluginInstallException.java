package sonia.scm.plugin;

import sonia.scm.ExceptionWithContext;

import static java.util.Collections.emptyList;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

abstract class PluginInstallException extends ExceptionWithContext {

  PluginInstallException(String message, PluginInformation information) {
    super(entity("Plugin", information.getId()).build(), message);
  }

  PluginInstallException(String message, Exception cause) {
    super(emptyList(), message, cause);
  }

  PluginInstallException(PluginInformation information, String message, Exception cause) {
    super(entity("Plugin", information.getId()).build(), message, cause);
  }
}
