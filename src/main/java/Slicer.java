package android.app.printerapp.viewer;

import android.app.Activity;

import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.printer.PrinterFiles;
import android.app.printerapp.printer.PrinterSlicing;
import android.app.printerapp.printer.StateUtils;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class Slicer {

    private static final int DELAY = 3000;


    private byte[] mData = null;
    private List<DataStorage> mDataList = null;

    private Activity mActivity;


    private JSONObject mExtras = new JSONObject();
private Timer mTimer;

    private boolean isRunning;
    private String mLastReference = null;
    private String mOriginalProject = null;
    private ModelPrinter mPrinter;

    public Slicer(Activity activity){

        mActivity = activity;
        isRunning = false;

        cleanTempFolder();
    }


    public void  setData(byte[] data){

        mData = data;

    }

    public void clearExtras(){

        mExtras = new JSONObject();
    }

    public void setExtras(String tag, Object value){


        try {

            if (mExtras.has(tag))
            if (mExtras.get(tag).equals(value)){
                return;
            }
            mExtras.put(tag,value);




        } catch (JSONException e) {
            e.printStackTrace();
        }

        ViewerMainFragment.slicingCallback();
    }


    public void setPrinter(ModelPrinter p){

        mPrinter = p;

    }



    public File createTempFile(){

        File tempFile = null;



        File tempPath =  new File(LibraryController.getParentFolder().getAbsolutePath() + "/temp");

        if (tempPath.mkdir()){



        } else {}

        try {


            int randomInt = new Random().nextInt(100000);

            tempFile = File.createTempFile("tmp",randomInt+".stl", tempPath);
            tempFile.deleteOnExit();


            try{


                 File lastFile = null;
                if (mLastReference!=null){
                    lastFile= new File(mLastReference);
                    lastFile.delete();
                }



            }
            catch (NullPointerException e){

                e.printStackTrace();
            }

            if (tempFile.exists()){

                mLastReference = tempFile.getAbsolutePath();

                DatabaseController.handlePreference(DatabaseController.TAG_RESTORE,"Last",mLastReference, true);

                DatabaseController.handlePreference(DatabaseController.TAG_SLICING, "Last", tempFile.getName(), true);


                StlFile.saveModel(mDataList,null,Slicer.this);

                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(mData);
                fos.getFD().sync();
                fos.close();

            } else {

            }




        } catch (Exception e) {

            e.printStackTrace();
        }


        return  tempFile;

    }


    public void sendTimer(List<DataStorage> data){


        if (isRunning) {


            mTimer.cancel();
            mTimer.purge();
            isRunning = false;
        }


        mTimer = new Timer();
        mDataList = data;
        mTimer.schedule(new SliceTask(),DELAY);
        isRunning = true;

    }

    public String getLastReference(){
        return mLastReference;
    }
    public String getOriginalProject() { return mOriginalProject; }

    public void setOriginalProject(String path) {

        mOriginalProject = path;


    }



    private class SliceTask extends TimerTask {

        @Override
        public void run() {


            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    if (mPrinter!=null){

                        if (mPrinter.getStatus()== StateUtils.STATE_OPERATIONAL){

                            PrinterFiles.deleteFile(mActivity, mPrinter.getAddress(), DatabaseController.getPreference(DatabaseController.TAG_SLICING, "Last"), "/local/");

                            Handler saveHandler = new Handler();
                            saveHandler.post(mSaveRunnable);



                        } else {



                            Toast.makeText(mActivity, R.string.viewer_printer_oq,Toast.LENGTH_LONG).show();

                        }


                    } else {

                        if (DatabaseController.count() > 1){

                        }
                        Toast.makeText(mActivity,R.string.viewer_printer_un,Toast.LENGTH_LONG).show();

                    }
                }
            });

            isRunning = false;


        }
    }
    private Runnable mSaveRunnable = new Runnable() {
        @Override
        public void run() {

            File mFile = createTempFile();
            PrinterSlicing.sliceCommand(mActivity, mPrinter.getAddress(), mFile, mExtras);
            ViewerMainFragment.showProgressBar(StateUtils.SLICER_UPLOAD, 0);


        }
    };

    private void cleanTempFolder(){

        File file = new File(LibraryController.getParentFolder() + "/temp/");

        LibraryController.deleteFiles(file);
    }

}
