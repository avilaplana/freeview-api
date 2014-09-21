package controllers

import models.TVChannelRepository
import play.api.libs.json.Json
import play.api.mvc.Action

import scala.concurrent.ExecutionContext.Implicits.global

object TVChannelController extends TVChannelController {

  override val channelRepository = new TVChannelRepository("tvChannel")
}
trait TVChannelController extends BaseController {

  val channelRepository : TVChannelRepository

  def channels = Action.async {
    channelRepository.listOfTVChannels().map {
      case head :: tail => {
        Ok(Json.toJson(head :: tail))
      }
      case Nil => NotFound
    }
  }

  def channelsByGenre(genre: String) = Action.async {
    channelRepository.listOfTVChannelsByGenre(genre.toUpperCase).map {
      case head :: tail => {
        Ok(Json.toJson(head :: tail))
      }
      case Nil => NotFound
    }
  }
}
