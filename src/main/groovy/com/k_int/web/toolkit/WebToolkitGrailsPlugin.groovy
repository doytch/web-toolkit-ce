package com.k_int.web.toolkit

import com.k_int.web.toolkit.databinding.ExtendedWebDataBinder
import com.k_int.web.toolkit.links.ProxyAwareCachingLinkGenerator
import com.k_int.web.toolkit.links.ProxyAwareLinkGenerator
import com.k_int.web.toolkit.refdata.GrailsDomainRefdataHelpers
import grails.config.Settings
import grails.core.GrailsClass
import grails.plugins.*
import grails.util.Environment
import grails.web.databinding.DataBindingUtils
import groovy.util.logging.Slf4j

@Slf4j
class WebToolkitGrailsPlugin extends Plugin {

  // the version or versions of Grails the plugin is designed for
  def grailsVersion = "3.3.0 > *"
  // resources that are excluded from plugin packaging
  def pluginExcludes = [
    "grails-app/views/error.gsp"
  ]
  def loadAfter = [
    'hibernate',
    'controllers',
    'dataBinding',
    'dataBindingGrails'
  ]

  def title = "Web Toolkit" // Headline display name of the plugin
  def author = "Steve Osguthorpe"
  def authorEmail = "steve.osguthorpe@k-int.com"
  def description = '''\
      Provides general tooling for web application frontend development.
    '''

  // License: one of 'APACHE', 'GPL2', 'GPL3'
  def license = "APACHE"

  // Details of company behind the plugin (if there is one)
  def organization = [ name: "Knowledge Integration", url: "http://www.k-int.com/" ]

  // Any additional developers beyond the author specified above.
  def developers = [ [ name: "Ian Ibbotson", email: "ian.ibbotson@k-int.com" ] ]

  Closure doWithSpring() { {ctx->
    def application = grailsApplication
    def config = application.config
    boolean trimStringsSetting = config.getProperty(Settings.TRIM_STRINGS, Boolean, true)
    boolean convertEmptyStringsToNullSetting = config.getProperty(Settings.CONVERT_EMPTY_STRINGS_TO_NULL, Boolean, true)
    Integer autoGrowCollectionLimitSetting = config.getProperty(Settings.AUTO_GROW_COLLECTION_LIMIT, Integer, 256)

    // Replace the default link generator.
    boolean isReloadEnabled = Environment.isDevelopmentMode() || Environment.current.isReloadEnabled()
    boolean cacheUrls = config.getProperty(Settings.WEB_LINK_GENERATOR_USE_CACHE, Boolean, !isReloadEnabled)
    grailsLinkGenerator(cacheUrls ? ProxyAwareCachingLinkGenerator : ProxyAwareLinkGenerator, config.getProperty(Settings.SERVER_URL) ?: null)

    // Replace the default data binder with out custom version.
    "${DataBindingUtils.DATA_BINDER_BEAN_NAME}"(ExtendedWebDataBinder, grailsApplication) {
      // trimStrings defaults to TRUE
      trimStrings = trimStringsSetting
      // convertEmptyStringsToNull defaults to TRUE
      convertEmptyStringsToNull = convertEmptyStringsToNullSetting
      // autoGrowCollectionLimit defaults to 256
      autoGrowCollectionLimit = autoGrowCollectionLimitSetting
    }
  }}

  @Override
  void doWithDynamicMethods() {
    // Bind extra methods to the class.
    log.debug("Extending Domain classes.")
    (grailsApplication.getArtefacts("Domain")).each {GrailsClass gc ->
      GrailsDomainRefdataHelpers.addMethods(gc)
    }
  }
}
