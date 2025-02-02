/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.residencenilratebandcalculator.components

import javax.inject.{Inject, Singleton}
import org.slf4j.MDC
import play.api.{Application, Configuration, Logging}

@Singleton
class Global @Inject()(config: Configuration)(implicit app: Application) extends Logging{

  lazy val appName = config.getString("appName").getOrElse("APP NAME NOT SET")
  lazy val loggerDateFormat: Option[String] = config.get[Option[String]]("logger.json.dateformat")

  logger.info(s"Starting microservice : $appName : in mode : ${app.mode}")
  MDC.put("appName", appName)
  loggerDateFormat.foreach(str => MDC.put("logger.json.dateformat", str))
}
