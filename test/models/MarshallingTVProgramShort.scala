package models

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

class MarshallingTVProgramShort extends PlaySpec with MustMatchers {
  val id = BSONObjectID.generate
  val idString = id.stringify
  val now = new DateTime(2014,10,10,10,0,0)
  val fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")

  "Write and reads" should {
    import TVProgram.{tvProgramShortWrites, tvProgramShortReads}

    "transform TVProgram object to json" in {
      Json.toJson(TVProgramShort("bbc1", now, now.plusHours(2), Some("documentary"), Some(id))).toString() mustBe
        s"""{"channel":"bbc1","start":"${fmt.print(now)}","end":"${fmt.print(now.plusHours(2))}","category":"documentary","id":"$idString"}"""
    }
    "transform json to TVProgram object" in {
      Json.parse(s"""{"channel":"bbc1","start":"${fmt.print(now)}","end":"${fmt.print(now.plusHours(2))}","category":"documentary","id":"$idString"}""")
        .as[TVProgramShort] mustBe TVProgramShort("bbc1", now, now.plusHours(2), Some("documentary"), Some(id))
    }
  }
}
