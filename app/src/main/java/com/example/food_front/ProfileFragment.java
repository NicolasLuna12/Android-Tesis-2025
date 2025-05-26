package com.example.food_front;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvNombre, tvEmail;
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
        profileManager = new ProfileManager(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Llamar al backend para obtener los datos del perfil
        displayUserProfile();

        // Encontrar el TextView de "Datos personales"
        TextView personalData = view.findViewById(R.id.personal_data);

        // Encontrar el TextView de "Cerrar sesion"
        TextView closeSession = view.findViewById(R.id.logout);

        // Encontrar el TextView del boton "Eliminar cuenta"
        TextView deleteAccount = view.findViewById(R.id.btn_delete_account);

        // Agregar un onClickListener para navegar a "Editar perfil"
        personalData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la transacción para mostrar el fragmento de "Editar perfil"
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_view, new EditProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // Agregar un onClickListener para navegar a "Cerrar sesión"
        closeSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la transacción para cerrar sesion
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                sessionManager.clearSession();
                profileManager.clearInfo();
                fragmentTransaction.replace(R.id.fragment_container_view, new HomeFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // Agregar un onClickListener para navegar a "Eliminar cuenta"
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirmar si realmente quiere eliminar la cuenta
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar cuenta")
                        .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Llamar a la función para eliminar la cuenta
                                deleteAccount();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        return view;
    }

    private void displayUserProfile() {
        // Usar los métodos específicos para obtener los datos
        String name = profileManager.getName();
        String surname = profileManager.getSurname();
        String email = profileManager.getEmail();

        // Mostrar los datos en los TextViews
        tvNombre.setText(name + " " + surname);  // Mostrar nombre completo
        tvEmail.setText(email);
    }

    private void deleteAccount() {
        String url = "https://backmobile1.onrender.com/appUSERS/delete/";

        // Crear la request de Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Mostrar un mensaje de éxito
                        Toast.makeText(getContext(), "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show();

                        // Limpiar la sesión del usuario
                        sessionManager.clearSession();
                        profileManager.clearInfo();

                        // Navegar al HomeFragment o iniciar sesión
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack(); // Limpiar backstack si es necesario
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view, new HomeFragment())
                                .commit();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + sessionManager.getToken());  // Agregar el token
                return headers;
            }
        };

        // Agregar la request a la cola de Volley
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

}