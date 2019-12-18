# Request FindSurface via JavaScript

> **NOTE:** This sample uses ES6+ and Little Endian.

## Sample Data 

This sample uses the following form of an array for input data:

```js
const POINTS = [ /* Sample Data */ ];
```

See [here](../README.md#sample-data) for the actual content of the sample data.

## How to: make a Request body

```js
let headerSize = 40;
let pointDataLength = POINTS.length * Float32Array.BYTES_PER_ELEMENT;
let pointCount = Math.floor( POINTS.length / 3 ); // 100

let requestBody = new ArrayBuffer( headerSize + pointDataLength );

// Fill Header
let headerPart = new DataView( requestBody, 0, headerSize );

headerPart.setUint8  ( 0x00, 0x46 ); // 'F'
headerPart.setUint8  ( 0x01, 0x53 ); // 'S'
headerPart.setUint8  ( 0x02, 0x01 ); // major - 1
headerPart.setUint8  ( 0x03, 0x00 ); // minor - 0
headerPart.setUint32 ( 0x04, headerSize, true );
headerPart.setUint32 ( 0x08, pointCount, true );
headerPart.setUint32 ( 0x0C, 0, true );     // offset
headerPart.setUint32 ( 0x10, 3 * Float32Array.BYTES_PER_ELEMENT, true ); // stride
headerPart.setFloat32( 0x14, 0.003, true ); // Measurement Accuracy
headerPart.setFloat32( 0x18, 0.15, true );  // Mean Distance
headerPart.setFloat32( 0x1C, 0.3, true );   // Touch Radius
headerPart.setUint32 ( 0x20, 45, true );    // Seed Index
headerPart.setUint8  ( 0x24, 0 );           // Not used
headerPart.setUint8  ( 0x25, 5 );           // Rad. Exp.
headerPart.setUint8  ( 0x26, 5 );           // Lat. Ext.
headerPart.setUint8  ( 0x27, 0 );           // Options

// Fill Data
let dataPart   = new Float32Array( requestBody, headerSize );

dataPart.set( POINTS );
```

## How to: Request FindSurface

### 1. Request *Binary Result Format*

```js
// Request with XMLHttpRequest
let xhr = new XMLHttpRequest();

xhr.onload = function(evt) {
	if( this.getResponseHeader("Content-Type") == "application/x-findsurface-response" ) {
		let responseBody = this.response; // instanceof ArrayBuffer
		// Do something with `responseBody`
	}
};

xhr.open("POST", "https://developers.curvsurf.com/FindSurface/plane");

xhr.setRequestHeader("Content-Type", "application/x-findsurface-request");
// "Content-Length " will be automatically attached by XMLHttpRequest
xhr.responseType = "arraybuffer";

xhr.send(requestBody);
```

### 2. Request *JSON Result Format*

```js
// Request with XMLHttpRequest
let xhr = new XMLHttpRequest();

xhr.onload = function(evt) {
	let responseJSON = this.response;
	// Do something with `responseJSON`
};

xhr.open("POST", "https://developers.curvsurf.com/FindSurface/plane.json");

xhr.setRequestHeader("Content-Type", "application/x-findsurface-request");
// "Content-Length " will be automatically attached by XMLHttpRequest
xhr.responseType = "json";

xhr.send(requestBody);
```

## How to: Parse Response ( *Binary Result Format* )

```js
let resp = new DataView(responseBody);

// Signiture & Version Check
if( resp.getUint16(0, false) != 0x4653 ) // magic word check
{
	console.log("Invalid Response Format");
	return;
}
else if( resp.getUint16(2, false) != 0x0100 ) // version check
{
	console.log("Invalid Response Version");
	return;
}

let resultCode = resp.getInt32( 0x08, true );
if( resultCode < 1 ) {
	if(resultCode == 0) { console.log("Not Found!"); }
	else { console.log("Unknown Error!"); }
	return;
}

let myResult = null;
switch(resultCode)
{
	case 1: // Plane
		console.log("Find Plane: ");
		myResult = {
			ll: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
			lr: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
			ur: { x: resp.getFloat32(44, true), y: resp.getFloat32(48, true), z: resp.getFloat32(52, true) },
			ul: { x: resp.getFloat32(56, true), y: resp.getFloat32(60, true), z: resp.getFloat32(64, true) }
		};
		break;

	case 2: // Sphere
		console.log("Find Sphere: ");
		myResult = {
			c: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
			r: resp.getFloat32(32, true)
		};
		break;

	case 3: // Cylinder
		console.log("Find Cylinder: ");
		myResult = {
			b: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
			t: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
			r: resp.getFloat32(44, true)
		};
	break;

	case 4: // Cone
		console.log("Find Cone: ");
		myResult = {
			b: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
			t: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
			br: resp.getFloat32(44, true),
			tr: resp.getFloat32(48, true)
		};
	break;

	case 5: // Torus
		console.log("Find Torus: ");
		myResult = {
			c: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
			n: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
			mr: resp.getFloat32(44, true),
			tr: resp.getFloat32(48, true)
		};
	break;

	default: // Never Should Reach Here
	break;
}
```
