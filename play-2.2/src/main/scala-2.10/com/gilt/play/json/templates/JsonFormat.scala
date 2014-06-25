package com.gilt.play.json.templates

import play.api.http.{ContentTypeOf, ContentTypes, MimeTypes}
import play.api.mvc.Codec
import play.api.templates.BufferedContent
import play.templates.Format

class Json(buffer: StringBuilder) extends BufferedContent[Json](buffer) {
  val contentType = MimeTypes.JSON
}

object Json {
  implicit def contentTypeJson(implicit codec: Codec): ContentTypeOf[Json] =
    ContentTypeOf[Json](Some(ContentTypes.JSON))
}

object JsonFormat extends Format[Json] {
  override def raw(text: String): Json = new Json(new StringBuilder(text))

  // see http://www.ietf.org/rfc/rfc4627.txt section 2.5
  override def escape(text: String): Json = {
    def esc(c: Char) = s"\\$c"
    new Json(text.foldLeft(new StringBuilder) {
      case (b, c) if c == '\\' || c == '"' => b.append(esc(c))
      case (b, c) if c < ' ' => b.append(f"\\u${c.toInt}%04x")
      case (b, c) => b.append(c)
    })
  }
}

