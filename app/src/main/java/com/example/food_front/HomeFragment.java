package com.example.food_front;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;
import android.graphics.drawable.Drawable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    private TextView tvName;
    private CircleImageView profileImage;
    private ProfileManager profileManager;

    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        tvName = view.findViewById(R.id.txtUser);
        profileImage = view.findViewById(R.id.profileImage);
        Button button1 = view.findViewById(R.id.btn1);
        Button button2 = view.findViewById(R.id.btn);
        Button button3 = view.findViewById(R.id.btn3);
        Button button4 = view.findViewById(R.id.btn4);
        ImageView imageView1 = view.findViewById(R.id.imageView3);
        ImageView imageView2 = view.findViewById(R.id.imageView4);
        FloatingActionButton btnWebsite = view.findViewById(R.id.btnWebsite);

        // Configurar el onClick para el botón de navegación web
        btnWebsite.setOnClickListener(v -> {
            String url = "https://ispcfood.netlify.app/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // Ya no necesitamos este clickListener en el texto
        // TextView tvSlogan = view.findViewById(R.id.textView2);

        profileManager = new ProfileManager(requireContext());
        SessionManager sessionManager = new SessionManager(requireContext());

        // Mostrar el nombre del usuario e imagen de perfil
        mostrarNombreUsuario();
        cargarImagenPerfil();

        // Asegura que los botones también usen los mismos IDs que las imágenes
        button1.setOnClickListener(v -> abrirProductosConFiltro(3)); // Hamburguesas id 3
        button2.setOnClickListener(v -> abrirProductosConFiltro(1)); // Empanadas id 1
        button3.setOnClickListener(v -> replaceFragment(new ProductsFragment())); // Todos
        button4.setOnClickListener(v -> abrirProductosConFiltro(2)); // Lomitos id 2
        imageView1.setOnClickListener(v -> abrirProductosConFiltro(3)); // Hamburguesas id 3
        imageView2.setOnClickListener(v -> abrirProductosConFiltro(2)); // Lomitos id 2

        return view;
    }

    private void mostrarNombreUsuario() {
        String nombreGuardado = profileManager.getName();
        if (nombreGuardado != null) {
            tvName.setText(getString(R.string.bienvenido_usuario, nombreGuardado));
        } else {
            tvName.setText(R.string.usuario);
        }
    }

    private void cargarImagenPerfil() {
        String baseUrl = profileManager.getProfileImageUrl(); // Obtener URL base sin timestamp
        String imageUrl = profileManager.getProfileImageUrlWithTimestamp(); // URL con timestamp para Glide
        
        Log.d(TAG, "URL base recuperada: " + baseUrl);
        Log.d(TAG, "URL con timestamp: " + imageUrl);

        if (baseUrl != null && !baseUrl.isEmpty()) {
            // Limpiar toda caché anterior
            com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
            
            // Descargar la imagen directamente sin usar Glide
            new Thread(() -> {
                try {
                    // Descargar la imagen directamente
                    java.net.URL url = new java.net.URL(baseUrl);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setUseCaches(false); // Evitar caché
                    connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                    connection.setRequestProperty("Pragma", "no-cache");
                    connection.connect();
                    
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(connection.getInputStream());
                    
                    // Actualizar UI en hilo principal
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            if (bitmap != null) {
                                profileImage.setImageBitmap(bitmap);
                                Log.d(TAG, "Imagen cargada exitosamente con descarga directa");
                            } else {
                                // Si falla, intentar con Glide como respaldo
                                cargarImagenConGlide(imageUrl);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al descargar directamente: " + e.getMessage());
                    // En caso de error, intentar con Glide
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> cargarImagenConGlide(imageUrl));
                    }
                }
            }).start();
        } else {
            Log.d(TAG, "No hay URL de imagen, usando imagen predeterminada");
            // Usar imagen predeterminada
            profileImage.setImageResource(R.drawable.default_profile);
        }
    }
    
    private void cargarImagenConGlide(String imageUrl) {
        // Usar Glide como método alternativo
        Glide.with(requireContext())
            .load(imageUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model,
                                               @NonNull Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Error al cargar la imagen: " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                  @NonNull Target<Drawable> target,
                                                  @NonNull DataSource dataSource,
                                                  boolean isFirstResource) {
                        Log.d(TAG, "Imagen cargada exitosamente desde: " + model);
                        return false;
                    }
                })
            .into(profileImage);
    }

    // Método para actualizar la imagen de perfil desde otro fragmento
    public void actualizarImagenPerfil(String url) {
        if (profileImage != null && url != null && !url.isEmpty()) {
            // Limpiar la caché primero
            com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
            
            String imageUrlWithTimestamp = url + "?nocache=" + Math.random() + "&t=" + System.currentTimeMillis();
            Log.d(TAG, "Actualizando imagen desde otro fragmento: " + imageUrlWithTimestamp);

            // Forzar la descarga directa de la imagen sin usar Glide
            new Thread(() -> {
                try {
                    // Descargar la imagen directamente
                    java.net.URL url1 = new java.net.URL(url);
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url1.openConnection().getInputStream());
                    
                    // Actualizar UI en el hilo principal
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            if (bitmap != null) {
                                // Actualizar la ImageView con el bitmap descargado
                                profileImage.setImageBitmap(bitmap);
                                Log.d(TAG, "Imagen en HomeFragment actualizada directamente con Bitmap");
                            } else {
                                // Si falla, intentar con Glide como respaldo
                                Glide.with(requireContext())
                                    .load(imageUrlWithTimestamp)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile)
                                    .into(profileImage);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al descargar directamente en HomeFragment: " + e.getMessage());
                    // Si falla, intentar con Glide como respaldo en el hilo principal
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            Glide.with(requireContext())
                                .load(imageUrlWithTimestamp)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(profileImage);
                        });
                    }
                }
            }).start();
        }
    }

    private void abrirProductosConFiltro(int categoriaId) {
        ProductsFragment fragment = new ProductsFragment();
        Bundle args = new Bundle();
        args.putInt("categoria_id", categoriaId);
        fragment.setArguments(args);
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment newFragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, newFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar imagen de perfil del servidor
        reloadProfileDataIfNeeded();
    }

    private void reloadProfileDataIfNeeded() {
        // Verificar si han pasado más de 5 minutos desde la última carga
        long lastUpdated = profileManager.getLastImageUpdate();
        long now = System.currentTimeMillis();
        
        if (now - lastUpdated > 5 * 60 * 1000) { // 5 minutos
            Log.d(TAG, "Han pasado más de 5 minutos, recargando datos del perfil");
            cargarImagenPerfil(); // Esto ya usa la URL con timestamp para forzar recarga
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ya no usamos BroadcastReceiver
    }
    
    @Override
    public void onStop() {
        super.onStop();
        // Ya no necesitamos desregistrar nada
    }
}
