# Request FindSurface via Python

## Sample Data 

This sample uses the following form of an array for input data:

```python
POINTS = [ 
	# Sample Data
]
```

See [here](../README.md#sample-data) for the actual content of the sample data.

## How to: make a Request body

```python
import struct
# Make Request Body Binary Data

SINGLE_BYTES = 4
HEADER_SIZE = 40
POINT_COUNT = int(len(POINTS) / 3)
POINT_STRIDE = SINGLE_BYTES * 3

requestBody = struct.pack( "BBBBIIIIfffIBBBB{0}f".format(len(POINTS)), 
	0x46,   # ord('F')
	0x53,   # ord('S')
	1,      # major - 1
	0,      # minor - 0
	HEADER_SIZE,  
	POINT_COUNT,
	0,     # offset
	POINT_STRIDE,
	0.003, # Measurement Accuracy
	0.15,  # Mean Distance
	0.3,   # Touch Radius
	45,    # Seed Index
	0,     # Not Used
	5,     # Rad. Exp.
	5,     # Lat. Ext.
	0,     # Options
	*POINTS
)
```

## How to: Request FindSurface

```python
import sys
if sys.version_info.major > 2: # For Version 3.X
	import http.client as httplib
else: # For Version 2.7.X
	import httplib

# Request to Server
headers = { "Content-Type": "application/x-findsurface-request" }
if sys.byteorder == 'big':
	headers["X-Content-Endian"] = "big"
	headers["X-Accept-Endian"] = "big"
# "Content-Length" header is added automatically for all methods if the length of the body can be determined

# Request URL is https://developers.curvsurf.com/FindSurface/plane
conn = httplib.HTTPSConnection("developers.curvsurf.com")
conn.request("POST", "/FindSurface/plane", requestBody, headers)
```

## How to: Parse Response

```python
resp = conn.getresponse()

if resp.status == 200:
	responseBody = resp.read()
	contentType = resp.getheader("Content-Type")

	if contentType == "application/x-findsurface-response":
		sig_ver = struct.unpack_from('BBBB', responseBody, 0)
		if sig_ver[0] == 0x46 and sig_ver[1] == 0x53 and sig_ver[2] == 1 and sig_ver[3] == 0:
			result = struct.unpack_from('i', responseBody, 8)[0]
			if result == 1: # plane
				rms = struct.unpack_from("f", responseBody, 16)[0]
				param = struct.unpack_from("12f", responseBody, 20)

				print( "Plane (rms: {0:g})".format(rms) )
				print( "LL: [ {0:g}, {1:g}, {2:g} ]".format(param[0], param[1], param[2]) )
				print( "LR: [ {0:g}, {1:g}, {2:g} ]".format(param[3], param[4], param[5]) )
				print( "UR: [ {0:g}, {1:g}, {2:g} ]".format(param[6], param[7], param[8]) )
				print( "UL: [ {0:g}, {1:g}, {2:g} ]".format(param[9], param[10], param[11]) )

			elif result == 2: # sphere
				rms = struct.unpack_from("f", responseBody, 16)[0]
				param = struct.unpack_from("4f", responseBody, 20)

				print( "Sphere (rms: {0:g})".format(rms) )
				print( "C : [ {0:g}, {1:g}, {2:g} ]".format(param[0], param[1], param[2]) )
				print( "R : {0:g}".format(param[3]) )

			elif result == 3: # cylinder
				rms = struct.unpack_from("f", responseBody, 16)[0]
				param = struct.unpack_from("7f", responseBody, 20)

				print( "Cylinder (rms: {0})".format(rms) )
				print( "B : [ {0}, {1}, {2} ]".format(param[0], param[1], param[2]) )
				print( "T : [ {0}, {1}, {2} ]".format(param[3], param[4], param[5]) )
				print( "R : {0}".format(param[6]) )

			elif result == 4: # cone
				rms = struct.unpack_from("f", responseBody, 16)[0]
				param = struct.unpack_from("8f", responseBody, 20)

				print( "Cone (rms: {0:g})".format(rms) )
				print( "B : [ {0:g}, {1:g}, {2:g} ]".format(param[0], param[1], param[2]) )
				print( "T : [ {0:g}, {1:g}, {2:g} ]".format(param[3], param[4], param[5]) )
				print( "BR: {0:g}".format(param[6]) )
				print( "TR: {0:g}".format(param[7]) )

			elif result == 5: # torus
				rms = struct.unpack_from("f", responseBody, 16)[0]
				param = struct.unpack_from("8f", responseBody, 20)

				print( "Torus (rms: {0:g})".format(rms) )
				print( "C : [ {0:g}, {1:g}, {2:g} ]".format(param[0], param[1], param[2]) )
				print( "N : [ {0:g}, {1:g}, {2:g} ]".format(param[3], param[4], param[5]) )
				print( "MR: {0:g}".format(param[6]) )
				print( "TR: {0:g}".format(param[7]) )

			elif result == 0: # Not Found
				print( "Not Found" )
			else:
				print( "Unknown Error: {0}".format(result) )
```
