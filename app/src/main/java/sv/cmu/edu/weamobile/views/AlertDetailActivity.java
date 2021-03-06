package sv.cmu.edu.weamobile.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import sv.cmu.edu.weamobile.R;
import sv.cmu.edu.weamobile.data.Message;
import sv.cmu.edu.weamobile.data.MessageState;
import sv.cmu.edu.weamobile.utility.AlertHelper;
import sv.cmu.edu.weamobile.utility.Constants;
import sv.cmu.edu.weamobile.utility.WEAUtil;


/**
 * An activity representing a single Alert detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link AlertDetailFragment}.
 */
public class AlertDetailActivity extends ActionBarActivity {

    AlertDetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String alertId = getIntent().getStringExtra(Constants.ARG_ITEM_ID);
        Message messageFromId = AlertHelper.getMessageFromId(getApplicationContext(), alertId);
        MessageState state = AlertHelper.getAlertState(getApplicationContext(), messageFromId);

//        if(state != null && !state.isAlreadyShown() && alert.isActive() && state.isToBeShown()){
//            setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
////            setTheme(android.R.style.Theme_Tra);
//        }else{
//            setTheme(android.R.style.Theme_DeviceDefault);
//        }

        //no one wants dialog theme... I worked so hard for it.
        setTheme(android.R.style.Theme_DeviceDefault);
        getActionBar().setDisplayShowHomeEnabled(true);

        setContentView(R.layout.activity_alert_detail);

        // Show the Up button in the action bar.
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = setArguments();

            fragment = new AlertDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.alert_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setArguments();
     }

    @Override
    public void onBackPressed() {
        if(fragment!=null){
            fragment.shutdownSpeech();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Constants.SHOW_MAIN_VIEW_ACTION);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
        WEAUtil.showMessageIfInDebugMode(getApplicationContext(),
                "Reached alert detail onNewIntent, updating intent");
        setArguments();
    }

    private Bundle setArguments() {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.ARG_ITEM_ID,
                getIntent().getStringExtra(Constants.ARG_ITEM_ID));

        arguments.putString(Constants.CONFIG_JSON,
                getIntent().getStringExtra(Constants.CONFIG_JSON));

        arguments.putBoolean("isDialog", getIntent().getBooleanExtra("isDialog",false));
        arguments.putBoolean("isAlertTime", getIntent().getBooleanExtra("isAlertTime", false));
        arguments.putString("feedback_url", getIntent().getStringExtra("feedback_url"));
        return arguments;
    }

}
