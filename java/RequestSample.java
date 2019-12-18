import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.net.ssl.HttpsURLConnection;

public class RequestSample
{
	public static void main(String[] args) throws Exception
	{
		final int REQ_HEADER_SIZE = 40;

		ByteBuffer requestBody = ByteBuffer.allocate( REQ_HEADER_SIZE + (Float.BYTES * POINTS.length) );
		requestBody.order( ByteOrder.nativeOrder() );
		
		// Fill Header Part
		requestBody.put( 0x00, (byte)0x46 ); // 'F'
		requestBody.put( 0x01, (byte)0x53 ); // 'S'
		requestBody.put( 0x02, (byte)0x01 ); // major - 1 
		requestBody.put( 0x03, (byte)0x00 ); // minor - 0
		requestBody.putInt( 0x04, REQ_HEADER_SIZE );

		requestBody.putInt( 0x08, POINTS.length / 3 ); // Point Count
		requestBody.putInt( 0x0C, 0 );                 // Point Offset
		requestBody.putInt( 0x10, Float.BYTES * 3 );   // Point Stride

		requestBody.putFloat( 0x14, 0.003f ); // Measurement Accracy
		requestBody.putFloat( 0x18, 0.15f );  // Mean Distance
		requestBody.putFloat( 0x1C, 0.3f );   // Touch Radius
		requestBody.putInt( 0x20, 45 );       // Seed Index
		requestBody.put( 0x24, (byte)0 );     // Not Used
		requestBody.put( 0x25, (byte)5 );     // Rad. Exp.
		requestBody.put( 0x26, (byte)5 );     // Lat. Ext.
		requestBody.put( 0x27, (byte)0 );     // Options

		// Fill Data Part
		requestBody.position( REQ_HEADER_SIZE );
		requestBody.asFloatBuffer().put( POINTS );

		// Request FindSurface
		URL url = new URL( "https://developers.curvsurf.com/FindSurface/plane" );
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);

		if( conn instanceof HttpsURLConnection ) {
			((HttpsURLConnection)conn).setHostnameVerifier( HttpsURLConnection.getDefaultHostnameVerifier() );
		}

		HttpURLConnection http = (HttpURLConnection)conn;
		http.setRequestMethod("POST");
		http.setRequestProperty("Content-Type", "application/x-findsurface-request");
		http.setRequestProperty("Content-Length", Integer.toString( requestBody.capacity() ));
		if( requestBody.order() == ByteOrder.BIG_ENDIAN ) {
			http.setRequestProperty( "X-Content-Endian", "big" );
			http.setRequestProperty( "X-Accept-Endian", "big" );
		}

		OutputStream reqWriter = http.getOutputStream();
		reqWriter.write( requestBody.array() );
		reqWriter.flush();
		reqWriter.close();

		// Check Response
		if( http.getResponseCode() == 200 )
		{
			byte[] responseBuffer = new byte[ http.getContentLength() ];

			InputStream respReader = http.getInputStream();
			respReader.read( responseBuffer );
			respReader.close();

			String contentType = http.getContentType();
			if( contentType.equals("application/x-findsurface-response") )
			{
				String endian = http.getHeaderField( "X-Content-Endian" );
        		ByteOrder respOrder = ( endian != null && endian.equalsIgnoreCase("big") ) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

				ByteBuffer responseBody = ByteBuffer.wrap( responseBuffer );
				responseBody.order( respOrder );

				if( responseBody.get(0x00) == 0x46 &&
					responseBody.get(0x01) == 0x53 &&
					responseBody.get(0x02) == 1 &&
					responseBody.get(0x03) == 0 )
				{
					int resultCode = responseBody.getInt(0x08);
					switch( resultCode )
					{
						case 1:
						{
							System.out.printf( "Plane (rms: %g): \n", responseBody.getFloat(0x10) );
							System.out.printf("LL: [ %g, %g, %g ]\n", 
								responseBody.getFloat(20), responseBody.getFloat(24), responseBody.getFloat(28) );
							System.out.printf("LR: [ %g, %g, %g ]\n", 
								responseBody.getFloat(32), responseBody.getFloat(36), responseBody.getFloat(40) );
							System.out.printf("UR: [ %g, %g, %g ]\n", 
								responseBody.getFloat(44), responseBody.getFloat(48), responseBody.getFloat(52) );
							System.out.printf("UL: [ %g, %g, %g ]\n", 
								responseBody.getFloat(56), responseBody.getFloat(60), responseBody.getFloat(64) );
						}
						break;

						case 2:
						{
							System.out.printf( "Sphere (rms: %g): \n", responseBody.getFloat(0x10) );
							System.out.printf("C : [ %g, %g, %g ]\n", 
								responseBody.getFloat(20), responseBody.getFloat(24), responseBody.getFloat(28) );
							System.out.printf("R : %g\n", responseBody.getFloat(32) );
						}
						break;

						case 3:
						{
							System.out.printf( "Cylinder (rms: %g): \n", responseBody.getFloat(0x10) );
							System.out.printf("B : [ %g, %g, %g ]\n", 
								responseBody.getFloat(20), responseBody.getFloat(24), responseBody.getFloat(28) );
							System.out.printf("T : [ %g, %g, %g ]\n", 
								responseBody.getFloat(32), responseBody.getFloat(36), responseBody.getFloat(40) );
							System.out.printf("R : %g\n", responseBody.getFloat(44) );
						}
						break;

						case 4:
						{
							System.out.printf( "Cone (rms: %g): \n", responseBody.getFloat(0x10) );
							System.out.printf("B : [ %g, %g, %g ]\n", 
								responseBody.getFloat(20), responseBody.getFloat(24), responseBody.getFloat(28) );
							System.out.printf("T : [ %g, %g, %g ]\n", 
								responseBody.getFloat(32), responseBody.getFloat(36), responseBody.getFloat(40) );
							System.out.printf("BR: %g\n", responseBody.getFloat(44) );
							System.out.printf("TR: %g\n", responseBody.getFloat(48) );
						}
						break;

						case 5:
						{
							System.out.printf( "Torus (rms: %g): \n", responseBody.getFloat(0x10) );
							System.out.printf("C : [ %g, %g, %g ]\n", 
								responseBody.getFloat(20), responseBody.getFloat(24), responseBody.getFloat(28) );
							System.out.printf("N : [ %g, %g, %g ]\n", 
								responseBody.getFloat(32), responseBody.getFloat(36), responseBody.getFloat(40) );
							System.out.printf("MR: %g\n", responseBody.getFloat(44) );
							System.out.printf("TR: %g\n", responseBody.getFloat(48) );
						}
						break;

						case 0:
							System.out.println("Not Found");
							break;

						default:
							System.out.println("Unknown Error: " + resultCode);
							break;
					}
				}
			}
			else if( contentType.startsWith("application/json") )
			{
				System.out.println( new String(responseBuffer, "UTF-8") );
			}
		}

		http.disconnect();
	}

	public final static float POINTS[] = {
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
	};
}