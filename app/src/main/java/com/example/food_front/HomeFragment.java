package com.example.food_front;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;


public class HomeFragment extends Fragment {

    private TextView tvName;
    private Button button1, button2, button3, button4;
    private ImageView imageView1, imageView2, imageView3, imageView4;
    private ProfileManager profileManager;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        tvName = view.findViewById(R.id.txtUser);
        button1 = view.findViewById(R.id.btn1);
        button2 = view.findViewById(R.id.btn);
        button3 = view.findViewById(R.id.btn3);
        button4 = view.findViewById(R.id.btn4);
        imageView1 = view.findViewById(R.id.imageView3);
        imageView2 = view.findViewById(R.id.imageView4);
        imageView3 = view.findViewById(R.id.imageView5);
        imageView4 = view.findViewById(R.id.imageView6);

        profileManager = new ProfileManager(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Mostrar el nombre del usuario almacenado en SharedPreferences
        mostrarNombreUsuario();


        // Configurar los listeners para los botones e imágenes
        button1.setOnClickListener(v -> replaceFragment(new ProductsFragment()));
        button2.setOnClickListener(v -> replaceFragment(new ProductsFragment()));
        button3.setOnClickListener(v -> replaceFragment(new ProductsFragment()));
        button4.setOnClickListener(v -> replaceFragment(new ProductsFragment()));
        imageView1.setOnClickListener(v -> replaceFragment(new ProductsFragment()));
        imageView2.setOnClickListener(v -> replaceFragment(new ProductsFragment()));
        imageView3.setOnClickListener(v -> replaceFragment(new ProductsFragment()));
        imageView4.setOnClickListener(v -> replaceFragment(new ProductsFragment()));

        return view;
    }

    private void replaceFragment(Fragment newFragment) {
        // Reemplazar fragmento actual con otro
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, newFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void mostrarNombreUsuario() {
        String nombreGuardado = profileManager.getName();
        if (nombreGuardado != null) {
            tvName.setText("Bienvenido " + nombreGuardado);
        } else {
            tvName.setText("Usuario");
        }
    }



}
