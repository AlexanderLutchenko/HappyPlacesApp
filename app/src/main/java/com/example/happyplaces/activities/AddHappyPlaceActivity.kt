package com.example.happyplaces.activities

import android.Manifest
import com.example.happyplaces.R
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.happyplaces.BuildConfig
import com.example.happyplaces.HappyPlacesApp
import com.example.happyplaces.database.HappyPlaceDAO
import com.example.happyplaces.database.HappyPlaceEntity
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.utilities.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


//Step4 Make the whole class an OnClickListener by inheriting from "View.OnClickListener" interface
//meaning any item can be clicked and processed if OnClick method below is prepared for this item

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    //Step2 prepare variables to use for calendar date picker dialog in "Date" selection box
    private var cal = Calendar.getInstance() //gets Calendar instance, default time zone and locale
    //A variable required for DatePickerDialog OnDateSetListener.
    //This listener indicates that user finished a date selection, it will be initialized below
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener


    //Step36 prepare a global variable to receive HappyPlace record from another activity
    //It wil also be used to determine if a user wants create a new record or to edit an existing record
    private var receivedRecord : HappyPlaceEntity? = null




    private var imageURI : Uri? = null
    private var latitude: Double = 0.0
    private var longitude : Double = 0.0


    //Step65 Add a variable for Location Client (needed for current location check in Step68)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //create a global DAO object to access the Database within the entire activity
    private lateinit var happyDAO : HappyPlaceDAO
    //create a binding object for activity_main.xml layout
    private lateinit var binding: ActivityAddHappyPlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Fill up the binding object with activity_main.xml layout items
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        //Align the xml view to this class (this activity)
        setContentView(binding.root)

        //Step1.10 Create a custom toolbar bar
        setSupportActionBar(binding.toolbarAddPlace)
        //Create a "back action" button in toolbar
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        //Set action of "back action" button same as a regular Android UI navigation "back button"
        binding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        //Step3 Create a DateSetListener required to set date when "Date" input clicked
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            //Step9 When Date is selected by user in DatePicker Dialog, call the prepared method to
            //input this date into the UI "Date" box
            inputDateInView()
        }


        //Step45 Initialise the Places API Class
        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity, BuildConfig.mapsAPIKey)
        }

        //In order to create a Database DAO instance, the Application class is required where the database will be initialised
        //If the db has been created previously in another module/class, it will be reused due to singleton pattern used to create it
        happyDAO = (application as HappyPlacesApp).db.createHappyDAO()

        //Step66 Initialise the Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        //Step37 If a user gets to this activity with intent to edit the existing record,
        //populate the screen with that record details
        if (intent.hasExtra("Edit Record Details")){
            receivedRecord = intent.getParcelableExtra("Edit Record Details") as HappyPlaceEntity?
            if (supportActionBar != null) {
                supportActionBar!!.title = "Edit Happy Place"
            }
            if (receivedRecord != null) {
                binding.etTitle.setText(receivedRecord?.title)
                binding.etDescription.setText(receivedRecord?.description)
                binding.etDate.setText(receivedRecord?.date)
                binding.etLocation.setText(receivedRecord?.location)
                latitude = receivedRecord!!.latitude
                longitude = receivedRecord!!.longitude
                binding.ivPlaceImage.setImageURI(Uri.parse(receivedRecord?.image))
                //update imageURI value to for user-input check of "Save" button(default imageURI = null)
                imageURI = Uri.parse(receivedRecord?.image)
                binding.btnSave.text = "Update"
                binding.tvAddImage.text = "Update Image"
            }
        }else{
            //Step10 Once the AddHappyPlace screen opens, and user get to the page to create
            //a new HappyPlace record (but not to edit an existing record)
            // set a current Date in the "Date" box of it to simplify date selection
            inputDateInView()
        }



