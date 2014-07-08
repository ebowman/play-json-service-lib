
# play-json-service-lib

## Introduction

`play-json-service-lib` is a library for Play Framework 2.2.3 and 2.3.0 that provides some helpers to make it easier to write services that provides RESTful JSON-based services using Play.

Play is slightly biased toward general purpose web development, and as a result there are some missing features that would simplify writing simple REST/JSON services.

This library collects a few useful tricks we’ve found helpful at Gilt, to make them easier to create RESTful JSON services. In particular,

1. Returning objects to be serialized as JSON, without having to call Json.toJson everywhere.
2. Easier support for basic [Link header](http://tools.ietf.org/html/rfc5988#section-5) support when paginating.
3. Support for returning a JSON document when an error occurs.
4. Support for automatically including a Location header when returning a `201 CREATED` HTTP response.

## Installation & Configuration

### Play 2.2

To use the library with Play 2.2 (it’s specifically compiled against 2.2.3 at present), include this in your `libraryDependencies` in build.sbt:

    "com.gilt" %% "play-json-service-lib-2-2" % "1.1.0"

You also need to register json for templates, so that errors can return a json document instead of the standard html error page. To do this, add this line to `build.sbt`:

    templatesTypes += ("json" -> "com.gilt.play.json.templates.JsonFormat")

(Be sure to add this after `play.Project.playScalaSettings`).

The library has a "provided" dependency on `com.typesafe.play:play:2.2.3` and `com.typesafe.play:play-json:2.2.3`, so you'll need to be sure your Play application depends on both these libraries.

### Play 2.3

To use it with Play 2.3 (it's compiled against 2.3.0 at present), include this in your `libraryDependencies`:

    "com.gilt" %% "play-json-service-lib-2-3" % "1.1.0"

You also need to register json for templates, so that errors can return a json document instead of the standard html error page. To do this, add this line to `build.sbt`:

    TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")

You'll also need to make sure both the `PlayScala` and `SbtTwirl` plugins are enabled.  For example, a simple build might look like:

    lazy val root = (project in file(".")). enablePlugins(PlayScala, SbtTwirl). settings(
      name := "play-2.3-example",
      scalaVersion := "2.11.1",
      libraryDependencies ++= Seq(
        "com.gilt" %% "play-json-service-lib-2-3" % "1.1.0"
      ),
      TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")
    )

The library has a "provided" dependency on `com.typesafe.play:play:2.3.0`, `com.typesafe.play:play-json:2.3.0`, and `com.typesafe.play:twirl-api:1.0.2`, so you'll need to be sure your Play application depends on these libraries.

## Usage

To use these features, extend your Play controllers from `com.gilt.play.json.controllers.JsonController`, like this:

    object Teams extends JsonController {

You need to need to implement two abstract methods in JsonController, `errorView` and `https`.  The first should be a twirl template that will be used to serve a JSON error document, and accepts a single argument of type `String*`.  You override `https` to tell the URL synthesis whether to generate URLs with scheme `http` or `https`. (Note that we have a preference toward absolute URLs, and this library is implemented to return them in both `Location` and `Link` response headers.)

    override def errorView = views.json.error
    override def https = false

`JsonController` introduces overridden versions of `Ok`, `Created`, `NotFound` and `BadRequest`.  `Ok` and `Created` take an object to be serialised as JSON, so you need to make sure there is an implicit `Writes` instance in scope for Play's JSON serialization to work.  These objects also need an implicit `Request[_]` instance in scope.

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
      models.Teams.getByEmail(key).map(Ok(_)).getOrElse(NotFound(s"No team with key=[$key]"))
    }

This common pattern where an `Option` is returned and generates either an `Ok` or `NotFound` can also use the `OkOption` method:

    def getByKey(key: String) = Action { implicit request =>
      OkOption(models.Teams.getByEmail(key), s"No team with key=[$key]")
    }

For use in `Action.async` controller methods, `OkFuture` and `OkFutureOption` lift these methods into the `Future` monad:

    def getByKey(key: String) = Action.async { implicit request =>
      OkFutureOption(models.Teams.getByEmail(key), s"No team with key=[$key]")
    }


### Location headers

The `Created` response makes it easy to generate a `Location:` header in a `201 CREATED` HTTP response, if you include an implicit `Call` instance in scope:

    def putByKey(key: String) = Action(parse.json) { implicit request =>
      Json.fromJson[models.Team](request.body) match {
        case JsSuccess(team, _) =>
          implicit val location = routes.Teams.getByKey(key)
          models.Teams.upsert(team).fold(Created(team))(team => Ok(team))
        case JsError(e) => BadRequest(s"Could not parse team from body: ${request.body}: $e")
      }
    }

### Pagination

Requests that return large responses often need to [support pagination](http://tools.ietf.org/html/rfc5988#section-5); to do this, we assume HTTP requests that include some kind of `limit` and `offset` parameters. You can implement pagination using `Ok` by providing an implicit `Pagination` instance in scope

    def list(limit: Int = 50, offset: Int = 0) = Action { implicit request =>
      val result = models.Teams.list().drop(offset).take(limit)
      implicit val pagination = paginate(result, limit, offset)(routes.Teams.list)
      Ok(result)
    }

If you are using async actions, `OkFuture` expects an implicit `Future[Pagination]`

## Examples

You can see a complete worked example for Play 2.2 [here](https://github.com/gilt/play-json-service-lib/tree/master/play-2.2-example).

You can see a complete worked example for Play 2.3 [here](https://github.com/gilt/play-json-service-lib/tree/master/play-2.3-example).
