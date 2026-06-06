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
    const val KEY_PRINTER_MAC = "KEY_PRINTER_MAC"
    const val KEY_DEVICE_WIFI_MAC = "KEY_DEVICE_WIFI_MAC"
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

    const val InsStockStatus="InStock"
    const val CompleteStatus="Completed"

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
    const val Get_AllItemAgainstPlan= "HRSlittingPlan/getAllItemAgainstPlan"   // come for crm ans slitting
    const val Get_SubmitSlit_From_Plan= "HRSlitting/initiateSlitting"
    const val GET_DELETE_TRANSACTION="HRSlitting/deleteHRSlittingTransaction"
    const val GET_Slitting_Add="HRSlitting/addChild"
    const val GET_Slitting_Delete="HRSlitting/deleteHRSlittingTransDetail"
    const val GET_ConfigsKey="ApplicationConfig/getConfigsByKey"
    const val GET_RegisterConfig="ApplicationConfig/registerConfig"
const val GET_SlitCoil="StockTransaction/coilSplit"

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
    const val GET_Pick_AddChild="Pickling/addChild"
    const val DeleteChildPickling= "Pickling/deletePicklingTransDetail"
    const val GET_PicklingDelete="Pickling/deletePicklingTransaction"
    const val GET_CrmDelete="CRM/deleteCRMTransaction"
    const val GET_GPDelete="Galvanizing/deleteGalvanizingTransaction"
//GP
    const val GET_Ongoing_GP="Galvanizing/getOngoingGalvanizingJobs"
    const val GET_Initiate_GP="Galvanizing/processGalvanizing"
    const val GET_GPDetail_Id="Galvanizing/getGalvanizingById"
    const val GET_GPDelete_Child="Galvanizing/deleteGalvanizingTransDetail"
    const val GET_GpAdd_Child="Galvanizing/addChild"

    //CRM

    const val GET_CRM_PlannedList="CRMPlan/getCRMPlannedList"
    const val Get_CRM_Scan="CRMPlan/validateAgainstCRMPlan"
    const val  GET_CRM_PlanID="CRMPlan/getCRMPlanbyId"
    const val PosT_ProcessCRM= "CRM/processCRM"
    const val GET_going_CRMJob="CRM/getOngoingCRMJobs"
    const val GET_CRM_BY_Id="CRM/getCRMByIdAsync"
    const val POST_CRMWithout_Plan="CRM/initiateCRMWithoutPlan"
    const val GET_PrintLabel= "PrintPRNDetails/printLabel"
    const val GET_ADDCRM_Child="CRM/addChild"
    const val GET_Delete_Child="CRM/deleteCRMDetail"

    //Gate Entry
    const val ADD_Supplier="GateTransaction/addSuppilerItems"
    const val GATE_Transaction="GateTransaction/gateTransactionEntry"
    const val GET_GATE_Transaction_List="GateTransaction/getAllGateTransaction"
    const val GET_GATE_Transaction_Edit="GateTransaction/getGateTransactionById"
    const val GET_ALL_Transporter_List="TransporterMaster/getAllTransporterList"
    // production Entry
    const val GET_ActionType="ActionTypeMaster/getAllActionType"
    const val GET_ProcessMachine="ProcessMachineMapping/getProcessMchineById"
    const val GET_BomInputCode="BOM/getBoMByInputCode"
    const val GET_GpBOmComponent="BOM/getBomcomponents"
    const val GET_ALL_GRADE="GradeAndGSM/getAllGrade"
    const val GET_ALL_GSM="GradeAndGSM/getAllGSM"
    const val GET_QCHistory="QC/getAllQcTransaction"
    const val GET_CoilSlit="StockTransaction/coilSplit"


    //change password
    const val CHANGE_PASSWORD = "UserManagement/change-password"




//Error
    const val SelectStationFirstError="Please select station first"
    const val SelectCoil="Please select at least one coil"
    const val  ChildMotherExceedError="Child weight and mother weight cannot exceed Mother weight"
    const val WeightExceed= "Weight exceeds Mother Coil"
    const val ValidChildWeightError="Enter valid Child Weight"
    const val EnterScrapWeight="Enter Scrap Weight"
    const val InvalidIronLoss="Invalid Iron Loss"
//intentString
const val LocationId = "LOCATION_ID"
    const val LocationName="LOCATION_NAME"
   const val CrmTranJob="CRM_TRAN_JOB"
    const val JobId="JOB_ID"
    const val BarcodeValue="BARCODE"
    const val MaterialCode="MaterialCode"
    const val PicklingId="PICKLING_ID"

    const val WidthId= "WIDTH"
    const val  HrSlittingId="HrSlitting_planID"
    const val SourceStockId="Source_StockID"
    const val MotherWeightV="Mother_Weight"
    const val SupplierNo="SupplierNo"
    const val ThicknessV="THICKNESS"
    const val GradeV="GRADE"
    const val HrSlittingPlanId="Hr_Slitting_PlanID"
//DropeDownValue
     const val SelectFromPlan= "Select from plan"
    const val  WithOutPlan="Without plan"

}