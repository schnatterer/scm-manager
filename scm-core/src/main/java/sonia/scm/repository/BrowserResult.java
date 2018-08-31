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


package sonia.scm.repository;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "browser-result")
public class BrowserResult implements Iterable<FileObject>, Serializable {
  private static final long serialVersionUID = 2818662048045182761L;

  private String branch;
  @XmlElement(name = "file")
  @XmlElementWrapper(name = "files")
  private List<FileObject> files;
  private String revision;
  private String tag;
  private String path;


  public BrowserResult() {
  }

  public BrowserResult(String revision, String tag, String branch,
                       List<FileObject> files, String path) {
    this.revision = revision;
    this.tag = tag;
    this.branch = branch;
    this.files = files;
    this.path = path;
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final BrowserResult other = (BrowserResult) obj;

    return Objects.equal(revision, other.revision)
      && Objects.equal(tag, other.tag)
      && Objects.equal(branch, other.branch)
      && Objects.equal(files, other.files)
      && Objects.equal(path, other.path);
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(revision, tag, branch, files, path);
  }


  @Override
  public Iterator<FileObject> iterator() {
    Iterator<FileObject> it = null;

    if (files != null) {
      it = files.iterator();
    }

    return it;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("revision", revision)
      .add("tag", tag)
      .add("branch", branch)
      .add("files", files)
      .add("path", path)
      .toString();
  }

  public String getBranch() {
    return branch;
  }

  public List<FileObject> getFiles() {
    return files;
  }

  public String getRevision() {
    return revision;
  }

  public String getTag() {
    return tag;
  }

  public String getPath() { return path; }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public void setFiles(List<FileObject> files) {
    this.files = files;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public void setPath(String path) { this.path = path; }


}
