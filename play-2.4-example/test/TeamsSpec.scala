import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test._
import models._

class TeamsSpec extends Specification {
  "Teams" should {
    "page correctly" in new WithApplication {
      Teams.clear()

      val teams = (1 to 5) map (i => Team(s"team$i", s"team$i@test.com"))
      teams.foreach(Teams.upsert)

      header("Link", route(FakeRequest(GET, s"/teams?limit=1&offset=0")).get) must equalTo {
        Some( s"""<http:///teams?limit=1&offset=1>; rel="next"""")
      }
      header("Link", route(FakeRequest(GET, s"/teams?limit=1&offset=1")).get) must equalTo {
        Some( s"""<http:///teams?limit=1>; rel="previous", <http:///teams?limit=1&offset=2>; rel="next"""")
      }
      for (offset <- 2 until teams.size) {
        header("Link", route(FakeRequest(GET, s"/teams?limit=1&offset=$offset")).get) must equalTo {
          Some( s"""<http:///teams?limit=1&offset=${offset-1}>; rel="previous", <http:///teams?limit=1&offset=${offset + 1}>; rel="next"""")
        }
      }
      val offset = teams.size
      header("Link", route(FakeRequest(GET, s"/teams?limit=1&offset=$offset")).get) must equalTo {
        Some( s"""<http:///teams?limit=1&offset=${offset-1}>; rel="previous"""")
      }
    }

    "paginate correctly when the last page is < limit in size" in new WithApplication {
      Teams.clear()
      val teams = (1 to 5) map (i => Team(s"team$i", s"team$i@test.com"))
      teams.foreach(Teams.upsert)

      header("Link", route(FakeRequest(GET, s"/teams?limit=${teams.size - 1}&offset=0")).get) must equalTo {
        Some(s"""<http:///teams?limit=${teams.size - 1}&offset=${teams.size - 1}>; rel="next"""")
      }
      header("Link", route(FakeRequest(GET, s"/teams?limit=${teams.size - 1}&offset=${teams.size - 1}")).get) must equalTo {
        Some(s"""<http:///teams?limit=${teams.size - 1}>; rel="previous"""")
      }
    }
  }
}
