import sys
import struct

if sys.version_info.major > 2: # For Version 3.X
	import http.client as httplib
else: # For Version 2.7.X
	import httplib

# Sample Data
POINTS = [
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
] # Note: We will use Float32 (IEEE 754 binary32) for each coordinates

# Make Request Body Binary Data
SINGLE_BYTES = 4
HEADER_SIZE = 40
POINT_COUNT = int(len(POINTS) / 3)
POINT_STRIDE = SINGLE_BYTES * 3

requestBody = struct.pack( "BBBBIIIIfffIBBBB{0}f".format(len(POINTS)), 
	0x46,   # 'F'
	0x53,   # 'S'
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

# Request to Server
headers = { "Content-Type": "application/x-findsurface-request" }
if sys.byteorder == 'big':
	headers["X-Content-Endian"] = "big"
	headers["X-Accept-Endian"] = "big"
# "Content-Length" header is added automatically for all methods if the length of the body can be determined

# Request URL is https://developers.curvsurf.com/FindSurface/plane
conn = httplib.HTTPSConnection("developers.curvsurf.com")
conn.request("POST", "/FindSurface/plane", requestBody, headers)

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


	elif contentType.startswith("application/json"):
		print( responseBody )
	
	


