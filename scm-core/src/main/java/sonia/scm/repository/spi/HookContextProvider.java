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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.api.HookException;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookFeatureIsNotSupportedException;
import sonia.scm.repository.api.HookMessageProvider;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public abstract class HookContextProvider
{

  /**
   * Method description
   *
   *
   * @return
   */
  public final HookMessageProvider getMessageProvider()
  {
    if (clientDisconnected)
    {
      throw new HookException(
        "message provider is only available in a synchronous hook execution.");
    }

    return createMessageProvider();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  final void handleClientDisconnect()
  {
    clientDisconnected = true;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract Set<HookFeature> getSupportedFeatures();

  /**
   * Method description
   *
   *
   * @return
   */
  public HookChangesetProvider getChangesetProvider()
  {
    throw new HookFeatureIsNotSupportedException(
      HookFeature.CHANGESET_PROVIDER);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected HookMessageProvider createMessageProvider()
  {
    throw new HookFeatureIsNotSupportedException(HookFeature.MESSAGE_PROVIDER);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean clientDisconnected = false;
}
