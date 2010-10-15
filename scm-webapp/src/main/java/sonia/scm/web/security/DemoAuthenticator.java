/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.User;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class DemoAuthenticator implements Authenticator
{

  /** Field description */
  private static final String DEMO_DISPLAYNAME = "Hans am Schalter";

  /** Field description */
  private static final String DEMO_MAIL = "hans@schalter.de";

  /** Field description */
  private static final String DEMO_PASSWORD = "hans123";

  /** Field description */
  private static final String DEMO_USERNAME = "hans";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param username
   * @param password
   *
   * @return
   */
  @Override
  public User authenticate(HttpServletRequest request,
                           HttpServletResponse response, String username,
                           String password)
  {
    User user = null;

    if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password))
    {
      user = new User(username, DEMO_DISPLAYNAME, DEMO_MAIL);
    }

    return user;
  }
}
