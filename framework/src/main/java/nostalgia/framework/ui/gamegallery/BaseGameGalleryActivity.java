package nostalgia.framework.ui.gamegallery;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nostalgia.framework.Emulator;
import nostalgia.framework.R;
import nostalgia.framework.base.EmulatorActivity;
import nostalgia.framework.remote.ControllableActivity;
import nostalgia.framework.ui.gamegallery.RomsFinder.OnRomsFinderListener;
import nostalgia.framework.utils.DatabaseHelper;
import nostalgia.framework.utils.DialogUtils;
import nostalgia.framework.utils.FileUtils;
import nostalgia.framework.utils.NLog;

abstract public class BaseGameGalleryActivity extends ControllableActivity
        implements OnRomsFinderListener {

    private static final String TAG = "BaseGameGalleryActivity";

    protected Set<String> exts;
    protected Set<String> inZipExts;
    protected boolean reloadGames = true;
    protected boolean reloading = false;
    private RomsFinder romsFinder = null;
    private DatabaseHelper dbHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashSet<String> exts = new HashSet<>(getRomExtensions());
        exts.addAll(getArchiveExtensions());
        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("android50comp", Context.MODE_PRIVATE);
        String androidVersion = Build.VERSION.RELEASE;

        if (!pref.getString("androidVersion", "").equals(androidVersion)) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.onUpgrade(db, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
            db.close();
            Editor editor = pref.edit();
            editor.putString("androidVersion", androidVersion);
            editor.apply();
            NLog.i(TAG, "Reinit DB " + androidVersion);
        }
        reloadGames = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!FileUtils.isSDCardRWMounted()) {
            showSDCardFailed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (romsFinder != null) {
            romsFinder.stopSearch();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void reloadGames(boolean searchNew, File selectedFolder) {
        if (romsFinder == null) {
            reloadGames = false;
            reloading = searchNew;
            romsFinder = new RomsFinder(exts, inZipExts, this, this, searchNew, selectedFolder);
            PermissionUtils.permission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .theme(new PermissionUtils.ThemeCallback() {
                        @Override
                        public void onActivityCreate(Activity activity) {
                            BarUtils.setStatusBarAlpha(activity, 0);
                        }
                    })
                    .callback(new PermissionUtils.FullCallback() {
                        @Override
                        public void onGranted(List<String> list) {
                            romsFinder.start();
                        }

                        @Override
                        public void onDenied(List<String> list, List<String> list1) {

                        }
                    }).request();
        }
    }

    @Override
    public void onRomsFinderFoundGamesInCache(ArrayList<GameDescription> oldRoms) {
        setLastGames(oldRoms);
    }

    @Override
    public void onRomsFinderNewGames(ArrayList<GameDescription> roms) {
        setNewGames(roms);
    }

    @Override
    public void onRomsFinderEnd(boolean searchNew) {
        romsFinder = null;
        reloading = false;
    }

    @Override
    public void onRomsFinderCancel(boolean searchNew) {
        romsFinder = null;
        reloading = false;
    }

    protected void stopRomsFinding() {
        if (romsFinder != null) {
            romsFinder.stopSearch();
        }
    }

    public void showSDCardFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Builder builder = new Builder(BaseGameGalleryActivity.this);
                builder.setTitle(R.string.error);
                builder.setMessage(R.string.gallery_sd_card_not_mounted);
                builder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                builder.setPositiveButton(R.string.exit, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                DialogUtils.show(builder.create(), true);
            }
        });
    }

    public abstract Class<? extends EmulatorActivity> getEmulatorActivityClass();

    abstract public void setLastGames(ArrayList<GameDescription> games);

    abstract public void setNewGames(ArrayList<GameDescription> games);

    abstract protected Set<String> getRomExtensions();

    public abstract Emulator getEmulatorInstance();

    protected Set<String> getArchiveExtensions() {
        HashSet<String> set = new HashSet<>();
        set.add("zip");
        return set;
    }

}
