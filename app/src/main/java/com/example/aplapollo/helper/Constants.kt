package com.example.aplapollo.helper

object Constants {

    val LONGITUDE: String = "longitude"
    val LATITUDE: String = "latitude"
    val ModelCode: String = "modelcode"
    val ColorCode: String = "colorcode"
    val VinNo: String = "vinno"
    const val KEY_USER_ID = "id"
    const val LOGGEDIN = "loggedIn"
    const val IS_ADMIN = "isAdmin"
    const val USERNAME = "username"
    const val TOKEN = "token"
    const val USER_COORDINATES = "coordinates"
    const val NO_INTERNET = "No Internet Connection"
    const val NETWORK_FAILURE = "Network Failure"
    const val CONFIG_ERROR = "Please configure network details"
    const val INCOMPLETE_DETAILS = "Please fill the required details"
    const val EXCEPTION_ERROR = "No Data Found"
    const val HTTP_ERROR_MESSAGE = "message"
    const val HTTP_HEADER_AUTHORIZATION = "Authorization"
    const val SHARED_PREF = "mahindra_yard_shared_pref"
    const val SERVER_IP = "server_ip"
    const val KEY_ISLOGGEDIN = "isLoggedIn"
    const val KEY_PRINTER_MAC = "KEY_PRINTER_MAC"
    const val KEY_PRINTER_CONNECTED="KEY_PRINTER_CONNECTED"


    const val TGS_BASE = "Tgs/api/"
    const val SERVICE_BASE = "Service/api/"
    const val API_BASE = "api/"


    //const val KEY_USERID = Constants.USER_ID
    const val KEY_IS_ADMIN = "isAdmin"
    const val ISFIRSTTIME = "is_first_time"
    const val SESSION_EXPIRE = "Session Expired ! Please relogin"
    const val KEY_USER_NAME = "userName"
    const val KEY_JWT_TOKEN = "jwtToken"
    const val KEY_IS_LOGGED_IN = "loggedIn"
    const val KEY_Refresh_Token = "refreshToken"
    const val KEY_COORDINATE = "location_coordinates"
    const val SERVER_IP_SHARED = "192.168.1.105"
    private const val PRINTER_MAC = "AB:3F:A4:CF:89:AD"
    const val SESSION_EXPIRED = "SESSION_EXPIRED"

    const val KEY_HTTP = "http"
    const val KEY_SERVER_IP = "serverIp"
    const val GET = 1
    const val POST = 2
    const val HTTP_OK = 200
    const val HTTP_CREATED = 201
    const val HTTP_EXCEPTION = 202
    const val HTTP_UPDATED = 204
    const val HTTP_FOUND = 302
    const val HTTP_NOT_FOUND = 404
    const val HTTP_CONFLICT = 409
    const val HTTP_INTERNAL_SERVER_ERROR = 500
    const val HTTP_ERROR = 400

    const val REFRESH_TOKEN_DATA = "AuthService/refreshtoken"
    const val DASHBOARD_DATA = "VehicleMilestone/GetVehicleMilestoneDashBoardData"
    const val GET_APP_DETAILS = "MobileApp/GetLatestApkVersion"
    const val DASHBOARD_GRAPH_DATA = "VehicleMilestone/GetDriverwiseVehicleParkedCount"
    const val ADD_LOCATIONS = "LocationMapping/AddLocations"
    const val ADD_DEALERLOCATIONS = "Dealer/AddDealer"
    const val GET_DEALERLOCATIONS = "Dealer/GetAllDealers"
    const val GENERATE_TOKEN = "UserManagement/authenticate"
    const val GET_PARENT_LOCATION = "LocationMapping/GetParentLocation"
    const val GET_YARD_LOCATION = "LocationMapping/GetYardLocation"
    const val serviceAPi="/Service/api/"
    const val tgsAPi="/Tgs/api/"
    const val BASE_URL = "BASE_URL"
// API paths
//    const val tgsAPi = "api/"
//    const val serviceAPi = "api/"
    const val LOGIN_URL = "AuthService/authenticate"



    const val Get_GRNData="MobileApp/getGRNData"

    const val QC_StatusSubmission="MobileApp/qcStatusSubmission"


    const val GET_Location="LocationMaster/getLocationsWithPagination"

    const val Print_PRN= "PrintPRNDetails/getZplContent"
    //Slitting
    const val Get_HR_SlittingPlannned_List="HRSlittingPlan/getHRSSlittingPlannedList"
    const val  Get_Slitting_planById="HRSlittingPlan/getHRSlittingPlanbyId"
    const val  Get_HRSlitting_Scan="HRSlittingPlan/validateAgainstPlan"
    const val Get_AllItemAgainstPlan= "HRSlittingPlan/getAllItemAgainstPlan"
    const val Get_SubmitSlit_From_Plan= "HRSlitting/initiateSlitting"

    //on Going
    const val  Get_OnGoing="HRSlitting/getOngoingHRSlittingJobs"
    const val Get_Hr_Slitting_Detail="HRSlitting/getHRSlittingDetailsById"
    const val Get_Complete_Hr_Slitting="HRSlitting/completeHRSlitting"

    //without plan
    const val POST_InitiateSlitting_WithoutPlan="HRSlitting/initiateSlittingWithoutPlan"
    const val Get_Stock_BYBatchOr_Barcode="StockTransaction/getStockByBatchOrBarcode"
     // pickling

    const val GET_Pickling_Product_By_Barcode = "Pickling/getProductToInitiatePicklingProcess"
    const val POST_Process_Pickling = "Pickling/processPickling"
    const val GET_Ongoing_Pickl_Jobs = "Pickling/getOngoingPicklingJobs"
    const val GET_Pickling_By_Id = "Pickling/getPicklingByIdAsync"

    //change password
    const val CHANGE_PASSWORD = "UserManagement/change-password"


    const val LOCATION_ID = "locationId"



}