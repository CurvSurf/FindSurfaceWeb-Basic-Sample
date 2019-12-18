# Request FindSurface via JAVA

## Sample Data 

This sample uses the following form of an array for input data:

```java
public final static float POINTS[] = { /* Sample Data */ };
```

See [here](../README.md#sample-data) for the actual content of the sample data.

## How to: make a Request body

```java
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
```

## How to: Request FindSurface

```java
// Request FindSurface
URL url = new URL( "https://developers.curvsurf.com/FindSurface/plane" );
URLConnection conn = url.openConnection();
conn.setDoOutput(true);

if( conn instanceof HttpsURLConnection ) {
	((HttpsURLConnection)conn).setHostnameVerifier( HttpsURLConnection.getDefaultHostnameVerifier() );
}

byte[] reqBodyBuffer = requestBody.array();

HttpURLConnection http = (HttpURLConnection)conn;
http.setRequestMethod("POST");
http.setRequestProperty("Content-Type", "application/x-findsurface-request");
http.setRequestProperty("Content-Length", Integer.toString( reqBodyBuffer.length ));
if( requestBody.order() == ByteOrder.BIG_ENDIAN ) {
	http.setRequestProperty( "X-Content-Endian", "big" );
	http.setRequestProperty( "X-Accept-Endian", "big" );
}

OutputStream reqWriter = http.getOutputStream();
reqWriter.write( reqBodyBuffer );
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
		// Do something with Response Data
	}
}
http.disconnect();
```

## How to: Parse Response

```java
String endian = http.getHeaderField( "X-Content-Endian" );
ByteOrder respOrder = ( endian != null && endian.equalsIgnoreCase("big") ) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

ByteBuffer responseBody = ByteBuffer.wrap( responseBuffer );
responseBody.order( respOrder );

if( responseBody.get(0x00) == 0x46 &&
	responseBody.get(0x01) == 0x53 &&
	responseBody.get(0x02) == 1 &&
	responseBody.get(0x03) == 0 )
{
	int resultCode = responseBody.get(0x08);
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
```
