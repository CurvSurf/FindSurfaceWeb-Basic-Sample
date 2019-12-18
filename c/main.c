#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <curl/curl.h>

#include "fsHeader.h"

#include "pointData.h" /* const float POINTS[]; */

typedef struct {
	void*  data;
	size_t capacity;
	size_t size;
} MyBuffer;

static size_t MyResponseWrtieCallback(void *contents, size_t size, size_t nmemb, void *userp)
{
	MyBuffer *pBuffer = (MyBuffer *)userp;
	size_t realsize = size * nmemb;
	size_t bufferRemain = pBuffer->capacity - pBuffer->size;

	if( bufferRemain < realsize ) {
		void *pNewBuffer = realloc( pBuffer->data, pBuffer->size + realsize );
		if( !pNewBuffer ) {
			fprintf(stderr, "Not enough memory (realloc return NULL)\n");
			return 0;
		}
		pBuffer->data = pNewBuffer;
		pBuffer->capacity = pBuffer->size + realsize;
	}

	memcpy( (void *)(((uint8_t *)(pBuffer->data)) + pBuffer->size), contents, realsize );
	pBuffer->size += realsize;

	return realsize;
}

static int isLittleEndian() {
	const uint32_t TMP = 0x01;
	return (((const uint8_t *)&TMP)[0] == 0x01) ? 1 : 0;
}

