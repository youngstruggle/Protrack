package com.nana.protrack;

import java.io.*;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.transport.ServiceConnection;
import org.ksoap2.transport.Transport;
import org.xmlpull.v1.*;

public class HttpTransportSE extends Transport {
    public HttpTransportSE(String url) {
        super(url);
    }
    public void call(String soapAction, SoapEnvelope envelope) throws IOException, XmlPullParserException {
        if (soapAction == null)
            soapAction = "\"\"";
        byte[] requestData = createRequestData(envelope);
        requestDump = debug ? new String(requestData) : null;
        responseDump = null;
        ServiceConnection connection = getServiceConnection();
        connection.setRequestProperty("User-Agent", "kSOAP/2.0");
        connection.setRequestProperty("SOAPAction", soapAction);
        connection.setRequestProperty("Content-Type", "text/xml");
        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Content-Length", "" + requestData.length);
        connection.setRequestMethod("POST");
        connection.connect();
        OutputStream os = connection.openOutputStream();
        os.write(requestData, 0, requestData.length);
        os.flush();
        os.close();
        requestData = null;
        InputStream is;
        try {
            connection.connect();
            is = connection.openInputStream();
        } catch (IOException e) {
            is = connection.getErrorStream();
            if (is == null) {
                connection.disconnect();
                throw (e);
            }
        }
        if (debug) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            while (true) {
                int rd = is.read(buf, 0, 256);
                if (rd == -1)
                    break;
                bos.write(buf, 0, rd);
            }
            bos.flush();
            buf = bos.toByteArray();
            responseDump = new String(buf);
            is.close();
            is = new ByteArrayInputStream(buf);
        }
        parseResponse(envelope, is);
    }

    protected ServiceConnection getServiceConnection() throws IOException {
        return new ServiceConnectionSE(url);
    }

}
