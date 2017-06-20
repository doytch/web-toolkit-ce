package com.k_int.web.tools

import com.k_int.web.toolkit.databinding.ExtendedWebDataBinder

class ValueConverterService {
  ExtendedWebDataBinder grailsWebDataBinder
  
  public def attemptConversion (Class targetType, value) {
    def val = grailsWebDataBinder.attemptConversion (targetType, value)
    
    val
  }
}