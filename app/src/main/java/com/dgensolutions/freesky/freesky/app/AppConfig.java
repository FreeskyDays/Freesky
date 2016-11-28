package com.dgensolutions.freesky.freesky.app;

/**
 * Created by Ganesh Kaple on 13-10-2016.
 */

public class AppConfig {
    // Server user login url
    public static String URL_LOGIN = "http://192.168.43.52/freesky/login.php";

    // Server user register url
    public static String URL_REGISTER = "http://192.168.43.52/freesky/register.php";

    // Server user register url
    public static String URL_VERIFY_OTP = "http://192.168.43.52/freesky/verify_otp.php";

    public static final boolean DEBUG = Boolean.parseBoolean("true");


    // server URL configuration
    //public static final String URL_REQUEST_SMS = "http://192.168.43.52/freesky/request_sms.php";
    //public static final String URL_VERIFY_OTP = "http://192.168.43.52/freesky/verify_otp.php";

    // SMS provider identification
    // It should match with your SMS gateway origin
    // You can use  MSGIND, TESTER and ALERTS as sender ID
    // If you want custom sender Id, approve MSG91 to get one
    public static final String SMS_ORIGIN = "FREESKY";

    // special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";
}
