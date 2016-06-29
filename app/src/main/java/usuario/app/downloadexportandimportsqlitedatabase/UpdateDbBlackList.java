package usuario.app.downloadexportandimportsqlitedatabase;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;


import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.channels.FileChannel;
import java.security.MessageDigest;


import android.content.BroadcastReceiver;

import com.google.gson.Gson;


/**
 * Created by Robson Souza on 17/06/2016.
 * Next steps:
 *   1- Implement a activity to request for user the Internet Permission.
 * @author Robson Souza
 * @version 1.1
 */
public class UpdateDbBlackList extends AsyncTask<Void, Void, String> {

    private Uri uriDbSource;
    private Uri uriMd5Source;
    private Context cActivityContext;
    private String sDbFileName;
    private String sDestinationPath;
    private String sLocalDownloadPath;
    private String sMd5FromUriMd5Source;
    private Long lIdDownload;

    //region Getters and Setters

    /**
     * Set the link where are the database.
     * @param uriDbSource - Link for your database in internet.
     */
    public void setUriDbSource(Uri uriDbSource) {
        try{
            this.uriDbSource = uriDbSource;
            File fTemp = new File(uriDbSource.toString());
            sDbFileName = fTemp.getName();
            fTemp = null;
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * Get the link where are the database
     * @return - Link for your database in internet.
     */
    public Uri getUriDbSource() {
        return uriDbSource;
    }

    /**
     * Set the context where the object will work.
     * @param cActivityContext - Context.
     */
    public void setActivityContext(Context cActivityContext) {
        this.cActivityContext = cActivityContext;
    }

    /**
     * Get the context where the object is working.
     * @return - Context
     */
    public Context getActivityContext() {
        return cActivityContext;
    }

    /**
     * Get the file name recovered from the link(setUriDbSource).
     * @return String - File name.
     */
    public String getDbFileName() {
        return sDbFileName;
    }

    /**
     * Get the destination path without file name. The file name will used from getDbFileName.
     * @return String - Destination path.
     */
    public String getDestinationPath() {
        return sDestinationPath;
    }

    /**
     * Set the destination path without file name. The file name will used from getDbFileName.
     * @param sDestinationPath
     */
    public void setDestinationPath(String sDestinationPath) {
        this.sDestinationPath = sDestinationPath;
    }

    /**
     * Verify if current database exists.
     * @return boolean - True if exists False if don't.
     */
    public boolean CurrentDatabaseExists(){
        try {
            File fTemp = new File(getDestinationPath()+"/"+getDbFileName());
            boolean bReturn = false;
            if (fTemp.exists())
                if (fTemp.isFile())
                    bReturn = true;
            return bReturn;
        }
        catch (Exception e){
            throw  e;
        }

    }

    /**
     * Verify if backup database exists.
     * @return boolean - True if exists False if don't.
     */
    public boolean BackupDatabaseExists(){
        try {
            File fTemp = new File(getDestinationPath()+"/"+getDbFileName().replace(".db",".old"));
            boolean bReturn = false;
            if (fTemp.exists())
                if (fTemp.isFile())
                    bReturn = true;
            return bReturn;
        }
        catch (Exception e){
            throw  e;
        }

    }

    /**
     * Get the MD5 from current database.
     * @return String - MD5 hash.
     */
    public String getMd5FromCurrentDatabase() {
        return GetMd5FromCurrentDatabase();
    }

    /**
     * Get the link where are the MD5 hash in Json format.
     * @return - Link for your MD5 in internet.
     */
    public Uri getUriMd5Source() {
        return uriMd5Source;
    }

    /**
     * Set the link where are the MD5 hash in Json format.
     * @param uriMd5Source
     */
    public void setUriMd5Source(Uri uriMd5Source) {
        this.uriMd5Source = uriMd5Source;
    }

    /**
     * Get the last MD5 recovered from the last Execute called.
     * If you want the current MD5 from link in internet you have to call Execute before
     * and only after this you will have the getMd5FromUriMd5Source updated.
     * @return String - MD5 hash.
     */
    public String getMd5FromUriMd5Source() {
        return sMd5FromUriMd5Source;
    }

    /**
     * Set the MD5 from link in internet.
     * @return String - MD5 hash.
     */
    private void setMd5FromUriMd5Source(String sMd5FromUriMd5Source) {
        this.sMd5FromUriMd5Source = sMd5FromUriMd5Source;
    }

    /**
     * Before use this method you have to call Execute.
     * @return boolean - True for getMd5FromUriMd5Source() == getMd5FromCurrentDatabase().
     */
    public boolean isUpdated(){
        return (getMd5FromCurrentDatabase().equalsIgnoreCase(getMd5FromUriMd5Source()));
    }
    //endregion

    /**
     * Constructor
     * @param context - App's context.
     * @param uriDbSource - Link for your database in internet.
     */
    public UpdateDbBlackList(Context context, Uri uriDbSource, Uri uriMd5){
        try {
            if (context==null || uriDbSource.getPath().isEmpty())
                throw new IllegalArgumentException("Context or uriDbSource");

            cActivityContext = context;
            setUriDbSource(uriDbSource);
            setUriMd5Source(uriMd5);
            setDestinationPath(cActivityContext.getFilesDir()+"/../databases/");
            sLocalDownloadPath = cActivityContext.getExternalFilesDir(null)+"/"+Environment.DIRECTORY_DOWNLOADS;
            lIdDownload = Long.valueOf(0);

            //Broadcast to receive the download's return.
            //If sucessful update the local database else don't do anything.
            BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
                DownloadManager downloadManager;
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                        if(downloadId == lIdDownload){
                            Cursor c= downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
                            if (c.moveToFirst()) {
                                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                                    TurnDbOld();
                                    if (!CopyToDatabasesPath())
                                        if(!CurrentDatabaseExists())
                                            RecoveryDatabase();
                                }
                            }
                        }
                    }
                }
            };
            cActivityContext.registerReceiver(downloadReceiver, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * Download the database with the blacklist apps. This application need to have internet access.
     * The database file will downloaded at Environment.DIRECTORY_DOWNLOADS.
     * @return boolean - True for download succesful or False for fail.
     */
    private boolean Download(){
        try{
            PackageManager packageManager = cActivityContext.getPackageManager();

            if (uriDbSource != null && packageManager.checkPermission(Manifest.permission.INTERNET,
                    cActivityContext.getPackageName()) == PackageManager.PERMISSION_GRANTED
                    && !getDbFileName().isEmpty()){
                DownloadManager downloadmanager;
                downloadmanager = (DownloadManager) cActivityContext.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uriDbSource);

                request.setTitle(cActivityContext.getApplicationInfo().name + " - Database");
                request.setDescription("Database download using DownloadManager");

                request.setDestinationInExternalFilesDir(cActivityContext, Environment.DIRECTORY_DOWNLOADS , getDbFileName());

                lIdDownload = downloadmanager.enqueue(request);
                return true;
            }
            return false;
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * Turn the current database to old database.
     * @return boolean - True for succesful or False for failure.
     */
    private boolean TurnDbOld(){
        try {
            File fCurrentDb = new File(getDestinationPath()+"/"+getDbFileName());
            boolean bFResult = false;
            if (fCurrentDb.exists())
                bFResult = fCurrentDb.renameTo(new File(fCurrentDb.getPath().replace(getDbFileName(), getDbFileName().replace(".db",".old"))));
            fCurrentDb = null;
            return bFResult;
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * Recovery the last database available.
     * @return boolean - True for succesful or False for failure.
     */
    public boolean RecoveryDatabase(){
        try {
            File fBackupDb = new File(getDestinationPath()+"/"+getDbFileName().replace(".db",".old"));
            File fDatabasesPath = new File(getDestinationPath()+"/"+getDbFileName());
            boolean bFResult = false;

            FileInputStream inStream = new FileInputStream(fBackupDb);
            FileOutputStream outStream = new FileOutputStream(fDatabasesPath);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();

            if (fBackupDb.exists()){
                inChannel.transferTo(0, inChannel.size(), outChannel);
                inStream.close();
                outStream.close();
                fBackupDb.delete();
                bFResult = true;
            }
            fBackupDb = null;
            fDatabasesPath = null;
            return bFResult;
        }
        catch (IOException e){
            return false;
        }
    }

    /**
     * Copy the downloaded database file to databases path and delete the source file.
     * @return boolean - True for succesful or False for failure.
     */
    private boolean CopyToDatabasesPath(){
        try {
            File fDownloadedDb = new File(sLocalDownloadPath+"/"+getDbFileName());
            File fDatabasesPath = new File(getDestinationPath());
            fDatabasesPath.mkdirs();
            fDatabasesPath = new File(getDestinationPath()+"/"+getDbFileName());
            boolean bFResult = false;

            FileInputStream inStream = new FileInputStream(fDownloadedDb);
            FileOutputStream outStream = new FileOutputStream(fDatabasesPath);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();

            if (fDownloadedDb.exists()){
                inChannel.transferTo(0, inChannel.size(), outChannel);
                inStream.close();
                outStream.close();
                fDownloadedDb.delete();
                bFResult = true;
            }
            fDownloadedDb = null;
            fDatabasesPath = null;
            return bFResult;
        }
        catch (IOException e){
            return false;
        }
    }

    /**
     * Delete all the previous downloaded database files from getUriDbSource.
     */
    private void DeletePreviousDownloads(){
        try {
            File fFiles[] = new File(sLocalDownloadPath).listFiles();
            if (fFiles != null)
                for (File f: fFiles) {
                    if ( (f.getName().toLowerCase().startsWith(getDbFileName().toLowerCase().replace(".db",""))) && (f.getName().toLowerCase().endsWith(".db")) )
                        f. delete();
                }
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * Delete all local databases.
     * @return int - Number of files deleted.
     */
    public int DeleteAllLocalDatabases(){
        try {
            int iFilesDeleteds=0;
            File fFiles[] = new File(getDestinationPath()).listFiles();
            if (fFiles != null)
                for (File f: fFiles) {
                    if (f.getName().equalsIgnoreCase(getDbFileName()) || f.getName().equalsIgnoreCase(getDbFileName().toLowerCase().replace(".db",".old"))){
                        f.delete();
                        iFilesDeleteds++;
                    }
                }
            return iFilesDeleteds;
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * Download from internet and import the local database file.
     * The import is a simple copy (like a CTRL+C CTRL+V), the file downloaded replace the local file.
     * This method has asynchronous calls -using Broadcast and Receiver- but don't worry,
     * the database is just replaced if download was sucessful. This method don't verify the MD5's hashs.
     *
     * If you want download the database only if the MD5 available in internet don't match with the local, please call Execute.
     * The Execute method will verify the MD5's hashs and will download the database if was different.
     * @return - boolean - True for succesful or False for fail.
     */
    public boolean UpdateLocalDatabase(){
        try{
            boolean bresult = false;
            DeletePreviousDownloads();
            Download();
            bresult = true;
        }
        catch (Exception e){
            throw e;
        }

        return true;
    }

    /**
     * Get the MD5 from current database.
     * References:
     * http://www.londatiga.net/it/programming/sample-code-to-get-md5-checksum-of-a-file-in-android/
     * @return
     */
    private String GetMd5FromCurrentDatabase(){
        String sMd5 = "";
        try {
            char[] hexDigits = "0123456789abcdef".toCharArray();
            /*
            BufferedReader bufferedReader = new BufferedReader(new FileReader(sPath));
            if (bufferedReader.ready())
                bufferedReader.readLine();
             */
            byte[] bytes = new byte[4096];
            int read = 0;
            MessageDigest digest = MessageDigest.getInstance("MD5");

            FileInputStream fileInputStream   = new FileInputStream(getDestinationPath()+"/"+getDbFileName());

            while ((read = fileInputStream.read(bytes)) != -1) {
                digest.update(bytes, 0, read);
            }

            byte[] messageDigest = digest.digest();

            StringBuilder sb = new StringBuilder(32);

            for (byte b : messageDigest) {
                sb.append(hexDigits[(b >> 4) & 0x0f]);
                sb.append(hexDigits[b & 0x0f]);
            }

            sMd5 = sb.toString();

        }
        catch (Exception e){
        }
        return sMd5;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        //Reference:
        //https://developer.android.com/reference/java/net/HttpURLConnection.html
        //http://www.devmedia.com.br/trabalhando-com-asynctask-no-android/33481
        //http://alvinalexander.com/blog/post/java/how-open-url-read-contents-httpurl-connection-java

        URL url = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        try
        {
            // create the HttpURLConnection
            //url = new URL(getUriMd5Source().toString());
            url = new URL("https://s3-sa-east-1.amazonaws.com/ahooahoo.com/malware.md5.txt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // just want to do an HTTP GET here
            connection.setRequestMethod("GET");

            // uncomment this if you want to write output to this url
            //connection.setDoOutput(true);

            // give it 15 seconds to respond
            connection.setReadTimeout(15*1000);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                // read the output from the server
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            // close the reader; this can throw an exception too, so
            // wrap it in another try/catch block.
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String sResult){
        try {
            //Reference:
            //http://www.devmedia.com.br/introducao-ao-formato-json/25275
            if (!sResult.isEmpty() && !sResult.equals(null)){
                Gson gson = new Gson();
                Hash hash = gson.fromJson(sResult, Hash.class);
                setMd5FromUriMd5Source(hash.getMd5());

                if (!getMd5FromUriMd5Source().equalsIgnoreCase(getMd5FromCurrentDatabase()))
                    UpdateLocalDatabase();
            }
        }
        catch (Exception e){

        }
    }

    /**
     * Just used with GSON.
     * Reference:
     * http://www.jsonschema2pojo.org/
     */
    public class Hash {

        private String md5;
        private String name;

        /**
         *
         * @return
         * The md5
         */
        public String getMd5() {
            return md5;
        }

        /**
         *
         * @param md5
         * The md5
         */
        public void setMd5(String md5) {
            this.md5 = md5;
        }

        /**
         *
         * @return
         * The name
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @param name
         * The name
         */
        public void setName(String name) {
            this.name = name;
        }

    }
}
