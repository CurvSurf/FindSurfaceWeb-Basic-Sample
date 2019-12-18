# Request FindSurface via C#

## Sample Data 

This sample uses the following form of an array for input data:

```csharp
public static readonly float[] POINTS = { /* Sample Points */ };
```

See [here](../README.md#sample-data) for the actual content of the sample data.

## How to: make a Request body

```csharp
readonly UInt32 REQ_HEADER_SIZE = 40;

byte[] requestBody = new byte[REQ_HEADER_SIZE + (sizeof(float) * POINTS.Length)];

// Fill Header Part
using (MemoryStream ms = new MemoryStream(requestBody))
{
	using (BinaryWriter bw = new BinaryWriter(ms))
	{
		bw.Write((byte)0x46); // 'F'
		bw.Write((byte)0x53); // 'S'
		bw.Write((byte)1);    // major - 1
		bw.Write((byte)0);    // minor - 0
		bw.Write(REQ_HEADER_SIZE);

		bw.Write((UInt32)(POINTS.Length / 3)); // Point Count
		bw.Write((UInt32)0);                   // Point Offset
		bw.Write((UInt32)(sizeof(float) * 3)); // Point Stride

		bw.Write(0.003f);     // Measurement Accuracy
		bw.Write(0.15f);      // Mean Distance
		bw.Write(0.3f);       // Touch Radius
		bw.Write((UInt32)45); // Seed Index
		bw.Write((byte)0);    // Not Used
		bw.Write((byte)5);    // Rad. Exp.
		bw.Write((byte)5);    // Lat. Ext.
		bw.Write((byte)0);    // Options

		bw.Close();
	}
	ms.Close();
}

// Fill Data Part
Buffer.BlockCopy(POINTS, 0, requestBody, (int)REQ_HEADER_SIZE, (sizeof(float) * POINTS.Length));
```

## How to: Request FindSurface

```csharp
// Request FindSurface
WebRequest req = WebRequest.Create("https://developers.curvsurf.com/FindSurface/plane");
req.Credentials = CredentialCache.DefaultCredentials;

req.Method = "POST";
req.ContentType = "application/x-findsurface-request";
req.ContentLength = requestBody.Length;
if (!BitConverter.IsLittleEndian) {
	req.Headers.Add("X-Content-Endian", "big");
	req.Headers.Add("X-Accept-Endian", "big");
}

using (Stream reqWriter = req.GetRequestStream())
{
	reqWriter.Write(requestBody, 0, requestBody.Length);
	reqWriter.Close();
}

// Check Response
HttpWebResponse resp = (HttpWebResponse)req.GetResponse();
if( resp.StatusCode == HttpStatusCode.OK )
{
	byte[] responseBody = new byte[resp.ContentLength];
	using (Stream respReader = resp.GetResponseStream())
	{
		respReader.Read(responseBody, 0, responseBody.Length);
		respReader.Close();
	}

	if( resp.ContentType.Equals("application/x-findsurface-response") )
	{
		// Do something with Response Data
	}
}
resp.Close();
```

## How to: Parse Response

```csharp
if( responseBody[0] == 0x46 && 
	responseBody[1] == 0x53 && 
	responseBody[2] == 1 && 
	responseBody[3] == 0 )
{
	int resultCode = BitConverter.ToInt32(responseBody, 0x08);
	switch( resultCode )
	{
		case 1:
		{
			Console.WriteLine("Plane (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
			Console.WriteLine("LL: [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 20), 
				BitConverter.ToSingle(responseBody, 24), 
				BitConverter.ToSingle(responseBody, 28));
			Console.WriteLine("LR: [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 32),
				BitConverter.ToSingle(responseBody, 36),
				BitConverter.ToSingle(responseBody, 40));
			Console.WriteLine("UR: [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 44),
				BitConverter.ToSingle(responseBody, 48),
				BitConverter.ToSingle(responseBody, 52));
			Console.WriteLine("UL: [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 56),
				BitConverter.ToSingle(responseBody, 60),
				BitConverter.ToSingle(responseBody, 64));
		}
		break;

		case 2:
		{
			Console.WriteLine("Sphere (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
			Console.WriteLine("C : [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 20),
				BitConverter.ToSingle(responseBody, 24),
				BitConverter.ToSingle(responseBody, 28));
			Console.WriteLine("R : {0:G}", BitConverter.ToSingle(responseBody, 32));
		}
		break;

		case 3:
		{
			Console.WriteLine("Cylinder (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
			Console.WriteLine("B : [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 20),
				BitConverter.ToSingle(responseBody, 24),
				BitConverter.ToSingle(responseBody, 28));
			Console.WriteLine("T : [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 32),
				BitConverter.ToSingle(responseBody, 36),
				BitConverter.ToSingle(responseBody, 40));
			Console.WriteLine("R : {0:G}", BitConverter.ToSingle(responseBody, 44));
		}
		break;

		case 4:
		{
			Console.WriteLine("Cone (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
			Console.WriteLine("B : [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 20),
				BitConverter.ToSingle(responseBody, 24),
				BitConverter.ToSingle(responseBody, 28));
			Console.WriteLine("T : [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 32),
				BitConverter.ToSingle(responseBody, 36),
				BitConverter.ToSingle(responseBody, 40));
			Console.WriteLine("BR: {0:G}", BitConverter.ToSingle(responseBody, 44));
			Console.WriteLine("TR: {0:G}", BitConverter.ToSingle(responseBody, 48));
		}
		break;

		case 5:
		{
			Console.WriteLine("Torus (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
			Console.WriteLine("C : [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 20),
				BitConverter.ToSingle(responseBody, 24),
				BitConverter.ToSingle(responseBody, 28));
			Console.WriteLine("N : [ {0:G}, {1:G}, {2:G} ]",
				BitConverter.ToSingle(responseBody, 32),
				BitConverter.ToSingle(responseBody, 36),
				BitConverter.ToSingle(responseBody, 40));
			Console.WriteLine("MR: {0:G}", BitConverter.ToSingle(responseBody, 44));
			Console.WriteLine("TR: {0:G}", BitConverter.ToSingle(responseBody, 48));
		}
		break;

		case 0:
			Console.WriteLine("Not Found");
			break;

		default:
			Console.WriteLine("Unknown Error: " + resultCode);
			break;
	}
}
```
