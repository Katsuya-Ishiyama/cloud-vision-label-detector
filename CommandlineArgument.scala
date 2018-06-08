package LabelDetection.commandline

object CommandlineArgumentParser {

  /** A Class for Commandline Arguments.
   * @constructor create a new commandline argument with srcImageDir.
   * @param srcImageDir the directory which the images is saved in.
   */
  case class CommandlineArguments(srcImageDir: String, outputDir: String)

  /** Parse commandline arguments.
   *  @param args An Array object which is given from commandline.
   *  @return CommandlineArgument instance.
   */
  def parse(args: Array[String]): CommandlineArguments = new CommandlineArguments(args(0), args(1))
}