int main(void)
{
	CURL *curl;
	CURLcode res;

	MyBuffer response = { NULL, 0, 0 };

	void *requestBody = NULL;
	long requestSize = 0;

	/* Request Body */
	requestSize = sizeof( FS_REQ_HEADER ) + sizeof( POINTS );
	requestBody = malloc( requestSize );

	/* Fill Request Body */
	if( requestBody )
	{
		FS_REQ_HEADER *pReqHeader = (FS_REQ_HEADER *)requestBody;
		void *pReqData = (void *)(pReqHeader + 1);

		pReqHeader->signature[0] = 'F';
		pReqHeader->signature[1] = 'S';
		pReqHeader->version[0]   = 1;
		pReqHeader->version[1]   = 0;
		pReqHeader->headerSize   = sizeof(FS_REQ_HEADER);
		/* Data Description */
		pReqHeader->pointCount   = (sizeof(POINTS) / sizeof(POINTS[0])) / 3;
		pReqHeader->pointOffset  = 0;
		pReqHeader->pointStride  = sizeof(float) * 3;
		/* Algorithm Parameter */
		pReqHeader->accuracy     = 0.003f;
		pReqHeader->meanDist     = 0.15f;
		pReqHeader->touchR       = 0.3f;
		pReqHeader->seedIndex    = 45;
		pReqHeader->reserved     = 0;
		pReqHeader->radExp       = 5;
		pReqHeader->latExt       = 5;
		/* Options */
		pReqHeader->options      = 0;

		/* Fill Data */
		memcpy( pReqData, POINTS, sizeof(POINTS) );
	}
	else {
		fprintf(stderr, "Not enough memroy: malloc() return NULL\n");
		return -1;
	}

	curl_global_init(CURL_GLOBAL_DEFAULT);

	/* Http Request */
	curl = curl_easy_init();
	if( curl ) 
	{
		struct curl_slist *headerList = NULL;

		/* Http Headers */
		headerList = curl_slist_append(headerList, "Content-Type: application/x-findsurface-request");
		if( !isLittleEndian() ) {
			headerList = curl_slist_append(headerList, "X-Content-Endian: big");
			headerList = curl_slist_append(headerList, "X-Accept-Endian: big");
		}

		/* Set URL */
		curl_easy_setopt(curl, CURLOPT_URL, "https://developers.curvsurf.com/FindSurface/plane");

		/* Set Request Header */
		curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headerList);

		/* Set POST Body */
		curl_easy_setopt(curl, CURLOPT_POST, 1L);
		curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, requestSize);
		curl_easy_setopt(curl, CURLOPT_POSTFIELDS, (char *)requestBody);

		/* Skip SSL verfication during testing */
		curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
		curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);

		/* Response Callback */
		curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, MyResponseWrtieCallback);
		curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&response);

		/* Request to Server */
		res = curl_easy_perform(curl);
		if(res == CURLE_OK) 
		{
			long response_code = 0;
			res = curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
			if( !res && response_code == 200 ) /* Check Response Code */
			{
				char *contentType = NULL;
				res = curl_easy_getinfo(curl, CURLINFO_CONTENT_TYPE, &contentType);

				if( !res ) /* By the "Content-Type" */
				{
					if( !strcmp( contentType, "application/x-findsurface-response" ) )
					{
						const FS_RESP_SIMPLE_HEADER *pRespHeader = (const FS_RESP_SIMPLE_HEADER *)response.data;
						if( pRespHeader->signature[0] == 'F' && pRespHeader->signature[1] == 'S' &&
							pRespHeader->version[0] == 1 && pRespHeader->version[1] == 0 ) 
						{
							switch( pRespHeader->resultCode ) 
							{
								case 1:
								{
									const FS_RESP_PLANE_PARAM *pPlane = (const FS_RESP_PLANE_PARAM *)(pRespHeader + 1);
									printf("Plane (rms: %g): \n", pRespHeader->rms);
									printf("LL: [ %g, %g, %g ]\n", pPlane->lowerLeft[0], pPlane->lowerLeft[1], pPlane->lowerLeft[2]);
									printf("LR: [ %g, %g, %g ]\n", pPlane->lowerRight[0], pPlane->lowerRight[1], pPlane->lowerRight[2]);
									printf("UR: [ %g, %g, %g ]\n", pPlane->upperRight[0], pPlane->upperRight[1], pPlane->upperRight[2]);
									printf("UL: [ %g, %g, %g ]\n", pPlane->upperLeft[0], pPlane->upperLeft[1], pPlane->upperLeft[2]);
								}
								break;

								case 2:
								{
									const FS_RESP_SPHERE_PARAM *pSphere = (const FS_RESP_SPHERE_PARAM *)(pRespHeader + 1);
									printf("Sphere (rms: %g): \n", pRespHeader->rms);
									printf("C: [ %g, %g, %g ]\n", pSphere->center[0], pSphere->center[1], pSphere->center[2]);
									printf("R: %g\n", pSphere->r);
								}
								break;

								case 3:
								{
									const FS_RESP_CYLINDER_PARAM *pCylinder = (const FS_RESP_CYLINDER_PARAM *)(pRespHeader + 1);
									printf("Cylinder (rms: %g): \n", pRespHeader->rms);
									printf("B: [ %g, %g, %g ]\n", pCylinder->bottom[0], pCylinder->bottom[1], pCylinder->bottom[2]);
									printf("T: [ %g, %g, %g ]\n", pCylinder->top[0], pCylinder->top[1], pCylinder->top[2]);
									printf("R: %g\n", pCylinder->r);
								}
								break;

								case 4:
								{
									const FS_RESP_CONE_PARAM *pCone = (const FS_RESP_CONE_PARAM *)(pRespHeader + 1);
									printf("Cone (rms: %g): \n", pRespHeader->rms);
									printf("B : [ %g, %g, %g ]\n", pCone->bottom[0], pCone->bottom[1], pCone->bottom[2]);
									printf("BR: %g\n", pCone->br);
									printf("T : [ %g, %g, %g ]\n", pCone->top[0], pCone->top[1], pCone->top[2]);
									printf("TR: %g\n", pCone->tr);
								}
								break;

								case 5:
								{
									const FS_RESP_TORUS_PARAM *pTorus = (const FS_RESP_TORUS_PARAM *)(pRespHeader + 1);
									printf("Torus (rms: %g): \n", pRespHeader->rms);
									printf("C : [ %g, %g, %g ]\n", pTorus->center[0], pTorus->center[1], pTorus->center[2]);
									printf("N : [ %g, %g, %g ]\n", pTorus->normal[0], pTorus->normal[1], pTorus->normal[2]);
									printf("MR: %g\n", pTorus->mr);
									printf("TR: %g\n", pTorus->tr);
								}
								break;

								case 0: 
									fprintf( stderr, "Not found\n" ); 
									break;

								default: 
									fprintf( stderr, "Unknown error (%d)\n", pRespHeader->resultCode ); 
									break;	
							}
						}
					}
					else if( !strncmp( contentType, "application/json", strlen("application/json") ) )
					{
						printf("JSON:\n%.*s\n", (int)(response.size), (const char *)(response.data) );
					}
					else
					{
						fprintf(stderr, "Unknonw Content Type %s\n", contentType);
					}
				}
			}
		}
		else { fprintf(stderr, "curl_easy_perform() failed: %d\n", res); }

		curl_easy_cleanup(curl);
	}
	else {
		fprintf(stderr, "Failed to create cURL context\n");
	}

	/* Release Memory */
	if(response.data) { free( response.data ); }
	if(requestBody) { free(requestBody); }

	curl_global_cleanup();

	return 0;
}
