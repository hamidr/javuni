package com.newmotion

import com.github.nscala_time.time.Imports._
import com.newmotion.repositories.{SessionFeeRepository, TariffRepository}


object TestUtils {

  //While serializing I need to use "DateTime.parse" and
  //it produces an object which is not equal to the same type made by "new DateTime"
  //So it's a workaround to parse the string of a DateTime object in order to make them
  //comparable! It hurts I know but this is just in the tests where I deseriliaze them.

  //refer to "DateTime.parse" Documentation!
  implicit class MyFixDateTime(time: DateTime) {
    def fixMe: DateTime = DateTime.parse(time.toString)
  }

  case class MockRestService(
     override val tariffRepository: TariffRepository,
     override val sessionFeeRepository: SessionFeeRepository
  ) extends RestService
}
