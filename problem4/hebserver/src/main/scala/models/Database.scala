package models

final case class Database(host: String, port: Int, dataCenter: String, keySpace: String)
