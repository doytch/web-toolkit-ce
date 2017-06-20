package com.k_int.web.toolkit

import java.util.Map

import com.k_int.web.toolkit.json.JsonSchemaUtils
import com.k_int.web.tools.RestfulResourceService

import grails.converters.JSON

class ConfigController {
  
  static responseFormats = ['json']
  RestfulResourceService restfulResourceService
  
  def resources () {
    
    boolean extended = params.get('extended') == 'extended'
        
    render ([
      resources : restfulResourceService.getResourceInfo (extended)
    ] as JSON )
  }
  
  def schemaEmbedded (String type) {

    String theUri = request.forwardURI.toLowerCase().replaceAll("${type.toLowerCase()}(\\/?)\$", "")
    
    def schema = JsonSchemaUtils.jsonSchema(type, grailsLinkGenerator.link(absolute: true, uri: theUri), true)
    render schema as JSON
  }
  
  
  def schema (String type) {

    String theUri = request.forwardURI.toLowerCase().replaceAll("${type.toLowerCase()}(\\/?)\$", "")
    def schema = JsonSchemaUtils.jsonSchema(type, grailsLinkGenerator.link(absolute: true, uri: theUri) ,false)
    render schema as JSON
  }
}