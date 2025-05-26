package com.example.food_front;

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

import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private TextView tvName;
    private Button button1, button2, button3, button4;
    private ImageView imageView1, imageView2, imageView3, imageView4;
    private CircleImageView profileImage;
    private ProfileManager profileManager;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        tvName = view.findViewById(R.id.txtUser);
        profileImage = view.findViewById(R.id.profileImage);
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
        imageView3.setOnClickListener(v -> replaceFragment(new ProductsFragment())); // Todos
        imageView4.setOnClickListener(v -> abrirProductosConFiltro(1)); // Empanadas id 1

        return view;
    }

    private void mostrarNombreUsuario() {
        String nombreGuardado = profileManager.getName();
        if (nombreGuardado != null) {
            tvName.setText("Bienvenido " + nombreGuardado);
        } else {
            tvName.setText("Usuario");
        }
    }

    private void cargarImagenPerfil() {
        String imageUrl = profileManager.getProfileImageUrl();
        Log.d("ImagenPerfil", "URL recuperada para cargar: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Asegurarse de que Glide no use cache para evitar problemas con imágenes antiguas
            Glide.with(requireContext())
                .load(imageUrl)
                .skipMemoryCache(true)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("ImagenPerfil", "Error al cargar la imagen: " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("ImagenPerfil", "Imagen cargada exitosamente");
                        return false;
                    }
                })
                .into(profileImage);
        } else {
            Log.d("ImagenPerfil", "No hay URL de imagen, usando imagen predeterminada");
            // Usar imagen predeterminada
            profileImage.setImageResource(R.drawable.default_profile);
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

    public CircleImageView getProfileImageView() {
        return profileImage;
    }
}
