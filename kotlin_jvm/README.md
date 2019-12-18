# Request FindSurface via Kotlin

> **NOTE:** This sample has been written for JVM and Android.

## Sample Data 

This sample uses the following form of an array for input data:

```kotlin
val POINTS = floatArrayOf ( /* Sample Data */ );
```

See [here](../README.md#sample-data) for the actual content of the sample data.

## How to: make a Request body

```kotlin
val SIZE_OF_FLOAT = 4
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
```

## How to: Request FindSurface

```kotlin
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
			// Do something with Response Data	
		}
	}

	disconnect()
}
```

## How to: Parse Response

```kotlin
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
```
