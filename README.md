
# play-json-service-lib

play-json-service-lib is a library for Play Framework 2.2.3 and 2.3.0 that provides some helpers to make it easier to write services that provides RESTful JSON-based services using Play.

Play is great but it’s slightly biased toward general purpose & web development, and as a result there are some missing features that simplify writing simple REST/JSON services.

This library collects a few useful tricks we’ve found helpful at Gilt, to make them easier to use across multiple play apps.

In particular, this library is focused on making a few things easier than they are by default with Play:

1. Returning objects to be serialized as JSON, without having to call Json.toJson everywhere.
2. Easier support for basic [Link header](http://tools.ietf.org/html/rfc5988#section-5) support when paginating.
3. Support for returning a JSON document when an error occurs.
4. Support for automatically returning a Location header when return a `201 CREATED` HTTP response.

## Usage

### Play 2.2

To use the library with Play 2.2 (it’s specifically compiled against 2.2.3 at present), include this in your `libraryDependencies` in build.sbt:

	"com.gilt" %% "play-json-service-lib-2.2" % "1.0.0"

You also need to register json for templates, so that errors can return a json document instead of the standard html error page. To do this, add this line to `build.sbt`:

	templatesTypes += ("json" -> "com.gilt.play.json.templates.JsonFormat")

(Be sure to add this after `play.Project.playScalaSettings`).

To use these features, extend your Play controllers from `com.gilt.play.json.controllers.JsonController`, like this:

	object Teams extends JsonController {

You need to need to implement two pure virtual methods in JsonController, `errorView` and `https`.  The first should be a twirl template that will be used to serve a JSON error document, and accepts a single argument of type `String*`.  You override `https` to tell the URL synthesis whether to generate URLs with scheme `http` or `https`.

	override def errorView = views.json.error
	override def https = false

`JsonController` introduces overridden versions of `Ok`, `Created`, `NotFound` and `BadRequest`.  `Ok` and `Created` take an object to be serialised to JSON, so you need to make sure there is an implicit `Writes` instance in scope for Play's JSON serialisation to work.  These objects also need an implicit `Request[_]` instance in scope.

For example, a simple example might be:

	def getByKey(key: String) = Action { implicit request =>
      Ok(models.Teams.getByEmail(key))
    }

`NotFound` and `BadRequest` can be used to return a standard error JSON document that you create.  For example, you might create a Play view like:

	@(messages: String*){
	"messages": [
	@for(message <- messages) {    "@message"
	}  ]
	}

In this case you can pass a `String` (or multiple `String`s) to `NotFound` or `BadRequest`, like:

	def getByKey(key: String) = Action { implicit request =>
	  models.Teams.getByEmail(key).map(OK(_)).getOrElse(NotFound(s"No team with key=[$key]"))  	}

#### Location headers

The `Created` response makes it easy to supply a `Location:` header in the HTTP response, if you provide an implicit `Call` instance in scope:

	def putByKey(key: String) = Action(parse.json) { implicit request =>
      Json.fromJson[models.Team](request.body) match {
        case JsSuccess(team, _) =>
	      implicit val location = routes.Teams.getByKey(key)
          models.Teams.upsert(team).fold(Created(team))(team => Ok(team))
        case JsError(e) => BadRequest(s"Could not parse team from body: ${request.body}: $e")
      }
  	}

#### Pagination

Requests that return large responses often need to support pagination; to do this, we assume HTTP requests that include `offset` and `limit` parameters. You can implement pagination using `Ok` by providing an implicit `Pagination` instance in scope

	def list(limit: Int = 50, offset: Int = 0) = Action { implicit request =>
      val result = models.Teams.list().drop(offset).take(limit)
      implicit val pagination = paginate(result, limit, offset)(routes.Teams.list)
      Ok(result)
  	}

You can see a complete worked example for Play 2.2 [todo].

### Play 2.3

To use it with Play 2.3 (it's compiled against 2.3.0 at present), include this in your `libraryDependencies`:

	"com.gilt" %% "play-json-service-lib-2.3" % "1.0.0"

You also need to register json for templates, so that errors can return a json document instead of the standard html error page. To do this, add this line to `build.sbt`:

	TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")

You'll also need to make sure both the `PlayScala` and `SbtTwirl` plugins are enabled.  For example, a simple build might look like:

	lazy val root = (project in file(".")). enablePlugins(PlayScala, SbtTwirl). settings(
      name := "play-2.3-example",
      scalaVersion := "2.11.1",
      libraryDependencies ++= Seq(
        "com.gilt" %% "play-json-service-lib-2-3" % "1.0.0-SNAPSHOT" changing
      ),
      TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")
  	)


To use these features, extend your Play controllers from `com.gilt.play.json.controllers.JsonController`, like this:

	object Teams extends JsonController {

You need to need to implement two pure virtual methods in JsonController, `errorView` and `https`.  The first should be a twirl template that will be used to serve a JSON error document, and accepts a single argument of type `String*`.  You override `https` to tell the URL synthesis whether to generate URLs with scheme `http` or `https`.

	override def errorView = views.json.error
	override def https = false

`JsonController` introduces overridden versions of `Ok`, `Created`, `NotFound` and `BadRequest`.  `Ok` and `Created` take an object to be serialised to JSON, so you need to make sure there is an implicit `Writes` instance in scope for Play's JSON serialisation to work.  These objects also need an implicit `Request[_]` instance in scope.

For example, a simple example might be:

	def getByKey(key: String) = Action { implicit request =>
      Ok(models.Teams.getByEmail(key))
    }

`NotFound` and `BadRequest` can be used to return a standard error JSON document that you create.  For example, you might create a Play view like:

	@(messages: String*){
	"messages": [
	@for(message <- messages) {    "@message"
	}  ]
	}

In this case you can pass a `String` (or multiple `String`s) to `NotFound` or `BadRequest`, like:

	def getByKey(key: String) = Action { implicit request =>
	  models.Teams.getByEmail(key).map(OK(_)).getOrElse(NotFound(s"No team with key=[$key]"))  	}

#### Location headers

The `Created` response makes it easy to supply a `Location:` header in the HTTP response, if you provide an implicit `Call` instance in scope:

	def putByKey(key: String) = Action(parse.json) { implicit request =>
      Json.fromJson[models.Team](request.body) match {
        case JsSuccess(team, _) =>
	      implicit val location = routes.Teams.getByKey(key)
          models.Teams.upsert(team).fold(Created(team))(team => Ok(team))
        case JsError(e) => BadRequest(s"Could not parse team from body: ${request.body}: $e")
      }
  	}

#### Pagination

Requests that return large responses often need to support pagination; to do this, we assume HTTP requests that include `offset` and `limit` parameters. You can implement pagination using `Ok` by providing an implicit `Pagination` instance in scope

	def list(limit: Int = 50, offset: Int = 0) = Action { implicit request =>
      val result = models.Teams.list().drop(offset).take(limit)
      implicit val pagination = paginate(result, limit, offset)(routes.Teams.list)
      Ok(result)
  	}

You can see a complete worked example for Play 2.3 [todo].

