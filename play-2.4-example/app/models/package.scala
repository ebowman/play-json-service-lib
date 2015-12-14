import play.api.libs.json._

package object models {
  implicit val formatTeam = Json.format[Team]
}
