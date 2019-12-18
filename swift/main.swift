import Foundation

let POINTS: [Float32] = [
    -0.5, -0.5, -0.0022,
    -0.389, -0.5, -0.00096,
    -0.278, -0.5, 0.0019,
    -0.167, -0.5, 0.00258,
    -0.0556, -0.5, 0.000432,
    0.0556, -0.5, -0.00125,
    0.167, -0.5, 0.00272,
    0.278, -0.5, -0.0017,
    0.389, -0.5, 0.000504,
    0.5, -0.5, 0.00286,
    -0.5, -0.389, -0.00242,
    -0.389, -0.389, -0.00221,
    -0.278, -0.389, 0.00162,
    -0.167, -0.389, -0.000678,
    -0.0556, -0.389, -0.00221,
    0.0556, -0.389, 0.000648,
    0.167, -0.389, -0.00248,
    0.278, -0.389, -0.00057,
    0.389, -0.389, 0.000732,
    0.5, -0.389, 0.000948,
    -0.5, -0.278, 0.0016,
    -0.389, -0.278, 0.00181,
    -0.278, -0.278, 0.000936,
    -0.167, -0.278, -0.00269,
    -0.0556, -0.278, 0.00072,
    0.0556, -0.278, 0.00204,
    0.167, -0.278, -0.0023,
    0.278, -0.278, -0.00264,
    0.389, -0.278, -0.00146,
    0.5, -0.278, 0.00206,
    -0.5, -0.167, -0.000756,
    -0.389, -0.167, -0.00252,
    -0.278, -0.167, 0.00224,
    -0.167, -0.167, 0.00229,
    -0.0556, -0.167, 0.0012,
    0.0556, -0.167, -0.00219,
    0.167, -0.167, -0.00197,
    0.278, -0.167, 0.000912,
    0.389, -0.167, -0.000888,
    0.5, -0.167, -0.000324,
    -0.5, -0.0556, 0.000768,
    -0.389, -0.0556, -0.00217,
    -0.278, -0.0556, 0.000468,
    -0.167, -0.0556, -0.000612,
    -0.0556, -0.0556, 0.00015,
    0.0556, -0.0556, -0.000606,
    0.167, -0.0556, 0.00118,
    0.278, -0.0556, -0.00119,
    0.389, -0.0556, 0.00182,
    0.5, -0.0556, -0.0011,
    -0.5, 0.0556, 0.00276,
    -0.389, 0.0556, 0.000414,
    -0.278, 0.0556, 0.00185,
    -0.167, 0.0556, -0.00116,
    -0.0556, 0.0556, -0.00114,
    0.0556, 0.0556, -0.000426,
    0.167, 0.0556, 0.00202,
    0.278, 0.0556, -0.0023,
    0.389, 0.0556, -0.00193,
    0.5, 0.0556, -0.00245,
    -0.5, 0.167, 0.00276,
    -0.389, 0.167, -0.00154,
    -0.278, 0.167, 0.00218,
    -0.167, 0.167, 0.000138,
    -0.0556, 0.167, -0.00226,
    0.0556, 0.167, 0.000372,
    0.167, 0.167, 0.000948,
    0.278, 0.167, 0.00292,
    0.389, 0.167, 0.00243,
    0.5, 0.167, 0.0012,
    -0.5, 0.278, -0.00227,
    -0.389, 0.278, 0.000192,
    -0.278, 0.278, 0.00203,
    -0.167, 0.278, 0.0012,
    -0.0556, 0.278, 0.00072,
    0.0556, 0.278, -0.00268,
    0.167, 0.278, -0.00241,
    0.278, 0.278, -0.0011,
    0.389, 0.278, -0.00087,
    0.5, 0.278, 0.000558,
    -0.5, 0.389, -0.00106,
    -0.389, 0.389, -0.00298,
    -0.278, 0.389, -0.00203,
    -0.167, 0.389, -0.00221,
    -0.0556, 0.389, -0.00114,
    0.0556, 0.389, -0.00203,
    0.167, 0.389, -0.00149,
    0.278, 0.389, -0.00212,
    0.389, 0.389, -0.00133,
    0.5, 0.389, -0.00228,
    -0.5, 0.5, 0.00257,
    -0.389, 0.5, 0.00257,
    -0.278, 0.5, -0.000816,
    -0.167, 0.5, 0.00175,
    -0.0556, 0.5, -0.00216,
    0.0556, 0.5, -0.00193,
    0.167, 0.5, -0.00274,
    0.278, 0.5, -7.2e-05,
    0.389, 0.5, 0.00213,
    0.5, 0.5, 0.00269
];

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
    }
    else if contentType.starts(with: "application/json")
    {
        print("JSON: ")
        print(String(data: responseData, encoding: String.Encoding.utf8)!)
    }
    finishCondition.signal()
    
})

reqTask.resume()
finishCondition.wait()

requestBody.deallocate();
