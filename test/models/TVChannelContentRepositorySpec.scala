package models

import org.scalatest.{BeforeAndAfter, MustMatchers}
import org.scalatestplus.play.PlaySpec
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class TVChannelContentRepositorySpec extends PlaySpec with MustMatchers with BeforeAndAfter {

  val collectionName = this.getClass.getName
  val tvChannelContentRepository = TVChannelContentRepository(collectionName)
  val collection = tvChannelContentRepository.collection

  val channelContentId = BSONObjectID.generate

  val tvContentTodayTest = TVChannelContent(Some(channelContentId), "testChannel1",
    Seq(
      TVProgram("programName1", 0, 1, "documentary"),
      TVProgram("programName2", 1, 2, "documentary"),
      TVProgram("programName3", 2, 3, "documentary"),
      TVProgram("programName4", 3, 4, "documentary"),
      TVProgram("programName5", 4, 5, "documentary"),
      TVProgram("programName6", 6, 7, "documentary")
    )
  )

  before {
    val content = collection.insert(tvContentTodayTest)
    val result = for {
      c <- content
    } yield c.ok

    val isOk = Await.result(result, Duration("20 seconds"))

    isOk match {
      case true => println("Elements inserted")
      case false => {
        collection.drop()
        throw new Exception("Error inserting elements")
      }
    }
  }

  after {
    tvChannelContentRepository.collection.drop()
  }
  //
  //  def findLeftContentByChannel(channelName: String): Seq[TVProgram] = ???
  //
  //  def findDayContentByChannel(channelName: String): Seq[TVProOptiongram] = ???
  //
  //  def findCurrentContentByChannel(channelName: String): TVProgram = ???

  "findDayContentByChannel" should {
    "return all the TV content for a particular channel available today" in {
      val content = tvChannelContentRepository.findDayContentByChannel("testChannel1")
      val result = Await.result(content, Duration("10 seconds"))
      result mustBe Some(tvContentTodayTest)
    }
  }

//  "findCurrentContentByChannel" should {
//    "return the TV content for a particular channel available now" in {
//    }
//  }
//
//  "findLeftContentByChannel" should {
//    "return the TV content for a particular channel available from now until the end of the day" in {
//
//    }
//  }


}