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



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.12
 */
public class LineBasedText implements Iterable<String>
{

  /**
   * Constructs ...
   *
   *
   * @param lines
   */
  public LineBasedText(List<String> lines)
  {
    this.lines = lines;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static LineBasedText create()
  {
    return new LineBasedText(new ArrayList<String>());
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   * @throws IOException
   */
  public static LineBasedText create(File file) throws IOException
  {
    LineBasedText result = null;
    InputStream input = null;

    try
    {
      input = new FileInputStream(file);
      result = create(input);
    }
    finally
    {
      IOUtil.close(input);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param content
   *
   * @return
   *
   * @throws IOException
   */
  public static LineBasedText create(String content) throws IOException
  {
    return create(new StringReader(content));
  }

  /**
   * Method description
   *
   *
   * @param data
   *
   * @return
   *
   * @throws IOException
   */
  public static LineBasedText create(byte[] data) throws IOException
  {
    return create(new ByteArrayInputStream(data));
  }

  /**
   * Method description
   *
   *
   * @param inputStream
   *
   * @return
   *
   * @throws IOException
   */
  public static LineBasedText create(InputStream inputStream) throws IOException
  {
    return create(new InputStreamReader(inputStream));
  }

  /**
   * Method description
   *
   *
   * @param reader
   *
   * @return
   *
   * @throws IOException
   */
  public static LineBasedText create(Reader reader) throws IOException
  {
    return create(new BufferedReader(reader));
  }

  /**
   * Method description
   *
   *
   * @param reader
   *
   * @return
   *
   * @throws IOException
   */
  public static LineBasedText create(BufferedReader reader) throws IOException
  {
    List<String> lines = new ArrayList<String>();
    String line = reader.readLine();

    while (line != null)
    {
      lines.add(line);
      line = reader.readLine();
    }

    return new LineBasedText(lines);
  }

  /**
   * Method description
   *
   *
   * @param line
   *
   * @return
   */
  public LineBasedText append(String line)
  {
    getLines().add(line);

    return this;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<String> iterator()
  {
    return (lines != null)
           ? lines.iterator()
           : null;
  }

  /**
   * Method description
   *
   *
   * @param writer
   */
  public void print(PrintWriter writer)
  {
    for (String line : lines)
    {
      writer.println(line);
    }
  }

  /**
   * Method description
   *
   *
   * @param writer
   */
  public void print(Writer writer)
  {
    print(new PrintWriter(writer));
  }

  /**
   * Method description
   *
   *
   * @param output
   */
  public void print(OutputStream output)
  {
    print(new PrintWriter(output));
  }

  /**
   * Method description
   *
   *
   * @param line
   *
   * @return
   */
  public LineBasedText remove(String line)
  {
    getLines().remove(line);

    return this;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    Iterator<String> lineIt = lines.iterator();

    while (lineIt.hasNext())
    {
      builder.append(lineIt.next());

      if (lineIt.hasNext())
      {
        builder.append("\\n");
      }
    }

    return builder.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getLines()
  {
    return lines;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<String> lines;
}
