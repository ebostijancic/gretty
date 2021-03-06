/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import ch.qos.logback.classic.selector.servlet.ContextDetachingSCL
import ch.qos.logback.classic.selector.servlet.LoggerContextFilter
import groovy.json.JsonSlurper
import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.util.resource.FileResource
import org.eclipse.jetty.webapp.Configuration
import org.eclipse.jetty.webapp.WebAppClassLoader
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.xml.XmlConfiguration
import org.eclipse.jetty.webapp.WebInfConfiguration
import org.eclipse.jetty.webapp.WebXmlConfiguration
import org.eclipse.jetty.webapp.MetaInfConfiguration
import org.eclipse.jetty.webapp.FragmentConfiguration
import org.eclipse.jetty.plus.webapp.EnvConfiguration
import org.eclipse.jetty.plus.webapp.PlusConfiguration
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration
import javax.servlet.DispatcherType

final class Runner extends RunnerBase {

  static void main(String[] args) {
    assert args.length != 0
    Map params = new JsonSlurper().parseText(args[0])
    new Runner(params).run()
  }

  private Runner(Map params) {
    super(params)
  }

  @Override
  protected void addConfigurationClasses(webAppContext, List<String> webappClassPath) {
    webAppContext.setConfigurations([
      new WebInfConfigurationEx(),
      new WebXmlConfiguration(),
      new MetaInfConfiguration(),
      new FragmentConfiguration(),
      new EnvConfiguration(),
      new PlusConfiguration(),
      new AnnotationConfigurationEx(webappClassPath),
      new JettyWebXmlConfiguration()
    ] as Configuration[])
  }

  @Override
  protected void applyJettyEnvXml(webAppContext, jettyEnvXml) {
    if(jettyEnvXml) {
      System.out.println "Configuring webAppContext from ${jettyEnvXml}"
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(jettyEnvXml).toURI().toURL())
      xmlConfiguration.configure(webAppContext)
    }
  }

  @Override
  protected void applyJettyXml() {
    if(params.jettyXml != null) {
      System.out.println "Configuring server from ${params.jettyXml}"
      XmlConfiguration xmlConfiguration = new XmlConfiguration(new File(params.jettyXml).toURI().toURL())
      xmlConfiguration.configure(server)
    }
  }

  @Override
  protected void configureConnectors() {
    if(server.getConnectors() != null && server.getConnectors().length != 0)
      return
    System.out.println 'Auto-configuring server connectors'
    SocketConnector connector = new SocketConnector()
    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60)
    connector.setSoLingerTime(-1)
    connector.setPort(params.port)
    server.setConnectors([ connector ] as Connector[])
  }

  @Override
  protected void configureRealm(context, String realm, String realmConfigFile) {
    if(realm && realmConfigFile) {
      if(context.getSecurityHandler().getLoginService() != null)
        return
      System.out.println 'Auto-configuring login service'
      context.getSecurityHandler().setLoginService(new HashLoginService(realm, realmConfigFile))
    }
  }

  @Override
  protected createServer() {
    // fix for issue https://github.com/akhikhl/gretty/issues/24
    org.eclipse.jetty.util.resource.Resource.defaultUseCaches = false
    return new Server()
  }

  @Override
  protected createWebAppContext(List<String> webappClassPath) {
    WebAppContext context = new WebAppContext()
    context.setExtraClasspath(webappClassPath.collect { it.endsWith('.jar') ? it : (it.endsWith('/') ? it : it + '/') }.findAll { !(it =~ /.*javax\.servlet-api.*\.jar/) }.join(';'))
    context.addEventListener(new ContextDetachingSCL())
    context.addFilter(LoggerContextFilter.class, '/*', EnumSet.of(DispatcherType.REQUEST))
    return context
  }

  @Override
  protected int getServerPort() {
    if(server.getConnectors() != null)
      for(Connector conn in server.getConnectors())
        return conn.getLocalPort()
    return params.port
  }

  @Override
  protected void setHandlersToServer(List handlers) {
    ContextHandlerCollection contexts = new ContextHandlerCollection()
    contexts.setServer(server)
    contexts.setHandlers(handlers as Handler[])
    server.setHandler(contexts)
  }
}
