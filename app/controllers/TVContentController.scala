package controllers

import java.net.URLDecoder

import configuration.{Environment, ApplicationContext}
import controllers.external.{TVShort, TVContentShort, TVLong, TVContentLong}
import models._
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.Action
import utils.TimeProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TVContentController extends TVContentController {
  override val contentRepository = ApplicationContext.tvContentRepository
  override implicit val host: String = Environment.host
  override implicit val time: TimeProvider = ApplicationContext.time
}

trait TVContentController extends BaseController {

  implicit val host: String
  implicit val time: TimeProvider
  val toTVShorts: Seq[TVContent] => Seq[TVContentShort] = _.map(TVShort(_))
  val toTVLong: TVContent => TVContentLong = TVLong(_)

  val contentRepository: ContentRepository

  def currentContent(channelName: String) = Action.async { c =>
    contentRepository.findCurrentContentByChannel(URLDecoder.decode(channelName, "UTF-8").toUpperCase).map { c =>
      buildResponse(c.map(toTVLong(_)), s"No TV content at this moment for the channel: $channelName")
    }
  }

  def tvContentDetails(tvContentID: String) = Action.async {
    contentRepository.findContentByID(tvContentID).map { c =>
      buildResponse(c.map(toTVLong(_)), s"No TV content details with id: $tvContentID")
    }
  }

  def contentLeft(channelName: String) = Action.async {
    contentRepository.findLeftContentByChannel(URLDecoder.decode(channelName, "UTF-8").toUpperCase).map {
      ltv => buildResponseSeq(toTVShorts(ltv), s"No TV content left for the channel: $channelName")
    }
  }

  def allContent(channelName: String) = Action.async {

    contentRepository.findDayContentByChannel(URLDecoder.decode(channelName, "UTF-8").toUpperCase).map {
      ltv => buildResponseSeq(toTVShorts(ltv), s"No TV content for the channel: $channelName")
    }
  }

  def allContentByTypeAndProvider(contentType: String, provider: String) = Action.async {
    contentRepository.findDayContentByTypeAndProvider(contentType.toLowerCase, provider.toUpperCase).map {
      ltv => buildResponseSeq(toTVShorts(ltv), s"No TV content for type: $contentType and provider: $provider")
    }
  }

  def currentContentByTypeAndProvider(contentType: String, provider: String) = Action.async {
    contentRepository.findCurrentContentByTypeAndProvider(contentType.toLowerCase, provider.toUpperCase).map {
      ltv => buildResponseSeq(toTVShorts(ltv), s"No TV content at this moment for the type: $contentType and provider: $provider")
    }
  }

  def contentLeftByTypeAndProvider(contentType: String, provider: String) = Action.async {
    contentRepository.findLeftContentByTypeAndProvider(contentType.toLowerCase(), provider.toUpperCase()).map {
      ltv => buildResponseSeq(toTVShorts(ltv), s"No TV content left for the type: $contentType and provider: $provider")
    }
  }

  def currentContentByProvider(provider: String) = Action.async {
    contentRepository.findCurrentContentByProvider(provider.toUpperCase()).map {
      ltv => buildResponseSeq(toTVShorts(ltv), s"No TV content at this moment for provider: $provider")
    }
  }


}
