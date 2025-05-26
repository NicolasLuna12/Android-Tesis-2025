package com.example.food_front;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

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
}
