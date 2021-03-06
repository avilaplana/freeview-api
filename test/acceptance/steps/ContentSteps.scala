package acceptance.steps

import acceptance.stubs.AuthStub._
import acceptance.support.Context._
import acceptance.support.Env._
import acceptance.support.{FilmBuilder, Http, ProgramBuilder, SeriesBuilder}
import controllers.external.TVContentShort
import controllers.{BadRequestResponse, NotFoundResponse}
import cucumber.api.DataTable
import cucumber.api.scala.{EN, ScalaDsl}
import org.joda.time.format.DateTimeFormat
import org.scalatest.Matchers
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class ContentSteps extends ScalaDsl with EN with Matchers with Http {


  When( """^the encoded token '(.*)'$""") { (tokenEncoded: String) =>
    world += "authToken" -> tokenEncoded
  }

  When( """^the user '(.*)' with token '(.*)' does not exist$""") { (username: String, token: String) =>
    val tokenEncoded = new sun.misc.BASE64Encoder().encode(s"${username}:${token}".getBytes)
    world += "authToken" -> tokenEncoded
    findToken(username, token, 404)
  }

  When( """^the user '(.*)' with token '(.*)'$""") { (username: String, token: String) =>
    val tokenEncoded = new sun.misc.BASE64Encoder().encode(s"${username}:${token}".getBytes)
    world += "authToken" -> tokenEncoded
    findToken(username, token)
  }

  Given( """^the TV Provider "(.+)"$""") { (provider: String) =>
    world += "provider" -> provider.toUpperCase
  }

  Given( """^today is "(.+)"$""") { (now: String) =>
    world += "today" -> now
  }

  Given( """^the time is "(.+)"$""") { (now: String) =>
  }

  Given( """^the TV guide now for channel "(.+)" is:$""") { (channel: String, requestData: DataTable) =>
    requestData.asLists(classOf[String]).asScala.tail.foreach {
      e => insertContent(e.asScala.toList, channel)
    }
  }

  Given( """^the TV guide for the rest of the day is:$""") { (requestData: DataTable) =>
    requestData.asLists(classOf[String]).asScala.tail.foreach {
      e => insertContent(e.asScala.toList, e.get(11))
    }
  }

  When( """^I GET the resource "(.+)"$""") { (url: String) =>
    val (statusCode, content) = world.get("authToken") match {
      case Some(auth) => GET(s"${host}$url", ("Authorization" -> auth))
      case None => GET(s"${host}$url")
    }
    world += "httpStatus" -> statusCode
    world += "content" -> content
  }

  Then( """^the HTTP status is "(.+)"$""") { (sc: String) =>
    world("httpStatus") shouldBe statusCode(sc)
  }

  Then( """^the response is empty list$""") { () =>
    world("content") shouldBe """[]"""
  }

  Then( """^the response is:$""") { (jsonExpected: String) =>
    world("httpStatus") match {
      case "200" =>
        val contentsExpected = Json.parse(jsonExpected.stripMargin).as[Seq[TVContentShort]]
        val contents = Json.parse(world("content")).as[Seq[TVContentShort]]
        contentsExpected shouldBe contents
      case "404" =>
        val contentsExpected = Json.parse(jsonExpected.stripMargin).as[NotFoundResponse]
        val contents = Json.parse(world("content")).as[NotFoundResponse]
        contentsExpected shouldBe contents
      case "400" =>
        val contentsExpected = Json.parse(jsonExpected.stripMargin).as[BadRequestResponse]
        val contents = Json.parse(world("content")).as[BadRequestResponse]
        contentsExpected shouldBe contents
      case "201" =>
        val contents = world("content")
        jsonExpected shouldBe contents
    }

  }

  Given( """^the TV guide now is:$""") { (requestData: DataTable) =>
    requestData.asLists(classOf[String]).asScala.tail.foreach {
      e => insertContent(e.asScala.toList, e.get(11))
    }
  }

  Then( """^the search contains the next content:$""") { (ce: DataTable) =>
    world("httpStatus") shouldBe "200"

    val contents = Json.parse(world("content")).as[Seq[TVContentShort]]
    val contentExpected = ce.asLists(classOf[String]).asScala.tail
    contentExpected.size shouldBe contents.size
    val z = contentExpected.zip(contents)
    z.map {
      case (ce, c) =>
        val title = ce.get(1)
        ce.get(0) match {
          case "film" => c.film.get.title shouldBe title
          case "program" => c.program.get.title shouldBe title
          case "series" =>
            val st = c.series.map(_.serieTitle == title)
            val et = for {
              s <- c.series
              e <- s.episode
              et <- e.episodeTitle
            } yield et == title
            st.getOrElse(false) || et.getOrElse(false) shouldBe true
        }
    }

  }

  private def insertContent(content: List[String], channel: String) = {
    val d = DateTimeFormat.forPattern("dd/MM/yyyy hh:mm a")
    val start = d.parseDateTime(world("today") + " " + content(3))
    val end = d.parseDateTime(world("today") + " " + content(4))
    val id = content(0)
    val typeContent = content(1)
    val title = content(2)

    val tvc = typeContent match {
      case "film" =>
        FilmBuilder(channel, world("provider"), start, end, title, content(5).toDouble, content(6), content(7), id)
      case "program" =>
        ProgramBuilder(channel, world("provider"), start, end, title, id)
      case "series" =>
        SeriesBuilder(channel, world("provider"), start, end, title, content(8), content(9), content(10), content(5).toDouble,
          content(6), content(7), id)
      case _ => throw new IllegalArgumentException(s"Type ")
    }
    Try(db.insert(tvCollection, tvc)) match {
      case Success(i) => println(s"Inserting $tvc correctlry")
      case Failure(e) => println(s"Error inserting $tvc: ${e.getMessage}"); e
    }

  }

}
