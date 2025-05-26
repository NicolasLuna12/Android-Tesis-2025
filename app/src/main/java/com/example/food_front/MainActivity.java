package com.example.food_front;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.food_front.databinding.ActivityMainBinding;
import com.example.food_front.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Cargar el LoginFragment al inicio

        // Check if a token exists to determine the initial fragment
        if (sessionManager.getToken() != null) {
            mostrarHome(); // Show HomeFragment if logged in
        } else {
            mostrarLogin(); // Show LoginFragment if not logged in
        }

//        mostrarLogin(); // Carga el LoginFragment al iniciar la aplicación


        binding.bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    mostrarHome(); // Show HomeFragment
                    return true;
                } else if (itemId == R.id.profile || itemId == R.id.menu || itemId == R.id.carrito) {
                    if (sessionManager.getToken() == null ){
                        mostrarLogin();
                        return false;
                    } else {
                        if (itemId == R.id.profile){
                            mostrarPerfil();
                            return true;
                        }else if (itemId == R.id.menu ){
                            mostrarProductos();
                            return true;
                        }else if (itemId == R.id.carrito){
                            mostrarCarrito(); // Show CartFragment
                            return true;
                        }
                    }
                } else if (itemId == R.id.contact) {
                    mostrarContact(); // Show ContactFragment
                    return true;
                }
                return false;
            }
        });
    }

    public void mostrarLogin() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, new LoginFragment());
        fragmentTransaction.commit();
    }


    public void mostrarHome() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new HomeFragment());
        fragmentTransaction.commit();
    }

    public void mostrarPerfil() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new ProfileFragment());
        fragmentTransaction.commit();
    }

    public void mostrarProductos() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new ProductsFragment());
        fragmentTransaction.commit();
    }

    public void mostrarCarrito() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new CartFragment());
        fragmentTransaction.commit();
    }

    public void mostrarContact() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new ContactFragment());
        fragmentTransaction.commit();
    }
}