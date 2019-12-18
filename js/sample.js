// ES6+
function requestFindSurface(buffer) {
	if( buffer instanceof ArrayBuffer ) {
		let xhr = new XMLHttpRequest();
		xhr.onload = function(evt) {
			if( this.getResponseHeader("Content-Type") == "application/x-findsurface-response" ) {
				parseResponse(this.response);
			}
		};
		xhr.open("POST", "https://developers.curvsurf.com/FindSurface/plane");
		xhr.setRequestHeader("Content-Type", "application/x-findsurface-request");
		// "Content-Length " will be automatically attached by XMLHttpRequest
		xhr.responseType = "arraybuffer";
		xhr.send(buffer);
	}
}

function requestFindSurfaceJSON(buffer) {
	if( buffer instanceof ArrayBuffer ) {
		let xhr = new XMLHttpRequest();
		xhr.onload = function(evt) {
			console.log( this.response );
		};
		xhr.open("POST", "https://developers.curvsurf.com/FindSurface/plane.json");
		xhr.setRequestHeader("Content-Type", "application/x-findsurface-request");
		// "Content-Length " will be automatically attached by XMLHttpRequest
		xhr.responseType = "json";
		xhr.send(buffer);
	}
}

function parseResponse(buffer) {
	if( buffer instanceof ArrayBuffer )
	{
		let resp = new DataView(buffer);

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
		
		console.log( "Headr Size: " + resp.getUint32(0x04, true) );
		let resultCode = resp.getInt32( 0x08, true );
		console.log( "Result Code: " + resultCode );
		if( resultCode < 1 ) {
			if(resultCode == 0) console.log("Not Found!");
			return;
		}

		console.log( "Data Length: " + resp.getUint32(0x0C, true) );
		console.log( "RMS: " + resp.getFloat32(0x10, true) );

		switch(resultCode)
		{
			case 1: // Plane
				console.log("Find Plane: ");
				console.log (
					{
						ll: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
						lr: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
						ur: { x: resp.getFloat32(44, true), y: resp.getFloat32(48, true), z: resp.getFloat32(52, true) },
						ul: { x: resp.getFloat32(56, true), y: resp.getFloat32(60, true), z: resp.getFloat32(64, true) }
					}
				);
				break;

			case 2: // Sphere
				console.log("Find Sphere: ");
				console.log (
					{
						c: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
						r: resp.getFloat32(32, true)
					}
				);
				break;

			case 3: // Cylinder
				console.log("Find Cylinder: ");
				console.log (
					{
						b: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
						t: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
						r: resp.getFloat32(44, true)
					}
				);
			break;

			case 4: // Cone
				console.log("Find Cone: ");
				console.log (
					{
						b: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
						t: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
						br: resp.getFloat32(44, true),
						tr: resp.getFloat32(48, true)
					}
				);
			break;

			case 5: // Torus
				console.log("Find Torus: ");
				console.log (
					{
						c: { x: resp.getFloat32(20, true), y: resp.getFloat32(24, true), z: resp.getFloat32(28, true) },
						n: { x: resp.getFloat32(32, true), y: resp.getFloat32(36, true), z: resp.getFloat32(40, true) },
						mr: resp.getFloat32(44, true),
						tr: resp.getFloat32(48, true)
					}
				);
			break;

			default: // Never Reach Here
			break;
		}
	}
}

function isMachineLittleEndian() { 
	return new Uint8Array((new Uint16Array([0x01])).buffer)[0] === 0x01;
}

function makeRequestBuffer()
{
	let headerSize = 40;
	let pointDataLength = POINTS.length * Float32Array.BYTES_PER_ELEMENT;
	let pointCount = Math.floor( POINTS.length / 3 );

	let requestBody = new ArrayBuffer( headerSize + pointDataLength );
	
	let headerPart = new DataView( requestBody, 0, headerSize );
	let dataPart   = new Float32Array( requestBody, headerSize );

	// Fill Header
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

	dataPart.set( POINTS );

	return requestBody;
}

const POINTS = [
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
