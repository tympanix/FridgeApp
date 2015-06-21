package gruppe3.dtu02128.fridgeapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Morten on 18-Jun-15.
 * Cursor adapter to display elements from
 * the database in a ListView
 */
public class MyCursorAdapter extends CursorAdapter {
    Context mContext;
    ItemDatabaseHelper dbhelp;
    private DateTime openExpireDate;

    public MyCursorAdapter(Context context, Cursor c,ItemDatabaseHelper dbhelp) {
        super(context, c,0);
        mContext = context;
        this.dbhelp = dbhelp;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Inflate for a single view
        Log.i("test","new view");
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Configure the view for each element
        Log.i("test", "Binding view");
        final String id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
        final int openExpire = cursor.getInt(cursor.getColumnIndexOrThrow("openexpire"));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

        //Get expiry date in millis
        Long millis = cursor.getLong(cursor.getColumnIndexOrThrow("dateexpire"));
        Log.i("expiry",millis.toString());
        Long addedmillis = cursor.getLong(cursor.getColumnIndexOrThrow("dateadded"));

        //Set title of product
        TextView txt = (TextView) view.findViewById(R.id.product_title);
        txt.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));

        // Alternate background colors
        View bg = view.findViewById(R.id.linlayout);
        if (cursor.getPosition() % 2 == 1) {
            bg.setBackgroundColor(view.getResources().getColor(R.color.abc_primary_text_material_dark));
        } else {
            bg.setBackgroundColor(Color.rgb(247, 247 ,247));
        }

        //Set on click for the linear layouts
        LinearLayout linlay = (LinearLayout) view.findViewById(R.id.clickme);
        linlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: START ACTIVITY FOR VIEWING MULTIPLE ITEMS
                mContext.startActivity(new Intent(mContext, ItemViewActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("item_id", id));
            }
        });

        //Get days until product expires
        //Type it and set the progress bar
        DateTime dateExpire = new DateTime(millis);
        DateTime dateAdded = new DateTime(addedmillis);
        DateTime today = new DateTime();

        Log.i("dates",name+" Current date " + today.toString());
        Log.i("dates", name + " Expiry date " + dateExpire.toString());

        //When opened, add number of days until expiry
        //If this date is after the actual expire date, use the actual expire date
        float daysToDateExpire = Days.daysBetween(today.toLocalDate(),dateExpire.toLocalDate()).getDays();

        //DateTime
        openExpireDate = today.plusDays(openExpire);
        if(openExpireDate.isAfter(dateExpire)) {
            openExpireDate = dateExpire;
        }

        //int daysToDateOpenExpire = Days.daysBetween(today.toLocalDate(),openExpireDate.toLocalDate()).getDays();



        TextView txt2 = (TextView) view.findViewById(R.id.text_until_expire);

        //Set progress bar
        ProgressBar progg = (ProgressBar) view.findViewById(R.id.progress);

        float daysFromadd = Days.daysBetween(dateAdded.toLocalDate(),dateExpire.toLocalDate()).getDays();

        Log.i("dates",daysFromadd + " " + daysToDateExpire + " " + (daysFromadd-daysToDateExpire)/daysFromadd);

        Log.i("dates",name + " Setting progress to " + (daysFromadd-daysToDateExpire)/daysFromadd*100);
        progg.setProgress(Math.round((daysFromadd-daysToDateExpire)/daysFromadd*100));

        //Configure check box and listeners
        CheckBox check = (CheckBox) view.findViewById(R.id.open_check);

        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Confirm the opened state of the item by the user
                int openstate = isChecked? 1 : 0;
                getDateExpire();
                Log.i("test", "changing checked state for" + id);
                ContentValues cont = new ContentValues();
                cont.put(ItemDatabaseHelper.OPEN,openstate);
                cont.put(ItemDatabaseHelper.EXPIRE_DATE,openExpireDate.getMillis());

                //Update opened state in database and update expiry date to reflect daysOpen
                dbhelp.getWritableDatabase().update(ItemDatabaseHelper.TABLE_NAME, cont, ItemDatabaseHelper._ID + "=?", new String[]{id});
                changeCursor(dbhelp.getWritableDatabase().rawQuery("SELECT  * FROM " + ItemDatabaseHelper.TABLE_NAME, null));
            }
        });

        int open = cursor.getInt(cursor.getColumnIndexOrThrow("open"));
        if(open == 1) {
            check.setChecked(true);
//            if(daysToDateOpenExpire > daysToDateExpire) {
//                txt2.setText(String.valueOf(daysToDateExpire));
//            } else {
//                txt2.setText(String.valueOf(daysToDateOpenExpire));
//            }
            txt2.setText(String.valueOf(Math.round(daysToDateExpire)));
            check.setClickable(false);

        } else {
            check.setChecked(false);
            txt2.setText(String.valueOf(Math.round(daysToDateExpire)));
        }

        ImageButton butt = (ImageButton) view.findViewById(R.id.remove_button);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Confirm the deletion of the item by the user via dialog

                //

                Log.i("test", id + " was clicked");
                dbhelp.getWritableDatabase().delete(ItemDatabaseHelper.TABLE_NAME, ItemDatabaseHelper._ID + "=?",
                        new String[]{id});
                changeCursor(dbhelp.getWritableDatabase().rawQuery("SELECT  * FROM " + ItemDatabaseHelper.TABLE_NAME, null));
            }
        });

    }

    public DateTime getDateExpire() {
        return this.openExpireDate;
    }
}