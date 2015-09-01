package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.rest.BackendService;
import com.attilapalfi.exceptional.services.ExceptionFactory;
import com.attilapalfi.exceptional.services.GpsService;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.ui.main.Constants;
import com.attilapalfi.exceptional.ui.main.MainActivity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExceptionTypeFragment extends Fragment {
    private static int instanceCounter = 0;

    private List<ExceptionType> exceptionTypes;
    private RecyclerView recyclerView;
    private ExceptionTypeAdapter typeAdapter;

    public ExceptionTypeFragment() {
        initExceptionTypes();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initTypeAdapter();
        View view = initRecyclerView(inflater, container);
        typeAdapter.notifyDataSetChanged();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initExceptionTypes() {
        int index = instanceCounter++ % ExceptionTypeManager.getInstance().getExceptionTypes().size();
        List<String> types = new ArrayList<>(ExceptionTypeManager.getInstance().getExceptionTypes());
        String typeOfThis = types.get(index);
        exceptionTypes = ExceptionTypeManager.getInstance().getExceptionTypesByName(typeOfThis);
    }

    private void initTypeAdapter() {
        if (exceptionTypes == null) {
            exceptionTypes = new ArrayList<>();
        }
        typeAdapter = new ExceptionTypeAdapter(getActivity(), exceptionTypes);
    }

    @NonNull
    private View initRecyclerView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_exception_type, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.exception_type_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(typeAdapter);
        typeAdapter.setRecyclerView(recyclerView);
        return view;
    }

    public static class ExceptionTypeAdapter extends RecyclerView.Adapter<ExceptionTypeAdapter.RowViewHolder> {
        private RecyclerView recyclerView;
        private Activity activity;
        private List<ExceptionType> values;
        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GpsService.getInstance().canGetLocation()) {
                    Exception exception = createException(view);
                    BackendService.getInstance().throwException(exception);
                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity.getApplicationContext(), R.string.can_throw_location_pls,
                            Toast.LENGTH_LONG).show();
                }
            }

            private Exception createException(View view) {
                int itemPosition = recyclerView.getChildPosition(view);
                ExceptionType exceptionType = values.get(itemPosition);
                BigInteger friendId = new BigInteger(activity.getIntent().getStringExtra(Constants.FRIEND_ID));
                Exception exception = ExceptionFactory.createExceptionWithType(
                        exceptionType,
                        FriendsManager.getInstance().getYourself().getId(),
                        friendId);
                setLocationForException(exception);
                return exception;
            }

            private void setLocationForException(Exception exception) {
                Location location = GpsService.getInstance().getLocation();
                exception.setLatitude(location.getLatitude());
                exception.setLongitude(location.getLongitude());
            }
        };

        public ExceptionTypeAdapter(Activity activity, List<ExceptionType> values) {
            this.activity = activity;
            this.values = values;
        }

        @Override
        public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exception_type_row_layout, parent, false);
            view.setOnClickListener(onClickListener);
            return new RowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RowViewHolder holder, int position) {
            holder.bindRow(values.get(position));
        }

        @Override
        public int getItemCount() {
            return values != null? values.size() : 0;
        }

        public void setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        public static class RowViewHolder extends RecyclerView.ViewHolder {
            private TextView shortNameView;
            private TextView fullNameView;
            private TextView descriptionView;

            public RowViewHolder(View itemView) {
                super(itemView);
                shortNameView = (TextView) itemView.findViewById(R.id.type_short_name_text);
                fullNameView = (TextView) itemView.findViewById(R.id.type_full_name_text);
                descriptionView = (TextView) itemView.findViewById(R.id.type_description_text);
            }

            public void bindRow(ExceptionType exceptionType) {
                shortNameView.setText(exceptionType.getShortName());
                String fullName = getFullName(exceptionType);
                fullNameView.setText(fullName);
                descriptionView.setText(exceptionType.getDescription());
            }

            @NonNull
            private String getFullName(ExceptionType exceptionType) {
                String fullNameParts[] = exceptionType.fullName().split("\\.");
                String fullName = "";
                for (String part : fullNameParts) {
                    fullName += part + "." + "\n";
                }
                fullName = fullName.substring(0, fullName.length() - 2);
                return fullName;
            }
        }
    }
}
