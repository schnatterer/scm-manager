/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sebastian Sdorra
 */
public final class GuavaCaches
{

  /**
   * the logger for GuavaCaches
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaCaches.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private GuavaCaches() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   * @param name
   *
   * @return
   */
  public static com.google.common.cache.Cache create(
    GuavaCacheConfiguration configuration, String name)
  {
    CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

    if (configuration.getConcurrencyLevel() != null)
    {
      builder.concurrencyLevel(configuration.getConcurrencyLevel());
    }

    if (configuration.getExpireAfterAccess() != null)
    {
      builder.expireAfterAccess(configuration.getExpireAfterAccess(),
        TimeUnit.SECONDS);
    }

    if (configuration.getExpireAfterWrite() != null)
    {
      builder.expireAfterWrite(configuration.getExpireAfterWrite(),
        TimeUnit.SECONDS);
    }

    if (configuration.getInitialCapacity() != null)
    {
      builder.initialCapacity(configuration.getInitialCapacity());
    }

    if (configuration.getMaximumSize() != null)
    {
      builder.maximumSize(configuration.getMaximumSize());
    }

    if (configuration.getMaximumWeight() != null)
    {
      builder.maximumWeight(configuration.getMaximumWeight());
    }

    if (isEnabled(configuration.getRecordStats()))
    {
      builder.recordStats();
    }

    if (isEnabled(configuration.getSoftValues()))
    {
      builder.softValues();
    }

    if (isEnabled(configuration.getWeakKeys()))
    {
      builder.weakKeys();
    }

    if (isEnabled(configuration.getWeakValues()))
    {
      builder.weakKeys();
    }

    if (logger.isTraceEnabled())
    {
      logger.trace("create new cache {} from builder: {}", name, builder);
    }

    return builder.build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  private static boolean isEnabled(Boolean v)
  {
    return (v != null) && v;
  }
}
