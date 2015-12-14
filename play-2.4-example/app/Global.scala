import java.io.{PrintWriter, StringWriter}

import play.api.GlobalSettings
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object Global extends GlobalSettings {
  override def onBadRequest(request: RequestHeader,
                            error: String): Future[Result] = {
    Future.successful(BadRequest(views.json.error(error)))
  }

  override def onError(request: RequestHeader,
                       ex: Throwable): Future[Result] = {
    val stringWriter = new StringWriter()
    ex.printStackTrace(new PrintWriter(stringWriter))
    Future.successful(BadRequest(views.json.error(
      "An unexpected error occurred", stringWriter.toString)))
  }
}