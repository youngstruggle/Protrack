package com.nana.protrack;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;

public class WebServices {
    //Namespace of the Webservice - can be found in WSDL
    private static String NAMESPACE = "http://ws/";
    //Webservice URL - WSDL File location
    private static String URL = "http://192.168.0.18:8081/LoginWebService/LoginWebService?wsdl";//Make sure you changed IP address
    //SOAP Action URI again Namespace + Web method name
    private static String SOAP_ACTION = "http://ws/";

    public static String invokeLoginWS(String userName, String passWord, String url, String webMethName) {
        System.out.println("Protrack Username : " + userName);
        System.out.println("Protrack Password : " + passWord);
        System.out.println("Protrack URL = " + url);

        boolean loginStatus = false;
        String wsResponse = "Invalid Response";
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters
        PropertyInfo unamePI = new PropertyInfo();
        PropertyInfo passPI = new PropertyInfo();
        // Set Username
        unamePI.setName("user");
        // Set Value
        unamePI.setValue(userName);
        // Set dataType
        unamePI.setType(String.class);
        // Add the property to request object
        request.addProperty(unamePI);
        //Set Password
        passPI.setName("password");
        //Set dataType
        passPI.setValue(passWord);
        //Set dataType
        passPI.setType(String.class);
        //Add the property to request object
        request.addProperty(passPI);
        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        URL = url + "/ProtrackService?wsdl";
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            SOAP_ACTION = SOAP_ACTION + webMethName;
            // Invoke web service
            androidHttpTransport.call(SOAP_ACTION, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to  boolean variable variable
//            loginStatus = Boolean.parseBoolean(response.toString());
            wsResponse = response.toString();

        } catch (Exception e) {
            //Assign Error Status true in static variable 'errored'
            LoginActivity.errored = true;
            MainActivity.errored = true;
            e.printStackTrace();
        }
        //Return string to calling object
        System.out.println("Estim Response = " + wsResponse);
        return wsResponse;
    }

    public static String invokeInsertLocationWS(String latitude, String longitude, String webMethName) {
        String url = "https://estim.co.id:8085/protrack";

        Log.i("invokeinsert" , "------------ invokeInsertLocationWS --------");
        Log.i("Latitude ", latitude);
        Log.i("Longitude ", longitude);

        System.out.println(" ==> Protrack latitude : " + latitude);
        System.out.println(" ==> Protrack longitude : " + longitude);
        System.out.println(" ==> Protrack URL = " + url);

        boolean loginStatus = false;
        String wsResponse = "Invalid Response";

        MarshalBase64 mbase = new MarshalBase64();// marshal is used to serialize the byte array

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        PropertyInfo unamePI = new PropertyInfo();
        PropertyInfo passPI = new PropertyInfo();

        unamePI.setName("latitude");
        unamePI.setValue(latitude);
        unamePI.setType(String.class);

        request.addProperty(unamePI);
        passPI.setName("longitude");
        passPI.setValue(longitude);
        passPI.setType(String.class);
        request.addProperty(passPI);

        // Create envelope
        SoapSerializationEnvelope envelope = new
                SoapSerializationEnvelope(SoapEnvelope.VER11);

        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        envelope.encodingStyle = SoapSerializationEnvelope.ENC2001;
        envelope.dotNet = true;

        // Create HTTP call object
//        URL = url + "/ProtrackService?wsdl";
        URL = url + "/ProtrackService?wsdl";
        Log.i("URL ",URL);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        mbase.register(envelope);

        try {
            SOAP_ACTION = SOAP_ACTION + webMethName;
            // Invoke web service
            androidHttpTransport.call(SOAP_ACTION, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to  boolean variable variable
//            loginStatus = Boolean.parseBoolean(response.toString());
            wsResponse = response.toString();

        } catch (Exception e) {
            //Assign Error Status true in static variable 'errored'
//            LoginActivity.errored = true;
//            MainActivity.errored = true;
            e.printStackTrace();
        }
        //Return string to calling object
        System.out.println("Estim ZK Response = " + wsResponse);
        return wsResponse;
    }


}
