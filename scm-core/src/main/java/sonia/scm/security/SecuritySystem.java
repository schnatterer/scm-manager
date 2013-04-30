/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;

import org.apache.shiro.subject.PrincipalCollection;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public interface SecuritySystem
{

  /**
   * Method description
   *
   *
   * @param permission
   *
   * @return
   */
  public StoredAssignedPermission addPermission(AssignedPermission permission);

  /**
   * Method description
   *
   *
   * @param permission
   */
  public void deletePermission(StoredAssignedPermission permission);

  /**
   * Method description
   *
   *
   * @param id
   */
  public void deletePermission(String id);

  /**
   * Method description
   *
   *
   * @param id
   * @param permission
   */
  public void modifyPermission(StoredAssignedPermission permission);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<StoredAssignedPermission> getAllPermissions();

  /**
   * Method description
   *
   *
   * @return
   */
  public List<PermissionDescriptor> getAvailablePermissions();

  /**
   * Method description
   *
   *
   * @param predicate
   *
   * @return
   */
  public List<StoredAssignedPermission> getPermissions(
    Predicate<AssignedPermission> predicate);

  /**
   * Method description
   *
   *
   * @return
   */
  public PrincipalCollection getSystemAccount();
}
