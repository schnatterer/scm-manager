/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.Validateable;
import sonia.scm.util.Util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
@StaticPermissions(
  value = "plugin",
  generatedClass = "PluginPermissions",
  permissions = {},
  globalPermissions = {"read", "manage"},
  custom = true, customGlobal = true
)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "plugin-information")
@Getter
@Setter
public class PluginInformation implements PermissionObject, Validateable, Cloneable, Serializable {

  private static final long serialVersionUID = 461382048865977206L;

  private String author;
  private String category;
  private PluginCondition condition;
  private String description;
  private String name;
  private PluginState state;
  private String version;
  private String avatarUrl;

  @Override
  public PluginInformation clone() {
    PluginInformation clone = new PluginInformation();
    clone.setName(name);
    clone.setAuthor(author);
    clone.setCategory(category);
    clone.setDescription(description);
    clone.setState(state);
    clone.setVersion(version);
    clone.setAvatarUrl(avatarUrl);

    if (condition != null) {
      clone.setCondition(condition.clone());
    }

    return clone;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final PluginInformation other = (PluginInformation) obj;

    //J-
    return
      Objects.equal(author, other.author)
        && Objects.equal(category, other.category)
        && Objects.equal(condition, other.condition)
        && Objects.equal(description, other.description)
        && Objects.equal(name, other.name)
        && Objects.equal(state, other.state)
        && Objects.equal(version, other.version)
        && Objects.equal(avatarUrl, other.avatarUrl);
    //J+
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(author, category, condition,
      description, name, state, version, avatarUrl);
  }

  @Override
  public String toString() {
    //J-
    return MoreObjects.toStringHelper(this)
      .add("author", author)
      .add("category", category)
      .add("condition", condition)
      .add("description", description)
      .add("name", name)
      .add("state", state)
      .add("version", version)
      .add("avatarUrl", avatarUrl)
      .toString();
    //J+
  }

  @Override
  public String getId() {
    return getName(true);
  }

  public String getName(boolean withVersion) {
    StringBuilder id = new StringBuilder(name);

    if (withVersion) {
      id.append(":").append(version);
    }
    return id.toString();
  }

  @Override
  public boolean isValid() {
    return Util.isNotEmpty(name) && Util.isNotEmpty(version);
  }
}
