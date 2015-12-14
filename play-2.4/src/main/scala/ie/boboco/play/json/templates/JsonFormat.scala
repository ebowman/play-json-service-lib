package ie.boboco.play.json.templates

import play.api.http.{ContentTypeOf, ContentTypes, MimeTypes}
import play.api.mvc.Codec
import play.twirl.api.{Format, BufferedContent}

import scala.collection.immutable.Seq

class Json(elements: Seq[Json], json: String) extends BufferedContent[Json](elements, json) {
  def this(text: String) = this(Nil, text)
  def this(elements: Seq[Json]) = this(elements, "")
  val contentType = MimeTypes.JSON
}

object Json {
  implicit def contentTypeJson(implicit codec: Codec): ContentTypeOf[Json] =
    ContentTypeOf[Json](Some(ContentTypes.JSON))
}

object JsonFormat extends Format[Json] {
  override def raw(text: String): Json = new Json(text)

  // see http://www.ietf.org/rfc/rfc4627.txt section 2.5
  override def escape(text: String): Json = {
    def esc(c: Char) = s"\\$c"
    new Json(text.foldLeft(new StringBuilder) {
      case (b, c) if c == '\\' || c == '"' => b.append(esc(c))
      case (b, c) if c < ' ' => b.append(f"\\u${c.toInt}%04x")
      case (b, c) => b.append(c)
    }.toString())
  }

  override def empty: Json = new Json("")

  override def fill(elements: Seq[Json]): Json = new Json(elements)
}

