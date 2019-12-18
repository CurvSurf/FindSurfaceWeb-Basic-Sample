// on the JVM & Android
// compile: kotlinc main.kt -include-runtime -d reqSample.jar
// run: java -jar reqSample.jar
import java.io.*
import java.net.*
import javax.net.ssl.*
import java.nio.*

fun main(args: Array<String>) 
{
    // Ready Request Body
    val REQ_HEADER_SIZE = 40

    val requestBody = ByteBuffer.allocate( REQ_HEADER_SIZE + (SIZE_OF_FLOAT * POINTS.size) )
    requestBody.order( ByteOrder.nativeOrder() )

    // Fill Header Part
    requestBody.put( 0x00, 0x46 ) // 'F'
    requestBody.put( 0x01, 0x53 ) // 'S'
    requestBody.put( 0x02, 0x01 ) // major - 1
    requestBody.put( 0x03, 0x00 ) // minor - 0
    requestBody.putInt( 0x04, REQ_HEADER_SIZE )

    requestBody.putInt( 0x08, POINTS.size / 3 )   // Point Count
    requestBody.putInt( 0x0C, 0 )                 // Point Offset
    requestBody.putInt( 0x10, SIZE_OF_FLOAT * 3 ) // Point Stride

    requestBody.putFloat( 0x14, 0.003f ) // Measurement Accracy
    requestBody.putFloat( 0x18, 0.15f )  // Mean Distance
    requestBody.putFloat( 0x1C, 0.3f )   // Touch Radius
    requestBody.putInt( 0x20, 45 )       // Seed Index
    requestBody.put( 0x24, 0 )           // Not Used
    requestBody.put( 0x25, 5 )           // Rad. Exp.
    requestBody.put( 0x26, 5 )           // Lat. Ext.
    requestBody.put( 0x27, 0 )           // Options

    // Fill Data Part
    requestBody.position( REQ_HEADER_SIZE )
    requestBody.asFloatBuffer().put( POINTS )

	// Request FindSurface
    val url = URL( "https://developers.curvsurf.com/FindSurface/plane" )
    val conn = url.openConnection()
    conn.doOutput = true

    if( conn is HttpsURLConnection ) {
		conn.hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
    }

    with( conn as HttpURLConnection )
    {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/x-findsurface-request")
        setRequestProperty("Content-Length", requestBody.capacity().toString())
        if( requestBody.order() == ByteOrder.BIG_ENDIAN ) {
            setRequestProperty( "X-Content-Endian", "big" )
            setRequestProperty( "X-Accept-Endian", "big" )
        }

        val reqWriter = outputStream
        reqWriter.write( requestBody.array() )
        reqWriter.flush()
        reqWriter.close()

		// Check Response
        if( responseCode == 200 )
        {
            val respBuffer = ByteArray( contentLength )
            val respReader = inputStream
            respReader.read(respBuffer)
            respReader.close()

            val contentType = getContentType()
            if( contentType.equals("application/x-findsurface-response") )
            {
                val endian = getHeaderField("X-Content-Endian")
                val respOrder = if (endian != null && endian.equals("big")) ByteOrder.BIG_ENDIAN
                                 else ByteOrder.LITTLE_ENDIAN

                val responseBody = ByteBuffer.wrap( respBuffer )
                responseBody.order( respOrder )

                if( responseBody.get(0x00) == 0x46.toByte() &&
                    responseBody.get(0x01) == 0x53.toByte() &&
                    responseBody.get(0x02) == 1.toByte() &&
                    responseBody.get(0x03) == 0.toByte() )
                {
                    val resultCode = responseBody.getInt( 0x08 )
                    when( resultCode )
                    {
                        1 -> {
                            println( "Plane (rms: %g): ".format( responseBody.getFloat(0x10) ) )
                            println( "LL: [ %g, %g, %g ]".format(
                                    responseBody.getFloat(20),
                                    responseBody.getFloat(24),
                                    responseBody.getFloat(28)) )
                            println( "LR: [ %g, %g, %g ]".format(
                                    responseBody.getFloat(32),
                                    responseBody.getFloat(36),
                                    responseBody.getFloat(40)) )
                            println("UR: [ %g, %g, %g ]".format(
                                    responseBody.getFloat(44),
                                    responseBody.getFloat(48),
                                    responseBody.getFloat(52)) )
                            println("UL: [ %g, %g, %g ]".format(
                                    responseBody.getFloat(56),
                                    responseBody.getFloat(60),
                                    responseBody.getFloat(64)) )
                        }
                        2 -> {
                            println( "Sphere (rms: %g): ".format( responseBody.getFloat(0x10) ) )
                            println( "C : [ %g, %g, %g ]".format(
                                    responseBody.getFloat(20),
                                    responseBody.getFloat(24),
                                    responseBody.getFloat(28)) )
                            println( "R : %g".format( responseBody.getFloat(32) ) )
                        }
                        3 -> {
                            println( "Cylinder (rms: %g): ".format( responseBody.getFloat(0x10) ) )
                            println( "B : [ %g, %g, %g ]".format(
                                    responseBody.getFloat(20),
                                    responseBody.getFloat(24),
                                    responseBody.getFloat(28)) )
                            println( "T : [ %g, %g, %g ]".format(
                                    responseBody.getFloat(32),
                                    responseBody.getFloat(36),
                                    responseBody.getFloat(40)) )
                            println( "R : %g".format( responseBody.getFloat(44) ) )
                        }
                        4 -> {
                            println( "Cone (rms: %g): ".format( responseBody.getFloat(0x10) ) )
                            println( "B : [ %g, %g, %g ]".format(
                                    responseBody.getFloat(20),
                                    responseBody.getFloat(24),
                                    responseBody.getFloat(28)) )
                            println( "T : [ %g, %g, %g ]".format(
                                    responseBody.getFloat(32),
                                    responseBody.getFloat(36),
                                    responseBody.getFloat(40)) )
                            println( "BR: %g".format( responseBody.getFloat(44) ) )
                            println( "TR: %g".format( responseBody.getFloat(48) ) )
                        }
                        5 -> {
                            println( "Torus (rms: %g): ".format( responseBody.getFloat(0x10) ) )
                            println( "C : [ %g, %g, %g ]".format(
                                    responseBody.getFloat(20),
                                    responseBody.getFloat(24),
                                    responseBody.getFloat(28)) )
                            println( "N : [ %g, %g, %g ]".format(
                                    responseBody.getFloat(32),
                                    responseBody.getFloat(36),
                                    responseBody.getFloat(40)) )
                            println( "MR: %g".format( responseBody.getFloat(44) ) )
                            println( "TR: %g".format( responseBody.getFloat(48) ) )
                        }
                        0 -> println( "Not Found" )
                        else -> println("Unknown Error: " + resultCode)
                    }
                }
            }
            else if( contentType.startsWith("application/json"))
            {
                println( String( respBuffer, java.nio.charset.Charset.forName("UTF-8")) )
            }
        }

		disconnect()
    }
}

