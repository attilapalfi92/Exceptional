package com.attilapalf.exceptional.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.attilapalf.exceptional.R;

/**
 */
public class ExceptionsFragment extends ListFragment {

    private String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
            "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
            "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
            "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
            "Android", "iPhone", "WindowsMobile" };



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new MyAdapter(getActivity(), values);
        setListAdapter(adapter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exceptions, null);
    }



    private class MyAdapter extends ArrayAdapter<String> {
        private Context context;
        private String[] values;

        public MyAdapter(Context context, String[] values) {
            super(context, R.layout.exc_row_layout, values);

            this.context = context;
            this.values = values;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.exc_row_layout, parent, false);
            TextView nameView = (TextView) rowView.findViewById(R.id.excName);
            TextView descView = (TextView) rowView.findViewById(R.id.excDesc);

            nameView.setTextSize(20);
            descView.setTextSize(15);
            nameView.setText("csirke.plukya.lethal.\n" + values[position] + "HurkatöltőException");
            descView.setText(values[position] + "HurkatöltőException akkor jön, amikor elfelejted" +
                    " az első szóban szereplő OS hurkáját feltölteni kaláccsal.");

            return rowView;
        }
    }

}
