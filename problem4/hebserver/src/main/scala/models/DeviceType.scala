package models

abstract class SensorType(val fieldName: String)
case object CarFuel extends SensorType("carFuel")
case object Thermostat extends SensorType("thermostat")
case object HeartRate extends SensorType("heartRate")