//Set the click listeners for AddHappyPlace view items
// "this" - pass it to overridden below OnClick method of the parent class "View.OnClickListener"
        //Step7 Set the listener for etDate
        binding.etDate.setOnClickListener(this)
        //Step11 Set the listener for tvAddImage
        binding.tvAddImage.setOnClickListener(this)
        //Step17 Set the listener for tvAddImage
        binding.btnSave.setOnClickListener(this)
        //Step47 Set the listener for etLocation
        binding.etLocation.setOnClickListener(this)
        //Step60 Set the listener for CurrentLocation button
        binding.tvSelectCurrentLocation.setOnClickListener(this)

    }

    //Step8 Function to input the selected date from DatePicker dialog as a text into UI box "Date"
    private fun inputDateInView(){
        //Working with dates requires to create a date format
        val myFormat = "MM.dd.yyyy" //Every time the date is engaged, the date format must be provided
        //simple date format helps to translate the date passed to it
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        //update the text in the input box "Date"
        binding.etDate.setText(sdf.format(cal.time).toString())
    }



    //Step61 Verify if the user's phone has Location services enabled,
    //as location is required further to determine user's location
    private fun isLocationEnabled():Boolean{
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }



    //Step5 As the main class of the current module inherits from the View.OnClickListener interface,
    //it requires to implement(override) the onClick method which should execute any OnClick
    //events of the entire activity
    override fun onClick(v: View?) {
        //implement code depending on which view is triggered
        when(v!!.id){
            //Step6 When the "Date" input clicked -show the dialog with calendar to select the date
            binding.etDate.id ->{
            DatePickerDialog(this@AddHappyPlaceActivity, dateSetListener,
                //set DatePickerDialog to point to current date when it is loaded
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }


            //Step12 When the "Add Image" button clicked - show two options to chose the image source
            binding.tvAddImage.id -> {
                //Create the Add-Image-Dialog to provide a user with image source options
                val addImageDialog = AlertDialog.Builder(this)
                addImageDialog.setTitle("Select the Image Source:")
                addImageDialog.setItems(arrayOf("Select photo from Gallery",
                    "Take photo with Camera")){
                    dialog, item ->
                    when(item){
                        //Actions to take depending on user Source selection
                        0 -> choosePhotoFromGallery() //Run the method to choose photo from gallery
                        1 -> takePhotoFromCamera()//Run the method to take a camera picture
                    }
                }
                addImageDialog.show()
            }
//Step18 When the "Save" button clicked - save the HappyPlace record to the database, or update it
            binding.btnSave.id -> {
                when { //check is user has filled up all required input fields
                    //"Date" field is filled with current date automatically, no input check required
                    binding.etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    }
                    binding.etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                    }
                    binding.etLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
                    }
    //imageURI only updated from null when user-selected image is stored to the app internal storage
                    imageURI == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> { //if all input fields are filled up
            //crate an instance of a HappyPlace entity to pass it to the DataBase DAO and store it
                        val happyPlaceRecord = HappyPlaceEntity(
                        //assign ID depending on a user intent to edit existing/create a new record
                            if(receivedRecord==null) 0 else receivedRecord!!.id,
                            binding.etTitle.text.toString(),
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            imageURI.toString(),
                            latitude,
                            longitude
                        )

                    //Step19 Action to take when user wants to create a new HappyPlace record
                    //when AddHappyPlace screen wasn't opened to update the existing record
                        if (receivedRecord == null){
                            lifecycleScope.launch {
                                happyDAO.insert(happyPlaceRecord)
                                Toast.makeText(this@AddHappyPlaceActivity, "Record saved", Toast.LENGTH_SHORT).show()
                            }
            //finish the current activity and open the screen of the last active activity(MainActivity)
                            finish()
                        }else{


                        //Step38 action to take when user is updating an existing HappyPlace record
                            lifecycleScope.launch {
                                happyDAO.update(happyPlaceRecord)
                                Toast.makeText(this@AddHappyPlaceActivity, "Record updated", Toast.LENGTH_SHORT).show()
                            }
            //finish the current activity and open the screen of the last active activity(MainActivity)
                            finish()
                        }
                    }
                }
            }

            //Step48 When Location Field clicked - open the Google Places API to get searchable location
            binding.etLocation.id -> {
                getLocationGooglePlacesAPI()
            }

            //Step62 Set actions for "Select Current Location" button
            binding.tvSelectCurrentLocation.id ->{
                //Check if location service is enabled on device
                if(!isLocationEnabled()){ //If no location settings enabled, send user to settings to enable it
                    Toast.makeText(this, "Your location provider is off.\nPlease turn it on in settings", Toast.LENGTH_LONG).show()
                lifecycleScope.launch{
                    delay(2500)
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                }else{//In case location provider is on, use the location
                    getCurrentLocation()
                }
            }
        }
    }

    //Step13 Method to chose photo from gallery
    private fun choosePhotoFromGallery() {
        //Check if gallery permission is currently denied
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                //WRITE_EXTERNAL_STORAGE implies READ_EXTERNAL_STORAGE being also granted
        //WRITE permission required later to store a selected image to the app internal storage
                this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            //If access is denied, run a method to request a user to allow permissions
            showRationaleDialogForPermissions("Gallery access required",
                "Gallery access must be granted to use this feature. " +
                        "Please allow access in the App Settings")
        } else {// If gallery permission isn't currently denied, request it
            //WRITE_EXTERNAL_STORAGE implies READ_EXTERNAL_STORAGE being also granted
            requestPermissionGallery.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    //Result Launcher to request gallery permission and open the gallery
    private val requestPermissionGallery: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // If gallery permission is granted, use the gallery
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            } else {
                // If permission is denied - show Toast
                Toast.makeText(this, "Access to gallery was denied", Toast.LENGTH_SHORT).show()
            }
        }
    //Use gallery to select an image and use it on activity screen
    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                //get contentURI from the intent result
                val contentURI = result.data?.data
                try{
                    //use contentURI to get the selected image
                    val selectedImage: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    //set selected image to the AddHappyPlace activity screen
                    binding.ivPlaceImage.setImageBitmap(selectedImage)
                    //save selected image to the app internal storage
//imageURI used to determine if user has selected an image before saving the new HappyPlace record
                    imageURI = saveImageToInternalStorage(selectedImage)
                }catch (e:IOException){ //catch Input/Output errors during image selection
                    e.printStackTrace()
                    Toast.makeText(this@AddHappyPlaceActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }



    //Step14 method to request a user to allow permissions
    private fun showRationaleDialogForPermissions(title: String, message: String){
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Go To Settings"){_,_ ->
                try { //Send a user to App settings to update allowed permissions
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){dialog, which ->
                dialog.dismiss()
            }
            .show()
    }


    //Step15 Function to save selected image(or camera photo) to the app internal storage
    private fun saveImageToInternalStorage(receivedImage: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext) //get the app context to locate its files
    //MODE_PRIVATE makes files accessible only for this application, or apps with the same user ID
        //create directory to store images within the app files location
        var file = wrapper.getDir("HappyPlacesImages", Context.MODE_PRIVATE)
        //use the created directory to create a file within it
        file = File(file, "${UUID.randomUUID()}.jpg")
        //store the passed to the function image content into the internal file
        try {
            val stream: OutputStream = FileOutputStream(file)
            receivedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){//catch Input/Output errors storing a file
            e.printStackTrace()
            Toast.makeText(this@AddHappyPlaceActivity, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
        //parse the path of the stored image into the URI format and return it
        return Uri.parse(file.absolutePath)
    }


    //Step16 Method to take photo with camera
    private fun takePhotoFromCamera(){
        //Check if camera access is currently denied
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,Manifest.permission.CAMERA)
        ) {
            ///If access is denied, run a method to request a user to allow permissions
            showRationaleDialogForPermissions( "Camera access required",
                "Camera access must be granted to use this feature. " +
                        "Please allow access in the App Settings")
        } else {// If camera access isn't currently denied, request it
            requestPermissionCamera.launch(Manifest.permission.CAMERA)
        }
    }
    //Result Launcher to request camera permission and launch the camera
    private val requestPermissionCamera: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // If camera permission is granted, use the camera
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intent)
            } else {
                //If permission is denied - show Toast
                Toast.makeText(this, "Access to camera was denied", Toast.LENGTH_SHORT).show()
            }
        }
    //Use camera to take a photo and use it to set image on main app screen
    private val cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                try{ //get the taken photo as a Bitmap from intent result
                    val photoCapture: Bitmap = result.data?.extras!!.get("data") as Bitmap
                    //set photo to the view on the activity screen
                    binding.ivPlaceImage.setImageBitmap(photoCapture)
                    //Save taken photo to the app internal storage
 //imageURI used to determine if user has selected an image before saving the new HappyPlace record
                    imageURI = saveImageToInternalStorage(photoCapture)
                }catch (e:IOException){//catch Input/Output errors during taking a photo
                    e.printStackTrace()
                    Toast.makeText(this@AddHappyPlaceActivity, "Failed to take a photo", Toast.LENGTH_SHORT).show()
                }
            }
        }




    //Step49 Method to start Google Places API
    private fun getLocationGooglePlacesAPI(){
        // These are the list of fields which are required by Google PLaces API
        try {
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
            // Start the autocomplete intent
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this@AddHappyPlaceActivity)
            googlePlacesAPILauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private val googlePlacesAPILauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if(result.resultCode == RESULT_OK  && result.data !=null) {

                val place : Place = Autocomplete.getPlaceFromIntent(result.data!!)
                binding.etLocation.setText(place.address)
                latitude=place.latLng!!.latitude
                longitude=place.latLng!!.longitude
            } else if (result.resultCode == RESULT_CANCELED) {
                // The user canceled the operation, will work on on back Pressed
                Toast.makeText(this@AddHappyPlaceActivity,"Canceled",Toast.LENGTH_LONG).show()
            }
        }



    //Step63 Method to get user's current location
    private fun getCurrentLocation(){
        //Check if Location access is currently denied
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,Manifest.permission.ACCESS_COARSE_LOCATION)){
            //If Location access is currently denied, run a method to request a user to allow permissions
            showRationaleDialogForPermissions("Location access required",
                "Location access must be granted to use this feature. " +
                        "Please allow access in the App Settings")
        }else{ //If location is not denied, request it and set the current location
            requestPermissionCurrentLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
  //Launcher to request current location permission and input it into "Location" field on the screen
    private val requestPermissionCurrentLocation: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                requestLocationAddress()
            } else {
                // If the permission is denied then show a text
                Toast.makeText(this, "Access to location was denied", Toast.LENGTH_SHORT).show()
            }
        }

    //Step69 Suppress "Missing Permission" warning of fusedLocationClient the following function uses,
    //as before that function call, Location Permission is successfully checked in Step 63
    @SuppressLint("MissingPermission")
    //Step68 Create function to transform user Location into human-readable address
    private fun requestLocationAddress(){
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMaxUpdates(1).build()
        val locationCallback= object : LocationCallback(){
            override fun onLocationResult(locatnReslt: LocationResult){
                latitude = locatnReslt.lastLocation!!.latitude
                longitude = locatnReslt.lastLocation!!.longitude
                //Engage the prepared helper-class to transform location into address
                val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, latitude, longitude)
                //Engage the helper-class interface to pass required functions to that class
                addressTask.setCustomAddressListener(object : GetAddressFromLatLng.AddressListener{
                    override fun onAddressFound(address:String?){
                        binding.etLocation.setText(address)
                    }
                    override fun onError(address:String?){
                        Log.e("Get Address::", "Could Not Get an Address")
                        binding.etLocation.setText(address)
                    }
                })
                //Run suspend function of the helper-class to get an address in background
                lifecycleScope.launch(Dispatchers.IO){
                    addressTask.getAddressInBckgrnd()
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //After all preparations to process a location data are configured, get a user-device location
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }



//Step43 Prepare to engage Google Cloud Platform APIs
    // 1) Login to console.cloud.google.com with Google account
    // 2) Create a new Project
    // 3) Enable APIs and Services -> Places API -> Enable
    // 4) Go to Project -> APIs and Services -> Credentials -> Create Credentials -> API key
    // 5) Go to created API key -> restrict for Android apps -> Add your app (protect your key, you pay for its usage on Google Cloud Platform)
        // 5.1) To get the Package name - read it under Android Studio Project app->java->"YOUR_PACKAGE_NAME"
        // 5.2) In order to get the SHA-1 certificate fingerprint for your app:
            // 5.2.a) In Android Studio app Project create a new Google Maps Fragment(New->Fragment->Google Maps Fragment)
            // 5.2.b) One of the created files is res/values/google_maps_api.xml. Find in it "SHA-1 certificate fingerprint" value
    // 6) Copy the created and protected API key at Google Cloud Platform, now paste it into Android Studio project:
        // There are two ways of using the API key in the Android Studio project - open and secure(safe to upload to GitHub and hide the API key)
        // 6.1) For open way: paste it into res/values/google_maps_api.xml "google_maps_key" string value
            //6.1.a) Now use it as a variable in AndroidManifest.xml file. Meta-data is created with the Google Maps Fragment in earlier step and awaits for the API key:
                    /*<meta-data
                    android:name="com.google.android.geo.API_KEY"
                     android:value="${@string/google_maps_key}" />*/
            //Inside any project activity the API Key can be accessed as a variable:
            // val apiKey = resources.getString(R.string.google_maps_key) (need to import com.example.happyplaces.R to activity)
        // 6.2) For secure way:
            // 6.2.a) Add a Secret Gradle Plugin that allows to create and read properties from Gradle's "local.properties" file
            // This file is not checked into GitHub version control system, meaning its content kept on yor local device but its properties can be
            // exposed as variables to Gradle-generated BuildConfig.java and AndroidManifest.xml files.
            // 6.2.b) In your project's root build.gradle file add a dependency, then synchronise Gradle:
                //classpath "com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1"
            //6.2.c) In your module build.gradle file add a plugin, then synchronise Gradle:
                //id 'com.google.android.libraries.mapsplatform.secrets-gradle-plugin'
            //6.2.d) In your Gradle local.properties file add
                //mapsAPIKey=YOUR_API_KEY_VALUE (Insert the real value instead of YOUR_API_KEY_VALUE)
            //6.2.e) Now use it as a variable in AndroidManifest.xml file. Meta-data is created with the Google Maps Fragment in earlier step and awaits for the API key:
                    /*<meta-data
                        android:name="com.google.android.geo.API_KEY"
                         android:value="${mapsAPIKey}" />*/
            //Inside any project activity the API Key can be accessed as a variable:
                //val apiKey = BuildConfig.mapsAPIKey
    //7) The next step is to create a Billing Account to make the API work
    //Google provides test functionality for free, but when lots of users employ the API key - it's a paid service
    //Go to Google Cloud Navigation Menu on top left -> Billing


}