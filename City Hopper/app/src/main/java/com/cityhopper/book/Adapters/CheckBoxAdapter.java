package com.cityhopper.book.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cityhopper.book.R;

import java.util.ArrayList;

public class CheckBoxAdapter extends BaseAdapter {
    private Context mContext;
    private String[] cArrayName;
    private boolean isCuisine;
    private ArrayList<String> items;

    public CheckBoxAdapter(Context c, @NonNull ArrayList<String> sCheckItem, @NonNull String[] types, boolean isCuisinne) {
        mContext = c;
        cArrayName = types;
        items = sCheckItem;
        isCuisine = isCuisinne;
    }

    public int getCount() {
        return cArrayName.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        CheckBox checkBox;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            checkBox = new CheckBox(mContext);

            checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            checkBox.setText(cArrayName[position]);
            checkBox.setGravity(Gravity.CENTER | Gravity.LEFT);
            checkBox.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
//                int states[][] = {{android.R.attr.state_checked}, {}};
//                int colors[] = {getResources().getColor(R.color.colorControlActivated), getResources().getColor(R.color.colorControlNormal)};
//                CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
        } else {
            checkBox = (CheckBox) convertView;
        }

        if (isCuisine) {
            if (items.get(position).compareTo("NA") != 0) {
                checkBox.setChecked(true);
            }
        } else {
            if (items.get(position).compareTo("NA") != 0) {
                checkBox.setChecked(true);
            }
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isCuisine) {
                        items.set(position, cArrayName[position]);
                    } else {
                        items.set(position, cArrayName[position]);
                    }
                } else {
                    if (isCuisine) {
                        items.set(position, "NA");
                    } else {
                        items.set(position, "NA");
                    }
                }
            }
        });

        return checkBox;
    }
}
