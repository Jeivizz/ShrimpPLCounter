import com.embasa.plcounter.vision.ShrimpCounter
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs

fun main() {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    val dir = "/mnt/user-data/uploads/"
    val photos = listOf(
        "foto1 (fundo branco)" to "test2.png",
        "foto2 (balde)" to "test.png",
        "foto3 (lotada)" to "1790775717008_image.png",
    )
    val counter = ShrimpCounter()
    for ((name, file) in photos) {
        val mat: Mat = Imgcodecs.imread(dir + file)
        val direct = counter.count(mat)

        val buf = MatOfByte()
        Imgcodecs.imencode(".jpg", mat, buf, MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 90))
        val viaJpeg = counter.countJpeg(buf.toArray())

        val xs = direct.detections.map { it.centerX }
        val ys = direct.detections.map { it.centerY }
        val inside = xs.all { it in 0.0..direct.imageWidth.toDouble() } && ys.all { it in 0.0..direct.imageHeight.toDouble() }
        println("%-22s direto=%3d  via JPEG q90=%3d  escala=%.3f  %dx%d  centros dentro=%s".format(
            name, direct.detections.size, viaJpeg.detections.size, direct.scale,
            direct.imageWidth, direct.imageHeight, inside))
        mat.release()
    }
}