package models

import java.util.concurrent.ConcurrentHashMap

case class Team(key: String, email: String)

object Teams {

  import scala.collection.JavaConverters._

  private val teams =
    new ConcurrentHashMap[String, Team]().asScala

  def list() = teams.values

  def upsert(team: Team): Option[Team] =
    teams.putIfAbsent(team.key, team)

  def getByKey(key: String): Option[Team] = teams.get(key)

  def delete(key: String): Option[Team] = teams.remove(key)

  def clear(): Unit = teams.clear()
}


