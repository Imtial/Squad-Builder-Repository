package com.example.squadbuilderrepository;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.squadbuilderrepository.Database.Player;
import com.example.squadbuilderrepository.Utils.ImageUtil;
import com.example.squadbuilderrepository.Utils.PlayerUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AddPlayerDialog extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView mPlayerImage;
    private TextView mNameLabelTextView;
    private EditText mEditTextName;
    private TextView mPositionLabelTextView;
    private Spinner mPositionSpinner;
    private EditText mEditTextPrice;
    private EditText mEditTextRating;
    private Button mButtonConfirm;
    private ProgressBar mProgressBar;

    private Uri mImageUri;
    private String mPlayerName;
    private String mPlayerPosition;
    private int mPositionIndex;
    private double mPlayerPrice;
    private double mPlayerRating;

    private FirebaseFirestore mDB;
    private StorageReference mStorageRef;

    private DataSetChangeListener mListener;

    public AddPlayerDialog() {
        mDB = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("playerPictures");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_add_player, container, false);

        mPlayerImage = rootView.findViewById(R.id.iv_player_picture);
        mNameLabelTextView = rootView.findViewById(R.id.tv_name_label);
        mEditTextName = rootView.findViewById(R.id.et_player_name);
        mPositionLabelTextView = rootView.findViewById(R.id.tv_position_label);
        mPositionSpinner = rootView.findViewById(R.id.spinner_position);
        mEditTextPrice = rootView.findViewById(R.id.et_player_price);
        mEditTextRating = rootView.findViewById(R.id.et_player_rating);
        mButtonConfirm = rootView.findViewById(R.id.btn_confirm);
        mProgressBar = rootView.findViewById(R.id.progressBar);

        mPlayerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mPositionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPlayerPosition = parent.getSelectedItem().toString();
                mPositionIndex = parent.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerName = mEditTextName.getText().toString().trim();
                String priceString = mEditTextPrice.getText().toString().trim();
                String ratingString = mEditTextRating.getText().toString().trim();

                if (mImageUri == null) {
                    Toast.makeText(getContext(), "Provide an image.", Toast.LENGTH_SHORT).show();
                }
                if (mPlayerName.length() == 0) {
                    mEditTextName.setError("Name must be provided");
                }
                if (mPlayerName.length() > 40) {
                    mEditTextName.setError("Name must contain less than 40 characters");
                }
                if (priceString.length() == 0) {
                    mEditTextPrice.setError("This field cannot be empty.");
                }
                if (ratingString.length() == 0) {
                    mEditTextRating.setError("This field cannot be empty.");
                }

                if (mImageUri != null && mPlayerName.length() > 0 && mPlayerName.length() <= 40
                        && priceString.length() > 0 && ratingString.length() > 0) {
                    mPlayerPrice = Double.parseDouble(priceString);
                    mPlayerRating = Double.parseDouble(ratingString);

                    final Player currentPlayer = new Player(mPlayerName, mPlayerPosition,
                            mPlayerPrice, mPlayerRating, mImageUri.toString());

                    DocumentReference docRef = mDB.collection("players")
                            .document(PlayerUtil.getTypeFromPosition(currentPlayer.getPosition()))
                            .collection("info").document();
                    String uploadId = docRef.getId();
                    currentPlayer.setPid(uploadId);

                    insertIntoLocalDB(currentPlayer);
//                    uploadFile(currentPlayer);
                }
            }
        });
        return rootView;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadFile (final Player currentPlayer) {

        StorageReference imageRef = mStorageRef.child(currentPlayer.getPid()
                + "." + ImageUtil.getExtensionFromUri(getContext(), mImageUri));

        imageRef.putFile(mImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        currentPlayer.setDownloadUrl(uri.toString());
                                        insertIntoCloud(currentPlayer);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        mButtonConfirm.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        mButtonConfirm.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()
                            / taskSnapshot.getTotalByteCount());
                        mProgressBar.setProgress((int) progress);
                    }
                });
    }

    private void insertIntoCloud(final Player currentPlayer) {
        DocumentReference docRef = mDB.collection("players").document(PlayerUtil.getTypeFromPosition(currentPlayer.getPosition()))
                .collection("info").document(currentPlayer.getPid());
        docRef.set(currentPlayer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Player Insertion Successful", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().beginTransaction().remove(AddPlayerDialog.this).commit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void insertIntoLocalDB (Player currentPlayer) {
        if (currentPlayer.getPid() != null && currentPlayer.getPid().length() > 0
                && mListener != null) {
            mListener.insertItem(currentPlayer);
            getActivity().getSupportFragmentManager().beginTransaction().remove(AddPlayerDialog.this).commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            mImageUri = data.getData();
            getActivity().getContentResolver().takePersistableUriPermission(mImageUri,
                    data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Picasso.get()
                    .load(mImageUri)
                    .resize(256, 256)
                    .centerCrop()
                    .into(mPlayerImage);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("name", mPlayerName);
        outState.putInt("position", mPositionIndex);
        if (mImageUri != null) outState.putString("uri", mImageUri.toString());
        outState.putString("price", mEditTextPrice.getText().toString());
        outState.putString("rating", mEditTextRating.getText().toString());
    }

    public interface DataSetChangeListener {
        void insertItem(Player player);
    }

    public void setDataSetChangeListener (DataSetChangeListener dataSetChangeListener) {
        mListener = dataSetChangeListener;
    }
}
