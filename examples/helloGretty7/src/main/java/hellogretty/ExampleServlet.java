package hellogretty;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class ExampleServlet extends HttpServlet {

  private static final long serialVersionUID = -6506276378398106663L;

  private VelocityEngine ve = new VelocityEngine();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Template template = ve.getTemplate("/hellogretty/templates/servletpage.html", "UTF-8");
    VelocityContext context = new VelocityContext();
    context.put("today", new java.util.Date());
    try (PrintWriter out = response.getWriter()) {
      template.merge(context, out);
      out.flush();
    }
  }

  @Override
  public void init() throws ServletException {
    super.init();
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    ve.init();
  }
}
