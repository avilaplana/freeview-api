package utils

import controllers._
import models._
import org.joda.time.DateTimeZone

object DomainBuilder {

  object TVShortWithTimeZone {
    def apply(tvContent: TVContent): TVContentShort = {

      val episode = for {
        s <- tvContent.series
        e <- s.episode
      } yield (e.episodeTitle, e.seasonNumber, e.episodeNumber)

      val es = episode match {
        case None => None
        case Some((et, sn, en)) => Some(EpisodeShort(et, sn, en))
      }

      TVContentShort(
        tvContent.channel,
//        tvContent.channelImageURL,
        "",
        tvContent.provider,
        tvContent.start.withZone(DateTimeZone.forID("Europe/London")),
        tvContent.end.withZone(DateTimeZone.forID("Europe/London")),
        tvContent.series.map(s => SeriesShort(s.serieTitle, es, s.rating, s.poster)),
        tvContent.film.map(p => FilmShort(p.title, p.rating, p.poster)),
        tvContent.program.map(p => ProgramShort(p.title)),
//        tvContent.onTimeNow,
        true,
//        tvContent.perCentTimeElapsed,
        Some(70),
        tvContent.id)
    }
  }

  object TVProgramWithTimeZone {
    def apply(tvProgram: TVContent): TVContentLong = TVLong(tvProgram.copy(
      start = tvProgram.start.withZone(DateTimeZone.forID("Europe/London")),
      end = tvProgram.end.withZone(DateTimeZone.forID("Europe/London"))))
  }
}
