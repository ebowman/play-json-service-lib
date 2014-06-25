package models

case class Team(key: String, email: String)

object Teams {

  import scala.collection.JavaConverters._

  private val teams = new java.util.concurrent.ConcurrentHashMap[String, Team]().asScala

  def list() = teams.values

  def upsert(team: Team): Option[Team] = teams.putIfAbsent(team.key, team)

  def getByEmail(email: String): Option[Team] = teams.get(email)

  def delete(email: String): Option[Team] = teams.remove(email)

  def clear(): Unit = teams.clear()
}


