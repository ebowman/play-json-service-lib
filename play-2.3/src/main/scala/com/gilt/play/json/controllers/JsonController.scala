package com.gilt.play.json.controllers

import com.gilt.play.json.templates.JsonFormat
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import play.mvc.Http

trait PaginatedController {

  /** Should generated links be https (as opposed to http) */
  def https: Boolean

  def uri(call: Call)(implicit request: Request[_]): String = call.absoluteURL(https)

  sealed trait IPagination {
    def link(url: String, dir: String) = s"""<$url>; rel="$dir""""

    def nextLink(url: String) = link(url, "next")

    def prevLink(url: String) = link(url, "previous")

    def links(implicit request: Request[_]): Option[(String, String)] = {
      this match {
        case Pagination(Some(prev), Some(next)) =>
          Some(("Link", Seq(prevLink(uri(prev)), nextLink(uri(next))).mkString(", ")))
        case Pagination(Some(prev), None) => Some(("Link", prevLink(uri(prev))))
        case Pagination(None, Some(next)) => Some(("Link", nextLink(uri(next))))
        case _ => None
      }
    }
  }

  object NoPagination extends IPagination

  case class Pagination(prev: Option[Call], next: Option[Call]) extends IPagination

  def paginate(items: Iterable[_], limit: Int, offset: Int)(callCapture: (Int, Int) => Call): IPagination = {
    val prev = if (offset <= 0) None else Some(callCapture(limit, math.max(0, offset - limit)))
    val next = if (items.isEmpty || items.size < limit) None else Some(callCapture(limit, offset + limit))
    (prev, next) match {
      case (None, None) => NoPagination
      case _ => Pagination(prev, next)
    }
  }
}

trait JsonController extends Controller with PaginatedController {

  import scala.language.reflectiveCalls

  def errorView: {def apply(msgs: String*): JsonFormat.Appendable}

  override val BadRequest = new Status(Http.Status.BAD_REQUEST) {
    def apply(messages: String*): Result = Results.BadRequest(errorView(messages: _*))
  }

  override val Ok = new Status(Http.Status.OK) {
    def apply[T](obj: T)(implicit writes: Writes[T],
                         request: Request[_],
                         pagination: IPagination = NoPagination): Result = {
      pagination.links.fold(Results.Ok(Json.toJson(obj))) {
        case links => Results.Ok(Json.toJson(obj)).withHeaders(links)
      }
    }
  }

  override val Created = new Status(Http.Status.CREATED) {
    def apply[T](obj: T)(implicit writes: Writes[T],
                         request: Request[_],
                         call: Call): Result = {
      Results.Created(Json.toJson(obj)).withHeaders(("Location", uri(call)))
    }
  }

  override val NotFound = new Status(Http.Status.NOT_FOUND) {
    def apply(messages: String*): Result = Results.NotFound(errorView(messages: _*))
  }
}
