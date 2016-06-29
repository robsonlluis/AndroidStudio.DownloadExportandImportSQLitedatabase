package usuario.app.downloadexportandimportsqlitedatabase;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnDownload;
    private Button btnCheckIfDbExist;

    private TextView txtUrl;
    private TextView txtMd5FromCurrentDb;
    private TextView txtMd5FromUrl;


    UpdateDbBlackList updateDbBlackList;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDownload = (Button) findViewById(R.id.btnDeleteDatabases);
        btnCheckIfDbExist = (Button) findViewById(R.id.btnCheckIfDbExist);
        txtUrl = (TextView) findViewById(R.id.txtURL);
        txtMd5FromCurrentDb = (TextView) findViewById(R.id.txtMd5FromCurrentDb);
        txtMd5FromUrl = (TextView) findViewById(R.id.txtMd5FromUrl);

        updateDbBlackList = new UpdateDbBlackList(this,Uri.parse(txtUrl.getText().toString()), Uri.parse(txtMd5FromUrl.getText().toString()));

        findViewById(R.id.btnDeleteDatabases).setOnClickListener(this);
        findViewById(R.id.btnCheckIfDbExist).setOnClickListener(this);
        findViewById(R.id.btnUpdateDatabase).setOnClickListener(this);
        findViewById(R.id.btnRecoceryLastLocalDb).setOnClickListener(this);
        findViewById(R.id.btnBackupDatabaseExists).setOnClickListener(this);
        findViewById(R.id.btnGetMd5FromCurrentDatabase).setOnClickListener(this);
        findViewById(R.id.btnGetMd5FromUriSource).setOnClickListener(this);
        findViewById(R.id.btnDatabaseIsUpdated).setOnClickListener(this);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onClick(View v) {
        int idView = v.getId();
        switch (idView) {
            case R.id.btnGetMd5FromUriSource:
                txtMd5FromUrl.setText(updateDbBlackList.getMd5FromUriMd5Source());
                break;
            case R.id.btnDatabaseIsUpdated:
                if (updateDbBlackList.isUpdated())
                    Toast.makeText(this, "Database updated. MD5s match.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "Database not updated. MD5s not match.", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnGetMd5FromCurrentDatabase:
                txtMd5FromCurrentDb.setText(updateDbBlackList.getMd5FromCurrentDatabase());
                break;
            case R.id.btnDeleteDatabases:
                Toast.makeText(this, updateDbBlackList.DeleteAllLocalDatabases()+" archives deleted", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnUpdateDatabase:
                updateDbBlackList = new UpdateDbBlackList(this,Uri.parse(txtUrl.getText().toString()), Uri.parse(txtMd5FromUrl.getText().toString()));
                updateDbBlackList.execute();
                break;
            case R.id.btnRecoceryLastLocalDb:
                if (updateDbBlackList.RecoveryDatabase())
                    Toast.makeText(this, "Database recovered.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "Database didn't recovered.", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnBackupDatabaseExists:
                if (updateDbBlackList.BackupDatabaseExists())
                    Toast.makeText(this, "Backup database exists.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "Backup database don't exists.", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnCheckIfDbExist:
                if (updateDbBlackList.CurrentDatabaseExists()){
                    Toast.makeText(this, "Current database exists.", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "Current database don't exists.", Toast.LENGTH_SHORT).show();

                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://usuario.app.downloadexportandimportsqlitedatabase/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://usuario.app.downloadexportandimportsqlitedatabase/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
