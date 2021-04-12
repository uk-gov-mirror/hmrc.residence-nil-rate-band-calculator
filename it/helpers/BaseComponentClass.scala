package helpers

import common.CommonPlaySpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, Environment}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.residencenilratebandcalculator.models.Calculator

class BaseComponentClass extends CommonPlaySpec with GuiceOneServerPerSuite with MockitoSugar {
  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .build()

  val calculateUrl = s"http://localhost:$port/residence-nil-rate-band-calculator/calculate"
  def nilRateBandUrl(date: String) = s"http://localhost:$port/residence-nil-rate-band-calculator/nilrateband/$date"
  val jsonHelper: jsonHelperFactory = jsonHelperFactory()
  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  val env: Environment = mock[Environment]
  val calculator = new Calculator(env)

}
