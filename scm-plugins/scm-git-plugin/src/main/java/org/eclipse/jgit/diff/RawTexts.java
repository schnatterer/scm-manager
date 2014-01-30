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



package org.eclipse.jgit.diff;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.util.RawParseUtils;

//~--- JDK imports ------------------------------------------------------------

import java.nio.charset.Charset;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.36
 */
public final class RawTexts
{

  /**
   * Constructs ...
   *
   */
  private RawTexts() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param text
   *
   * @return
   */
  public static byte[] toBytes(RawText text)
  {
    return text.content;
  }

  /**
   * Method description
   *
   *
   * @param charset
   * @param text
   *
   * @return
   */
  public static RawText withEncoding(String charset, RawText text)
  {
    return withEncoding(Charset.forName(charset), text);
  }

  /**
   * Method description
   *
   *
   * @param charset
   * @param text
   *
   * @return
   */
  public static RawText withEncoding(Charset charset, RawText text)
  {
    return new RawTextWithEncoding(charset, text.content);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/01/30
   * @author         Enter your name here...
   */
  private static class RawTextWithEncoding extends RawText
  {

    /**
     * Constructs ...
     *
     *
     * @param encoding
     * @param input
     */
    private RawTextWithEncoding(Charset encoding, byte[] input)
    {
      super(input);
      this.encoding = encoding;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param start
     * @param end
     *
     * @return
     */
    @Override
    protected String decode(int start, int end)
    {
      return RawParseUtils.decode(encoding, content, start, end);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Charset encoding;
  }
}
