package com.example.squadbuilderrepository.Utils;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.squadbuilderrepository.Database.Player;
import com.example.squadbuilderrepository.Database.PlayerRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSyncUtil {

    private static String [] NON_EXISTENT_NODES = {"GK", "DEF", "MID", "FW"};

    public static void syncLocalWithCloud (final Application application) {
        Thread dbOperationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final PlayerRepository repository = PlayerRepository.getInstance(application);
                final List<String> playerIds = repository.getStaticPlayerIDs();
                CollectionReference colRef = FirebaseFirestore.getInstance().collection("players");
                final CollectionReference[] nodeRefs = new CollectionReference[NON_EXISTENT_NODES.length];
                for (int i = 0; i < nodeRefs.length; i++) {
                    nodeRefs[i] = colRef.document(NON_EXISTENT_NODES[i]).collection("info");
                    nodeRefs[i].get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot docSnap : queryDocumentSnapshots) {
                                final String key = docSnap.getId();
                                if (!playerIds.contains(key)) {
                                    // download the image
                                    // get image uri
                                    final Player player = docSnap.toObject(Player.class);
                                    final StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(player.getDownloadUrl());
                                    ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                        @Override
                                        public void onSuccess(StorageMetadata storageMetadata) {
                                            final String fileName = storageMetadata.getName();
//                                            String contentType = storageMetadata.getContentType();
//                                            String ext = contentType.equalsIgnoreCase("image/png") ? "png" : "jpg";

                                            final File file = new File(application.getExternalFilesDir(null), fileName);
                                            final Uri uri = Uri.fromFile(file);
                                            ref.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Log.d("SYNCING", fileName+": " + uri);
                                                    // insert the entry
                                                    player.setImageUri(uri.toString());
                                                    player.setPid(key);
                                                    repository.insert(player);
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
        dbOperationThread.start();
    }

    public static void syncCloudWithLocal (final Application application) {

        Thread dbOperationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                PlayerRepository repository = PlayerRepository.getInstance(application);
                final List<Player> localPlayerList = repository.getStaticPlayerList();
                final List<String> localPlayerIds = new ArrayList<>();
                for (Player p : localPlayerList) localPlayerIds.add(p.getPid());

                final CollectionReference[] collectionRefs = new CollectionReference[NON_EXISTENT_NODES.length];
                for (int i = 0; i < collectionRefs.length; i++) {
                    collectionRefs[i] = FirebaseFirestore.getInstance().collection("players")
                            .document(NON_EXISTENT_NODES[i]).collection("info");
                    final int finalI = i;
                    collectionRefs[i].get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (final QueryDocumentSnapshot docSnap : queryDocumentSnapshots) {
                                if (!localPlayerIds.contains(docSnap.getId())) {
                                    String downloadURL = docSnap.toObject(Player.class).getDownloadUrl();
                                    if (downloadURL != null) {
                                        FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(downloadURL)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        collectionRefs[finalI].document(docSnap.getId()).delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d("SYNCING", "Entry with id " + docSnap.getId() + " deleted");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d("SYNCING", e.getMessage() + "");
                                                                    }
                                                                });
                                                        Log.d("SYNCING", "Image with id " + docSnap.getId() + " deleted.");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("SYNCING", e.getMessage() + "");
                                                    }
                                                });
                                    }
                                } else {
                                    int index = localPlayerIds.indexOf(docSnap.getId());
                                    localPlayerIds.remove(index);
                                    localPlayerList.remove(index);
                                }
                            }
                        }
                    });
                }
                for (final Player p : localPlayerList) {
                    final DocumentReference docRef = FirebaseFirestore.getInstance().collection("players")
                            .document(PlayerUtil.getTypeFromPosition(p.getPosition())).collection("info")
                            .document(p.getPid());
                    Uri uri = Uri.parse(p.getImageUri());

                    String ext = ImageUtil.getExtensionFromUri(application.getApplicationContext(), uri);
                    if (!ext.equalsIgnoreCase("jpg") && !ext.equalsIgnoreCase("jpeg"))
                        ext = "png";

                    final String fileName = p.getPid() + "." + ext ;

                    try {
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setContentType("image/"+ext).build();
                        FirebaseStorage.getInstance().getReference("playerPictures")
                                .child(fileName).putBytes(ImageUtil.convertToBytes(application.getApplicationContext(), uri), metadata)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d("SYNCING", fileName + " uploaded");
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        p.setDownloadUrl(uri.toString());
                                        docRef.set(p, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("SYNCING", p.getPid() + " document created");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("SYNCING", e.getMessage()+"");
                                                    }
                                                });
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("SYNCING", e.getMessage() + "");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        dbOperationThread.start();

    }
}