const val SIZE_OF_FLOAT = 4
val POINTS = floatArrayOf (
	-0.5f, -0.5f, -0.0022f,
	-0.389f, -0.5f, -0.00096f,
	-0.278f, -0.5f, 0.0019f,
	-0.167f, -0.5f, 0.00258f,
	-0.0556f, -0.5f, 0.000432f,
	0.0556f, -0.5f, -0.00125f,
	0.167f, -0.5f, 0.00272f,
	0.278f, -0.5f, -0.0017f,
	0.389f, -0.5f, 0.000504f,
	0.5f, -0.5f, 0.00286f,
	-0.5f, -0.389f, -0.00242f,
	-0.389f, -0.389f, -0.00221f,
	-0.278f, -0.389f, 0.00162f,
	-0.167f, -0.389f, -0.000678f,
	-0.0556f, -0.389f, -0.00221f,
	0.0556f, -0.389f, 0.000648f,
	0.167f, -0.389f, -0.00248f,
	0.278f, -0.389f, -0.00057f,
	0.389f, -0.389f, 0.000732f,
	0.5f, -0.389f, 0.000948f,
	-0.5f, -0.278f, 0.0016f,
	-0.389f, -0.278f, 0.00181f,
	-0.278f, -0.278f, 0.000936f,
	-0.167f, -0.278f, -0.00269f,
	-0.0556f, -0.278f, 0.00072f,
	0.0556f, -0.278f, 0.00204f,
	0.167f, -0.278f, -0.0023f,
	0.278f, -0.278f, -0.00264f,
	0.389f, -0.278f, -0.00146f,
	0.5f, -0.278f, 0.00206f,
	-0.5f, -0.167f, -0.000756f,
	-0.389f, -0.167f, -0.00252f,
	-0.278f, -0.167f, 0.00224f,
	-0.167f, -0.167f, 0.00229f,
	-0.0556f, -0.167f, 0.0012f,
	0.0556f, -0.167f, -0.00219f,
	0.167f, -0.167f, -0.00197f,
	0.278f, -0.167f, 0.000912f,
	0.389f, -0.167f, -0.000888f,
	0.5f, -0.167f, -0.000324f,
	-0.5f, -0.0556f, 0.000768f,
	-0.389f, -0.0556f, -0.00217f,
	-0.278f, -0.0556f, 0.000468f,
	-0.167f, -0.0556f, -0.000612f,
	-0.0556f, -0.0556f, 0.00015f,
	0.0556f, -0.0556f, -0.000606f,
	0.167f, -0.0556f, 0.00118f,
	0.278f, -0.0556f, -0.00119f,
	0.389f, -0.0556f, 0.00182f,
	0.5f, -0.0556f, -0.0011f,
	-0.5f, 0.0556f, 0.00276f,
	-0.389f, 0.0556f, 0.000414f,
	-0.278f, 0.0556f, 0.00185f,
	-0.167f, 0.0556f, -0.00116f,
	-0.0556f, 0.0556f, -0.00114f,
	0.0556f, 0.0556f, -0.000426f,
	0.167f, 0.0556f, 0.00202f,
	0.278f, 0.0556f, -0.0023f,
	0.389f, 0.0556f, -0.00193f,
	0.5f, 0.0556f, -0.00245f,
	-0.5f, 0.167f, 0.00276f,
	-0.389f, 0.167f, -0.00154f,
	-0.278f, 0.167f, 0.00218f,
	-0.167f, 0.167f, 0.000138f,
	-0.0556f, 0.167f, -0.00226f,
	0.0556f, 0.167f, 0.000372f,
	0.167f, 0.167f, 0.000948f,
	0.278f, 0.167f, 0.00292f,
	0.389f, 0.167f, 0.00243f,
	0.5f, 0.167f, 0.0012f,
	-0.5f, 0.278f, -0.00227f,
	-0.389f, 0.278f, 0.000192f,
	-0.278f, 0.278f, 0.00203f,
	-0.167f, 0.278f, 0.0012f,
	-0.0556f, 0.278f, 0.00072f,
	0.0556f, 0.278f, -0.00268f,
	0.167f, 0.278f, -0.00241f,
	0.278f, 0.278f, -0.0011f,
	0.389f, 0.278f, -0.00087f,
	0.5f, 0.278f, 0.000558f,
	-0.5f, 0.389f, -0.00106f,
	-0.389f, 0.389f, -0.00298f,
	-0.278f, 0.389f, -0.00203f,
	-0.167f, 0.389f, -0.00221f,
	-0.0556f, 0.389f, -0.00114f,
	0.0556f, 0.389f, -0.00203f,
	0.167f, 0.389f, -0.00149f,
	0.278f, 0.389f, -0.00212f,
	0.389f, 0.389f, -0.00133f,
	0.5f, 0.389f, -0.00228f,
	-0.5f, 0.5f, 0.00257f,
	-0.389f, 0.5f, 0.00257f,
	-0.278f, 0.5f, -0.000816f,
	-0.167f, 0.5f, 0.00175f,
	-0.0556f, 0.5f, -0.00216f,
	0.0556f, 0.5f, -0.00193f,
	0.167f, 0.5f, -0.00274f,
	0.278f, 0.5f, -7.2e-05f,
	0.389f, 0.5f, 0.00213f,
	0.5f, 0.5f, 0.00269f
)