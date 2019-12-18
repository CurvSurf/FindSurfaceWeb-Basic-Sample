#ifndef _FS_HEADER_H_
#define _FS_HEADER_H_

#include <stdint.h>

#pragma pack(push, 1)

#define FS_REQ_OPT_REQUEST_INOUTLIERS   0x01
#define FS_REQ_OPT_USE_DOUBLE_PRECISION 0x02

typedef struct {
	uint8_t  signature[2];
	uint8_t  version[2];
	uint32_t headerSize;
	uint32_t pointCount;
	uint32_t pointOffset;
	uint32_t pointStride;
	float accuracy;
	float meanDist;
	float touchR;
	uint32_t seedIndex;
	uint8_t reserved;
	uint8_t radExp;
	uint8_t latExt;
	uint8_t options;
} FS_REQ_HEADER;

typedef struct {
	uint8_t  signature[2];
	uint8_t  version[2];
	uint32_t headerSize;
	int32_t  resultCode;
	/* Following params are only available when resultCode > 0 */
	uint32_t dataLength;
	float    rms;
} FS_RESP_SIMPLE_HEADER;

typedef struct {
	float lowerLeft[3];
	float lowerRight[3];
	float upperRight[3];
	float upperLeft[3];
} FS_RESP_PLANE_PARAM;

typedef struct {
	float center[3];
	float r;
} FS_RESP_SPHERE_PARAM;

typedef struct {
	float bottom[3];
	float top[3];
	float r;
} FS_RESP_CYLINDER_PARAM;

typedef struct {
	float bottom[3];
	float top[3];
	float br;
	float tr;
} FS_RESP_CONE_PARAM;

typedef struct {
	float center[3];
	float normal[3];
	float mr;
	float tr;
} FS_RESP_TORUS_PARAM;

#pragma pack(pop)

#endif /* _FS_HEADER_H_ */