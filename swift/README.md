# Request FindSurface via Swift

## Sample Data 

This sample uses the following form of an array for input data:

```swift
let POINTS: [Float32] = [ /* Sample Data */ ];
```

See [here](../README.md#sample-data) for the actual content of the sample data.

## How to: make a Request body

There are many ways to set up buffers for Request bodies. You may import a C header for a Request body using [a bridging header](https://developer.apple.com/documentation/swift/imported_c_and_objective-c_apis/importing_objective-c_into_swift). 

This sample uses `UnsafeMutableRawPointer` to set up a Request body.

```swift
// Ready Request Body
let REQ_HEADER_SIZE = 40
let PointCount = POINTS.count / 3
let DataLength    = POINTS.count * MemoryLayout<Float32>.size
let RequestLength = REQ_HEADER_SIZE + DataLength

var requestBody = UnsafeMutableRawPointer.allocate(byteCount: RequestLength, alignment: 1)

// Fill Header Part
requestBody.storeBytes(of: 0x46, toByteOffset: 0x00, as: UInt8.self) // 'F'
requestBody.storeBytes(of: 0x53, toByteOffset: 0x01, as: UInt8.self) // 'S'
requestBody.storeBytes(of: 0x01, toByteOffset: 0x02, as: UInt8.self) // major - 1
requestBody.storeBytes(of: 0x00, toByteOffset: 0x03, as: UInt8.self) // minor - 0
requestBody.storeBytes(of: UInt32(REQ_HEADER_SIZE), toByteOffset:0x04, as: UInt32.self)
// Data Description
requestBody.storeBytes(of: UInt32(PointCount), toByteOffset: 0x08, as: UInt32.self) // Point Count
requestBody.storeBytes(of: 0, toByteOffset: 0x0C, as: UInt32.self)                  // Point Offset
requestBody.storeBytes(of: (3 * MemoryLayout<Float32>.size), toByteOffset: 0x10, as: UInt32.self) // Point Stride
// Algorithm Parameter
requestBody.storeBytes(of: 0.003, toByteOffset: 0x14, as:Float32.self) // Measurement Accuracy
requestBody.storeBytes(of: 0.15, toByteOffset: 0x18, as:Float32.self)  // Mean Distance
requestBody.storeBytes(of: 0.3, toByteOffset: 0x1C, as: Float32.self)  // Touch Raidus
requestBody.storeBytes(of: 45, toByteOffset: 0x20, as: UInt32.self)    // Seed Index
requestBody.storeBytes(of: 0, toByteOffset: 0x24, as: UInt8.self)      // Not Used
requestBody.storeBytes(of: 5, toByteOffset: 0x25, as: UInt8.self)      // Rad. Exp.
requestBody.storeBytes(of: 5, toByteOffset: 0x26, as: UInt8.self)      // Lat. Ext.
// Options
requestBody.storeBytes(of: 0, toByteOffset: 0x27, as: UInt8.self)      // Options

// Fill Data Part
(requestBody + 0x28).copyMemory(from: POINTS, byteCount: DataLength)
```

## How to: Request FindSurface

```swift
// Request FindSurface
var req = URLRequest(url: URL(string: "https://developers.curvsurf.com/FindSurface/plane")!)
req.httpMethod = "POST"
req.setValue("application/x-findsurface-request", forHTTPHeaderField: "Content-Type")
// "Content-Length " will be automatically attached by URLRequest().
//req.setValue(String(RequestLength), forHTTPHeaderField: "Content-Length")
if CFByteOrderGetCurrent() == CFByteOrder( CFByteOrderBigEndian.rawValue ) {
    req.setValue("big", forHTTPHeaderField: "X-Content-Endian")
    req.setValue("big", forHTTPHeaderField: "X-Accept-Endian")
}
req.httpBody = Data(bytesNoCopy: requestBody, count: RequestLength, deallocator: Data.Deallocator.none)

// Request & Check Response
let finishCondition = NSCondition();
let reqTask = URLSession.shared.dataTask(with: req, completionHandler: { data, response, error in
    guard let responseData = data,
          let resp = response as? HTTPURLResponse,
          error == nil,
          resp.statusCode == 200,
          let contentType = resp.allHeaderFields["Content-Type"] as? String
    else {
        finishCondition.signal()
        return
    }

    if contentType == "application/x-findsurface-response"
    {        
        // Do something with Response Data
    }

    finishCondition.signal()
})

reqTask.resume()
finishCondition.wait()
```

## How to: Parse Response

```swift
responseData.withUnsafeBytes({ responseBody in
	if responseBody.load(fromByteOffset: 0x00, as: UInt8.self) == 0x46 &&
		responseBody.load(fromByteOffset: 0x01, as: UInt8.self) == 0x53 &&
		responseBody.load(fromByteOffset: 0x02, as: UInt8.self) == 0x01 &&
		responseBody.load(fromByteOffset: 0x03, as: UInt8.self) == 0x00
	{
		let resultCode = responseBody.load(fromByteOffset: 0x08, as: Int32.self)
		let rms = resultCode < 1 ? 0.0 : responseBody.load(fromByteOffset: 0x10, as: Float32.self)
		switch resultCode
		{
		case 1:
			print("Plane (rms: \(rms)")
			print(String(format: "LL: [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 20, as: Float32.self),
							responseBody.load(fromByteOffset: 24, as: Float32.self),
							responseBody.load(fromByteOffset: 28, as: Float32.self)))
			print(String(format: "LR: [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 32, as: Float32.self),
							responseBody.load(fromByteOffset: 36, as: Float32.self),
							responseBody.load(fromByteOffset: 40, as: Float32.self)))
			print(String(format: "UR: [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 44, as: Float32.self),
							responseBody.load(fromByteOffset: 48, as: Float32.self),
							responseBody.load(fromByteOffset: 52, as: Float32.self)))
			print(String(format: "UL: [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 56, as: Float32.self),
							responseBody.load(fromByteOffset: 60, as: Float32.self),
							responseBody.load(fromByteOffset: 64, as: Float32.self)))
		case 2:
			print("Sphere (rms: \(rms)")
			print(String(format: "C : [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 20, as: Float32.self),
							responseBody.load(fromByteOffset: 24, as: Float32.self),
							responseBody.load(fromByteOffset: 28, as: Float32.self)))
			print(String(format: "R : %g", responseBody.load(fromByteOffset: 32, as: Float32.self)))
		case 3:
			print("Cylinder (rms: \(rms)")
			print(String(format: "B : [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 20, as: Float32.self),
							responseBody.load(fromByteOffset: 24, as: Float32.self),
							responseBody.load(fromByteOffset: 28, as: Float32.self)))
			print(String(format: "T : [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 32, as: Float32.self),
							responseBody.load(fromByteOffset: 36, as: Float32.self),
							responseBody.load(fromByteOffset: 40, as: Float32.self)))
			print(String(format: "R : %g", responseBody.load(fromByteOffset: 44, as: Float32.self)))
		case 4:
			print("Cone (rms: \(rms)")
			print(String(format: "B : [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 20, as: Float32.self),
							responseBody.load(fromByteOffset: 24, as: Float32.self),
							responseBody.load(fromByteOffset: 28, as: Float32.self)))
			print(String(format: "T : [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 32, as: Float32.self),
							responseBody.load(fromByteOffset: 36, as: Float32.self),
							responseBody.load(fromByteOffset: 40, as: Float32.self)))
			print(String(format: "BR: %g", responseBody.load(fromByteOffset: 44, as: Float32.self)))
			print(String(format: "TR: %g", responseBody.load(fromByteOffset: 48, as: Float32.self)))
		case 5:
			print("Torus (rms: \(rms)")
			print(String(format: "C : [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 20, as: Float32.self),
							responseBody.load(fromByteOffset: 24, as: Float32.self),
							responseBody.load(fromByteOffset: 28, as: Float32.self)))
			print(String(format: "N : [ %g, %g, %g ]",
							responseBody.load(fromByteOffset: 32, as: Float32.self),
							responseBody.load(fromByteOffset: 36, as: Float32.self),
							responseBody.load(fromByteOffset: 40, as: Float32.self)))
			print(String(format: "MR: %g", responseBody.load(fromByteOffset: 44, as: Float32.self)))
			print(String(format: "TR: %g", responseBody.load(fromByteOffset: 48, as: Float32.self)))
		case 0:
			print("Not Found")
		default:
			print("Unknown Error: \(resultCode)")
		}
	}
})
```
