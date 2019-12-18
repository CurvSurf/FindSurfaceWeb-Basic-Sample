using System;
using System.IO;
using System.Net;
using System.Text;

namespace CurvSurf.FindSurface.Example
{
	public class RequestSample
	{
		public static readonly UInt32 REQ_HEADER_SIZE = 40;

        static void Main(string[] args)
        {
            byte[] requestBody = new byte[REQ_HEADER_SIZE + (sizeof(float) * POINTS.Length)];

            // Fill Header Part
            using (MemoryStream ms = new MemoryStream(requestBody))
            {
                using (BinaryWriter bw = new BinaryWriter(ms))
                {
                    bw.Write((byte)0x46); // 'F'
                    bw.Write((byte)0x53); // 'S'
                    bw.Write((byte)1);    // major - 1
                    bw.Write((byte)0);    // minor - 0
                    bw.Write(REQ_HEADER_SIZE);

                    bw.Write((UInt32)(POINTS.Length / 3)); // Point Count
                    bw.Write((UInt32)0);                   // Point Offset
                    bw.Write((UInt32)(sizeof(float) * 3)); // Point Stride

                    bw.Write(0.003f);     // Measurement Accuracy
                    bw.Write(0.15f);      // Mean Distance
                    bw.Write(0.3f);       // Touch Radius
                    bw.Write((UInt32)45); // Seed Index
                    bw.Write((byte)0);    // Not Used
                    bw.Write((byte)5);    // Rad. Exp.
                    bw.Write((byte)5);    // Lat. Ext.
                    bw.Write((byte)0);    // Options

                    bw.Close();
                }
                ms.Close();
            }

            // Fill Data Part
            Buffer.BlockCopy(POINTS, 0, requestBody, (int)REQ_HEADER_SIZE, (sizeof(float) * POINTS.Length));

            // Request FindSurface
            WebRequest req = WebRequest.Create("https://developers.curvsurf.com/FindSurface/plane");
            req.Credentials = CredentialCache.DefaultCredentials;

            req.Method = "POST";
            req.ContentType = "application/x-findsurface-request";
            req.ContentLength = requestBody.Length;
            if (!BitConverter.IsLittleEndian) {
                req.Headers.Add("X-Content-Endian", "big");
                req.Headers.Add("X-Accept-Endian", "big");
            }

            using (Stream reqWriter = req.GetRequestStream())
            {
                reqWriter.Write(requestBody, 0, requestBody.Length);
                reqWriter.Close();
            }

            // Check Response
            HttpWebResponse resp = (HttpWebResponse)req.GetResponse();
            if( resp.StatusCode == HttpStatusCode.OK )
            {
                byte[] responseBody = new byte[resp.ContentLength];
                using (Stream respReader = resp.GetResponseStream())
                {
                    respReader.Read(responseBody, 0, responseBody.Length);
                    respReader.Close();
                }

                if( resp.ContentType.Equals("application/x-findsurface-response") )
                {
                    if( responseBody[0] == 0x46 && 
                        responseBody[1] == 0x53 && 
                        responseBody[2] == 1 && 
                        responseBody[3] == 0 )
                    {
                        int resultCode = BitConverter.ToInt32(responseBody, 0x08);
                        switch( resultCode )
                        {
                            case 1:
                            {
                                Console.WriteLine("Plane (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
                                Console.WriteLine("LL: [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 20), 
                                    BitConverter.ToSingle(responseBody, 24), 
                                    BitConverter.ToSingle(responseBody, 28));
                                Console.WriteLine("LR: [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 32),
                                    BitConverter.ToSingle(responseBody, 36),
                                    BitConverter.ToSingle(responseBody, 40));
                                Console.WriteLine("UR: [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 44),
                                    BitConverter.ToSingle(responseBody, 48),
                                    BitConverter.ToSingle(responseBody, 52));
                                Console.WriteLine("UL: [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 56),
                                    BitConverter.ToSingle(responseBody, 60),
                                    BitConverter.ToSingle(responseBody, 64));
                            }
                            break;

                            case 2:
                            {
                                Console.WriteLine("Sphere (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
                                Console.WriteLine("C : [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 20),
                                    BitConverter.ToSingle(responseBody, 24),
                                    BitConverter.ToSingle(responseBody, 28));
                                Console.WriteLine("R : {0:G}", BitConverter.ToSingle(responseBody, 32));
                            }
                            break;

                            case 3:
                            {
                                Console.WriteLine("Cylinder (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
                                Console.WriteLine("B : [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 20),
                                    BitConverter.ToSingle(responseBody, 24),
                                    BitConverter.ToSingle(responseBody, 28));
                                Console.WriteLine("T : [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 32),
                                    BitConverter.ToSingle(responseBody, 36),
                                    BitConverter.ToSingle(responseBody, 40));
                                Console.WriteLine("R : {0:G}", BitConverter.ToSingle(responseBody, 44));
                            }
                            break;

                            case 4:
                            {
                                Console.WriteLine("Cone (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
                                Console.WriteLine("B : [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 20),
                                    BitConverter.ToSingle(responseBody, 24),
                                    BitConverter.ToSingle(responseBody, 28));
                                Console.WriteLine("T : [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 32),
                                    BitConverter.ToSingle(responseBody, 36),
                                    BitConverter.ToSingle(responseBody, 40));
                                Console.WriteLine("BR: {0:G}", BitConverter.ToSingle(responseBody, 44));
                                Console.WriteLine("TR: {0:G}", BitConverter.ToSingle(responseBody, 48));
                            }
                            break;

                            case 5:
                            {
                                Console.WriteLine("Torus (rms: {0:G})", BitConverter.ToSingle(responseBody, 0x10));
                                Console.WriteLine("C : [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 20),
                                    BitConverter.ToSingle(responseBody, 24),
                                    BitConverter.ToSingle(responseBody, 28));
                                Console.WriteLine("N : [ {0:G}, {1:G}, {2:G} ]",
                                    BitConverter.ToSingle(responseBody, 32),
                                    BitConverter.ToSingle(responseBody, 36),
                                    BitConverter.ToSingle(responseBody, 40));
                                Console.WriteLine("MR: {0:G}", BitConverter.ToSingle(responseBody, 44));
                                Console.WriteLine("TR: {0:G}", BitConverter.ToSingle(responseBody, 48));
                            }
                            break;

                            case 0:
                                Console.WriteLine("Not Found");
                                break;

                            default:
                                Console.WriteLine("Unknown Error: " + resultCode);
                                break;
                        }
                    }
                }
                else if( resp.ContentType.StartsWith("application/json") )
                {
                    Console.WriteLine(Encoding.UTF8.GetString(responseBody));
                }
            }
            resp.Close();
        }

        public static readonly float[] POINTS = {
            -0.5f, -0.5f, -0.0022f,
            -0.389f, -0.5f, -0.00096f,
            -0.278f, -0.5f, 0.0019f,
            -0.167f, -0.5f, 0.00258f,
            -0.0556f, -0.5f, 0.000432f,
            0.0556f, -0.5f, -0.00125f,
            0.167f, -0.5f, 0.00272f,
            0.278f, -0.5f, -0.0017f,
            0.389f, -0.5f, 0.000504f,
            0.5f, -0.5f, 0.00286f,
            -0.5f, -0.389f, -0.00242f,
            -0.389f, -0.389f, -0.00221f,
            -0.278f, -0.389f, 0.00162f,
            -0.167f, -0.389f, -0.000678f,
            -0.0556f, -0.389f, -0.00221f,
            0.0556f, -0.389f, 0.000648f,
            0.167f, -0.389f, -0.00248f,
            0.278f, -0.389f, -0.00057f,
            0.389f, -0.389f, 0.000732f,
            0.5f, -0.389f, 0.000948f,
            -0.5f, -0.278f, 0.0016f,
            -0.389f, -0.278f, 0.00181f,
            -0.278f, -0.278f, 0.000936f,
            -0.167f, -0.278f, -0.00269f,
            -0.0556f, -0.278f, 0.00072f,
            0.0556f, -0.278f, 0.00204f,
            0.167f, -0.278f, -0.0023f,
            0.278f, -0.278f, -0.00264f,
            0.389f, -0.278f, -0.00146f,
            0.5f, -0.278f, 0.00206f,
            -0.5f, -0.167f, -0.000756f,
            -0.389f, -0.167f, -0.00252f,
            -0.278f, -0.167f, 0.00224f,
            -0.167f, -0.167f, 0.00229f,
            -0.0556f, -0.167f, 0.0012f,
            0.0556f, -0.167f, -0.00219f,
            0.167f, -0.167f, -0.00197f,
            0.278f, -0.167f, 0.000912f,
            0.389f, -0.167f, -0.000888f,
            0.5f, -0.167f, -0.000324f,
            -0.5f, -0.0556f, 0.000768f,
            -0.389f, -0.0556f, -0.00217f,
            -0.278f, -0.0556f, 0.000468f,
            -0.167f, -0.0556f, -0.000612f,
            -0.0556f, -0.0556f, 0.00015f,
            0.0556f, -0.0556f, -0.000606f,
            0.167f, -0.0556f, 0.00118f,
            0.278f, -0.0556f, -0.00119f,
            0.389f, -0.0556f, 0.00182f,
            0.5f, -0.0556f, -0.0011f,
            -0.5f, 0.0556f, 0.00276f,
            -0.389f, 0.0556f, 0.000414f,
            -0.278f, 0.0556f, 0.00185f,
            -0.167f, 0.0556f, -0.00116f,
            -0.0556f, 0.0556f, -0.00114f,
            0.0556f, 0.0556f, -0.000426f,
            0.167f, 0.0556f, 0.00202f,
            0.278f, 0.0556f, -0.0023f,
            0.389f, 0.0556f, -0.00193f,
            0.5f, 0.0556f, -0.00245f,
            -0.5f, 0.167f, 0.00276f,
            -0.389f, 0.167f, -0.00154f,
            -0.278f, 0.167f, 0.00218f,
            -0.167f, 0.167f, 0.000138f,
            -0.0556f, 0.167f, -0.00226f,
            0.0556f, 0.167f, 0.000372f,
            0.167f, 0.167f, 0.000948f,
            0.278f, 0.167f, 0.00292f,
            0.389f, 0.167f, 0.00243f,
            0.5f, 0.167f, 0.0012f,
            -0.5f, 0.278f, -0.00227f,
            -0.389f, 0.278f, 0.000192f,
            -0.278f, 0.278f, 0.00203f,
            -0.167f, 0.278f, 0.0012f,
            -0.0556f, 0.278f, 0.00072f,
            0.0556f, 0.278f, -0.00268f,
            0.167f, 0.278f, -0.00241f,
            0.278f, 0.278f, -0.0011f,
            0.389f, 0.278f, -0.00087f,
            0.5f, 0.278f, 0.000558f,
            -0.5f, 0.389f, -0.00106f,
            -0.389f, 0.389f, -0.00298f,
            -0.278f, 0.389f, -0.00203f,
            -0.167f, 0.389f, -0.00221f,
            -0.0556f, 0.389f, -0.00114f,
            0.0556f, 0.389f, -0.00203f,
            0.167f, 0.389f, -0.00149f,
            0.278f, 0.389f, -0.00212f,
            0.389f, 0.389f, -0.00133f,
            0.5f, 0.389f, -0.00228f,
            -0.5f, 0.5f, 0.00257f,
            -0.389f, 0.5f, 0.00257f,
            -0.278f, 0.5f, -0.000816f,
            -0.167f, 0.5f, 0.00175f,
            -0.0556f, 0.5f, -0.00216f,
            0.0556f, 0.5f, -0.00193f,
            0.167f, 0.5f, -0.00274f,
            0.278f, 0.5f, -7.2e-05f,
            0.389f, 0.5f, 0.00213f,
            0.5f, 0.5f, 0.00269f
        };
	}
}