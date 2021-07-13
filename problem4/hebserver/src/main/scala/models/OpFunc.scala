package models

sealed trait OpFunc
case object Max extends OpFunc
case object Min extends OpFunc
case object Average extends OpFunc
case object Median extends OpFunc

