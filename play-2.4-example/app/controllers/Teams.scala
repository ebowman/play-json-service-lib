package controllers

import com.gilt.play.json.controllers.JsonController
import play.api.libs.json._
import play.api.mvc._

object Teams extends JsonController {

  override def errorView = views.json.error
  override def https = false

  import scala.language.reflectiveCalls

  def list(limit: Int = 50, offset: Int = 0) = Action {
    implicit request =>
      val result = models.Teams.list().drop(offset).take(limit)
      implicit val pagination =
        paginate(result, limit, offset)(routes.Teams.list)
      Ok(result)
  }

  def getByKey(key: String) = Action { implicit request =>
    OkOption(models.Teams.getByKey(key), s"No team $key")
  }

  def putByKey(key: String) = Action(parse.json) {
    implicit request =>
    Json.fromJson[models.Team](request.body) match {
      case JsSuccess(team, _) =>
        implicit val location = routes.Teams.getByKey(key)
        models.Teams.upsert(team).fold(Created(team)) {team =>
          Ok(team)
        }
      case JsError(e) =>
        BadRequest(s"Could not parse team : ${request.body}: $e")
    }
  }

  def deleteByKey(key: String) = Action { implicit request =>
    if (models.Teams.delete(key).isDefined) Ok
    else NotFound(s"No team with key $key")
  }
}
