/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.webapp.WebInfConfiguration

class WebInfConfigurationEx extends WebInfConfiguration {

  // backported from jetty-9
  @Override
  protected List<Resource> findJars(WebAppContext context) throws Exception {
    List<Resource> jarResources = new ArrayList<Resource>();
    List<Resource> webInfLibJars = findWebInfLibJars(context);
    if (webInfLibJars != null)
      jarResources.addAll(webInfLibJars);
    List<Resource> extraClasspathJars = findExtraClasspathJars(context);
    if (extraClasspathJars != null)
      jarResources.addAll(extraClasspathJars);
    return jarResources;
  }

  // backported from jetty-9
  protected List<Resource> findWebInfLibJars(WebAppContext context) throws Exception {
    Resource web_inf = context.getWebInf();
    if (web_inf==null || !web_inf.exists())
        return null;

    List<Resource> jarResources = new ArrayList<Resource>();
    Resource web_inf_lib = web_inf.addPath("/lib");
    if (web_inf_lib.exists() && web_inf_lib.isDirectory())
    {
        String[] files=web_inf_lib.list();
        for (int f=0;files!=null && f<files.length;f++)
        {
            try
            {
                Resource file = web_inf_lib.addPath(files[f]);
                String fnlc = file.getName().toLowerCase(Locale.ENGLISH);
                int dot = fnlc.lastIndexOf('.');
                String extension = (dot < 0 ? null : fnlc.substring(dot));
                if (extension != null && (extension.equals(".jar") || extension.equals(".zip")))
                {
                    jarResources.add(file);
                }
            }
            catch (Exception ex)
            {
                LOG.warn(Log.EXCEPTION,ex);
            }
        }
    }
    return jarResources;
  }

  // backported from jetty-9
  protected List<Resource> findExtraClasspathJars(WebAppContext context) throws Exception {
    if (context == null || context.getExtraClasspath() == null)
        return null;

    List<Resource> jarResources = new ArrayList<Resource>();
    StringTokenizer tokenizer = new StringTokenizer(context.getExtraClasspath(), ",;");
    while (tokenizer.hasMoreTokens())
    {
        Resource resource = context.newResource(tokenizer.nextToken().trim());
        String fnlc = resource.getName().toLowerCase(Locale.ENGLISH);
        int dot = fnlc.lastIndexOf('.');
        String extension = (dot < 0 ? null : fnlc.substring(dot));
        if (extension != null && (extension.equals(".jar") || extension.equals(".zip")))
        {
            jarResources.add(resource);
        }
    }

    return jarResources;
  }
}

