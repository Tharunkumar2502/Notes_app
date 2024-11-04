package com.example.diarynotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;  // Make sure to import Log
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView noteDateTextView;
    private EditText noteEditText;
    private Button saveNoteButton, selectImageButton;
    private ImageView noteImageView;
    private String selectedDate;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference notesDatabaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Initialize UI elements
        noteDateTextView = findViewById(R.id.noteDateTextView);
        noteEditText = findViewById(R.id.noteEditText);
        saveNoteButton = findViewById(R.id.saveNoteButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        noteImageView = findViewById(R.id.noteImageView);

        // Get selected date from intent
        selectedDate = getIntent().getStringExtra("selectedDate");
        if (selectedDate != null) {
            noteDateTextView.setText("Date: " + selectedDate);
        }

        // Initialize Firebase components
        firebaseDatabase = FirebaseDatabase.getInstance();
        notesDatabaseReference = firebaseDatabase.getReference("DiaryNotes");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("images");

        // Image picker button click listener
        selectImageButton.setOnClickListener(v -> openImagePicker());

        // Save note button click listener
        saveNoteButton.setOnClickListener(v -> {
            String noteText = noteEditText.getText().toString();
            if (!noteText.isEmpty()) {
                // Save note to Firebase
                saveNoteToFirebase(selectedDate, noteText);

                // Create note object for API
                Note note = new Note(selectedDate, noteText, null); // Replace null with actual image URL if needed
                MyApiService apiService = RetrofitClient.getRetrofitInstance().create(MyApiService.class);

                // Attempt to save the note to the API
                apiService.saveDiaryEntry(note).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(NoteActivity.this, "Note saved to API", Toast.LENGTH_SHORT).show();
                        } else {
                            // Log the error response
                            Log.e("API Error", "Response Code: " + response.code() + ", Message: " + response.message());
                            try {
                                Log.e("API Error Body", response.errorBody().string()); // Log the error body
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(NoteActivity.this, "Failed to save note to API: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API Failure", t.getMessage(), t);
                        Toast.makeText(NoteActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // Upload image if it exists
                if (imageUri != null) {
                    uploadImageToFirebase();
                } else {
                    clearInputs();
                }
            } else {
                Toast.makeText(NoteActivity.this, "Please write a note before saving.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            noteImageView.setVisibility(View.VISIBLE);
            noteImageView.setImageURI(imageUri);
        }
    }

    // Save note to Firebase Realtime Database
    private void saveNoteToFirebase(String date, String note) {
        notesDatabaseReference.child(date).setValue(note)
                .addOnSuccessListener(aVoid -> Toast.makeText(NoteActivity.this, "Note saved to Firebase for " + date, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(NoteActivity.this, "Failed to save note: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Upload selected image to Firebase Storage
    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference imageRef = storageReference.child("images/" + System.currentTimeMillis() + "_note.jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(NoteActivity.this, "Image uploaded to Firebase", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    })
                    .addOnFailureListener(e -> Toast.makeText(NoteActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // Clear input fields
    private void clearInputs() {
        noteEditText.setText("");
        noteImageView.setVisibility(View.GONE);
        imageUri = null;
    }
}
