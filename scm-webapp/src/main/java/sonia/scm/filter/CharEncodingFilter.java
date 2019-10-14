package sonia.scm.filter;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.UnsupportedEncodingException;

@WebElement(Filters.PATTERN_RESTAPI)
@Provider
public class CharEncodingFilter implements ContainerRequestFilter {
  @Context
  private HttpServletRequest servletRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws UnsupportedEncodingException {
    servletRequest.setAttribute(InputPart.DEFAULT_CONTENT_TYPE_PROPERTY, "text/plain; charset=UTF-8");
    servletRequest.setAttribute(InputPart.DEFAULT_CHARSET_PROPERTY, "UTF-8");
    servletRequest.setCharacterEncoding("UTF-8");
  }
}
