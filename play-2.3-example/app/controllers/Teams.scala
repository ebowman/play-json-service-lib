package controllers

import com.gilt.play.json.controllers.JsonController
import models.Team
import play.api.libs.json._
import play.api.mvc._

object Teams extends JsonController {

  override def errorView = views.json.error
  override def https = false

  import scala.language.reflectiveCalls

  def list(limit: Int = 50, offset: Int = 0) = Action {
    implicit request =>
      val result = models.Teams.list().drop(offset).take(limit)
      implicit val pagination = paginate(result, limit, offset)(routes.Teams.list)
      Ok(result)
  }

  def getByKey(key: String) = Action { implicit request =>
    models.Teams.getByEmail(key).fold(NotFound: Result)(Ok(_))
  }

  def putByKey(key: String) = Action(parse.json) { implicit request =>
    implicit val call = routes.Teams.getByKey(key)
    Json.fromJson[models.Team](request.body) match {
      case JsSuccess(team, _) =>
        models.Teams.upsert(team).fold(Created(team))(team => Ok(team))
      case JsError(e) => BadRequest(s"Could not parse team from body: ${request.body}: $e")
    }
  }

  def deleteByKey(key: String) = Action { implicit request =>
    models.Teams.delete(key).map(_ => Ok).getOrElse(NotFound(s"No team with key $key"))
  }
}
