package com.example.chatify.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.adapters.CommentAdapter;
import com.example.chatify.adapters.PostAdapter;
import com.example.chatify.model.Comment;
import com.example.chatify.model.Post;
import com.example.chatify.model.User;
import com.example.chatify.presenter.CommunityPresenter;
import com.example.chatify.utils.AppSharedPreferences;
import com.example.chatify.view.CommunityView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.chatify.utils.AppConst.DB_POSTS_KEY;
import static com.example.chatify.utils.AppConst.DB_POSTS_LIKE_COUNT;
import static com.example.chatify.utils.AppConst.LOG_COMMUNITY;
import static com.example.chatify.utils.AppConst.REQUEST_CODE_PERMISSION_STORAGE;

//TODO: add comments
//TODO: add groups or category
//TODO: add tags
//FixMe: call addImage after permission granted
//FixMe: (suggestion) comment can have valueEventListener

public class CommunityFragment extends Fragment implements CommunityView, View.OnClickListener, PostAdapter.ClickListener, ValueEventListener {
    @BindView(R.id.recycler_view)
    RecyclerView postList;

    @BindView(R.id.loader)
    ProgressBar loader;

    @BindView(R.id.community_trending_post)
    Button trendingButton;

    private boolean trending = false;

    private ProgressBar dialogLoader;

    private EditText dialogTitle;
    private EditText dialogDescription;

    private ImageView dialogImage;

    private Activity context;
    private Dialog dialog;
    private Uri uri = null;

    private Unbinder unbinder;
    private DatabaseReference postsRef;
    private Query query;

    private CommunityPresenter presenter;
    private PostAdapter adapter;
    private CommentAdapter commentAdapter;

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
        if (getActivity() != null) {
            context = getActivity();
        }

        presenter = new CommunityPresenter(this);
        postsRef = FirebaseDatabase.getInstance().getReference().child(DB_POSTS_KEY);
        adapter = new PostAdapter(this);

        query = postsRef;
        query.addValueEventListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        postList.setLayoutManager(linearLayoutManager);
        postList.setAdapter(adapter);
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
                presenter.createPost(
                        AppSharedPreferences.getUser(context),
                        dialogTitle.getText().toString(),
                        dialogDescription.getText().toString(),
                        String.format(Locale.getDefault(),"%1$tA %1$tb %1$td %1$tY", Calendar.getInstance()),
                        String.format("%1$tI:%1$tM %1$Tp", Calendar.getInstance()),
                        uri);
                break;
            case R.id.dialog_post_add_image:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_STORAGE);
                    } else {
                        addImage();
                    }
                } else {
                    addImage();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addImage();
            } else {
                Toast.makeText(context, "Permission denied", LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (result != null) {
                    uri = result.getUri();
                    dialogImage.setVisibility(VISIBLE);
                    Picasso.get().load(uri).into(dialogImage);
                }
            }
        }
    }

    @Override
    public void hideLoader() {
        dialogLoader.setVisibility(GONE);
    }

    @Override
    public void error(int error) {
        Toast.makeText(context, error, LENGTH_LONG).show();
    }

    @Override
    public void postSuccess(User user) {
        dialog.dismiss();
        Toast.makeText(context, "Success", LENGTH_LONG).show();
        AppSharedPreferences.setUser(context, user);
    }

    @Override
    public void commentSuccess(Comment comment) {
        hideLoader();
        dialogTitle.setText("");
        commentAdapter.add(comment);
    }

    @Override
    public void like(String postKey) {
        presenter.like(postKey);
    }

    @Override
    public void comments(String postKey, List<Comment> comments) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_comments);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        commentAdapter = new CommentAdapter(comments);
        RecyclerView commentList = dialog.findViewById(R.id.recycler_view);
        dialogTitle = dialog.findViewById(R.id.dialog_comment);
        dialogLoader = dialog.findViewById(R.id.loader);

        dialogTitle.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                dialogLoader.setVisibility(VISIBLE);
                presenter.comment(
                        postKey,
                        String.format(Locale.getDefault(),"%1$tA %1$tb %1$td %1$tY", Calendar.getInstance()),
                        String.format("%1$tI:%1$tM %1$Tp", Calendar.getInstance()),
                        dialogTitle.getText().toString(),
                        AppSharedPreferences.getUser(context)
                );
            }
            return false;
        });

        commentList.setLayoutManager(new LinearLayoutManager(context));
        commentList.setAdapter(commentAdapter);

        dialog.show();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            List<Post> posts = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    post.setKey(snapshot.getKey());
                    posts.add(post);
                }
            }
            adapter.update(posts);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Toast.makeText(context, R.string.error_something_wrong, LENGTH_SHORT).show();
        Log.e(LOG_COMMUNITY, databaseError.getDetails());
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
        dialog.findViewById(R.id.dialog_post_add_image).setOnClickListener(this);

        dialogLoader = dialog.findViewById(R.id.loader);
        dialogTitle = dialog.findViewById(R.id.dialog_post_title);
        dialogDescription = dialog.findViewById(R.id.dialog_post_description);
        dialogImage = dialog.findViewById(R.id.dialog_post_image);

        dialog.show();
    }

    @OnClick(R.id.community_trending_post)
    void trendingPost() {
        if (trending) {
            trending = false;
            query.removeEventListener(this);
            query = postsRef;
            query.addValueEventListener(this);
            trendingButton.setText(R.string.trending);
        } else {
            trending = true;
            query.removeEventListener(this);
            query = postsRef.orderByChild(DB_POSTS_LIKE_COUNT);
            query.addValueEventListener(this);
            trendingButton.setText(R.string.latest);
        }
    }

    private void addImage() {
        CropImage.activity().start(context);
    }
}
