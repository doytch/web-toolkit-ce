package com.k_int.web.toolkit.custprops

import javax.persistence.Transient
import javax.validation.UnexpectedTypeException

import com.k_int.web.toolkit.databinding.BindUsingWhenRef
import com.k_int.web.toolkit.refdata.RefdataValue
import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.MultiTenant
import grails.gorm.annotation.Entity
import grails.util.GrailsClassUtils
import grails.util.GrailsNameUtils
import grails.web.databinding.DataBindingUtils
import groovy.util.logging.Log4j

@Log4j
@Entity
class CustomPropertyDefinition implements MultiTenant<CustomPropertyDefinition> {
  
  static transients = ['propertyInstance']
  private static final String DEFINITION_PROPERTY = 'definitionClass'

  String id
  String name
  String label
  String description
  Class<? extends CustomProperty> type
  
  // Denotes primary. This is the primary sort, then weight, then id.
  boolean primary = false
  
  // Used for ordering. Larger weight values sink.
  int weight = 0
  
  boolean defaultInternal = true
    
  private static String nameToLabel (String value) {
    // Strip double whitespace entries.
    return value?.trim().replaceAll(/([a-z0-9A-Z])([A-Z][a-z])/, '$1 $2')
  }
  
  def beforeValidate() {
    if (!this.label && this.name) {
      this.label = nameToLabel(this.name)
    }
  }
  
//  static hasMany = [
//    propertyInstances: CustomProperty
//  ]
//  
//  static mappedBy = [
//    propertyInstances: "definition"
//  ]
  
  static constraints = {
    name            (nullable: false, blank: false, unique: true)
    description     (nullable: true, blank: false)
    type            (bindable: false, nullable: false)
    label           (nullable: false, blank: false)
    primary         (nullable: false)
  }

  static mapping = {
    tablePerHierarchy false
    id column: 'pd_id', generator: 'uuid', length:36
    name column: 'pd_name', index: 'td_name_idx'
    description column: 'pd_description'
    type column: 'pd_type', index: 'td_type_idx'
    label column: 'pd_label', index: 'td_label_idx'
    weight column: 'pd_weight', index: 'td_weight_idx'
    primary column: 'pd_primary', index: 'td_primary_idx'
    sort 'primary': 'asc', 'weight':'asc', 'id':'asc'
  }
  
  static CustomPropertyDefinition forType (final Class<? extends CustomProperty> type, final Map otherProps = [:]) {
    CustomPropertyDefinition definition = null
    if (type) {
      // Grab the class or default to this one.
      Class cpdc = GrailsClassUtils.getStaticFieldValue(type, DEFINITION_PROPERTY) ?: CustomPropertyDefinition
      definition = cpdc.newInstance()
      
      // Use the binder instead.
      DataBindingUtils.bindObjectToInstance(definition, new SimpleMapDataBindingSource( otherProps ))
      definition.type = type
    }
    definition
  }
  
  static CustomPropertyDefinition forType (final String type, final Map otherProps = [:]) {
    CustomPropertyDefinition definition = null
    final Class<? extends CustomProperty> typeClass = Class.forName(
      "${CustomProperty.class.package.name}.types.${CustomProperty.class.simpleName}${GrailsNameUtils.getClassName(type)}"
    )
    if (typeClass) {
      definition = forType(typeClass, otherProps)
    }
    definition
  }

  CustomProperty getPropertyInstance(Map extraProperties = [:]) {
    type?.newInstance([internal: (this.defaultInternal)] + extraProperties + [definition: this])
  }
}

