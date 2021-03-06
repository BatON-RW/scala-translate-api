package net.renalias

import dispatch.Request

package object translate {

// TODO: are all these languages really supported by all APIs?
sealed class Language(val langCode:String)
case object English extends Language("EN")
case object Spanish extends Language("ES")
case object French extends Language("FR")
case object German extends Language("DE")
case object Italian extends Language("IT")
case object Portuguese extends Language("PT")
case object Russian extends Language("RU")
case object Swedish extends Language("SE")
case object Finnish extends Language("FI")
case object Norwegian extends Language("NO")
case object Dutch extends Language("NL")

type TranslationResult = Either[TranslationFailure, TranslationSuccess]

/**
 * Class that encapsulates an error response from an API call
 */
case class TranslationFailure(val errorCode:String, val errorDescription:String, val ex:Option[Exception] = None)

/**
 * Class that encapsulates a successful translation result
 */
case class TranslationSuccess(val result:String) {
  override def toString = result
}

protected[translate] object Helpers {
  val buildParam = (p:(String,String)) => p._1 + "=" + Request.encode_%(p._2)
  val buildQuery = (l:List[(String,String)]) => l.flatMap({params:(String,String) => List(buildParam(params))}).mkString("&")
}

/**
 * Abstract trait defining the methods that each translation API must implement
 *
 * This class is not meant to be used directly by API users, use instead the Translate class
 * and mix in the correct trait providing a specific API implementation
 */
trait BaseTranslationAPI {
  def translate(text:String, from:Language, to:Language): TranslationResult
}

/**
 * Provides an Http client based on the Databinder Dispatch library for those APIs that need it. Methods
 * newExecutor and newAsyncExecutor provide blocking and non-blocking Http clients
 */
protected[translate] trait HttpSupport {
  import dispatch._
  var newExecutor = () => new Http
  var newAsyncExecutor = () => new nio.Http
}

/**
 * Base object that should be instantiated, including one of the specific traits that
 * provide the translation itself and any configuration objects as required by the
 * specific trait
 *
 * Use it like this:
 *
 * <code>
 * trait MyBingConfig extends BingConfig { var appId= "your-bing-appId" }
 * val translator = new Translate with Bing with MyBingConfig
 * val result = translator.translate("text to translate", English, Spanish)
 * </code>
 *
 * Translation results are returned as an Either object, where Left indicates an error, wrapped
 * in a TranslationFailure object and Right indicates success, wrapped in a TranslationSuccess object. Therefore,
 * results can be processed as follows:
 *
 * <code>
 * result match {
 *  case Left(TranslationFailure(_, message, _)) => println("There was an error: " + message)
 *  case Right(TranslationSuccess(text)) => println("translation result: " + text)
 * }
 * </code>
 */
abstract class Translate extends BaseTranslationAPI

}