#import <Foundation/Foundation.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "fsHeader.h"
#include "pointData.h" // const float POINTS[];

static BOOL isLittleEndian() {
    const uint32_t TMP = 0x01;
    return (((const uint8_t *)&TMP)[0] == 0x01) ? YES : NO;
}

int main(void)
{
    long requestSize = sizeof(FS_REQ_HEADER) + sizeof(POINTS);
    void *requestBody = malloc(requestSize);
    
    // Fill Request Body
    if( requestBody )
    {
        //> Fill Header Part
        FS_REQ_HEADER *pReqHeader = (FS_REQ_HEADER *)requestBody;
        void *pReqData = (void *)(pReqHeader + 1);

        pReqHeader->signature[0] = 'F';
        pReqHeader->signature[1] = 'S';
        pReqHeader->version[0]   = 1;
        pReqHeader->version[1]   = 0;
        pReqHeader->headerSize   = sizeof(FS_REQ_HEADER);
        // Data Description
        pReqHeader->pointCount   = (sizeof(POINTS) / sizeof(POINTS[0])) / 3;
        pReqHeader->pointOffset  = 0;
        pReqHeader->pointStride  = sizeof(float) * 3;
        // Algorithm Parameter
        pReqHeader->accuracy     = 0.003f;
        pReqHeader->meanDist     = 0.15f;
        pReqHeader->touchR       = 0.3f;
        pReqHeader->seedIndex    = 45;
        pReqHeader->reserved     = 0;
        pReqHeader->radExp       = 5;
        pReqHeader->latExt       = 5;
        // Options
        pReqHeader->options      = 0;

        //> Fill Data Part
        memcpy( pReqData, POINTS, sizeof(POINTS) );
    }
    else {
        fprintf(stderr, "Not enough memroy: malloc() return NULL\n");
        return -1;
    }
    
    @autoreleasepool {
        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL: [NSURL URLWithString: @"https://developers.curvsurf.com/FindSurface/plane"]];
        [request setHTTPMethod: @"POST"];
        [request setValue: @"application/x-findsurface-request" forHTTPHeaderField: @"Content-Type"];
        // "Content-Length " will be automatically attached by NSMutableURLRequest.
        //[request setValue: [NSString stringWithFormat: @"%ld", requestSize] forHTTPHeaderField: @"Content-Length"];
        if( !isLittleEndian() ) {
            [request setValue: @"big" forHTTPHeaderField: @"X-Content-Endian"];
            [request setValue: @"big" forHTTPHeaderField: @"X-Accept-Endian"];
        }
        [request setHTTPBody: [NSData dataWithBytesNoCopy: requestBody length:(NSUInteger)requestSize freeWhenDone: NO]];
        
        NSCondition *finishCondition = [[NSCondition alloc] init];
        NSURLSessionDataTask * reqTask = [[NSURLSession sharedSession] dataTaskWithRequest: request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            NSHTTPURLResponse *resp = (NSHTTPURLResponse *)response;
            if(data == nil || resp == nil || error != nil || resp.statusCode != 200) {
                [finishCondition signal];
                return;
            }
            NSString *contentType = [resp.allHeaderFields objectForKey:@"Content-Type"];
            
            if([contentType isEqualToString:@"application/x-findsurface-response"])
            {
                const FS_RESP_SIMPLE_HEADER *pRespHeader = (const FS_RESP_SIMPLE_HEADER *)[data bytes];
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
            else if( [contentType hasPrefix:@"application/json"])
            {
                NSLog( @"%@", [[NSString alloc] initWithData: data encoding: NSUTF8StringEncoding] );
            }
            
            [finishCondition signal];
        }];
        [reqTask resume];
        [finishCondition wait];
    }
    
    free(requestBody);
    return 0;
}
