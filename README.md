# FindSurface Web API Samples

**Curv*Surf* FindSurface™ Web API** Samples - BasicSample

## Overview

This sample source codes present a simple usage of FindSurface Web API in different languages.

Using FindSurface Web API, a client manipulates Request and Response to send a request to our server and to receive a result of the request. See the links below for more details of Request and Response descriptions.

- [Request](https://developers.curvsurf.com/Documentation/?PAGE=Web%2Findex.html%3FPAGE%3Drequest.html)
- [Response (Binary)](https://developers.curvsurf.com/Documentation/?PAGE=Web%2Findex.html%3FPAGE%3Dresponse_binary.html)
- [Response (Json)](https://developers.curvsurf.com/Documentation/?PAGE=Web%2Findex.html%3FPAGE%3Dresponse_json.html)

The sample demo shows how to find a plane from a point cloud using FindSurface Web API and focuses on explaining the following steps:

1. Configuring a header and body of Request.
1. Interpreting result data from Response (Binary).

We provide samples for several languages as follows:

- [C (with libcurl)](c/README.md)
- [C#](cs/README.md)
- [Java](java/README.md)
- [Javascript (ES6+)](js/README.md)
- [Kotlin (with JVM)](kotlin_jvm/README.md)
- [Objective C](objc/README.md)
- [Swift](swift/README.md)
- [Python](python/README.md)

In these samples, they use a hard-coded data array for input point cloud data in order to keep themselves as simple as possible (without any boiler-plate code for File I/O).

The data array represents 100 points forming a rectangle on the XY-plane, having an error of 0.003 in the direction of the Z-axis. Each value of points must be represented as IEEE floating point representation and in these samples, the points use 32-bit single precision values without other attributes (e.g., colors).

### Sample Data
```c
// 100 Points (X, Y, Z) for each line
[
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
]
```

## CONTACT

Send an email to support@curvsurf.com to contact our support team.
