package com.example.food_front;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;
import com.example.food_front.utils.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView tvNombre, tvEmail;
    private CircleImageView profileImage;
    private ProfileManager profileManager;
    private SessionManager sessionManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar las vistas
        tvNombre = view.findViewById(R.id.user_name);
        tvEmail = view.findViewById(R.id.user_email);
        profileImage = view.findViewById(R.id.profile_image);

        profileManager = new ProfileManager(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Llamar al backend para obtener los datos del perfil
        displayUserProfile();

        // Hacer la imagen de perfil clickable
        profileImage.setOnClickListener(v -> selectImage());

        // Encontrar el TextView de "Datos personales"
        TextView personalData = view.findViewById(R.id.personal_data);

        // Encontrar el TextView de "Cerrar sesion"
        TextView closeSession = view.findViewById(R.id.logout);

        return view;
    }

    private void displayUserProfile() {
        // Usar los métodos específicos para obtener los datos
        String name = profileManager.getName();
        String surname = profileManager.getSurname();
        String email = profileManager.getEmail();
        String imageUrl = profileManager.getProfileImageUrl();

        // Mostrar los datos en los TextViews
        tvNombre.setText(name + " " + surname);  // Mostrar nombre completo
        tvEmail.setText(email);

        // Cargar la imagen de perfil usando Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("ImagenPerfil", "Cargando imagen en ProfileFragment: " + imageUrl);
            Glide.with(requireContext())
                .load(imageUrl)
                .skipMemoryCache(true) // Para evitar problemas con la caché
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profileImage);
        } else {
            Log.d("ImagenPerfil", "No hay URL de imagen, usando imagen predeterminada en ProfileFragment");
            // Usar imagen predeterminada
            profileImage.setImageResource(R.drawable.default_profile);
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                uploadImage(imageBytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Archivo no encontrado", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error al leer el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage(byte[] imageBytes) {
        String url = "https://backmobile1.onrender.com/appUSERS/upload_profile_image/";

        // Mostrar un mensaje mientras se sube la imagen
        Toast.makeText(requireContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        // Crear una solicitud multipart para subir la imagen
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    // Procesar la respuesta del servidor
                    String responseBody = new String(response.data);
                    Log.d("ImagenPerfil", "Respuesta del servidor: " + responseBody);

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String imageUrl = jsonObject.getString("imagen_perfil_url");

                        // Guardar la nueva URL de la imagen en SharedPreferences
                        profileManager.saveProfileImageUrl(imageUrl);

                        // Actualizar la interfaz con la nueva imagen
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(profileImage);

                        Toast.makeText(requireContext(), "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Manejar el error
                    Log.e("ImagenPerfil", "Error al subir la imagen: " + error.toString());
                    Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", "1"); // Ajustar esto para usar el ID real del usuario
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir el token de autenticación si es necesario
                String token = sessionManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        // Añadir la imagen como un archivo al cuerpo de la solicitud
        multipartRequest.addByteData("image", imageBytes, "profile_image.jpg", "image/jpeg");

        // Añadir la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(multipartRequest);
    }
}
