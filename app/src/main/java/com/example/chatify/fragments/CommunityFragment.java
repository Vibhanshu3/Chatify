package com.example.chatify.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.adapters.PostAdapter;
import com.example.chatify.model.Post;
import com.example.chatify.model.User;
import com.example.chatify.presenter.CommunityPresenter;
import com.example.chatify.utils.AppSharedPreferences;
import com.example.chatify.view.CommunityView;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static com.example.chatify.utils.AppConst.DB_POSTS_KEY;

//TODO: add image
//TODO: add groups or category
//TODO: add tags
//FixMe: floating action button location

public class CommunityFragment extends Fragment implements CommunityView, View.OnClickListener {
    @BindView(R.id.recycler_view)
    RecyclerView postList;

    @BindView(R.id.loader)
    ProgressBar loader;

    private Context context;
    private Dialog dialog;
    private ProgressBar dialogLoader;
    private EditText dialogTitle;
    private EditText dialogDescription;

    private Unbinder unbinder;

    private CommunityPresenter presenter;

    public CommunityFragment() {
    }

    public static CommunityFragment newInstance() {
        return new CommunityFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    private void init() {
        if (getContext() != null) {
            context = getContext();
        }

        presenter = new CommunityPresenter(this);

        postList.setLayoutManager(new LinearLayoutManager(context));

        PostAdapter adapter = new PostAdapter(new FirebaseRecyclerOptions
                .Builder<Post>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child(DB_POSTS_KEY), Post.class)
                .build());

        postList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dialog.dismiss();
                break;
            case R.id.create:
                dialogLoader.setVisibility(VISIBLE);
                Log.d("test123", String.format("%1$tA %1$tb %1$td %1$tY %1$tI:%1$tM %1$Tp", Calendar.getInstance()));
                presenter.createPost(
                        AppSharedPreferences.getUser(context),
                        dialogTitle.getText().toString(),
                        dialogDescription.getText().toString(),
                        String.format(Locale.getDefault(),"%1$tA %1$tb %1$td %1$tY", Calendar.getInstance()),
                        String.format("%1$tI:%1$tM %1$Tp", Calendar.getInstance()),
                        null);
                break;
        }
    }

    @Override
    public void postError(int error) {
        dialogLoader.setVisibility(GONE);
        Toast.makeText(context, error, LENGTH_LONG).show();
    }

    @Override
    public void postSuccess(User user) {
        dialog.dismiss();
        Toast.makeText(context, "Success", LENGTH_LONG).show();
        AppSharedPreferences.setUser(context, user);
    }

    @OnClick(R.id.community_new_post)
    void newPost() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_new_post);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        dialog.findViewById(R.id.cancel).setOnClickListener(this);
        dialog.findViewById(R.id.create).setOnClickListener(this);

        dialogLoader = dialog.findViewById(R.id.loader);
        dialogTitle = dialog.findViewById(R.id.dialog_post_title);
        dialogDescription = dialog.findViewById(R.id.dialog_post_description);

        dialog.show();
    }
}
