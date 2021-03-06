package com.example.luisguzmn.healthcare40.Cowtech54;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luisguzmn.healthcare40.R;

import org.w3c.dom.Text;

import java.util.function.Consumer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.Observer;
import io.reactivex.Observable;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CowTabFragment1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CowTabFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CowTabFragment1 extends Fragment implements View.OnClickListener{

    private final String TAG = CowTabFragment1.class.getSimpleName();

    private static final String DISCONNECTED = "Disconnected";

    //Service and Observer RxJava
    CowService cowService;
    CowService.CowBinder cowBinder;
//    private ContentTestBinding binding; //See https://developer.android.com/topic/libraries/data-binding/
    Disposable disposable; //stackoverflow.com/questions/14695537/android-update-activity-ui-from-service
    Disposable disposable2;
    //   IBinder cowBinder;
    boolean sBound = false;

    // GUI Components
    CheckBox btRadio, sdRadio, lvRadio, obdRadio, gpsCheckbox;

    private TextView sStatusTxt;
    private TextView sDeviceNameTxt;
    private TextView sDeviceMacTxt;
    private TextView sFileTxt;
    private TextView sLatestLocationTxt;
    private TextView sLatTxt;
    private TextView sLonTxt;
    private TextView sSatTxt;

    private Button sStartBtn;
    private Button sStopBtn;
    private Button sBindBtn;
    private Button sUnbindBtn;
    private Button sUpdateBtn;

    private TextView sPairedDeviceTxt;


    //Preferences
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private OnFragmentInteractionListener mListener;

    public CowTabFragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CowTabFragment1.
     */

    public static CowTabFragment1 newInstance() {
        CowTabFragment1 fragment = new CowTabFragment1();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();

    }

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cow_tab1, container, false);
        //Mode radio buttons
        btRadio = (CheckBox) view.findViewById(R.id.modeBtRadio);
        sdRadio = (CheckBox) view.findViewById(R.id.modeSDRadio);
        lvRadio = (CheckBox) view.findViewById(R.id.modeLabviewRadio);
        obdRadio = (CheckBox) view.findViewById(R.id.modeOBDRadio);
        //Gps checkbox
        gpsCheckbox = (CheckBox) view.findViewById(R.id.checkboxGpsTab1);

        //TextViews
        sStatusTxt = (TextView) view.findViewById(R.id.txtStatusTab1);
        sDeviceNameTxt  = (TextView) view.findViewById(R.id.txtDeviceNameTab1);
        sDeviceMacTxt = (TextView) view.findViewById(R.id.txtDeviceMacTab1);
        sFileTxt = (TextView) view.findViewById(R.id.txtFileTab1);
        sLatestLocationTxt  = (TextView) view.findViewById(R.id.txtLatestGPSTab1);
        sLatTxt = (TextView) view.findViewById(R.id.txtLatTab1);
        sLonTxt = (TextView) view.findViewById(R.id.txtLonTab1);
        sSatTxt = (TextView) view.findViewById(R.id.txtSatTab1);

        sStartBtn = (Button) view.findViewById(R.id.cowTab1StartBtn);
        sStopBtn = (Button) view.findViewById(R.id.cowTab1StopBtn);
        sBindBtn = (Button) view.findViewById(R.id.cowTab1BindBtn);
        sUnbindBtn = (Button) view.findViewById(R.id.cowTab1UnbindBtn);

        sUpdateBtn = (Button) view.findViewById(R.id.cowTab1UpdateBtn);





        //DEVICE
        //Mode radio buttons--------

        btRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMode(view, "BT");
            }
        });
        sdRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMode(view, "SD");
            }
        });
        lvRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMode(view, "LV");
            }
        });
        obdRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMode(view, "OBD");
            }
        });
        gpsCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMode(view,"GPS");
            }
        });

        //Mode end radio buttons


        sStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CowService.class);
                getActivity().startService(intent);
                Log.d(TAG, "START BTN CLICKED");
            }
        });
        sStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CowService.class);
                getActivity().stopService(intent);
            }
        });
        sBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sBound) {
                    getActivity().unbindService(sServerConn);
                    //disposable.clear(); // do not send event after activity has been destroyed
                    disposable.dispose();
                    sStatusTxt.setText(DISCONNECTED);
                    sBound=false;
                }
                Intent intent = new Intent(getActivity(),CowService.class);
                getActivity().bindService(intent,sServerConn, Context.BIND_AUTO_CREATE);
            }
        });
        sUnbindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CowService.class);
                if(sBound) {
                    getActivity().unbindService(sServerConn);
                    //disposable.clear(); // do not send event after activity has been destroyed
                    disposable.dispose();
                    sStatusTxt.setText(DISCONNECTED);
                    sBound=false;
                }
            }
        });
        sUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sBound){
                    String filePathStr;
                    filePathStr = cowService.getFileCSV().getFilePathStr();
                    String byteFirst = cowService.btMessageManager.MatIDs.get(2222).getByte1().getFirst().toString();
                    Toast.makeText(getActivity().getApplicationContext(), byteFirst, Toast.LENGTH_LONG).show();
                }
            }
        });

        //Update the paired device textview
        String pairedDeviceMac = prefs.getString("cow_paired_mac", "Not synced");


        return view;
    }

    //Radio buttons ===
    public void radioMode(View view, String mode){
        boolean checked = ((CheckBox) view).isChecked();
        if(cowService!=null) {
            if (checked)
                cowService.modeSend(mode, true);
            else
                cowService.modeSend(mode, false);
        }

        //If there is not connection Do not change the status of the Radio button
        isRadioConnected(view, checked);
    }
    void isRadioConnected(View view, boolean checked){
        if(cowService!=null) {
            if(!cowService.isDeviceConnected()){
                ((CheckBox) view).setChecked(!checked);
                Toast.makeText(getApplicationContext(), "Device not connected", Toast.LENGTH_SHORT).show();
            }
        }else{
            ((CheckBox) view).setChecked(!checked);
            String message = "Service not yet started";
            Log.d(TAG, message);
            Toast.makeText(getApplicationContext(), "Service not started", Toast.LENGTH_SHORT).show();
        }
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((CheckBox) view).isChecked();


        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.modeBtRadio:
                if(checked)
                    cowService.modeSend("BT",true);
                else
                    cowService.modeSend("BT",false);
                //If there is not connection Do not change the status of the Radio button
                isRadioConnected(view, checked);

                break;
            case R.id.modeSDRadio:
                if(checked)
                    cowService.modeSend("SD",true);
                else
                    cowService.modeSend("SD",false);
                isRadioConnected(view, checked);

                break;
            case R.id.modeLabviewRadio:
                if(checked)
                    cowService.modeSend("LV",true);
                else
                    cowService.modeSend("LV",false);
                isRadioConnected(view, checked);

                break;
            case R.id.modeOBDRadio:
                if(checked)
                    cowService.modeSend("OBD",true);
                else
                    cowService.modeSend("OBD",false);
                isRadioConnected(view, checked);

                break;
        }
    }


    protected ServiceConnection sServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CowService.CowBinder binder = (CowService.CowBinder) service;
            cowService = binder.getService();
            //cowService.set
            sBound = true;
            Log.d(TAG, "onServiceConnected");
            //Disposable for the subscriber Called from CowService to update the UI
            disposable = cowService.observeString()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(string -> sStatusTxt.setText(string[0]));
            disposable = cowService.observeString()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(string -> sFileTxt.setText(string[1]));
            disposable = cowService.observeString()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(string -> sLatTxt.setText(string[2]));
            disposable = cowService.observeString()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(string -> sLonTxt.setText(string[3]));
            disposable = cowService.observeString()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(string -> sSatTxt.setText(string[4]));

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sBound = false;
            Log.d(TAG, "onServiceDisconnected");
        }
    };


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Update the paired device textview
        String pairedDeviceMac = prefs.getString("cow_paired_mac", "Not synced");
        //sPairedDeviceTxt.setText("Device: "+ pairedDeviceMac);
    }


    @Override
    public void onClick(View view) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
