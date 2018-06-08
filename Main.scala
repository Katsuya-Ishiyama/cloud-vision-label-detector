import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.io.{File, PrintWriter}
import scala.collection.JavaConverters._
import com.google.protobuf.ByteString
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Feature.Type
import LabelDetection.commandline.CommandlineArgumentParser
import LabelDetection.commandline.CommandlineArgumentParser.CommandlineArguments

/*
 * 参考URL: 
 *  https://cloud.google.com/vision/docs/libraries#client-libraries-usage-java
 *  https://qiita.com/toastkidjp/items/5500521ff5dc0346c2b1
 *  https://stackoverflow.com/questions/38339440/how-to-convert-a-java-stream-to-a-scala-stream
 */

object LabelDetector {
  def main(args: Array[String]): Unit = {
    val cmdArgs: CommandlineArguments = CommandlineArgumentParser.parse(args)

    // CSVのwriterを準備する
    val writer = new PrintWriter(new File(s"${cmdArgs.outputDir}/yuruchara_labels.csv"))
    val header = "fullpath,filename,description,score,topicality\n"
    writer.write(header)

    val imageFiles = getImageFiles(cmdArgs.srcImageDir)
    var processFileNum = 0
    imageFiles.foreach{filePath =>
      processFileNum += 1
      println(s"processing $processFileNum $filePath")

      val image = loadImageAsBase64(filePath)
      val responses = fetchImageLabels(image)
      responses.iterator().asScala.foreach{ res =>

        val annotationsList = res.getLabelAnnotationsList().iterator().asScala
        annotationsList.foreach { annotation =>

          val description = annotation.getDescription()
          val score       = annotation.getScore()
          val topicality  = annotation.getTopicality()

          // CSVに書き込む
          // TODO: pythonでいうPath().nameみたいな形で取りたい
          val fileName = filePath.toString.split("/").last
          val line = s"$filePath,$fileName,$description,$score,$topicality\n"
          writer.write(line)

        }

      }

    }

    writer.flush()
    writer.close()

  }

  def getImageFiles(dir: String) = Files.list(Paths.get(dir)).iterator().asScala

  def loadImageAsBase64(path: Path) = {
    val data       = Files.readAllBytes(path)
    val imageBytes = ByteString.copyFrom(data)

    Image.newBuilder().setContent(imageBytes).build()
  }

  def fetchImageLabels(image: Image) = {
    val feature = Feature.newBuilder().setType(Type.LABEL_DETECTION).build()
    val request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build()

    // 画像のラベルを取得して返却
    val vision = ImageAnnotatorClient.create()
    vision.batchAnnotateImages(List(request).asJava).getResponsesList()
  }

}
