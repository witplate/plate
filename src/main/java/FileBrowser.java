package android.app.printerapp.viewer;

import android.app.Activity;
import android.app.Dialog;
import android.app.printerapp.R;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.library.LibraryModelCreation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class FileBrowser extends Activity  {
	private static Context mContext;
	private static File mCurrentPath;
	private static File[] mDialogFileList;
	private static int mSelectedIndex = -1;
	private static OnFileListDialogListener mFileListListener;
	private static OnClickListener mClickListener;
	private static DialogInterface.OnKeyListener mKeyListener;

	private static String mTitle;
	private static String mExtStl;
	private static String mExtGcode;

	public static void show(String path) {
		try {
			mCurrentPath = new File(path);
			mDialogFileList = new File(path).listFiles();
			if (mDialogFileList == null) {
				// NG
				if (mFileListListener != null) {
					mFileListListener.onClickFileList(null);
				}
			} else {
				List<String> list = new ArrayList<String>();
				List<File> fileList = new ArrayList<File>();
				// create file list
				Arrays.sort(mDialogFileList, new Comparator<File>() {

					@Override
					public int compare(File object1, File object2) {
						return object1.getName().toLowerCase(Locale.US).compareTo(object2.getName().toLowerCase(Locale.US));
					}
				});
				for (File file : mDialogFileList) {
					if (!file.canRead()) {
						continue;
					}
					String name = null;
					if (file.isDirectory()) {
						if (!file.getName().startsWith(".")) {
							name = file.getName() + File.separator;
						}
					} else {
						if (LibraryController.hasExtension(0,file.getName())) {
							name = file.getName();
						}
						
						if (LibraryController.hasExtension(1,file.getName())) {
							name = file.getName();
						}
					}

					if (name != null) {


                        if ((LibraryController.hasExtension(0,name))||(LibraryController.hasExtension(1,name))
                                ||file.isDirectory()){
                            list.add(name);
                            fileList.add(file);
                        }

					}
				}

                final Dialog dialog;

                LayoutInflater li = LayoutInflater.from(mContext);
                View view = li.inflate(R.layout.dialog_list, null);

                final uk.co.androidalliance.edgeeffectoverride.ListView listView =
                        (uk.co.androidalliance.edgeeffectoverride.ListView) view.findViewById(R.id.dialog_list_listview);
                listView.setSelector(mContext.getResources().getDrawable(R.drawable.list_selector));
                TextView emptyText = (TextView) view.findViewById(R.id.dialog_list_emptyview);
                listView.setEmptyView(emptyText);

                FileBrowserAdapter fileBrowserAdapter = new FileBrowserAdapter(mContext, list, fileList);
                listView.setAdapter(fileBrowserAdapter);
                listView.setDivider(null);

                mDialogFileList = fileList.toArray(mDialogFileList);

                MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(mContext)
                        .title(mTitle)
                        .customView(view, false)
                        .negativeText(R.string.annuler)
                        .negativeColorRes(R.color.theme_accent_1)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                mFileListListener.onClickFileList(null);
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.keyListener(mKeyListener);

                dialog = dialogBuilder.build();
                dialog.show();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        mSelectedIndex = position;
                        if ((mDialogFileList == null) || (mFileListListener == null)) {
                        } else {
                            File file = mDialogFileList[position];

                            if (file.isDirectory()) {

                                show(file.getAbsolutePath());
                            } else {

                                mFileListListener.onClickFileList(file);
                            }
                        }
                        dialog.dismiss();
                    }
                });
			}
		} catch (SecurityException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public interface OnFileListDialogListener {
		public void onClickFileList(File file);
	}

}