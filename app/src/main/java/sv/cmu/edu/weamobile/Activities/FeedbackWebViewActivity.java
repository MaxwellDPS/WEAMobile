package sv.cmu.edu.weamobile.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import sv.cmu.edu.weamobile.Data.Alert;
import sv.cmu.edu.weamobile.R;
import sv.cmu.edu.weamobile.Utility.AlertHelper;
import sv.cmu.edu.weamobile.Utility.Constants;
import sv.cmu.edu.weamobile.Utility.Logger;


public class FeedbackWebViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_web_view);

        int alertId = getIntent().getIntExtra(Constants.ALERT_ID, -1);
        if (alertId != -1 ) {
            Alert alert = AlertHelper.getAlertFromId(getApplicationContext(), String.valueOf(alertId));
            if(alert != null){
                WebView wv = (WebView) this.findViewById(R.id.webView);
                wv.getSettings().setJavaScriptEnabled(true);
                wv.setWebViewClient(new MyBrowser());

                String url = AlertHelper.getFedbackURL(getApplicationContext(),alert);
                Logger.log(url);
                wv.loadUrl(url);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feedback_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}